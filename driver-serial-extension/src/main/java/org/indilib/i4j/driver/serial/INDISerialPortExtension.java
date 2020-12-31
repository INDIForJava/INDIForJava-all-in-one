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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import jssc.SerialPort;
import jssc.SerialPortException;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.driver.*;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.NumberEvent;
import org.indilib.i4j.driver.event.TextEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.indilib.i4j.properties.INDIStandardElement.PORT;
import static org.indilib.i4j.properties.INDIStandardProperty.DEVICE_PORT;

/**
 * Most astronomical devices are controlled by a serial connection, this
 * extension handles the connection for the driver so the driver does only have
 * to know the connection specification and then work with the input- and
 * output-streams. TODO: reading and writing to the serial port should also be
 * encapsulated. This will be done as soon as there are more drivers using this
 * extension.
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
    @InjectProperty(std = DEVICE_PORT, label = "Ports", group = INDIDriver.GROUP_OPTIONS, saveable = true)
    protected INDITextProperty port;

    /**
     * The element representing the serial connection port.
     */
    @InjectElement(std = PORT, label = "Port", textValue = "/dev/ttyUSB0")
    protected INDITextElement portElement;

    /**
     * the property representing the connection details for serial connection
     * port.
     */
    @InjectProperty(name = "PORTS_DETAILS", label = "Port details", group = INDIDriver.GROUP_OPTIONS, saveable = true)
    protected INDINumberProperty portDetails;

    /**
     * The element representing the serial connection port Baut rate.
     */
    @InjectElement(name = "BAUD", label = "Baut rate", numberValue = SerialPort.BAUDRATE_4800, numberFormat = "%10.0f")
    protected INDINumberElement portBaut;

    /**
     * The element representing the serial connection port Data bits.
     */
    @InjectElement(name = "DATABITS", label = "Data bits", numberValue = SerialPort.DATABITS_8, numberFormat = "%2.0f")
    protected INDINumberElement portDataBits;

    /**
     * The element representing the serial connection port Stop bits.
     */
    @InjectElement(name = "STOPBITS", label = "Stop bits", numberValue = SerialPort.STOPBITS_1, numberFormat = "%2.0f")
    protected INDINumberElement portStopBits;

    /**
     * The element representing the serial connection port.
     */
    @InjectElement(name = "PARITY", label = "parity", numberValue = SerialPort.PARITY_NONE, numberFormat = "%2.0f")
    protected INDINumberElement portParity;

    /**
     * The indicator interface if the extension is active for the driver.
     */
    private INDISerialPortInterface serialPortInterface;

    /**
     * the real serial port.
     */
    private SerialPort serialPort;

    /**
     * Minimum milliseconds between commands.
     */
    private int minimumMillisecondsBetweenCommands = 0;

    /**
     * last time a command was send.
     */
    private long lastSendCommand;

    /**
     * The shutdown hook to clear the connection on jvm shutdown.
     */
    private Thread shutdownHook;

    /**
     * can the port details be changed from the client?
     */
    private boolean portDetailesFixed = true;

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
        port.setEventHandler(new TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                property.setValues(elementsAndValues);
                property.setState(PropertyStates.OK);
                updateProperty(property);
            }
        });
        portDetails.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                property.setValues(elementsAndValues);
                property.setState(PropertyStates.OK);
                updateProperty(property);
            }
        });
        serialPortInterface = (INDISerialPortInterface) driver;
        shutdownHook = new Thread(() -> {
            try {
                if (serialPort != null) {
                    serialPort.closePort();
                }
            } catch (Exception e) {
                LOG.error("exception during close of the seiral port", e);
            }
        }, "serial port close hook");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        addProperty(port);
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
     * make sure the miminum time between commands is honnered.
     */
    private void waitBeforeNextCommand() {
        if (minimumMillisecondsBetweenCommands > 0) {
            long now = System.currentTimeMillis();
            long timeToWait = minimumMillisecondsBetweenCommands - (now - lastSendCommand);
            if (timeToWait > 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("waiting time between commands activated for " + timeToWait + " milliseconds.");
                }
                try {
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    LOG.warn("waiting time between commands interrupted");
                }
            }
            lastSendCommand = System.currentTimeMillis();
        }
    }

    /**
     * Close the serial port if it was opend.
     * 
     * @return true if successful.
     */
    public synchronized boolean close() {
        try {
            if (serialPort != null) {
                // Close serial port
                serialPort.closePort();
            }
            serialPort = null;
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
    }

    @Override
    public void disconnect() {
        if (!isActive()) {
            return;
        }
        close();
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
        if (serialPort != null) {
            close();
        }
        try {
            serialPort = new SerialPort(portElement.getValue());
            // Open serial port
            serialPort.openPort();
            serialPort.setParams(portBaut.getIntValue(), portDataBits.getIntValue(), portStopBits.getIntValue(), portParity.getIntValue());
            return true;
        } catch (SerialPortException e) {
            handleSerialException(e);
            return false;
        }
    }

    /**
     * @return the Minimum milliseconds between commands.
     */
    public int getMinimumMillisecondsBetweenCommands() {
        return minimumMillisecondsBetweenCommands;
    }

    /**
     * set the Minimum milliseconds between commands.
     * 
     * @param minimumMillisecondsBetweenCommands
     *            the new value
     */
    public void setMinimumMillisecondsBetweenCommands(int minimumMillisecondsBetweenCommands) {
        this.minimumMillisecondsBetweenCommands = minimumMillisecondsBetweenCommands;
    }

    /**
     * can the port details be changed by the client? defaults to false because
     * most devices have fixed settings.
     * 
     * @param portDetailesFixed
     *            true if they can.
     */
    public void setPortDetailesFixed(boolean portDetailesFixed) {
        this.portDetailesFixed = portDetailesFixed;
        if (!this.portDetailesFixed) {
            addProperty(portDetails);
        } else {
            removeProperty(portDetails);
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
        portBaut.setValue(newBaudrate);
        updateProperty(portDetails);
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
        portDataBits.setValue(newDatabits);
        updateProperty(portDetails);
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
        portParity.setValue(newParity);
        updateProperty(portDetails);
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
        portStopBits.setValue(newStopbits);
        updateProperty(portDetails);
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
        if (serialPort != null) {
            try {

                if (skipQueue) {
                    skipBytes();
                }
                waitBeforeNextCommand();
                serialPort.writeByte(value);
            } catch (Exception e) {
                throw new IllegalStateException("serial port communication with telescope interupted", e);
            }
        } else {
            LOG.warn("serial send ignored, port closed");
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
        if (serialPort != null) {
            try {
                skipBytes();
                waitBeforeNextCommand();
                serialPort.writeBytes(bytes);
            } catch (Exception e) {
                throw new IllegalStateException("serial port communication with telescope interupted", e);
            }
        } else {
            LOG.warn("serial send ignored, port closed");
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
        if (serialPort != null) {
            try {
                if (serialPort.getInputBufferBytesCount() > 0) {
                    // ok there is something wrong here lets wait for more to
                    // come.
                    Thread.sleep(MILLISECONDS_TO_WAIT_BEFORE_SKIPPING_BYTES);
                    // now consume them all
                    byte[] buffer = serialPort.readBytes();
                    StringBuilder string = new StringBuilder();
                    for (byte b : buffer) {
                        StringBuilder hexString = new StringBuilder(Integer.toHexString(b & UNSIGNED_BYTE_HELPER));
                        while (hexString.length() < 2) {
                            hexString.insert(0, "0");
                        }
                        string.append(hexString);
                    }
                    LOG.warn("skipped 0x" + string);
                }
            } catch (Exception e) {
                throw new IllegalStateException("serial port communication with telescope interupted", e);
            }
        } else {
            LOG.warn("serial skip ignored, port closed");
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
            return serialPort.readBytes(1)[0];
        } catch (Exception e) {
            throw new IllegalStateException("serial port communication with telescope interupted", e);
        }
    }

    /**
     * non blocking read all the availabe bytes from the interface.
     * 
     * @return the read bytes.
     */
    public byte[] readBytes() {
        try {
            return serialPort.readBytes();
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
        if (serialPort != null) {
            try {
                return serialPort.readBytes(nrOfBytes);
            } catch (Exception e) {
                throw new IllegalStateException("serial port communication with telescope interupted", e);
            }
        } else {
            LOG.warn("serial read ignored, port closed");
            return new byte[0];
        }
    }
}
