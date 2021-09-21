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

import java.io.File;
import java.util.Map;

import org.alfresco.transformer.executors.PdfToOcrdPdfTransformerExecutor;
import org.alfresco.transformer.probes.ProbeTestTransform;
import org.alfresco.transformer.transformers.OcrTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

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
	
    private final PdfToOcrdPdfTransformerExecutor javaExecutor;
    private final OcrTransformer ocrTransfromer;
    
    @Autowired
    public TransformerController(PdfToOcrdPdfTransformerExecutor javaExecutor, OcrTransformer ocrTransfromer) {
		this.javaExecutor = javaExecutor;
		this.ocrTransfromer = ocrTransfromer;
	}

	@Override
    public String getTransformerName() {
        return "ocr";
    }

    @Override
    public String version() {
        return "1.1";
    }

    @Override
    public ProbeTestTransform getProbeTestTransform() {
        return new ProbeTestTransform(this, "quick.pdf", "quick2.pdf",
        		60, 16, 400, 10240, 60 * 30 + 1, 60 * 15 + 20) {
            @Override
            protected void executeTransformCommand(File sourceFile, File targetFile) {
                TransformerController.this.javaExecutor.call(sourceFile, targetFile);
            }
        };
    }
    
    @Override
    public void transformImpl(String transformName, String sourceMimetype, String targetMimetype,
    		Map<String, String> transformOptions, File sourceFile, File targetFile) {

    	if ("ocrembedded".equals(transformName)) {
    	  ocrTransfromer.embedMetadata(transformName, sourceMimetype, targetMimetype, transformOptions, sourceFile, targetFile);
		} else {
          javaExecutor.call(sourceFile, targetFile, transformName, targetMimetype);
		}
    }
}
