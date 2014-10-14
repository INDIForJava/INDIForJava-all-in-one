package org.indilib.i4j.client.examples;

/*
 * #%L
 * INDI for Java Client Library
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
import java.io.IOException;
import java.util.Date;
import org.indilib.i4j.Constants;

import org.indilib.i4j.Constants.BLOBEnables;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.client.INDIBLOBElement;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIElementListener;
import org.indilib.i4j.client.INDIServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A INDI Client that listens to a particular BLOB Element and saves it to a
 * file whenever it is updated.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.31, April 11, 2012
 */
public class AutoBLOBSaver implements INDIElementListener {

    /**
     * A logger for errors.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AutoBLOBSaver.class);

    /**
     * The connection to a INDI server.
     */
    private INDIServerConnection connection;

    /**
     * The device name.
     */
    private String deviceName;

    /**
     * The BLOB property name.
     */
    private String propertyName;

    /**
     * The BLOB element name.
     */
    private String elementName;

    /**
     * The first parameter of the application is the device to listen.
     */
    private static final int DEVICE_PARAMETER = 0;

    /**
     * The second parameter of the application is the property to listen.
     */
    private static final int PROPERTY_PARAMETER = 1;

    /**
     * The third parameter of the application is the element to listen.
     */
    private static final int ELEMENT_PARAMETER = 2;

    /**
     * The fourth parameter of the application is the host of the INDI server.
     */
    private static final int HOST_PARAMETER = 3;

    /**
     * The fifth parameter of the application is the port of the INDI server.
     */
    private static final int PORT_PARAMETER = 4;

    /**
     * Creates an AutoBLOBSaver that will connect to a particular INDI Server.
     * 
     * @param host
     *            The host of the server
     * @param port
     *            The port of the server
     */
    public AutoBLOBSaver(String host, int port) {
        connection = new INDIServerConnection(host, port);
    }

    /**
     * Connects to the INDI Server and listens to a particular device, property
     * and element. It will save the BLOB data of that element as it changes.
     * 
     * @param deviceToListen
     *            The name of the device
     * @param propertyToListen
     *            The name of the property
     * @param elementToListen
     *            The name of the element
     */
    public void listenAndSaveBLOB(String deviceToListen, String propertyToListen, String elementToListen) {
        this.deviceName = deviceToListen;
        this.propertyName = propertyToListen;
        this.elementName = elementToListen;

        try {
            connection.connect();
            connection.askForDevices(); // Ask for all the devices.
        } catch (IOException e) {
            LOG.error("Problem with the connection: " + connection.getHost() + ":" + connection.getPort(), e);
        }

        INDIBLOBElement el = null;

        while (el == null) { // Wait until the Server has a Device with a
                             // property and element with the required name
            try {
                el = (INDIBLOBElement) connection.getElement(deviceToListen, propertyToListen, elementToListen);
                try {
                    Thread.sleep(Constants.WAITING_INTERVAL); // Wait 0.5
                                                              // seconds.
                } catch (InterruptedException e) {
                }
            } catch (ClassCastException e) {
                LOG.info("The Element is not a BLOB one.");
                System.exit(-1);
            }
        }

        try {
            /* Enable receiving BLOBs from this Device */
            el.getProperty().getDevice().blobsEnable(BLOBEnables.ALSO);
        } catch (IOException e) {
        }

        el.addINDIElementListener(this); // We add ourselves as the listener for
                                         // the Element.
    }

    /**
     * Does the actual saving when the element data changes.
     * 
     * @param element
     *            The element that has changed.
     */
    @Override
    public void elementChanged(INDIElement element) {
        INDIBLOBElement be = (INDIBLOBElement) element;

        INDIBLOBValue v = be.getValue();

        if (v != null) {
            String fileName = deviceName + "_" + propertyName + "_" + elementName + "_" + new Date() + v.getFormat();

            File f = new File(fileName);

            try {
                LOG.info("Saving " + f);
                be.getValue().saveBLOBData(f); // Saves the data to file
            } catch (IOException e) {
                LOG.error("could not save " + f, e);
            }
        }
    }

    /**
     * Parses the arguments and creates the Client if they are correct.
     * 
     * @param args
     *            The arguments of the application
     */
    public static void main(String[] args) {
        if ((args.length < PORT_PARAMETER) || (args.length > PORT_PARAMETER + 1)) {
            printErrorMessageAndExit();
        }

        String deviceName = args[DEVICE_PARAMETER];
        String propertyName = args[PROPERTY_PARAMETER];
        String elementName = args[ELEMENT_PARAMETER];
        String host = args[HOST_PARAMETER];
        int port = Constants.INDI_DEFAULT_PORT;

        if (args.length > PORT_PARAMETER) {
            try {
                port = Integer.parseInt(args[PORT_PARAMETER]);
            } catch (NumberFormatException e) {
                printErrorMessageAndExit();
            }
        }

        AutoBLOBSaver abs = new AutoBLOBSaver(host, port);

        abs.listenAndSaveBLOB(deviceName, propertyName, elementName);
    }

    /**
     * Prints a error message and exits the application.
     */
    private static void printErrorMessageAndExit() {
        System.out.println("The program must be called in the following way:");

        System.out.println("> java AutoBLOBSaver device property element host [port]\n  where");
        System.out.println("    device - is the INDI Device name");
        System.out.println("    property - is the BLOB Property name");
        System.out.println("    element - is the BLOB Element name");
        System.out.println("    host - is the INDI Server to connect to");
        System.out.println("    port - is the INDI Server port. If not present the default port (" + Constants.INDI_DEFAULT_PORT + ") will be used.\n");

        System.exit(-1);
    }
}
