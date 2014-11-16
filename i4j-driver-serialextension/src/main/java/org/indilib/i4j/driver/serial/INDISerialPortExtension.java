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

/**
 * Most astronomical devices are controlled by a serial connection, this
 * extension handles the connection for the driver so the driver does only have
 * to know the connection specification and then work with the input- and
 * output-streams.<br/>
 * TODO: reading and writing to the serial port should also be encapulated. This
 * will be done as soon as there are more drivers using this extension.
 */
public class INDISerialPortExtension extends INDIDriverExtension<INDIDriver> {

    /**
     * helper to convert integers to unsigned bytes and back.
     */
    private static final int UNSIGNED_BYTE_HELPER = 0xFF;

    /**
     * wait 100 miniseconds before skipping bytes in the queue.
     */
    private static final long MILLISECONDS_TO_WAIT_BEFORE_SKIPPING_BYTES = 100L;

    /**
     * The logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDISerialPortExtension.class);

    /**
     * the property representing the serial connection port.
     */
    @InjectProperty(name = "PORTS", label = "Ports", group = INDIDriver.GROUP_OPTIONS, saveable = true)
    protected INDITextProperty port;

    /**
     * The element representing the serial connection port.
     */
    @InjectElement(name = "PORT", label = "Port", textValue = "/dev/ttyUSB0")
    protected INDITextElement portElement;

    /**
     * The indicator interface if the extension is active for the driver.
     */
    private INDISerialPortInterface serialPortInterface;

    /**
     * the real serial port.
     */
    private SerialPort serialPort;

    /**
     * the connection baut rate.
     */
    private int baudrate = SerialPort.BAUDRATE_4800;

    /**
     * the number of data bits.
     */
    private int databits = SerialPort.DATABITS_8;

    /**
     * the number os stop bits.
     */
    private int stopbits = SerialPort.STOPBITS_1;

    /**
     * the parity type to use.
     */
    private int parity = SerialPort.PARITY_NONE;

    /**
     * The shutdown hook to clear the connection on jvm shutdown.
     */
    private Thread shutdownHook;

    /**
     * Extension constructor. do not call this yourself.
     * 
     * @param driver
     *            the driver to connect the extension to.
     */
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
        serialPortInterface = (INDISerialPortInterface) driver;
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

    /**
     * Handle any servial exception, by logging it and sendig a message to the
     * client.
     * 
     * @param e
     *            the exception that occured.
     */
    private void handleSerialException(SerialPortException e) {
        updateProperty(port, "Serial port error " + e.getMessage());
        LOG.error("Serial port error", e);
    }

    /**
     * Close the serial port if it was opend.
     * 
     * @return true if successful.
     */
    public synchronized boolean close() {
        try {
            if (this.serialPort != null) {
                // Close serial port
                this.serialPort.closePort();
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

    /**
     * @return an open serial port to send or receive bytes. If the port was not
     *         yet opened it will be.
     */
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

    /**
     * open the serial port with the specified properties. If it was already
     * open, close it first.
     * 
     * @return true if successful.
     */
    public synchronized boolean open() {
        if (this.serialPort != null) {
            close();
        }
        try {
            this.serialPort = new SerialPort(portElement.getValue());
            // Open serial port
            this.serialPort.openPort();
            this.serialPort.setParams(baudrate, databits, stopbits, parity);
            return true;
        } catch (SerialPortException e) {
            handleSerialException(e);
            return false;
        }
    }

    /**
     * set the baut rate.
     * 
     * @param newBaudrate
     *            the baut rate to set.
     * @return this extension itself (builder pattern).
     */
    public INDISerialPortExtension setBaudrate(int newBaudrate) {
        this.baudrate = newBaudrate;
        return this;
    }

    /**
     * set the number of databits.
     * 
     * @param newDatabits
     *            the number of databits to set.
     * @return this extension itself (builder pattern).
     */
    public INDISerialPortExtension setDatabits(int newDatabits) {
        this.databits = newDatabits;
        return this;
    }

    /**
     * set the parity.
     * 
     * @param newParity
     *            the parity to set.
     * @return this extension itself (builder pattern).
     */
    public INDISerialPortExtension setParity(int newParity) {
        this.parity = newParity;
        return this;
    }

    /**
     * set the number of stopbits.
     * 
     * @param newStopbits
     *            the number of stopbits to set.
     * @return this extension itself (builder pattern).
     */
    public INDISerialPortExtension setStopbits(int newStopbits) {
        this.stopbits = newStopbits;
        return this;
    }

    /**
     * send one byte over the serial port. An illegal state exception will be
     * thrown if the communication breaks down.
     * 
     * @param value
     *            the byte value to send.
     * @param skipQueue
     *            if true all bytes currently in the read queue will be scipped.
     */
    public void sendByte(byte value, boolean skipQueue) {
        try {
            if (skipQueue) {
                skipBytes();
            }
            this.serialPort.writeByte(value);
        } catch (Exception e) {
            throw new IllegalStateException("serial port communication with telescope interupted", e);
        }
    }

    /**
     * send multiple bytes over the serial port. An illegal state exception will
     * be thrown if the communication breaks down. All bytes currently in the
     * read queue will be scipped.
     * 
     * @param bytes
     *            the bytes to send.
     * @param skipQueue
     *            if true all bytes currently in the read queue will be scipped.
     */
    public void sendBytes(byte[] bytes, boolean skipQueue) {
        try {
            skipBytes();
            this.serialPort.writeBytes(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("serial port communication with telescope interupted", e);
        }
    }

    /**
     * send one byte over the serial port. An illegal state exception will be
     * thrown if the communication breaks down. All bytes currently in the read
     * queue will be scipped.
     * 
     * @param value
     *            the byte value to send (the integer will be cast to a byte).
     * @param skipQueue
     *            if true all bytes currently in the read queue will be scipped.
     */
    public void sendByte(int value, boolean skipQueue) {
        sendByte((byte) value, skipQueue);
    }

    /**
     * skip all bytes currently in the queue.
     */
    public void skipBytes() {
        try {
            if (this.serialPort.getInputBufferBytesCount() > 0) {
                // ok there is something wrong here lets wait for more to come.
                Thread.sleep(MILLISECONDS_TO_WAIT_BEFORE_SKIPPING_BYTES);
                // now consume them all
                byte[] buffer = this.serialPort.readBytes();
                StringBuffer string = new StringBuffer();
                for (byte b : buffer) {
                    String hexString = Integer.toHexString(b & UNSIGNED_BYTE_HELPER);
                    while (hexString.length() < 2) {
                        hexString = "0" + hexString;
                    }
                    string.append(hexString);
                }
                LOG.warn("skipped 0x" + string);
            }
        } catch (Exception e) {
            throw new IllegalStateException("serial port communication with telescope interupted", e);
        }
    }

    /**
     * blocking read one byte from the serial port. An illegal state exception
     * will be thrown if the communication breaks down.
     * 
     * @return the read byte.
     */
    public byte readByte() {
        try {
            return this.serialPort.readBytes(1)[0];
        } catch (Exception e) {
            throw new IllegalStateException("serial port communication with telescope interupted", e);
        }
    }

    /**
     * blocking read a number of bytes from the serial port. An illegal state
     * exception will be thrown if the communication breaks down.
     * 
     * @param nrOfBytes
     *            the number of bytes to read.
     * @return the read byte array.
     */
    public byte[] readByte(int nrOfBytes) {
        try {
            return this.serialPort.readBytes(nrOfBytes);
        } catch (Exception e) {
            throw new IllegalStateException("serial port communication with telescope interupted", e);
        }
    }
}
