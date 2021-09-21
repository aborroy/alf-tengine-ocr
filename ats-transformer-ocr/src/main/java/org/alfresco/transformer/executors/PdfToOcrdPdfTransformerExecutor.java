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
package org.alfresco.transformer.executors;

import java.io.File;
import java.util.concurrent.Executors;

import org.alfresco.transform.exceptions.TransformException;
import org.alfresco.transformer.command.StreamGobbler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PdfToOcrdPdfTransformerExecutor implements JavaExecutor
{
	@Override
	public String getTransformerId() {
		return "ocr";
	}
	
    @Override
    public void call(File sourceFile, File targetFile, String... args) throws TransformException
    {

        try
        {

            Process process = Runtime.getRuntime().exec(
                    String.format("/usr/local/bin/ocrmypdf " + sourceFile.getPath() + " " + targetFile.getPath()));
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            process.waitFor();

        } 
        catch (Exception e)
        {
            throw new TransformException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    PdfToOcrdPdfTransformerExecutor.getMessage(e));
        }

        if (!targetFile.exists() || targetFile.length() == 0)
        {
            throw new TransformException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Transformer failed to create an output file");
        }

    }

    private static String getMessage(Exception e)
    {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
