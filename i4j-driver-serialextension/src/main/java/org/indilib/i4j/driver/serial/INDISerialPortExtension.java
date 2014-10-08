package org.indilib.i4j.driver.serial;

/*
 * #%L
 * INDI for Java Serial Port Extension
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
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

import jssc.SerialPort;
import jssc.SerialPortException;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.TextEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INDISerialPortExtension extends INDIDriverExtension<INDIDriver> {

    private static Logger LOG = LoggerFactory.getLogger(INDISerialPortExtension.class);

    protected static final String OPTIONS_TAB = "Options";

    @InjectProperty(name = "PORTS", label = "Ports", group = OPTIONS_TAB, saveable = true)
    protected INDITextProperty port;

    @InjectElement(name = "PORT", label = "Port", textValue = "/dev/ttyUSB0")
    protected INDITextElement portElement;

    private INDISerialPortInterface servialPortInterface;

    private SerialPort serialPort;

    private int baudrate = SerialPort.BAUDRATE_4800;

    private int databits = SerialPort.DATABITS_8;

    private int stopbits = SerialPort.STOPBITS_1;

    private int parity = SerialPort.PARITY_NONE;

    private Thread shutdownHook;

    public INDISerialPortExtension(INDIDriver driver) {
        super(driver);
        if (!isActive()) {
            return;
        }
        this.port.setEventHandler(new TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                property.setValues(elementsAndValues);
                property.setState(PropertyStates.OK);
                updateProperty(property);
            }
        });
        servialPortInterface = (INDISerialPortInterface) driver;
        this.shutdownHook = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (serialPort != null) {
                        serialPort.closePort();
                    }
                } catch (Exception e) {
                    LOG.error("exception during close of the seiral port", e);
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    private void handleSerialException(SerialPortException e) {
        updateProperty(port, "Serial port error " + e.getMessage());
        LOG.error("Serial port error", e);
    }

    public synchronized boolean close() {
        try {
            if (this.serialPort != null) {
                this.serialPort.closePort();// Close serial port
            }
            this.serialPort = null;
            return true;
        } catch (SerialPortException e) {
            handleSerialException(e);
            return false;
        }
    }

    @Override
    public void connect() {
        if (!isActive()) {
            return;
        }
        addProperty(port);
    }

    @Override
    public void disconnect() {
        if (!isActive()) {
            return;
        }
        close();
        removeProperty(port);
    }

    public SerialPort getOpenSerialPort() {
        if (serialPort == null || !serialPort.isOpened()) {
            open();
        }
        return serialPort;
    }

    @Override
    public boolean isActive() {
        return driver instanceof INDISerialPortInterface;
    }

    public synchronized boolean open() {
        if (this.serialPort != null) {
            close();
        }
        try {
            this.serialPort = new SerialPort(portElement.getValue());
            this.serialPort.openPort();// Open serial port
            this.serialPort.setParams(baudrate, databits, stopbits, parity);
            return true;
        } catch (SerialPortException e) {
            handleSerialException(e);
            return false;
        }
    }

    public INDISerialPortExtension setBaudrate(int baudrate) {
        this.baudrate = baudrate;
        return this;
    }

    public INDISerialPortExtension setDatabits(int databits) {
        this.databits = databits;
        return this;
    }

    public INDISerialPortExtension setParity(int parity) {
        this.parity = parity;
        return this;
    }

    public INDISerialPortExtension setStopbits(int stopbits) {
        this.stopbits = stopbits;
        return this;
    }
}
