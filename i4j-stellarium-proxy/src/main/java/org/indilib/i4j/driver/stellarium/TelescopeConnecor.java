package org.indilib.i4j.driver.stellarium;

/*
 * #%L
 * INDI for Java Proxy for stelarium
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
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

import java.io.IOException;
import java.util.Date;

import org.indilib.i4j.INDIURI;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;
import org.indilib.i4j.server.api.INDIServerAccessLookup;
import org.indilib.i4j.server.api.INDIServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class connects to an indi server and tries to find the requested
 * telescope. after that it keeps an eye on the pointig vector of the telescope.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class TelescopeConnecor implements INDIDeviceListener, INDIPropertyListener, INDIServerConnectionListener {

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StellariumServer.class);

    /**
     * The telescope indi device.
     */
    private INDIDevice indiDevice;

    /**
     * the devive name to search.
     */
    private String indiDeviceName;

    /**
     * the pointing direction property of the telescope.
     */
    private INDIProperty indiEqnProperty;

    /**
     * the server connection to the telescope.
     */
    private INDIServerConnection serverConnection;

    /**
     * indicatior to signal a stop of the connection with the telescope.
     */
    private boolean stop;

    /**
     * Constructor for the telescope connectior.
     * 
     * @param indiUrl
     *            url to connecto to
     * @param autoDetect
     *            should the connector try to find any telescope?
     */
    public TelescopeConnecor(String indiUrl, boolean autoDetect) {
        INDIURI url = null;

        String host;
        int port;
        if (!indiUrl.trim().isEmpty()) {
            url = new INDIURI(indiUrl);
            host = url.getHost();
            port = url.getPort();
        } else {
            INDIServerInterface currentServer = INDIServerAccessLookup.indiServerAccess().get();
            host = currentServer.getHost();
            port = currentServer.getPort();
            url = new INDIURI("indi://" + host + ":" + port);
        }
        serverConnection = new INDIServerConnection(host, port);
        indiDeviceName = url.getDevice();
        if (this.indiDeviceName == null || this.indiDeviceName.isEmpty()) {
            autoDetect = true;
        }
        if (autoDetect) {
            try {
                serverConnection.addINDIDeviceListener(this.indiDeviceName, this);
                serverConnection.addINDIServerConnectionListener(this);
                serverConnection.connect();
                serverConnection.askForDevices();
            } catch (IOException e) {
                LOG.error("could not ask for devices");
                stop = true;
            }
        } else {
            try {
                serverConnection.addINDIDeviceListener(this.indiDeviceName, this);
                serverConnection.connect();
                serverConnection.askForDevices(this.indiDeviceName, "EQUATORIAL_EOD_COORD");
            } catch (IOException e) {
                LOG.error("could not ask for device " + this.indiDevice);
                stop = true;
            }
        }
    }

    /**
     * close the connection with the telescope.
     */
    public void close() {
        this.stop = true;
        try {
            serverConnection.disconnect();
        } catch (Exception e) {
            LOG.warn("already disconnected", e);
        }
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        this.stop = true;
    }

    /**
     * to be implemented by the subclass a notice, what the complet url of the
     * detected telescope is.
     * 
     * @param string
     *            the url to the telescope.
     */
    public abstract void indiUrlIdentified(String string);

    @Override
    public void messageChanged(INDIDevice device) {

    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        device.addINDIDeviceListener(this);
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {

    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty property) {
        if (indiDevice == null && "EQUATORIAL_EOD_COORD".equals(property.getName())) {
            indiDevice = device;
            indiEqnProperty = property;
            indiEqnProperty.addINDIPropertyListener(this);
            indiUrlIdentified("indi://" + serverConnection.getHost() + ":" + serverConnection.getPort() + "/" + indiDevice.getName());
        }
    }

    @Override
    public void propertyChanged(INDIProperty property) {
        Object ra = property.getElement("RA").getValue();
        Object dec = property.getElement("DEC").getValue();
        if (ra instanceof Double && dec instanceof Double) {
            receivedVectorFromINDI(((Double) ra).doubleValue(), ((Double) dec).doubleValue());
        }
    }

    /**
     * reporting for the subclass that a new direction vector was received from
     * the telescope, the subclass should do something with it.
     * 
     * @param ra
     *            the right assertion of the vector
     * @param dec
     *            the declination of the vector
     */
    public abstract void receivedVectorFromINDI(double ra, double dec);

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        if (indiDevice == device) {
            close();
        }
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty property) {
        if (device == indiDevice && "EQUATORIAL_EOD_COORD".equals(property.getName())) {
            indiDevice = null;
            indiEqnProperty = null;
        }
    }

    /**
     * send a goto vetor command to the telescope.
     * 
     * @param ra
     *            the right assertion of the vector
     * @param dec
     *            the declination of the vector
     */
    public void sendVectorToINDI(double ra, double dec) {
        try {
            indiEqnProperty.getElement("RA").setDesiredValue(Double.valueOf(ra));
            indiEqnProperty.getElement("DEC").setDesiredValue(Double.valueOf(dec));
            indiEqnProperty.sendChangesToDriver();
        } catch (Exception e) {
            LOG.warn("could not send vector to INDI");
        }
    }
}
