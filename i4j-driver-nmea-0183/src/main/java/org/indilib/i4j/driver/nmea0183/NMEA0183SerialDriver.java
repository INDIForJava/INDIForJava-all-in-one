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

import java.util.Date;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.connection.INDIConnectionHandler;
import org.indilib.i4j.driver.nmea0183.NMEA0183Extention.ILineReader;
import org.indilib.i4j.driver.serial.INDISerialPortExtension;
import org.indilib.i4j.protocol.api.INDIConnection;

public class NMEA0183SerialDriver extends INDIDriver implements INDIConnectionHandler {

    @InjectExtension
    private INDISerialPortExtension serialPortExtension;

    @InjectExtension
    private NMEA0183Extention<NMEA0183SerialDriver> nmea0183Extention;

    protected NMEA0183SerialDriver(INDIConnection connection) {
        super(connection);
        serialPortExtension.setPortDetailesFixed(false);
        nmea0183Extention.setReader(new ILineReader() {

            /**
             * byte buffer from the seial port.
             */
            byte[] buffer = new byte[1024];

            int pointer;

            @Override
            public String readLine() {
                // lets see if there is a line in the buffer.
                String line = readLineFromBuffer();
                while (line == null) {
                    // no line in buffer so now block for at least one byte.
                    buffer[pointer++] = serialPortExtension.readByte();
                    // read the available rest
                    byte[] rest = serialPortExtension.readBytes();
                    if (rest != null) {
                        System.arraycopy(rest, 0, buffer, pointer, rest.length);
                        pointer += rest.length;
                    }
                    // lets see if there is a line in the buffer.
                    line = readLineFromBuffer();
                }
                return line;
            }

            private String readLineFromBuffer() {
                int stringStart = 0;
                while (stringStart < pointer && Character.isWhitespace(buffer[stringStart])) {
                    stringStart++;
                }
                int endIndex = stringStart;
                while (endIndex < pointer && buffer[endIndex] != '\r' || buffer[endIndex] != '\n') {
                    endIndex++;
                }
                if (stringStart < endIndex) {
                    String line = new String(buffer, stringStart, endIndex - stringStart).trim();
                    System.arraycopy(buffer, endIndex, buffer, 0, pointer - endIndex);
                    pointer = pointer - endIndex;
                    if (!line.isEmpty()) {
                        return line;
                    }
                }
                return null;
            }

            @Override
            public void close() {
                serialPortExtension.close();
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
        return NMEA0183SerialDriver.class.getSimpleName();
    }
}
