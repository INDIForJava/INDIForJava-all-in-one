package org.indilib.i4j.driver.examples;

/*
 * #%L
 * INDI for Java Driver examples
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
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

import static org.indilib.i4j.Constants.PropertyStates.OK;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.FileUtils;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.driver.INDIBLOBElement;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.connection.INDIConnectionHandler;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example class representing a very basic INDI Driver.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @author Richard van Nieuwenhoven
 */
// START SNIPPET: declaration
public class INDIDriverExample extends INDIDriver implements INDIConnectionHandler {

    // END SNIPPET: declaration
    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIDriverExample.class);

    /**
     * file read buffer size.
     */
    private static final int BUFFER_SIZE = 4096;

    // START SNIPPET: fieldsClasic
    /**
     * the image property.
     */
    private INDIBLOBProperty imageP;

    /**
     * the image element.
     */
    private INDIBLOBElement imageE;

    // END SNIPPET: fieldsClasic

    // START SNIPPET: fieldsInject
    /**
     * Define the Switch Property with this driver as its owner, name
     * "sendImage", label "Send Image", group "Image Properties", initial state
     * IDLE, Read/Write permission and AtMostOne rule for the switch.
     */
    @InjectProperty(name = "sendImage", label = "Send Image", group = "Image Properties", switchRule = SwitchRules.AT_MOST_ONE)
    private INDISwitchProperty sendP;

    /**
     * Define the Switch Element with name "sendImage", label "Send Image" and
     * initial status OFF.
     */
    @InjectElement()
    private INDISwitchElement sendE;

    // END SNIPPET: fieldsInject

    /**
     * Constructs an instance of a <code>INDIDriverExample</code> with a
     * particular <code>INDIConnection</code> to write the messages to the
     * clients.
     * 
     * @param connection
     *            the indi connection to the server.
     */

    // START SNIPPET: constructor
    public INDIDriverExample(INDIConnection connection) {
        super(connection);
        // END SNIPPET: constructor
        // START SNIPPET: initClasic
        // Define the BLOB Property with this Driver as its owner, name "image",
        // label "Image", group "Image Properties", initial state IDLE and Read
        // Only.
        imageP = newBlobProperty().name("image").label("Image").group("Image Properties").permission(PropertyPermissions.RO).create();
        // Define the BLOB Element with name "image" and label "Image". Its
        // initial value is empty.
        imageE = imageP.newElement().create();
        // END SNIPPET: initClasic
        // START SNIPPET: eventHandler
        sendP.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newSendValue(elementsAndValues);
            }
        });
        // END SNIPPET: eventHandler

    }

    // START SNIPPET: getName
    @Override
    public String getName() {
        return "INDI Driver Example";
    }

    // END SNIPPET: getName

    /**
     * Processes the changes sent by the client to the Switch Property. If the
     * Switch property is the CONNECTION one it adds or removes the image and
     * send Properties. If the Property is the "sendImage", it loads an image
     * from disk, sets it to the image property and sends it back to the client.
     * 
     * @param elementsAndValues
     *            the new values for the property.
     */
    // START SNIPPET: action
    private void newSendValue(INDISwitchElementAndValue[] elementsAndValues) {
        // set the property state to ok
        sendP.setState(OK);
        // update the propery from the values send by the client
        sendP.setValues(elementsAndValues);
        // send the property (state) back to the client
        updateProperty(sendP);
        // If the sendImage element has been switched one we send the image
        if (sendE.isOn()) {
            boolean imageLoaded = loadImageFromFile();
            if (imageLoaded) {
                // set the element to off
                sendE.setOff();
                // send the property
                updateProperty(sendP);

                // the sendImage´ property as OK
                imageP.setState(PropertyStates.OK);
                // Send the sendImage property to the client.
                updateProperty(imageP);
            } else {
                updateProperty(sendP, "no image found");
            }
        }
    }

    // END SNIPPET: action

    // START SNIPPET: connection
    @Override
    public void driverConnect(Date timestamp) {
        LOG.info("Driver connect");
        this.addProperty(imageP);
        this.addProperty(sendP);
    }

    @Override
    public void driverDisconnect(Date timestamp) {
        LOG.info("Driver disconnect");
        this.removeProperty(imageP);
        this.removeProperty(sendP);
    }

    // END SNIPPET: connection

    /**
     * Loads the image "image.jpg" from the same directory into the image
     * property.
     * 
     * @return true if the loading has been succesful. false otherwise.
     */
    private boolean loadImageFromFile() {
        if (imageE.getValue().getSize() == 0) { // If it has not been already
                                                // loaded

            byte[] fileContents;

            try {
                InputStream in;
                File file = new File(FileUtils.getI4JBaseDirectory(), "image.jpg");
                // Create an input stream from the file object and read it
                // all
                if (file.exists()) {
                    in = new FileInputStream(file);
                } else {
                    in = INDIDriverExample.class.getResourceAsStream(file.getName());
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int count;
                while ((count = in.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                }
                in.close();
                out.close();
                fileContents = out.toByteArray();
            } catch (IOException e) {
                LOG.error("Could not write file", e);
                return false;
            }

            // Create the new BLOB value and set it to the image element.
            INDIBLOBValue v = new INDIBLOBValue(fileContents, ".jpg");

            imageE.setValue(v);
        }

        return true;
    }
}
