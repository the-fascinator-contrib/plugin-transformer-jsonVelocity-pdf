/*
 * The Fascinator - Plugin - Transformer - JsonVelocity
 * Copyright (C) 2010-2011 University of Southern Queensland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.googlecode.fascinator.transformer.jsonVelocity;

import java.io.File;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.fascinator.api.PluginManager;
import com.googlecode.fascinator.api.storage.DigitalObject;
import com.googlecode.fascinator.api.storage.Storage;
import com.googlecode.fascinator.common.JsonSimpleConfig;
import com.googlecode.fascinator.common.storage.StorageUtils;

/**
 * Tests the JsonVelocityPDFTransformer
 * 
 * @author Jianfeng Li
 */
public class JsonVelocityTransformerPDFTest {

    private JsonVelocityPDFTransformer jsonVelocityPDFTransformer;

    @SuppressWarnings("unused")
	private JsonSimpleConfig config;

    private Storage ram;

    private DigitalObject sourceObject, outputObject;

    @Before
    public void init() throws Exception {
    }

    @After
    public void close() throws Exception {
        if (sourceObject != null) {
            sourceObject.close();
        }
        if (ram != null) {
            ram.shutdown();
        }
    }

    private void transform() throws Exception {
        // Storage
        ram = PluginManager.getStorage("ram");
        ram.init("{}");

        File file = new File(getClass().getResource("/test-config.json")
                .toURI());
        config = new JsonSimpleConfig(file);
        jsonVelocityPDFTransformer = new JsonVelocityPDFTransformer();
        jsonVelocityPDFTransformer.init(file);

        File source = new File(getClass().getResource("/object.tfpackage")
                .toURI());
        sourceObject = StorageUtils.storeFile(ram, source);
        outputObject = jsonVelocityPDFTransformer.transform(sourceObject, "{}");
    }

    @Test
    public void transformFormat() throws Exception {
        transform();

        Set<String> payloadIdList = outputObject.getPayloadIdList();
        Assert.assertTrue(payloadIdList.contains("Data Management Plan.pdf"));
        Assert.assertEquals(payloadIdList.size(), 2);

    }

 

   

}
