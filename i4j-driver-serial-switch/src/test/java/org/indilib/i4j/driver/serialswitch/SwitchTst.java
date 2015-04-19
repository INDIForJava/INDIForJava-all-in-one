package org.indilib.i4j.driver.serialswitch;

/*
 * #%L
 * INDI for Java Driver for the cheap serial switches
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

import jssc.SerialPort;

public class SwitchTst {

    public static void main(String[] args) throws Exception {
        SerialPort serialPort = new SerialPort("/dev/ttyUSB3");
        // Open serial port
        serialPort.openPort();
        serialPort.setParams(9600, 8, 1, 0);

        serialPort.writeByte((byte) 0x50);
        Thread.sleep(10);
        serialPort.writeByte((byte) 0x51);
        while (true) {
            Thread.sleep(100);
            serialPort.writeByte((byte) 0xFF);
            Thread.sleep(100);
            serialPort.writeByte((byte) 0x00);
        }
    }
}