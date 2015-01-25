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

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import net.sf.marineapi.nmea.io.SentenceReader;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortList;
import static jssc.SerialPort.*;

public class TestListSerialPorts {

    public static void main(String[] args) throws Exception {
        String portName = "/dev/rfcomm0";
        // |MHU - Humidity
        // |MMB - Barometer

        // $WIMDA,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>,<9>,<10>,<11>,
        // <12>,<13>,<14>,<15>,<16>,<17>,<18>,<19>,<20>*hh
        // <CR><LF>
        // Fields
        // <1> Barometric pressure, inches of mercury, to the nearest 0.01 inch
        // <2> I = inches of mercury
        // <3> Barometric pressure, bars, to the nearest .001 bar
        // <4> B = bars
        // <5> Air temperature, degrees C, to the nearest 0.1 degree C
        // <6> C = degrees C
        // <7> Water temperature, degrees C (this field left blank by
        // WeatherStation)
        // <8> C = degrees C (this field left blank by WeatherStation)
        // <9> Relative humidity, percent, to the nearest 0.1 percent (this
        // field left
        // blank by WeatherStation)
        // <10> Absolute humidity, percent (this field left blank by
        // WeatherStation)
        // <11> Dew point, degrees C, to the nearest 0.1 degree C (this field
        // left blank
        // by WeatherStation)
        // <12> C = degrees C
        // <13> Wind direction, degrees True, to the nearest 0.1 degree
        // <14> T = true
        // <15> Wind direction, degrees Magnetic, to the nearest 0.1 degree
        // <16> M = magnetic
        // <17> Wind speed, knots, to the nearest 0.1 knot
        // <18> N = knots
        // <19> Wind speed, meters per second, to the nearest 0.1 m/s
        // <20> M = meters per second

        // for (String port : SerialPortList.getPortNames()) {
        // System.out.println("detected " + port);
        // portName = port;
        // }
        // Thread.sleep(100L);
        portName = "/dev/rfcomm0";
        System.out.println("connecting " + portName);
        // /dev/rfcomm0
        SerialPort port = new SerialPort(portName);

        port.openPort();
        port.setParams(BAUDRATE_9600, DATABITS_8, STOPBITS_1, PARITY_NONE);

        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);

        TestMArine testMArine = new TestMArine();
        SentenceReader reader = new SentenceReader(in);
        reader.addSentenceListener(testMArine);
        reader.start();

        while (true) {
            byte[] readBytes = port.readBytes(10);
            if (readBytes != null) {
                out.write(readBytes);
            }
        }

    }
}
