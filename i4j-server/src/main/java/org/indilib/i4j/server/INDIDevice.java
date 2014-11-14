package org.indilib.i4j.server;

/*
 * #%L
 * INDI for Java Server Library
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.codec.Charsets;
import org.indilib.i4j.Constants;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.INDIProtocolReader;
import org.indilib.i4j.server.api.INDIDeviceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A class that represents a generic INDI Device to which the server connects
 * and parses its messages.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.32, January 13, 2013
 */
public abstract class INDIDevice extends INDIDeviceListener implements INDIDeviceInterface {

    /**
     * The logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIDevice.class);

    /**
     * The reader that reads from the Device.
     */
    private INDIProtocolReader reader;

    /**
     * The Server that listens to this Device.
     */
    private INDIServer server;

    /**
     * Constructs a new <code>INDIDevice</code>.
     * 
     * @param server
     *            The Server that listens to this Device
     * @throws INDIException
     *             If there is an error connecting or instantiating the Device.
     */
    protected INDIDevice(INDIServer server) throws INDIException {
        this.server = server;
    }

    /**
     * Closes the connections of the device.
     */
    public abstract void closeConnections();

    /**
     * Stops the reader and closes its connections.
     */
    public void destroy() {
        isBeingDestroyed();

        reader.setStop(true);

        closeConnections();
    }

    @Override
    public void finishReader() {
        server.removeDevice(this);
        LOG.info("Finished reading from Driver " + getDeviceIdentifier());
    }

    @Override
    public abstract InputStream getInputStream();

    /**
     * Gets the <code>OutputStream</code> of the Device.
     * 
     * @return The <code>OutputStream</code> of the Device.
     */
    public abstract OutputStream getOutputStream();

    /**
     * Notify drivers that they are being destroyed.
     */
    public abstract void isBeingDestroyed();

    /**
     * Checks if the Device corresponds to a particular Device Identifier.
     * 
     * @param deviceIdentifier
     *            The Device Identifier to check.
     * @return <code>true</code> if the Device corresponds to the Device
     *         Identifier.
     */
    public abstract boolean isDevice(String deviceIdentifier);

    /**
     * Deals with a possible new Device name. If the device is a single one it
     * just stores the name if none has been previously fixed. In case of a
     * multiple device (like a Network one) it will probably add it to a list.
     * 
     * @param possibleNewName
     *            The new possible new name.
     */
    protected abstract void dealWithPossibleNewDeviceName(String possibleNewName);

    /**
     * Checks if the Device has a particular name. Specially important for
     * multiple name devices (Network ones).
     * 
     * @param name
     *            The name to check.
     * @return <code>true</code> if the Device respond to <code>name</code>.
     *         <code>false</code> otherwise.
     */
    protected abstract boolean hasName(String name);

    @Override
    protected void parseXMLElement(Element child) {
        String nodeName = child.getNodeName();

        if (nodeName.equals("getProperties")) {
            processGetProperties(child);
        } else if (nodeName.equals("defTextVector")) {
            checkName(child);
            processDefXXXVector(child);
        } else if (nodeName.equals("defNumberVector")) {
            checkName(child);
            processDefXXXVector(child);
        } else if (nodeName.equals("defSwitchVector")) {
            checkName(child);
            processDefXXXVector(child);
        } else if (nodeName.equals("defLightVector")) {
            checkName(child);
            processDefXXXVector(child);
        } else if (nodeName.equals("defBLOBVector")) {
            checkName(child);
            processDefXXXVector(child);
        } else if (nodeName.equals("setTextVector")) {
            processSetXXXVector(child);
        } else if (nodeName.equals("setNumberVector")) {
            processSetXXXVector(child);
        } else if (nodeName.equals("setSwitchVector")) {
            processSetXXXVector(child);
        } else if (nodeName.equals("setLightVector")) {
            processSetXXXVector(child);
        } else if (nodeName.equals("setBLOBVector")) {
            processSetXXXVector(child);
        } else if (nodeName.equals("message")) {
            processMessage(child);
        } else if (nodeName.equals("delProperty")) {
            processDelProperty(child);
        }
    }

    @Override
    protected void processGetProperties(Element xml) {
        super.processGetProperties(xml);
        server.notifyClientListenersGetProperties(this, xml);
    }

    @Override
    protected void sendXMLMessage(String xml) {
        try {
            getOutputStream().write(xml.getBytes(Charsets.UTF_8));
            getOutputStream().flush();
        } catch (IOException e) {
            destroy();
        }
    }

    /**
     * Starts the reader. Usually not directly called by Server particular
     * implementations.
     */
    protected void startReading() {
        reader = new INDIProtocolReader(this);
        reader.start();
    }

    /**
     * Checks the name in a XML element to detect possible new names in the
     * Driver (specially for multiple possible devices, like the Network one).
     * 
     * @param elem
     *            The XML element from which to extract the name of the Device.
     */
    private void checkName(Element elem) {
        String newName = elem.getAttribute("device").trim();

        if (!(newName.isEmpty())) {
            dealWithPossibleNewDeviceName(newName);
        }
    }

    /**
     * Processes the <code>defXXXVector</code> XML message.
     * 
     * @param xml
     *            The <code>defXXXVector</code> XML message
     */
    private void processDefXXXVector(Element xml) {
        String device = xml.getAttribute("device");

        if (device.isEmpty()) {
            return;
        }

        String property = xml.getAttribute("name").trim();

        if (property.isEmpty()) {
            return;
        }

        String state = xml.getAttribute("state").trim();

        if (!Constants.isValidPropertyState(state)) {
            return;
        }

        server.notifyDeviceListenersDefXXXVector(this, xml);
    }

    /**
     * Processes the <code>delProperty</code> XML message.
     * 
     * @param xml
     *            The <code>delProperty</code> XML message
     */
    private void processDelProperty(Element xml) {
        String device = xml.getAttribute("device");

        if (!hasName(device)) { // Some conditions to ignore the messages
            return;
        }

        server.notifyDeviceListenersDelProperty(this, xml);
    }

    /**
     * Processes the <code>message</code> XML message.
     * 
     * @param xml
     *            The <code>message</code> XML message
     */
    private void processMessage(Element xml) {
        server.notifyDeviceListenersMessage(this, xml);
    }

    /**
     * Processes the <code>setXXXVector</code> XML message.
     * 
     * @param xml
     *            The <code>setXXXVector</code> XML message
     */
    private void processSetXXXVector(Element xml) {
        String device = xml.getAttribute("device");

        if (!hasName(device)) { // Some conditions to ignore the messages
            return;
        }

        String property = xml.getAttribute("name").trim();

        if (property.isEmpty()) {
            return;
        }

        server.notifyDeviceListenersSetXXXVector(this, xml);
    }
}
