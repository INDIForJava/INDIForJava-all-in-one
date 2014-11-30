package org.indilib.i4j.server;

/*
 * #%L
 * INDI for Java Server Library
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
import java.net.ServerSocket;
import java.net.Socket;

import org.indilib.i4j.Constants;
import org.indilib.i4j.protocol.io.INDISocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic indi protocol connection acceptor. it wil accept standard indi
 * clients connection to the server socket.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class INDIServerSocketAcceptor implements INDIServerAcceptor {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIServerSocketAcceptor.class);

    /**
     * The port to which the Server listens.
     */
    private int listeningPort;

    /**
     * If <code>true</code> the mainThread will continue running.
     */
    private boolean mainThreadRunning;

    /**
     * The socket to which the Server listens.
     */
    private ServerSocket socket;

    /**
     * constructor for the basic server socket acceptor.
     * 
     * @param listeningPort
     *            the port to liten to.
     */
    public INDIServerSocketAcceptor() {
        mainThreadRunning = false;
        this.listeningPort = 0;
    }

    /**
     * Gets the port to which the Server listens.
     * 
     * @return The port to which the Server listens.
     */
    private int getListeningPort() {
        if (listeningPort <= 0) {
            this.listeningPort = Constants.INDI_DEFAULT_PORT;
        }
        return listeningPort;
    }

    /**
     * The thread listens to the server socket and when a client connects, it is
     * added to the list of clients.
     */
    @Override
    public void run() {
        try {
            socket = new ServerSocket(getListeningPort());
        } catch (IOException e) {
            LOG.error("Could not listen on port: " + listeningPort + " (maybe busy)");
            return; // The thread will stop
        }

        LOG.info("Listening to port " + listeningPort);

        mainThreadRunning = true;

        while (mainThreadRunning) {
            Socket clientSocket;

            try {
                clientSocket = socket.accept();
            } catch (IOException e) {
                // This is usually the escape point of the thread when the
                // server is stopped.
                LOG.error("Server has stopped listening to new Client connections", e);
                mainThreadRunning = false;
                return; // The thread will stop
            }

            if (clientSocket != null) {
                INDISocketConnection clientConnection = new INDISocketConnection(clientSocket);
                if (!acceptClient(clientConnection)) {
                    try {
                        clientConnection.close();
                    } catch (IOException e) {
                        LOG.warn("client close exception", e);
                    }
                    LOG.info("Client " + clientSocket.getInetAddress() + " rejected");
                }
            }
        }
    }

    @Override
    public boolean isRunning() {
        return mainThreadRunning;
    }

    @Override
    public void start() {
        if (!mainThreadRunning) {
            Thread serverThread = new Thread(this);
            serverThread.start();
        }
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            LOG.warn("server port close exception", e);
        }
    }

    @Override
    public String getHost() {
        return socket.getInetAddress().getHostName();
    }

    @Override
    public int getPort() {
        return socket.getLocalPort();
    }

    @Override
    public String getName() {
        return "INDI";
    }

    @Override
    public void setArguments(Object... arguments) {
        if (arguments != null && arguments.length > 0 && arguments[0] != null) {
            try {
                this.listeningPort = Integer.parseInt(arguments[0].toString().trim());
            } catch (Exception e) {
                LOG.error("argument was not port integer", e);
            }
        }
    }
}
