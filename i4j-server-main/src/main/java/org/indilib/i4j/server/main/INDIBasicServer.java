package org.indilib.i4j.server.main;

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
import java.util.Arrays;

import org.indilib.i4j.protocol.api.INDIConnection;
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
 * @author Richard van Nieuwenhoven
 */
public class INDIBasicServer implements INDIServerEventHandler {

    /**
     * maximum startup time before exit in seconds .
     */
    private static final int MAX_STARTUP_TIME_IN_SEC = 10;

    /**
     * one scecond in milliseconds.
     */
    private static final long ONE_SECOND_IN_MILLISECODS = 1000L;

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
     * Constructs the server with a particular port.
     * 
     * @param host
     *            the host to bind the server.
     * @param port
     *            The port to which the server will listen.
     */
    public INDIBasicServer(String host, Integer port) {
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

        Integer port = null;
        String host = null;
        try {
            commandLine.parseSystemProperties();
            commandLine.parseArgument(args, false);

            if (commandLine.isHelp()) {
                commandLine.printHelp();
                return;
            }

            port = commandLine.getPort();
            host = commandLine.getHost();

            commandLine.addLibraries();
        } catch (Exception e1) {
            LOG.error("Incorrect command line (or error in the startup scripts)", e1);
            commandLine.printHelp();
            return;
        }
        INDIBasicServer server = new INDIBasicServer(host, port);
        server.start(commandLine);
    }

    /**
     * start the server and process the command line and startup commands. then
     * wait for the server to end. If the interactive mode is active start the
     * processing.
     * 
     * @param commandLine
     *            the commandline options.
     */
    private void start(INDICommandLine commandLine) {
        commandLine.setBasicServer(this).execute(false);
        waitWithTimeoutForTheServer(false);
        if (!server.isServerRunning()) {
            LOG.error("could not start server, exiting!");
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread("server stop hook") {

            @Override
            public void run() {
                if (server.isServerRunning()) {
                    server.stopServer();
                    waitWithTimeoutForTheServer(true);
                    if (server.isServerRunning()) {
                        LOG.error("could not normaly end server, exiting!");
                    } else {
                        LOG.info("server stoped!");
                    }
                }
            }
        });
        if (commandLine.isInteractive()) {
            commandLine.printInteractiveHelp();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            try {

                while (server.isServerRunning()) {
                    String line = in.readLine();
                    if (line != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            try {
                                new INDICommandLine(line).setBasicServer(this).execute(true);
                            } catch (Exception e) {
                                print("Error in command: " + e.getMessage() + ". (see log)");
                                LOG.error("could not parse/execute command", e);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("io exception", e);
            }
        } else {
            while (server.isServerRunning()) {
                // TODO wait for kill signal
                try {
                    Thread.sleep(ONE_SECOND_IN_MILLISECODS);
                } catch (InterruptedException e) {
                    LOG.debug("sleep interrrupted");
                }
            }
        }
    }

    /**
     * wait for the server to start or stop (max MAX_STARTUP_TIME_IN_SEC).
     * 
     * @param started
     *            wait for start or stop
     */
    private void waitWithTimeoutForTheServer(boolean started) {
        int countDown = MAX_STARTUP_TIME_IN_SEC;
        while (server.isServerRunning() == started && countDown-- > 0) {
            try {
                Thread.sleep(ONE_SECOND_IN_MILLISECODS);
            } catch (InterruptedException e) {
                LOG.debug("sleep interrrupted");
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
    public boolean acceptClient(INDIConnection clientSocket) {
        return true;
    }

    /**
     * print the string to stad out and the log.
     * 
     * @param string
     *            the string to print.
     */
    public static void print(String string) {
        System.out.println(string);
        LOG.info("command print: " + string);
    }
}
