/*
 * The Fascinator - Plugin - Transformer - Json Velocity Transformer PDF
 *
 * Copyright (C) 2008-2010 University of Southern Queensland
 * Copyright (C) 2013 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.googlecode.fascinator.transformer.jsonVelocity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.xhtmlrenderer.pdf.ITextRenderer;

import com.googlecode.fascinator.api.storage.DigitalObject;
import com.googlecode.fascinator.api.storage.Payload;
import com.googlecode.fascinator.api.storage.StorageException;
import com.googlecode.fascinator.api.transformer.TransformerException;
import com.itextpdf.text.DocumentException;

/**
 * <p>
 * This plugin transform a Json Payload to a PDF via XHTML/CSS based on the provided
 * Velocity templates. The transformed formats will then be stored as Payloads.
 * The transformer utilises <a href='http://code.google.com/p/flying-saucer/'/>Flying saucer and IText</a> to transform the XHTML into PDF.
 * </p>
 * 
 * <h3>Configuration</h3>
 *
 * <p>Keep in mind that each data source can provide overriding configuration.
 * This transformer currently allows overrides on all fields (except 'id').
 * </p>
 *
 * <table border="1">
 * <tr>
 * <th>Option</th>
 * <th>Description</th>
 * <th>Required</th>
 * <th>Default</th>
 * </tr>
 * 
 * <tr>
 * <td>id</td>
 * <td>Id of the transformer</td>
 * <td><b>Yes</b></td>
 * <td>jsonVelocity</td>
 * </tr>
 * 
 * <tr>
 * <td>sourcePayload</td>
 * <td>Source payload from which the object will be transformed. Currently only
 * JSON payloads are supported.</td>
 * <td><b>No</b></td>
 * <td>object.tfpackage</td>
 * </tr>
 * 
 * <tr>
 * <td>templatesPath</td>
 * <td>Velocity template file or directory.</td>
 * <td><b>Yes</b></td>
 * <td>N/A - Must be provided</td>
 * </tr>
 * 
 * <tr>
 * <td>portalId</td>
 * <td>The portal to use when generating external URLs inside the templates. The
 * server's configured URL base will be used as well.</td>
 * <td><b>No</b></td>
 * <td>default</td>
 * </tr>
 *
 * <h3>Examples</h3>
 * <ol>
 * <li>
 * Adding JsonVelocity Transformer to The Fascinator
 * 
 * <pre>
 * "jsonVelocity": {
 *     "id" : "jsonVelocityPDF",
 *     "sourcePayload" : "object.tfpackage",
 *     "templatesPath" : "src/main/resources/templates"
 * }
 * </pre>
 * 
 * </li>
 * </ol>
 * 
 * </p>
 * 
 * @author Jianfeng Li
 */
public class JsonVelocityPDFTransformer extends JsonVelocityTransformer {
   
    /**
     * Gets plugin Id
     * 
     * @return pluginId
     */
    @Override
    public String getId() {
        return "jsonVelocityPDF";
    }

    /**
     * Gets plugin name
     * 
     * @return pluginName
     */
    @Override
    public String getName() {
        return "Json Velocity PDF Transformer";
    }

    /**
     * Store the provided data
     *
     * @param object: The object to store the data in
     * @param pid: The payload ID to use in the object
     * @param data : The data to store
     * @return Payload: The payload object successfully stored
     * @throws TransformerException if storage fails
     */
    @Override
    protected Payload storeData(DigitalObject object, String pid, String data)
            throws TransformerException {
        try {
            try {
                return object.createStoredPayload(pid, stream(data));
            } catch (StorageException ex) {
                // Already exists, try an update
                return object.updatePayload(pid, stream(data));
            }
        } catch (UnsupportedEncodingException ex) {
            throw new TransformerException("Error in data encoding: ", ex);
        } catch (StorageException ex) {
            throw new TransformerException("Error storing payload: ", ex);
        } catch (DocumentException ex) {
            throw new TransformerException("Error creating PDF (document is not well formed): ", ex);
        } catch (IOException ex) {
            throw new TransformerException("Error creating PDF: ", ex);
        }
        
    }

    /**
     * Convert the provided String into an InputStream to pass to the storage API.
     *
     * @param string: The String to convert
     * @return InputStream: The InputStream holding the String's data
     * @throws IOException 
     * @throws DocumentException src/test/java/com/googlecode/fascinator/transformer/jsonVelocity/JsonVelocityTransformerPDFTest.java
     */
    private InputStream stream(String string) throws DocumentException, IOException {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	
    	ITextRenderer renderer = new ITextRenderer();
    	renderer.setDocumentFromString(string,new File(".").toURI().toURL().toString());
    	renderer.layout();
    	renderer.createPDF(bos);
	
    	return new ByteArrayInputStream(bos.toByteArray());
    }

   

    /**
     * Given the name of the provided template, change the extension for use as
     * a payload ID. At this point in time, this is hardcoded to .pdf
     *
     * @param templateName: The name of the template file
     * @return String: The payload ID to use
     */
    @Override
    protected String payloadName(String templateName) {
        return templateName.substring(0, templateName.indexOf(".")) + ".pdf";
    }

    
 
	@Override
	protected String getUrlBase() {
		return systemConfig.getString(null, "urlBase");
	}

	@Override
	protected String getSystemPortal() {
		return systemConfig.getString(DEFAULT_PORTAL,
	            "transformerDefaults", "jsonVelocityPDF", "portalId");
	}

	@Override
	protected String getSystemPayload() {
		return systemConfig.getString(DEFAULT_PAYLOAD,
	            "transformerDefaults", "jsonVelocityPDF", "sourcePayload");
	}

	@Override
	protected String getTemplatePath() {
		return systemConfig.getString(null,
	            "transformerDefaults", "jsonVelocityPDF", "templatesPath");
	}

}
