/*
 * #%L
 * Alfresco Transform Service Transformer
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.transformer;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.transformer.executors.PdfToOcrdPdfTransformerExecutor;
import org.alfresco.transformer.fs.FileManager;
import org.alfresco.transformer.logging.LogEntry;
import org.alfresco.transformer.probes.ProbeTestTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for a transformer.
 *
 * Status Codes:
 *
 * 200 Success
 * 400 Bad Request: Invalid target mimetype <mimetype>
 * 400 Bad Request: Request parameter <name> is missing (missing mandatory parameter)
 * 400 Bad Request: Request parameter <name> is of the wrong type
 * 400 Bad Request: Transformer exit code was not 0 (possible problem with the source file)
 * 400 Bad Request: The source filename was not supplied
 * 500 Internal Server Error: (no message with low level IO problems)
 * 500 Internal Server Error: The target filename was not supplied (should not happen as targetExtension is checked)
 * 500 Internal Server Error: Transformer version check exit code was not 0
 * 500 Internal Server Error: Transformer version check failed to create any output
 * 500 Internal Server Error: Could not read the target file
 * 500 Internal Server Error: The target filename was malformed (should not happen because of other checks)
 * 500 Internal Server Error: Transformer failed to create an output file (the exit code was 0, so there should be some content)
 * 500 Internal Server Error: Filename encoding error
 * 507 Insufficient Storage: Failed to store the source file
 */
@Controller
public class TransformerController extends AbstractTransformerController {
	
    private static final Logger logger = LoggerFactory.getLogger(TransformerController.class);

    @Autowired
    private PdfToOcrdPdfTransformerExecutor javaExecutor;

    @Override
    public String getTransformerName() {
        return "ocr";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public ProbeTestTransform getProbeTestTransform() {
        // See the Javadoc on this method and Probes.md for the choice of these values.
        return new ProbeTestTransform(this, "quick.pdf", "quick2.pdf",
        		60, 16, 400, 10240, 60 * 30 + 1, 60 * 15 + 20) {
            @Override
            protected void executeTransformCommand(File sourceFile, File targetFile) {
                TransformerController.this.javaExecutor.call(sourceFile, targetFile);
            }
        };
    }

    @PostMapping(value = "/transform", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> transform(HttpServletRequest request,
	        @RequestParam("file") final MultipartFile sourceMultipartFile,
	        @RequestParam("sourceMimetype") final String sourceMimetype,
	        @RequestParam("targetExtension") final String targetExtension,
	        @RequestParam("targetMimetype") final String targetMimetype,
	
	        @RequestParam(value = "timeout", required = false) final Long timeout,
	        @RequestParam(value = "testDelay", required = false) final Long testDelay) {
    	
    	// We know the target extension and MIME type
    	// Let's make a random unique filename for temporary storage to hand back to the ATS Transform Router
        final String targetFilename = FileManager.createTargetFileName(sourceMultipartFile.getOriginalFilename(), targetExtension);

        // Inform the probe of pending transformation
        // This allows ATS to track usage statistics
        this.getProbeTestTransform().incrementTransformerCount();

        // Using parameters, get Java IO File references all squared away
        // This means downloading the source file to the local file system
        // It would be better if you stream straight from the source and not make a copy
        final File sourceFile = FileManager.createSourceFile(request, sourceMultipartFile);
        final File targetFile = FileManager.createTargetFile(request, targetFilename);
        // Both files are deleted by TransformInterceptor.afterCompletion

        // Store all the extra parameters in a Map
        final Map<String, String> transformOptions = this.createTransformOptions();

        // Make a decision on what sub-transformer or algorithm to use for the transformation
        final String transform = this.getTransformerName(sourceFile, sourceMimetype, targetMimetype, transformOptions);

        // Execute the transformation
        this.javaExecutor.call(sourceFile, targetFile, transform, targetMimetype);

        // Prepare the response
        final ResponseEntity<Resource> body = FileManager.createAttachment(targetFilename, targetFile);

        // Some logging overhead
        LogEntry.setTargetSize(targetFile.length());
        long time = LogEntry.setStatusCodeAndMessage(OK.value(), "Success");
        time += LogEntry.addDelay(testDelay);
        
        // Inform the probe of the time taken for the transformation
        // This allows ATS to track duration statistics
        this.getProbeTestTransform().recordTransformTime(time);

        return body;
    }

    @Override
    public void processTransform(final File sourceFile, final File targetFile,
	        final String sourceMimetype, final String targetMimetype,
	        final Map<String, String> transformOptions, final Long timeout) {
        logger.debug("Processing request with: sourceFile '{}', targetFile '{}', transformOptions" +
                     " '{}', timeout {} ms", sourceFile, targetFile, transformOptions, timeout);

        final String transform = this.getTransformerName(sourceFile, sourceMimetype, targetMimetype, transformOptions);

        this.javaExecutor.call(sourceFile, targetFile, transform, targetMimetype);
    }
    
}
