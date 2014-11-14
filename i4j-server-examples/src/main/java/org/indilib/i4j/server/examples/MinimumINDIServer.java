package org.indilib.i4j.server.examples;

/*
 * #%L
 * INDI for Java Server examples
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

import java.net.Socket;
import java.util.Arrays;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.examples.RandomNumberGeneratorDriver;
import org.indilib.i4j.server.api.INDIClientInterface;
import org.indilib.i4j.server.api.INDIDeviceInterface;
import org.indilib.i4j.server.api.INDIServerAccessLookup;
import org.indilib.i4j.server.api.INDIServerEventHandler;
import org.indilib.i4j.server.api.INDIServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An almost minimum INDI Server. It just has one working Driver:
 * RandomNumberGeneratorDriver (please check the INDI for Java Driver examples)
 * and it only accepts connections from 127.0.0.1 (localhost).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.21, April 4, 2012
 */
public final class MinimumINDIServer {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MinimumINDIServer.class);

    /**
     * bytes representing the localhost ip adress.
     */
    private static final byte[] LOCALHOST = {
        127,
        0,
        0,
        1
    };

    /**
     * Just loads the available driver.
     */
    private MinimumINDIServer() {
        INDIServerInterface server = INDIServerAccessLookup.indiServerAccess().createOrGet(null, -1);
        server.addEventHandler(new INDIServerEventHandler() {

            @Override
            public void driverDisconnected(INDIDeviceInterface device) {
            }

            @Override
            public void connectionWithClientEstablished(INDIClientInterface client) {
            }

            @Override
            public void connectionWithClientBroken(INDIClientInterface client) {
            }

            /**
             * Accepts the client if it is 127.0.0.1 (localhost).
             * 
             * @param socket
             *            socket to accept client from.
             * @return <code>true</code> if it is the 127.0.0.1 host.
             */
            @Override
            public boolean acceptClient(Socket clientSocket) {
                byte[] address = clientSocket.getInetAddress().getAddress();

                if (Arrays.equals(address, LOCALHOST)) {
                    return true;
                }

                return false;
            }
        });
        // Loads the Java Driver. Please note that its class must be in the
        // classpath.
        try {
            server.loadJavaDriver(RandomNumberGeneratorDriver.class.getName());
        } catch (INDIException e) {
            LOG.error("indi exception", e);

            System.exit(-1);
        }
    }

    /**
     * Just creates one instance of this server.
     * 
     * @param args
     *            arguments of the command line.
     */
    public static void main(String[] args) {
        new MinimumINDIServer();
    }
}
