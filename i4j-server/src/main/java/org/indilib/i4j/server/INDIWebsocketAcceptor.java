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

import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.server.api.INDIServerAccessLookup;
import org.indilib.i4j.server.api.INDIServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netiq.websockify.WebsockifyServer;

public class INDIWebsocketAcceptor implements INDIServerAcceptor {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIWebsocketAcceptor.class);

    private int localPort;

    private WebsockifyServer wss = new WebsockifyServer();

    @Override
    public void run() {
        INDIServerInterface server = INDIServerAccessLookup.indiServerAccess().get();
        wss.connect(localPort, server.getHost(), server.getPort());
    }

    @Override
    public boolean acceptClient(INDIConnection clientSocket) {
        // FOR NOW no clients to accept
        return false;
    }

    @Override
    public boolean isRunning() {
        return wss != null;
    }

    @Override
    public void start() {
        new Thread(this).start();
    }

    @Override
    public void close() {
        wss.close();
        wss = null;
    }

    @Override
    public String getHost() {
        return INDIServerAccessLookup.indiServerAccess().get().getHost();
    }

    @Override
    public int getPort() {
        return localPort;
    }

    @Override
    public String getName() {
        return "Websocket";
    }

    @Override
    public void setArguments(Object... arguments) {
        if (arguments != null && arguments.length > 0 && arguments[0] != null) {
            try {
                this.localPort = Integer.parseInt(arguments[0].toString().trim());
            } catch (Exception e) {
                LOG.error("argument was not port integer", e);
            }
        }
    }

}
