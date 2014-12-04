package org.indilib.i4j.protocol.url.websocket;

/*
 * #%L
 * INDI Protocol implementation
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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.indilib.i4j.protocol.INDIProtocol;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.api.INDIInputStream;
import org.indilib.i4j.protocol.api.INDIOutputStream;
import org.indilib.i4j.protocol.io.INDIProtocolFactory;
import org.indilib.i4j.protocol.url.INDIURLStreamHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The websocket client for the connection to the indiserver.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIWebSocketConnection extends URLConnection implements INDIConnection {

    /**
     * size of the buffer for the piped connection.
     */
    private static final int BUFFER_SIZE = 16 * 1024;

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIWebSocketConnection.class);

    /**
     * the indi input stream to read the protokol objects.
     */
    private INDIInputStream indiInputStream;

    /**
     * the output stream to write the incomming bytes from the server socket.
     */
    private PipedOutputStream outputStream;

    /**
     * the output sreatm where the objects to send are transformed and sein to
     * the server as websocket messages.
     */
    private INDIOutputStream indiOutputStream;

    /**
     * the websocket session.
     */
    private Session session;

    /**
     * construct a websocet connection to the server.
     * 
     * @param url
     *            the connection describing url
     */
    public INDIWebSocketConnection(URL url) {
        super(url);
        try {
            outputStream = new PipedOutputStream();
            INDIProtocolFactory.createINDIInputStream(new PipedInputStream(outputStream, BUFFER_SIZE));
        } catch (IOException e) {
            LOG.error("should not happen, when then the memory is out");
        }
        indiOutputStream = new INDIOutputStream() {

            @Override
            public synchronized void writeObject(INDIProtocol<?> element) throws IOException {
                synchronized (INDIWebSocketConnection.this) {
                    session.getBasicRemote().sendText(INDIProtocolFactory.toXml(element));
                }
            }

            @Override
            public void close() throws IOException {
                synchronized (INDIWebSocketConnection.this) {
                    session.close();
                }
            }
        };
    }

    static {
        INDIURLStreamHandlerFactory.init();
    }

    @Override
    public void connect() throws IOException {

        try {
            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

            ClientManager client = ClientManager.createClient();
            client.connectToServer(new Endpoint() {

                @Override
                public void onOpen(Session newSession, EndpointConfig config) {
                    INDIWebSocketConnection.this.session = newSession;
                    newSession.addMessageHandler(new MessageHandler.Partial<ByteBuffer>() {

                        @Override
                        public void onMessage(ByteBuffer partialMessage, boolean last) {
                            try {
                                outputStream.write(partialMessage.array());
                                if (last) {
                                    outputStream.flush();
                                }
                            } catch (IOException e) {
                                LOG.error("could not pass data stream", e);
                            }
                        }
                    });

                }
            }, cec, new URI(getURL().toExternalForm().replace("windi:", "ws:")));
        } catch (Exception e) {
            LOG.error("could not connect to websocket");
        }
    }

    @Override
    public INDIInputStream getINDIInputStream() throws IOException {
        return indiInputStream;
    }

    @Override
    public INDIOutputStream getINDIOutputStream() throws IOException {
        return indiOutputStream;
    }

    @Override
    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            synchronized (INDIWebSocketConnection.this) {
                session.close();
            }
        }
    }

}
