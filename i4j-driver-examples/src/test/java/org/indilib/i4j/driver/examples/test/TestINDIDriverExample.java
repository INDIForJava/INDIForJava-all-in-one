package org.indilib.i4j.driver.examples.test;

/*
 * #%L
 * INDI for Java Driver Examples
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.driver.examples.INDIDriverExample;
import org.indilib.i4j.driver.junit.INDIDriverRunner;
import org.indilib.i4j.driver.junit.TestClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.indilib.i4j.properties.INDIGeneralProperties.CONNECTION;
import static org.indilib.i4j.properties.INDIGeneralProperties.CONNECT;

/**
 * Tests for the example driver.
 * 
 * @author Richard van Nieuwenhoven
 */
// START SNIPPET: runWith
@RunWith(INDIDriverRunner.class)
public class TestINDIDriverExample {

    // END SNIPPET: runWith

    /**
     * the image must be at least this big.
     */
    private static final int MINIMUM_IMAGE_SIZE = 12000;

    // START SNIPPET: fields
    /**
     * the driver to test, will be set by the INDIDriverRunner.
     */
    private INDIDriverExample driver;

    /**
     * the client to access the driver to test, will be set by the
     * INDIDriverRunner.
     */
    private TestClient testClient;

    // END SNIPPET: fields

    /**
     * test if the image can be send.
     * 
     * @throws Exception
     *             when fails
     */
    // START SNIPPET: test
    @Test
    public void testSomething() throws Exception {
        testClient.waitForProperty(CONNECTION)//
                .setValue(CONNECT, SwitchStatus.ON)//
                .send()//
                .waitForProperty("sendImage")//
                .setValue("sendImage", SwitchStatus.ON)//
                .send()//
                .waitForResponse("image");
        Assert.assertTrue(testClient.getBlobValue("image").getSize() > MINIMUM_IMAGE_SIZE);
    }

    // END SNIPPET: test

    /**
     * test if the send property is set back to of after sending an image.
     * 
     * @throws Exception
     *             when fails
     */
    @Test
    public void testSomethingElse() throws Exception {
        testClient.waitForProperty(CONNECTION)//
                .setValue(CONNECT, SwitchStatus.ON)//
                .send()//
                .waitForProperty("sendImage")//
                .setValue("sendImage", SwitchStatus.ON)//
                .send()//
                .waitForResponse("image");
        Assert.assertEquals(SwitchStatus.OFF, testClient.getSwitchValue("sendImage"));

    }
}
