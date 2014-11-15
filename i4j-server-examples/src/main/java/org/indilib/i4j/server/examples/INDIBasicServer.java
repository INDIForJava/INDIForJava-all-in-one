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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.indilib.i4j.Constants;
import org.indilib.i4j.FileUtils;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.server.api.INDIClientInterface;
import org.indilib.i4j.server.api.INDIDeviceInterface;
import org.indilib.i4j.server.api.INDIServerAccessLookup;
import org.indilib.i4j.server.api.INDIServerEventHandler;
import org.indilib.i4j.server.api.INDIServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple INDI Server that basically sends all messages from drivers and
 * clients and viceversa, just performing basic checks of messages integrity. It
 * allows to dinamically load / unload different kinds of Devices with simple
 * shell commands.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.32, July 23, 2013
 */
public class INDIBasicServer implements INDIServerEventHandler {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIBasicServer.class);

    /**
     * The only server.
     */
    private INDIServerInterface server;

    /**
     * @return The only server.
     */
    protected INDIServerInterface getServer() {
        return server;
    }

    /**
     * Constructs the server.
     */
    public INDIBasicServer() {
        this(-1);
    }

    /**
     * Constructs the server with a particular port.
     * 
     * @param port
     *            The port to which the server will listen.
     */
    public INDIBasicServer(Integer port) {
        server = INDIServerAccessLookup.indiServerAccess().createOrGet(null, port);
        server.addEventHandler(this);
    }

    /**
     * Main logic of the program: creates the server, parses the arguments and
     * attends shell commands from the standard input.
     * 
     * @param args
     *            The arguments of the program.
     */
    public static void main(String[] args) {
        INDICommandLine commandLine = new INDICommandLine();
        print("INDI for Java Basic Server initializing...");

        try {
            commandLine.parseArgument(args);
        } catch (Exception e1) {
            LOG.error("Incorrect command line (or error in the startup scripts)", e1);
            commandLine.printHelp();
            return;
        }

        Integer port = null;

        try {
            port = commandLine.getPort();
        } catch (ParseException e1) {
            LOG.error("Incorrect command line", e1);
            commandLine.printHelp();
            return;
        }
        INDIBasicServer server = new INDIBasicServer(port);

        commandLine.setBasicServer(server).execute(false);

        if (commandLine.isInteractive()) {
            commandLine.printInteractiveHelp();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;
            try {
                while (server.server.isServerRunning()) {
                    line = in.readLine();
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            new INDICommandLine(line).setBasicServer(server).execute(true);
                        } catch (Exception e) {
                            print("Error in command: " + e.getMessage() + ". (see log)");
                            LOG.error("could not parse/execute command", e);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("io exception", e);
            }
        } else {
            while (server.server.isServerRunning()) {
                // TODO wait for kill signal
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOG.debug("sleep interrrupted");
                }
            }
        }
    }

    /**
     * Prints a message about the broken connection to the standard err.
     * 
     * @param client
     *            The Client whose connection has been broken
     */
    @Override
    public void connectionWithClientBroken(INDIClientInterface client) {
        print("Connection with client " + client.getInetAddress() + " has been broken.");
    }

    /**
     * Prints a message about the established connection to the standard err.
     * 
     * @param client
     *            The Client whose connection has been established
     */
    @Override
    public void connectionWithClientEstablished(INDIClientInterface client) {
        print("Connection with client " + client.getInetAddress() + " established.");
    }

    @Override
    public void driverDisconnected(INDIDeviceInterface device) {
        String names = Arrays.toString(device.getNames());

        print("Driver " + device.getDeviceIdentifier() + " has been disconnected. The following devices have dissapeared: " + names);
    }

    @Override
    public boolean acceptClient(Socket clientSocket) {
        return true;
    }

    public static void print(String string) {
        System.out.println(string);
        LOG.info("command print: " + string);
    }
}
