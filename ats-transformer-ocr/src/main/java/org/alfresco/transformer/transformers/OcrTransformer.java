package org.alfresco.transformer.transformers;

import java.io.File;
import java.util.Map;

import org.alfresco.transformer.executors.PdfToOcrdPdfTransformerExecutor;
import org.alfresco.transformer.executors.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OcrTransformer implements Transformer {

	public static final String ID = "ocrembedded";
	
	private final PdfToOcrdPdfTransformerExecutor executor;
	
	@Autowired
	public OcrTransformer(PdfToOcrdPdfTransformerExecutor executor) {
		this.executor = executor;
	}

	@Override
	public String getTransformerId() {
		return ID;
	}

	@Override
	public void embedMetadata(String transformName, String sourceMimetype, String targetMimetype,
			Map<String, String> transformOptions, File sourceFile, File targetFile) {
		
		executor.call(sourceFile, targetFile, transformName, targetMimetype);
	}

}
