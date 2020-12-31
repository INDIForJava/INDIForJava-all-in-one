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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.properties.INDIDeviceDescriptor;

/**
 * Listen to newly coming properties and try to determine the type of device
 * the properties describe.
 * 
 * @author Richard van Nieuwenhoven
 */
public class ActiveDeviceListener implements INDIDeviceListener, INDIPropertyListener {

    /**
     * currently known properties per device.
     */
    private Map<INDIDevice, Set<String>> properties = new HashMap<>();

    /**
     * all detected devices sorted by the descriptor and the position in the
     * list is the order of detection.
     */
    private List<INDIDevice> activeDevices = new LinkedList<>();

    /**
     * the property defining the active devices.
     */
    private final INDITextElement activeDevicesElement;

    /**
     * the server connection.
     */
    private final INDIServerConnection serverConnection;

    public ActiveDeviceListener(INDITextElement activeDevicesProperty, INDIServerConnection serverConnection) {
        this.activeDevicesElement = activeDevicesProperty;
        this.serverConnection = serverConnection;
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        property.addINDIPropertyListener(this);
        propertyChanged(property);
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        property.removeINDIPropertyListener(this);
    }

    @Override
    public void messageChanged(INDIDevice device) {

    }

    @Override
    public void propertyChanged(INDIProperty<?> property) {
        Set<String> deviceProperties = properties.computeIfAbsent(property.getDevice(), k -> new HashSet<>());
        if (!deviceProperties.add(property.getName())) {
            detectDevives(property.getDevice(), deviceProperties);
        }
    }

    /**
     * rebuild the active devices index by removing the current device and
     * re-detect it's type and re-add it at the appropriate position.
     * 
     * @param indiDevice
     *            the device to re-detect
     * @param deviceProperties
     *            the currently known properties
     */
    private void detectDevives(INDIDevice indiDevice, Set<String> deviceProperties) {
        // just to be sure we re-add all properties of the device.
        for (INDIProperty<?> property : indiDevice.getAllProperties()) {
            deviceProperties.add(property.getName());
        }
        boolean detected = false;
        INDIDeviceDescriptor[] deviceType = INDIDeviceDescriptor.detectDeviceType(deviceProperties);
        for (INDIDeviceDescriptor indiDeviceDescriptor : deviceType) {
            if (activeDevicesElement.getName().equals(indiDeviceDescriptor.name())) {
                detected = true;
                break;
            }
        }
        if (detected) {
            if (!activeDevices.contains(indiDevice)) {
                activeDevices.add(indiDevice);
            }
        } else {
            activeDevices.remove(indiDevice);
        }
        fillActiveDeviveProperty();
    }

    /**
     * with the new device descriptions rebuild the elements of the active
     * device property.
     */
    private void fillActiveDeviveProperty() {
        boolean somethingChanged = false;
        StringBuilder message = new StringBuilder("available devices on this server \n");
        INDIDeviceDescriptor descriptor = INDIDeviceDescriptor.valueOf(activeDevicesElement.getName());
        INDIDevice currentDevice = null;
        message.append(descriptor.name());
        message.append('=');
        for (INDIDevice indiDevice : this.activeDevices) {
            if (isDevice(indiDevice)) {
                currentDevice = indiDevice;
                message.append('*');
            }
            message.append(indiDevice.getName());
            message.append(',');
        }
        // remove the last comma.
        message.setLength(message.length() - 1);
        message.append('\n');
        if (currentDevice == null && this.activeDevices.size() > 0) {
            currentDevice = this.activeDevices.get(0);
            String urlName;
            if (activeDevicesElement.getValue().indexOf(':') >= 0) {
                URL url = serverConnection.getURL();
                String portString = url.getPort() < 0 || url.getDefaultPort() == url.getPort() ? "" : ":" + url.getPort();
                urlName = url.getProtocol() + "://" + url.getHost() + portString + "?device=" + currentDevice.getName();
            } else {
                urlName = currentDevice.getName();
            }
            activeDevicesElement.setValue(urlName);
            somethingChanged = true;
        }
        if (somethingChanged) {
            activeDevicesElement.getProperty().getDriver().updateProperty(activeDevicesElement.getProperty(), message.toString());
        }
    }

    private boolean isDevice(INDIDevice indiDevice) {
        String value = activeDevicesElement.getValue();
        if (value.indexOf(':') >= 0) {
            try {
                String device = ActiveDeviceExtension.getDeviceFromUrl(new URL(value));
                return indiDevice.getName().equals(device);
            } catch (MalformedURLException e) {
                return false;
            }
        } else {
            return indiDevice.getName().equals(value);
        }
    }
}
