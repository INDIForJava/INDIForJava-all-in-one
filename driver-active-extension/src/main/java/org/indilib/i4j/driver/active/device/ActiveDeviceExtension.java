package org.indilib.i4j.driver.active.device;

/*
 * #%L
 * INDI for Java Acive Device Extension
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.properties.INDIDeviceDescriptor;
import org.indilib.i4j.properties.INDIStandardElement;
import org.indilib.i4j.properties.INDIStandardProperty;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.url.INDIURLConnection;
import org.indilib.i4j.server.api.INDIServerAccessLookup;
import org.indilib.i4j.server.api.INDIServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This extension provides the active_device property with all possible values.
 * this special property is used to allow linking together different drivers for
 * information excange.
 * 
 * @author Richard van Nieuwenhoven
 *
 */
public class ActiveDeviceExtension extends INDIDriverExtension<INDIDriver> {

    /**
     * The logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ActiveDeviceExtension.class);

    /**
     * auto detect button to scan the indi server for telescopes.
     */
    @InjectProperty(name = "autoDetect", label = "auto detect telescope")
    private INDISwitchProperty autoDetectP;

    /**
     * auto detect button to scan the indi server for telescopes.
     */
    @InjectElement(switchValue = SwitchStatus.ON)
    private INDISwitchElement autoDetect;

    /**
     * the property containing the list with all active devices for the parent.
     */
    private INDITextProperty activeDevices;

    /**
     * the list with device types that can be activated.
     */
    private List<INDIDeviceDescriptor> deviceTypes = new LinkedList<>();

    /**
     * property/element mappings from an other device to this device.
     */
    private Map<INDIDeviceDescriptor, List<ElementMapping>> elementMappings = new HashMap<>();

    /**
     * Constructor for the extension.
     * 
     * @param driver
     *            the driver to connect to.
     */
    public ActiveDeviceExtension(INDIDriver driver) {
        super(driver);
    }

    /**
     * select the possible device types that can be connected to the device.
     * This method may only be called before the first connect.
     * 
     * @param deviceTypes
     *            the array of device types
     */
    public void setDeviceTypes(INDIDeviceDescriptor... deviceTypes) {
        this.deviceTypes.clear();
        List<INDIStandardElement> allElements = Arrays.asList(INDIStandardProperty.ACTIVE_DEVICES.elements());
        // we need a connection between the ACTIVE_DEVICES elements and and the
        // device descriptors.
        for (INDIDeviceDescriptor indiStandardElement : deviceTypes) {
            if (allElements.contains(indiStandardElement)) {
                this.deviceTypes.add(indiStandardElement);
            }
        }
    }

    @Override
    public void connect() {
        if (activeDevices == null) {
            initializeActiveDevices();
        }
        super.connect();
    }

    /**
     * the property containing the elements with the possible active devices
     * will
     * be created when the first connect happens.
     */
    private synchronized void initializeActiveDevices() {
        if (activeDevices == null) {
            LOG.info("Active devices initializing for " + driver.getName() + " and device types " + Arrays.toString(this.deviceTypes.toArray()));
            this.activeDevices = driver.newProperty(INDITextProperty.class)//
                    .saveable(true)//
                    .name(INDIStandardProperty.ACTIVE_DEVICES.name())//
                    .label(INDIStandardProperty.ACTIVE_DEVICES.name())//
                    .create();
            for (INDIDeviceDescriptor deviceType : this.deviceTypes) {
                this.activeDevices.newElement().name(deviceType.name()).create();
            }
            this.activeDevices.setEventHandler(new org.indilib.i4j.driver.event.TextEvent() {

                @Override
                public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                    property.setValues(elementsAndValues);
                    property.setState(PropertyStates.OK);
                    updateProperty(property);
                    for (INDITextElementAndValue indiTextElementAndValue : elementsAndValues) {
                        updateDriverConnection(indiTextElementAndValue.getElement());
                    }
                }
            });
        }
    }

    /**
     * an element was changed now change the driver connection to the linked
     * device.
     * 
     * @param element
     *            the changed element.
     */
    protected void updateDriverConnection(INDITextElement element) {
        LOG.info("Active devices element changed for " + driver.getName() + " and device type " + element.getName());
        createServerConnection(element);
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }

    Map<String, INDIServerConnection> serverConnections;

    private void createServerConnection(INDITextElement element) {
        String elementValue = element.getValue();
        INDIServerConnection serverConnection = null;
        INDIServerInterface currentServer = initializeLocalServerConnection();
        String indiDeviceName = "";
        // if a ':' is in the device name the name is surly an url.
        if (!elementValue.trim().isEmpty() && elementValue.indexOf(':') > 0) {
            try {
                URL parsedUrl = new URL(elementValue.trim());
                if (!currentServer.isLocalURL(parsedUrl)) {
                    int port = parsedUrl.getPort();
                    if (port < 1) {
                        port = parsedUrl.getDefaultPort();
                    }
                    String connectionKey = connectionKey(parsedUrl.getHost(), port);
                    serverConnection = serverConnections.get(connectionKey);
                    if (serverConnection == null) {
                        serverConnection = new INDIServerConnection((INDIConnection) parsedUrl.openConnection());
                        serverConnections.put(connectionKey, serverConnection);
                    }
                }
                indiDeviceName = getDeviceFromUrl(parsedUrl);
            } catch (MalformedURLException e) {
                String errorMessage = "not a legal url \"" + elementValue.trim() + "\" using the current server.";
                LOG.error(errorMessage);
                driver.updateProperty(activeDevices, errorMessage);
            } catch (IOException e) {
                String errorMessage = "could not connect \"" + elementValue.trim() + "\" using the current server.";
                LOG.error(errorMessage);
                driver.updateProperty(activeDevices, errorMessage);
            }
        } else {
            indiDeviceName = elementValue.trim();
        }
        if (serverConnection != null && indiDeviceName != null && !indiDeviceName.trim().isEmpty()) {
            indiDeviceName = indiDeviceName.trim();
            serverConnection.addINDIDeviceListener(indiDeviceName, new ActiveDeviceListener(element, serverConnection));
            try {
                serverConnection.askForDevices();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                driver.updateProperty(activeDevices, e.getMessage());
            }
        }
    }

    protected static String getDeviceFromUrl(URL parsedUrl) {
        String indiDeviceName = "";
        List<String> device = INDIURLConnection.splitQuery(parsedUrl).get("device");
        if (device != null && !device.isEmpty()) {
            indiDeviceName = device.get(0);
        }
        return indiDeviceName;
    }

    private INDIServerInterface initializeLocalServerConnection() {
        INDIServerInterface currentServer = INDIServerAccessLookup.indiServerAccess().get();
        String connectionKey = connectionKey(currentServer.getHost(), currentServer.getPort());
        INDIServerConnection serverConnection = serverConnections.get(connectionKey);
        if (serverConnection == null) {
            serverConnection = new INDIServerConnection(currentServer.createConnection());
            serverConnections.put(connectionKey, serverConnection);
        }
        return currentServer;
    }

    private String connectionKey(String host, int port) {
        return host + ':' + port;
    }

    public void add(INDIDeviceDescriptor deviceType, ElementMapping mapping) {
        List<ElementMapping> mappings = elementMappings.computeIfAbsent(deviceType, k -> new LinkedList<>());
        mappings.add(mapping);
    }
}
