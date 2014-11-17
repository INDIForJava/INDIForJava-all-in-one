package org.indilib.i4j.protocol.url;

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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import org.indilib.i4j.protocol.api.INDIInputStream;
import org.indilib.i4j.protocol.api.INDIOutputStream;
import org.indilib.i4j.protocol.io.INDIProtocolFactory;

public class INDIURLConnection extends URLConnection {

    /**
     * timeout to use with tcp connections.
     */
    private static final int CONNECT_TIMEOUT = 20000;

    private Socket socket;

    private INDIInputStream inputStream;

    private INDIOutputStream ouputStream;

    protected INDIURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        int port = getURL().getPort();
        if (port <= 0) {
            port = getURL().getDefaultPort();
        }
        String host = getURL().getHost();
        if (host == null || host.isEmpty()) {
            host = "localhost";
        }
        try {
            socket = new Socket();

            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT);
        } catch (IOException e) {
            throw new IOException("Problem connecting to " + host + ":" + port);
        }

    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            if (socket == null) {
                connect();
            }
            inputStream = INDIProtocolFactory.createINDIInputStream(socket.getInputStream());
        }
        return (InputStream) inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (ouputStream == null) {
            if (socket == null) {
                connect();
            }
            ouputStream = INDIProtocolFactory.createINDIOutputStream(socket.getOutputStream());
        }
        return (OutputStream) ouputStream;
    }

}
