/*
 * #%L
 * Alfresco Transform Core
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.transformer.executors;

import static org.alfresco.transformer.util.RequestParamMap.TIMEOUT;
import static org.alfresco.transformer.util.Util.stringToLong;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OcrmypdfCommandExecutor extends AbstractCommandExecutor {
	public static String ID = "ocrmypdf";
	public static String EXTRA_ARGUMENTS = "--extra-arguments";

	public static final String LICENCE = "This transformer uses ocrmypdf which uses the Tesseract library from Google Inc. See the license at https://github.com/tesseract-ocr/tesseract/blob/master/LICENSE";

	private final String exe;
	private final String arguments;

	@Autowired
	public OcrmypdfCommandExecutor(@Value("${ocrmypdf.path}") String exe, @Value("${ocrmypdf.arguments}")  String arguments) {
		if (exe == null || exe.isEmpty()) {
			throw new IllegalArgumentException("OcrmypdfCommandExecutor EXE variable cannot be null or empty");
		}
		this.exe = exe;
		this.arguments = arguments;
		super.transformCommand = createTransformCommand();
		super.checkCommand = createCheckCommand();
	}

	@Override
	public String getTransformerId() {
		return ID;
	}

	@Override
	protected RuntimeExec createTransformCommand() {
		RuntimeExec runtimeExec = new RuntimeExec();
		Map<String, String[]> commandsAndArguments = new HashMap<>();
		commandsAndArguments.put(".*", new String[] { exe, "SPLIT:${options}", "${source}", "${target}" });
		runtimeExec.setCommandsAndArguments(commandsAndArguments);

		Map<String, String> defaultProperties = new HashMap<>();
		defaultProperties.put("key", null);
		runtimeExec.setDefaultProperties(defaultProperties);

		runtimeExec.setErrorCodes("1");

		return runtimeExec;
	}

	@Override
	protected RuntimeExec createCheckCommand() {
		RuntimeExec runtimeExec = new RuntimeExec();
		Map<String, String[]> commandsAndArguments = new HashMap<>();
		commandsAndArguments.put(".*", new String[] { exe, "--version" });
		runtimeExec.setCommandsAndArguments(commandsAndArguments);
		return runtimeExec;
	}

	@Override
	public void transform(String transformName, String sourceMimetype, String targetMimetype,
			Map<String, String> transformOptions, File sourceFile, File targetFile) {
		Long timeout = stringToLong(transformOptions.get(TIMEOUT));
		String extraArguments = transformOptions.get(EXTRA_ARGUMENTS);
		
		StringBuilder args = new StringBuilder(arguments);
		if(extraArguments != null) {
			args.append(" ").append(extraArguments);
		}			

		run(args.toString(), sourceFile, targetFile, timeout);
	}
	
	@Override
	public void embedMetadata(String transformName, String sourceMimetype, String targetMimetype,
			Map<String, String> transformOptions, File sourceFile, File targetFile) {
		transform(transformName, sourceMimetype, targetMimetype, transformOptions, sourceFile, targetFile);
	}

	public void transformJpeg(String transformName, String sourceMimetype, String targetMimetype,
			Map<String, String> transformOptions, File sourceFile, File targetFile) {
		Long timeout = stringToLong(transformOptions.get(TIMEOUT));
		String extraArguments = "--image-dpi=96";
		
		StringBuilder args = new StringBuilder(arguments);
		if(extraArguments != null) {
			args.append(" ").append(extraArguments);
		}			

		run(args.toString(), sourceFile, targetFile, timeout);
	}

}
