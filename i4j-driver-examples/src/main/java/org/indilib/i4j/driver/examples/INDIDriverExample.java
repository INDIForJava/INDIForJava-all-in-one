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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.driver.INDIBLOBElement;
import org.indilib.i4j.driver.INDIBLOBElementAndValue;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIConnectionHandler;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example class representing a very basic INDI Driver.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.3, April 5, 2012
 */
public class INDIDriverExample extends INDIDriver implements INDIConnectionHandler {

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIDriverExample.class);

    // START SNIPPET: fields
    // The Properties and Elements of this Driver
    /**
     * the image property.
     */
    private INDIBLOBProperty imageP;

    /**
     * the image element.
     */
    private INDIBLOBElement imageE;

    /**
     * switch property.
     */
    private INDISwitchProperty sendP;

    /**
     * switch element.
     */
    private INDISwitchElement sendE;

    // END SNIPPET: fields

    /**
     * Constructs an instance of a <code>INDIDriverExample</code> with a
     * particular <code>inputStream</code> from which to read the incoming
     * messages (from clients) and a <code>outputStream</code> to write the
     * messages to the clients.
     * 
     * @param inputStream
     *            The stream from which to read messages.
     * @param outputStream
     *            The stream to which to write the messages.
     */
    public INDIDriverExample(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);

        // Define the BLOB Property with this Driver as its owner, name "image",
        // label "Image", group "Image Properties", initial state IDLE and Read
        // Only.
        imageP = new INDIBLOBProperty(this, "image", "Image", "Image Properties", PropertyStates.IDLE, PropertyPermissions.RO);
        // Define the BLOB Element with name "image" and label "Image". Its
        // initial value is empty.
        imageE = new INDIBLOBElement(imageP, "image", "Image");

        // Define the Switch Property with this driver as its owner, name
        // "sendImage", label "Send Image", group "Image Properties", initial
        // state IDLE, Read/Write permission and AtMostOne rule for the switch.
        sendP = new INDISwitchProperty(this, "sendImage", "Send Image", "Image Properties", PropertyStates.IDLE, PropertyPermissions.RW, SwitchRules.AT_MOST_ONE);
        // Define the Switch Element with name "sendImage", label "Send Image"
        // and initial status OFF
        sendE = new INDISwitchElement(sendP, "sendImage", "Send Image", SwitchStatus.OFF);
    }

    // Returns the name of the Driver
    @Override
    public String getName() {
        return "INDI Driver Example";
    }

    // No Number Properties can be set by clients. Empty method
    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
    }

    // No BLOB Properties can be set by clients. Empty method
    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
    }

    // No Text Properties can be set by clients. Empty method
    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
    }

    // Processes the changes sent by the client to the Switch Properties. If the
    // Switch property is the CONNECTION one it adds or removes the image and
    // send Properties. If the Property is the "sendImage", it loads an image
    // from disk, sets it to the image property and sends it back to the client.
    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {

        if (property == sendP) { // If the property is the sendImage one
            if (elementsAndValues.length > 0) { // If any element has been
                                                // updated
                INDISwitchElement el = elementsAndValues[0].getElement();
                SwitchStatus s = elementsAndValues[0].getValue();

                if ((el == sendE) && (s == SwitchStatus.ON)) { // If the
                                                               // sendImage
                                                               // element has
                                                               // been switched
                                                               // one we send
                                                               // the image
                    boolean imageLoaded = loadImageFromFile();

                    if (imageLoaded) {
                        sendP.setState(PropertyStates.OK); // Set the state of
                                                           // the sendImage
                                                           // property as OK

                        imageP.setState(PropertyStates.OK); // Set the state of
                                                            // the image
                                                            // property as OK
                        updateProperty(sendP); // Send the sendImage property to
                                               // the client.
                        updateProperty(imageP); // Send the image property to
                                                // the client.
                    }
                }
            }
        }
    }

    // Add the image and send properties changes
    @Override
    public void driverConnect(Date timestamp) {
        printMessage("Driver connect");
        this.addProperty(imageP);
        this.addProperty(sendP);
    }

    // Remove the image and send properties changes
    @Override
    public void driverDisconnect(Date timestamp) {
        printMessage("Driver disconnect");
        this.removeProperty(imageP);
        this.removeProperty(sendP);
    }

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
                File file = new File("image.jpg");

                // Create a buffer big enough to hold the file
                int size = (int) file.length();
                fileContents = new byte[size];
                // Create an input stream from the file object and read it all
                FileInputStream in = new FileInputStream(file);
                in.read(fileContents);
                in.close();
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
