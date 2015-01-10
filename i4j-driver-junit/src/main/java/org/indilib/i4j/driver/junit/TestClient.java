package org.indilib.i4j.driver.junit;

/*
 * #%L
 * INDI for Java Driver JUNIT Test Utilities
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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.INDIProtocolReader;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;
import org.indilib.i4j.protocol.INDIProtocol;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.junit.Assert;

/**
 * This is a test client to make compact eficent unit test for drivers. It has a
 * lot of methods that help testin the drivers.
 * 
 * @author Richard van Nieuwenhoven
 */
public class TestClient implements INDIDeviceListener, INDIServerConnectionListener {

    /**
     * sleep step befor checking again.
     */
    private static final long SLEEP_STEP = 10L;

    /**
     * default total time to wait something to happen.
     */
    private static final long MILLISECONDS_TO_WAIT_FOR_SOMETHING = 1000L;

    /**
     * the server connection to the driver.
     */
    private INDIServerConnection serverConn;

    /**
     * the client device for the driver.
     */
    private INDIDevice deviceClient;

    /**
     * the protokol reader for the client.
     */
    private INDIProtocolReader indiProtocolReader;

    /**
     * the properties that where set since the last send.
     */
    private Set<String> receivedPropertiesAfterLastSend = new HashSet<>();

    /**
     * properties that where changed sins the last send.
     */
    private Set<INDIProperty<?>> changedProperties = new HashSet<>();

    /**
     * construct the test client based on the specified indi connection.
     * 
     * @param connection
     *            the connection to the driver to test
     */
    public TestClient(INDIConnection connection) {
        serverConn = new INDIServerConnection(connection) {

            @Override
            public void processProtokolMessage(INDIProtocol<?> xml) {
                if (xml.getName() != null) {
                    receivedPropertiesAfterLastSend.add(xml.getName());
                }
                super.processProtokolMessage(xml);
            }
        };
        serverConn.addINDIServerConnectionListener(this);
        indiProtocolReader = new INDIProtocolReader(serverConn, "test client");
        indiProtocolReader.start();
    }

    /**
     * wait for a property to apear, with a maximum of 1 second. at timeout a
     * junit failure is thrown.
     * 
     * @param name
     *            the property to wait for
     * @return the test class itself
     * @throws Exception
     *             if the wait fails.
     */
    public TestClient waitForProperty(String name) throws Exception {
        return waitForProperty(name, MILLISECONDS_TO_WAIT_FOR_SOMETHING);
    }

    /**
     * wait for a property to apear, with a maximum of 1 second. at timeout a
     * junit failure is thrown.
     * 
     * @param name
     *            the property to wait for
     * @param max
     *            the maximal time in milliseconds to wait
     * @return the test class itself
     * @throws Exception
     *             if the wait fails.
     */
    public TestClient waitForProperty(String name, long max) throws Exception {
        long start = System.currentTimeMillis();
        while (deviceClient == null) {
            waitWithTimeout(max, start);
        }
        INDIProperty<?> property = deviceClient.getProperty(name);
        while (property == null) {
            waitWithTimeout(max, start);
            property = deviceClient.getProperty(name);
            if (property != null) {
                return this;
            }
        }
        return this;
    }

    /**
     * wait a little and when the total time passed exeeds the maximum time fail
     * the test.
     * 
     * @param max
     *            the maximal time in milliseconds.
     * @param start
     *            the start time in system milliseconds.
     * @throws Exception
     *             if the maximum time is passed
     */
    private void waitWithTimeout(long max, long start) throws Exception {
        if (System.currentTimeMillis() - start > max) {
            Assert.fail("Timeout waiting");
        }
        Thread.sleep(SLEEP_STEP);
    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        this.deviceClient = device;
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
    }

    @Override
    public void messageChanged(INDIDevice device) {
    }

    /**
     * set the element with the specified name to the specified value. Fails the
     * test if the element did not exist, or the value was not campatible with
     * the element.
     * 
     * @param elementName
     *            the name of element to set
     * @param value
     *            the value to set
     * @return the testclient itself (builder pattern)
     * @throws Exception
     *             if the set fails in any way
     */
    public TestClient setValue(String elementName, Object value) throws Exception {
        for (INDIProperty<?> property : deviceClient.getPropertiesAsList()) {
            for (INDIElement element : property) {
                if (element.getName().equals(elementName)) {
                    changedProperties.add(property);
                    element.setDesiredValue(value);
                    return this;
                }
            }
        }
        Assert.fail("element " + elementName + " not found");
        return this;
    }

    /**
     * send all properties changed since the last send.
     * 
     * @return the testclient itself (builder pattern)
     * @throws Exception
     *             if the send fails in any way
     */
    public TestClient send() throws Exception {
        receivedPropertiesAfterLastSend.clear();
        for (INDIProperty<?> indiProperty : changedProperties) {
            indiProperty.sendChangesToDriver();
        }
        changedProperties.clear();
        return this;
    }

    /**
     * stop the client, streams and the threads connected to it.
     */
    public void stop() {
        indiProtocolReader.setStop(true);
        serverConn.disconnect();
    }

    /**
     * wait for a indi response of the driver that has the specified name, this
     * can be a property or a element. Wait at most the default wait time after
     * that fail the test.
     * 
     * @param name
     *            the property to wait for
     * @return the test class itself
     * @throws Exception
     *             if the wait fails.
     */
    public TestClient waitForResponse(String name) throws Exception {
        return waitForResponse(name, MILLISECONDS_TO_WAIT_FOR_SOMETHING);
    }

    /**
     * wait for a indi response of the driver that has the specified name, this
     * can be a property or a element. Wait at most max millseconds after that
     * fail the test.
     * 
     * @param name
     *            the property to wait for
     * @param max
     *            the maximal time in milliseconds to wait
     * @return the test class itself
     * @throws Exception
     *             if the wait fails.
     */
    private TestClient waitForResponse(String name, long max) throws Exception {
        long start = System.currentTimeMillis();
        while (!receivedPropertiesAfterLastSend.contains(name)) {
            waitWithTimeout(max, start);
        }
        return this;
    }

    /**
     * @return the clientz device connected to the driver.
     */
    public INDIDevice getDevice() {
        return deviceClient;
    }

    /**
     * get the value of an element with the specified name and cast it to a
     * blob. This method fails the test if the value is not a blob or the
     * element did not exist.
     * 
     * @param elementName
     *            the name of the element to search for,
     * @return the blob value of the element.
     */
    public INDIBLOBValue getBlobValue(String elementName) {
        for (INDIProperty<?> property : deviceClient.getPropertiesAsList()) {
            for (INDIElement element : property) {
                if (element.getName().equals(elementName)) {
                    return (INDIBLOBValue) element.getValue();
                }
            }
        }
        Assert.fail("element " + elementName + " not found");
        return null;
    }

    /**
     * get the value of an element with the specified name. This method fails
     * the test if the element did not exist.
     * 
     * @param elementName
     *            the name of the element to search for,
     * @return value of the element.
     */
    public Object getValue(String elementName) {
        for (INDIProperty<?> property : deviceClient.getPropertiesAsList()) {
            for (INDIElement element : property) {
                if (element.getName().equals(elementName)) {
                    return element.getValue();
                }
            }
        }
        Assert.fail("element " + elementName + " not found");
        return null;
    }

    /**
     * get the value of an element with the specified name and cast it to a
     * SwitchStatus. This method fails the test if the value is not a switch or
     * the element did not exist.
     * 
     * @param elementName
     *            the name of the element to search for,
     * @return the switch value of the element.
     */
    public SwitchStatus getSwitchValue(String elementName) {
        for (INDIProperty<?> property : deviceClient.getPropertiesAsList()) {
            for (INDIElement element : property) {
                if (element.getName().equals(elementName)) {
                    return (SwitchStatus) element.getValue();
                }
            }
        }
        Assert.fail("element " + elementName + " not found");
        return null;
    }
}
