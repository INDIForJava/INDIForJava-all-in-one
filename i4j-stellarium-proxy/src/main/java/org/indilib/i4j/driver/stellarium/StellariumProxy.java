package org.indilib.i4j.driver.stellarium;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Date;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.driver.event.TextEvent;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.indilib.i4j.properties.INDIGeneralProperties.ACTIVE_DEVICES;
import static org.indilib.i4j.properties.INDIGeneralProperties.ACTIVE_TELESCOPE;

/**
 * This driver will start a server socket and accept the stellarium protokol
 * clients there. It will on the other side try to detect the current indi
 * telescope and connect the stallarium server to it.
 * 
 * @author Richard van Nieuwenhoven
 */
public class StellariumProxy extends INDIDriver {

    /**
     * the default port for the stellarium server.
     */
    private static final int DEFAULT_STELLARIUM_PORT = 10001;

    /**
     * the minimal port number for the stellarium server.
     */
    private static final int MINIMAL_TCP_IP_PORT_NUMBER = 1000;

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StellariumProxy.class);

    /**
     * The indi url with the path to a telescope driver.
     */
    @InjectProperty(name = ACTIVE_DEVICES, label = "URI of the telescope", saveable = true)
    private INDITextProperty indiUrlP;

    /**
     * The indi uri of the telescope driver.
     */
    @InjectElement(name = ACTIVE_TELESCOPE, textValue = "indi:///")
    private INDITextElement indiUrl;

    /**
     * The connection data of the stellarium server.
     */
    @InjectProperty(name = "stellarium", label = "The stellarium connection", saveable = true)
    private INDITextProperty stellariumConnection;

    /**
     * the host name to bind the connection to. (Currently not used)
     */
    @InjectElement(name = "host", label = "host", textValue = "localhost")
    private INDITextElement stellariumConnectionHost;

    /**
     * The port the stellarium server binds to.
     */
    @InjectElement(name = "port", label = "port", textValue = "10001")
    private INDITextElement stellariumConnectionPort;

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
     * The stellarium server.
     */
    private StellariumServer stellariumServer;

    /**
     * The telescope connection holder.
     */
    private TelescopeConnecor telescopeConnecor;

    /**
     * Initializes the driver. It creates the Proerties and its Elements.
     * 
     * @param connection
     *            the indi connection to the server.
     */
    public StellariumProxy(INDIConnection connection) {
        super(connection);
        indiUrlP.setEventHandler(new TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                indiUrlChange(elementsAndValues);

            }
        });
        stellariumConnection.setEventHandler(new TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                stellariumConnectionChanged(elementsAndValues);
            }
        });
        autoDetectP.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                autoDetectPressed(elementsAndValues);
            }
        });
        addProperty(indiUrlP);
        addProperty(stellariumConnection);
        addProperty(autoDetectP);
        createStellariumServer();
        createTelescopeConnecor();
    }

    @Override
    public String getName() {
        return "Stellarium-Proxy";
    }

    @Override
    public void isBeingDestroyed() {
        if (stellariumServer != null) {
            stellariumServer.stop();
        }
        if (telescopeConnecor != null) {
            telescopeConnecor.close();
        }
    }

    /**
     * Rescan for the telescope.
     * 
     * @param elementsAndValues
     *            the new values for the property
     */
    protected void autoDetectPressed(INDISwitchElementAndValue[] elementsAndValues) {
        autoDetectP.setValues(elementsAndValues);
        autoDetectP.setState(PropertyStates.OK);
        updateProperty(autoDetectP);
        createTelescopeConnecor();
    }

    /**
     * the indi url was changed on the client, accept the values and rescan for
     * the telescope.
     * 
     * @param elementsAndValues
     *            the new values for the property
     */
    protected void indiUrlChange(INDITextElementAndValue[] elementsAndValues) {
        indiUrlP.setValues(elementsAndValues);
        indiUrlP.setState(PropertyStates.OK);
        updateProperty(indiUrlP);
        createTelescopeConnecor();
    }

    /**
     * The stellarium server binding data changed, reinitialize thenstellarium
     * server.
     * 
     * @param elementsAndValues
     *            the new values for the property
     */
    protected void stellariumConnectionChanged(INDITextElementAndValue[] elementsAndValues) {
        stellariumConnection.setValues(elementsAndValues);
        stellariumConnection.setState(PropertyStates.OK);
        updateProperty(stellariumConnection);
        createStellariumServer();
    }

    /**
     * Destroy the old stellarium connection and reinitialze a new one.
     */
    private void createStellariumServer() {
        if (stellariumServer != null) {
            stellariumServer.stop();
            stellariumServer = null;
        }
        int stellariumPort;
        try {
            stellariumPort = Integer.parseInt(stellariumConnectionPort.getValue());
        } catch (NumberFormatException e) {
            stellariumPort = DEFAULT_STELLARIUM_PORT;
            updateProperty(stellariumConnection, "port not a legal number using " + DEFAULT_STELLARIUM_PORT);
        }
        if (stellariumPort < MINIMAL_TCP_IP_PORT_NUMBER) {
            stellariumPort = DEFAULT_STELLARIUM_PORT;
            updateProperty(stellariumConnection, "port less then 1000 using " + DEFAULT_STELLARIUM_PORT);
        }
        try {
            stellariumServer = new StellariumServer(stellariumPort) {

                @Override
                public void vectorRecievedFromStellariumClient(double ra, double dec) {
                    if (telescopeConnecor != null) {
                        telescopeConnecor.sendVectorToINDI(ra, dec);
                    } else {
                        LOG.warn("no telescope connected do goto command ignored");
                    }
                }
            };
        } catch (Exception e) {
            updateProperty(stellariumConnection, "could not start stellarium stellariumServer");
            LOG.error("could not start stellarium stellariumServer", e);
        }
    }

    /**
     * destroy the old telescope connector and try to reconnect.
     */
    private void createTelescopeConnecor() {
        if (telescopeConnecor != null) {
            telescopeConnecor.close();
            telescopeConnecor = null;
        }
        try {
            telescopeConnecor = new TelescopeConnecor(indiUrl.getValue(), autoDetect.isOn()) {

                @Override
                public void indiUrlIdentified(String string) {
                    indiUrl.setValue(string);
                    updateProperty(indiUrlP);
                }

                @Override
                public void receivedVectorFromINDI(double ra, double dec) {
                    stellariumServer.sendVectorToStellariumClients(ra, dec);
                }
            };
        } catch (Exception e) {
            updateProperty(indiUrlP, "could not connect to telescope");
            LOG.error("could not connect to telescope", e);
        }
    }

}
