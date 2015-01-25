package org.indilib.i4j.driver.nmea0183;

/*
 * #%L
 * INDI for Java NMEA 0183 stream driver
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.connection.INDIConnectionHandler;
import org.indilib.i4j.driver.serial.INDISerialPortExtension;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.driver.nmea0183.NMEA0183Extention.ILineReader;

public class NMEA0183UDPDriver extends INDIDriver implements INDIConnectionHandler {

    private DatagramSocket socket;

    @InjectExtension
    private NMEA0183Extention<NMEA0183UDPDriver> nmea0183Extention;

    protected NMEA0183UDPDriver(INDIConnection connection) {
        super(connection);
        nmea0183Extention.setReader(new ILineReader() {

            Deque<String> queue = new LinkedList<>();

            /**
             * byte buffer from the seial port.
             */
            byte[] buffer = new byte[1024];

            @Override
            public String readLine() {
                // lets see if there is a line in the buffer.
                String line = readLineFromBuffer();
                while (line == null) {
                    // no line in buffer so now block for at least one byte.
                    receive();
                    // lets see if there is a line in the buffer.
                    line = readLineFromBuffer();
                }
                return line;
            }

            private String readLineFromBuffer() {
                return queue.pop();
            }

            @Override
            public void close() {
                socket.close();
            }

            /**
             * Receive UDP packet and return as String
             */
            private String receive() {
                String data = null;
                DatagramPacket pkg = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(pkg);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                data = new String(pkg.getData(), 0, pkg.getLength());
                String[] lines = data.split("\\r?\\n");
                queue.addAll(Arrays.asList(lines));
                return data;
            }

        });
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        nmea0183Extention.connect();
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        nmea0183Extention.disconnect();
    }

    @Override
    public String getName() {
        return NMEA0183UDPDriver.class.getSimpleName();
    }
}
