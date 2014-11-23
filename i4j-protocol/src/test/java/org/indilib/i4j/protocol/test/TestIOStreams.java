package org.indilib.i4j.protocol.test;

/*
 * #%L INDI Protocol implementation %% Copyright (C) 2012 - 2014 indiforjava %%
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Lesser Public License for more details. You should have received a copy of
 * the GNU General Lesser Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>. #L%
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.indilib.i4j.protocol.GetProperties;
import org.indilib.i4j.protocol.INDIProtocol;
import org.indilib.i4j.protocol.OneBlob;
import org.indilib.i4j.protocol.OneText;
import org.indilib.i4j.protocol.SetTextVector;
import org.indilib.i4j.protocol.api.INDIInputStream;
import org.indilib.i4j.protocol.api.INDIOutputStream;
import org.indilib.i4j.protocol.io.INDIPipedConnections;
import org.indilib.i4j.protocol.io.INDIProtocolFactory;
import org.indilib.i4j.protocol.io.INDISocketConnection;
import org.indilib.i4j.protocol.url.INDIURLConnection;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the encode and decode indi protokol streams.
 * 
 * @author Richard van Nieuwenhoven
 */
public class TestIOStreams {

    /**
     * how many tries to connect befor failing.
     */
    private static final int TRIES_TO_WAIT_FOR_SERVER = 10;

    /**
     * time to wait.
     */
    private static final long HUNDERD_MILLISECONDS = 100L;

    /**
     * nr of bytes to test.
     */
    private static final int TEST_BYTE_SIZE = 200;

    /**
     * Test the encode and decode streams.
     * 
     * @throws Exception
     *             fails the test
     */
    @Test
    public void testEncodeDecode() throws Exception {
        OneBlob elem1 = new OneBlob().setByteContent(new byte[TEST_BYTE_SIZE]).setName("hugo");
        OneText elem2 = new OneText().setTextContent("XXX").setName("hugo");
        OneText elem3 = new OneText().setName("hugo");
        OneText elem4 = new OneText().setName("hugo");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        INDIOutputStream out = INDIProtocolFactory.createINDIOutputStream(bytes);
        out.writeObject(elem1);
        out.writeObject(elem2);
        out.writeObject(elem3);
        out.writeObject(elem4);

        SetTextVector list = new SetTextVector()//
                .setDevice("ZZ")//
                .addElement(elem1)//
                .addElement(elem2)//
                .addElement(elem3)//
                .addElement(elem4);
        out.writeObject(list);
        out.close();

        INDIInputStream in = INDIProtocolFactory.createINDIInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        INDIProtocol<?> object;
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        out = INDIProtocolFactory.createINDIOutputStream(outBytes);
        while ((object = in.readObject()) != null) {
            out.writeObject(object);
        }
        in.close();
        out.close();
        String expected =
                "<oneBLOB name=\"hugo\" size=\"200\">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</oneBLOB><oneText name=\"hugo\">XXX</oneText><oneText name=\"hugo\"></oneText><oneText n"
                        + "ame=\"hugo\"></oneText><setTextVector device=\"ZZ\"><oneBLOB name=\"hugo\" size=\"200\">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</oneBLOB><oneText name=\"hugo\">XXX</on"
                        + "eText><oneText name=\"hugo\"></oneText><oneText name=\"hugo\"></oneText></setTextVector>";
        Assert.assertEquals(expected, new String(outBytes.toByteArray(), "UTF-8"));
    }

    /**
     * test the socket and url connection.
     * 
     * @throws Exception
     *             fails the test
     */
    @Test
    public void testINDIProtocolConnection() throws Exception {
        final int[] serverport = new int[1];
        serverport[0] = -1;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ServerSocket socket = new ServerSocket(0);
                    serverport[0] = socket.getLocalPort();
                    int count = 0;
                    while (count < 1 && !socket.isClosed()) {
                        Socket client = socket.accept();
                        count++;
                        INDISocketConnection socketConnection = new INDISocketConnection(client);
                        OneText onetext = (OneText) socketConnection.getINDIInputStream().readObject();
                        onetext.setTextContent(onetext.getTextContent() + " was here ");
                        socketConnection.getINDIOutputStream().writeObject(onetext);
                        onetext.setTextContent(onetext.getTextContent() + "again");
                        socketConnection.getINDIOutputStream().writeObject(onetext);
                        // give some time to react.
                        Thread.sleep(HUNDERD_MILLISECONDS);
                        socketConnection.close();
                    }
                    socket.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        int count = TRIES_TO_WAIT_FOR_SERVER;
        while (serverport[0] < 0 && count-- > 0) {
            Thread.sleep(HUNDERD_MILLISECONDS);
        }
        if (count < 0) {
            Assert.fail("could not start server");
        }
        OneText element = new OneText();
        element.setTextContent("123");

        INDIURLConnection connection = (INDIURLConnection) new URL("indi://0.0.0.0:" + serverport[0]).openConnection();

        connection.getINDIOutputStream().writeObject(element);
        OneText result1 = (OneText) connection.getINDIInputStream().readObject();
        OneText result2 = (OneText) connection.getINDIInputStream().readObject();

        Assert.assertEquals("123 was here ", result1.getTextContent());
        Assert.assertEquals("123 was here again", result2.getTextContent());
        connection.close();
    }

    /**
     * test the piped connection.
     * 
     * @throws Exception
     *             fails the test
     */
    @Test
    public void testINDIPipedConnection() throws Exception {
        final INDIPipedConnections connections = new INDIPipedConnections();

        final OneText[] manyTimes = new OneText[1];

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    OneText element = new OneText();
                    element.setTextContent("123");
                    connections.first().getINDIOutputStream().writeObject(element);
                    element = (OneText) connections.first().getINDIInputStream().readObject();
                    element.setTextContent(element.getTextContent() + "A");
                    connections.first().getINDIOutputStream().writeObject(element);
                    element = (OneText) connections.first().getINDIInputStream().readObject();
                    element.setTextContent(element.getTextContent() + "B");
                    connections.first().getINDIOutputStream().writeObject(element);
                    element = (OneText) connections.first().getINDIInputStream().readObject();
                    element.setTextContent(element.getTextContent() + "C");
                    manyTimes[0] = element;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    OneText element = (OneText) connections.second().getINDIInputStream().readObject();
                    element.setTextContent(element.getTextContent() + "D");
                    connections.second().getINDIOutputStream().writeObject(element);
                    element = (OneText) connections.second().getINDIInputStream().readObject();
                    element.setTextContent(element.getTextContent() + "E");
                    connections.second().getINDIOutputStream().writeObject(element);
                    element = (OneText) connections.second().getINDIInputStream().readObject();
                    element.setTextContent(element.getTextContent() + "F");
                    connections.second().getINDIOutputStream().writeObject(element);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();
        int count = TRIES_TO_WAIT_FOR_SERVER;
        while (manyTimes[0] == null && count-- > 0) {
            Thread.sleep(HUNDERD_MILLISECONDS);
        }
        Assert.assertEquals("123DAEBFC", manyTimes[0].getTextContent());
    }

    /**
     * This test is normally ignored because it only logs all message comming
     * from the server.
     * 
     * @throws Exception
     *             fails the test
     */
    @Test
    @Ignore
    public void testINDIServerConnection() throws Exception {
        GetProperties element = new GetProperties();
        element.setVersion("1.7");

        // "indi:///" = localhost and default port
        INDIURLConnection connection = (INDIURLConnection) new URL("indi:///").openConnection();
        connection.getINDIOutputStream().writeObject(element);

        INDIProtocol<?> result = connection.getINDIInputStream().readObject();
        while (result != null) {
            System.out.println(result);
            result = connection.getINDIInputStream().readObject();
        }
        connection.close();
    }
}
