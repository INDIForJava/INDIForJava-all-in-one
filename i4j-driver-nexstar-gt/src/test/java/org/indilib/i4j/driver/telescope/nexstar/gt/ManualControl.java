package org.indilib.i4j.driver.telescope.nexstar.gt;

/*
 * #%L
 * INDI for Java Driver for the NexStar GT Mount
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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import jssc.SerialPort;

public class ManualControl {

    // max scale azi 0B 16 1E alti 0B 16 1E

    static class Reader implements Runnable {

        public Reader(SerialPort serialPort) {
            this.serialPort = serialPort;
        }

        SerialPort serialPort;

        @Override
        public void run() {
            while (serialPort.isOpened()) {
                try {
                    if (serialPort.getInputBufferBytesCount() > 0) {
                        System.err.println(serialPort.readHexString());
                        Thread.sleep(25L);
                    } else {
                        Thread.sleep(100L);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            final SerialPort serialPort = new SerialPort("/dev/ttyAMA0");
            serialPort.openPort();// Open serial port
            serialPort.setParams(SerialPort.BAUDRATE_4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        serialPort.closePort();
                    } catch (Exception e) {
                    }
                }
            }));

            String line;
            new Thread(new Reader(serialPort)).start();
            while ((line = reader.readLine()) != null) {
                for (int i = 0; i < line.length(); i += 2) {
                    int value = Integer.parseInt(line.substring(i, i + 2), 16);
                    serialPort.writeInt(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
