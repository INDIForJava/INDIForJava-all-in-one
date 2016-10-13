package org.indilib.i4j.driver.qhyfilterwheel;

/*
 * #%L
 * INDI for Java Driver for the QHY Filter Wheel
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIBLOBElementAndValue;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDIPortProperty;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.connection.INDIConnectionHandler;
import org.indilib.i4j.driver.filterwheel.INDIFilterWheelDriver;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that acts as a INDI for Java Driver for the QHY Filter Wheel.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class I4JQHYFilterWheelDriver extends INDIFilterWheelDriver implements INDIConnectionHandler, Runnable {

    /**
     * bit filter for the second byte.
     */
    private static final int HIGH_BYTE_FILTER = 0xff00;

    /**
     * bit filter for a byte.
     */
    private static final int BYTE_FILTER = 0xff;

    /**
     * Device send a filter changed info response.
     */
    private static final byte FILTER_RESPONSE_FILTER_CHANGED = '-';

    /**
     * Device send a new filter status info response.
     */
    private static final byte FILTER_RESPONSE_STATUS_INFO = 0;

    /**
     * Number of bytes to read for the file status info response
     */
    private static final int FILTER_RESPONSE_STATUS_INFO_SIZE = 16;

    /**
     * buffer size for reading commands from the device.
     */
    private static final int READ_BUFFER_SIZE = 512;

    /**
     * To be documented value of non existent filter position 6.
     */
    private static final int FILTER_POSITION_VALUE_6 = 600;

    /**
     * To be documented value of non existent filter position 7.
     */
    private static final int FILTER_POSITION_VALUE_7 = 700;

    /**
     * To be documented value of non existent filter position 8.
     */
    private static final int FILTER_POSITION_VALUE_8 = 800;

    /**
     * how many bits in a byte.
     */
    private static final int BITS_PER_BYTE = 8;

    /**
     * Milliseconds wait time between send and setup.
     */
    private static final int MILLISECONDS_TIME_BEWEEN_SEND_AND_SETUP = 10;

    /**
     * Milliseconds wait time before closing.
     */
    private static final int MILLISECONDS_TIME_BEFORE_CLOSE = 200;

    /**
     * maximum filter position.
     */
    private static final int MAXIMUM_FILTER_POSITION = 1000;

    /**
     * filter position 1.
     */
    private static final int FILTER_POSITIONS_1 = 0;

    /**
     * filter position 2.
     */
    private static final int FILTER_POSITIONS_2 = 1;

    /**
     * filter position 3.
     */
    private static final int FILTER_POSITIONS_3 = 2;

    /**
     * filter position 4.
     */
    private static final int FILTER_POSITIONS_4 = 3;

    /**
     * filter position 5.
     */
    private static final int FILTER_POSITIONS_5 = 4;

    /**
     * number of filter position.
     */
    private static final int NUMBER_OF_FILTER_POSITIONS = 5;

    /**
     * Logger to log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(I4JQHYFilterWheelDriver.class);

    /**
     * The PORTS property.
     */
    private INDIPortProperty portP;

    /**
     * The serial input stream to communicate with the filter wheel.
     */
    private FileInputStream fwInput;

    /**
     * The serial output stream to communicate with the filter wheel.
     */
    private FileOutputStream fwOutput;

    /**
     * A thread that listens to the answers of the filter wheel.
     */
    private Thread readingThread;

    /**
     * The filter positions (to configure the wheel).
     */
    private INDINumberProperty filterPositionsP;

    /**
     * The filter positions (to configure the wheel).
     */
    private INDINumberElement[] filterPositionsE;

    /**
     * To return to factory settings.
     */
    private INDISwitchProperty factorySettingsP;

    /**
     * To return to factory settings.
     */
    private INDISwitchElement factorySettingsE;

    /**
     * Constructs an instance of a <code>I4JQHYFilterWheelDriver</code> with a
     * particular <code>inputStream</code> from which to read the incoming
     * messages (from clients) and a <code>outputStream</code> to write the
     * messages to the clients.
     * 
     * @param connection
     *            the indi connection to the server.
     */
    public I4JQHYFilterWheelDriver(INDIConnection connection) {
        super(connection);

        initializeStandardProperties();

        portP = INDIPortProperty.create(this, "/dev/ttyUSB0");

        filterPositionsP = newNumberProperty() //
                .name("filter_positions").label("Filter Positions").group("Expert Configuration")//
                .timeout(0).create();

        filterPositionsE = new INDINumberElement[NUMBER_OF_FILTER_POSITIONS];

        filterPositionsE[FILTER_POSITIONS_1] = filterPositionsP.newElement()//
                .name("filter_1_position").label("Filter 1 Position")//
                .maximum(MAXIMUM_FILTER_POSITION).step(1).numberFormat("%1.0f").create();
        filterPositionsE[FILTER_POSITIONS_2] = filterPositionsP.newElement()//
                .name("filter_2_position").label("Filter 2 Position")//
                .maximum(MAXIMUM_FILTER_POSITION).step(1).numberFormat("%1.0f").create();
        filterPositionsE[FILTER_POSITIONS_3] = filterPositionsP.newElement()//
                .name("filter_3_position").label("Filter 3 Position")//
                .maximum(MAXIMUM_FILTER_POSITION).step(1).numberFormat("%1.0f").create();
        filterPositionsE[FILTER_POSITIONS_4] = filterPositionsP.newElement()//
                .name("filter_4_position").label("Filter 4 Position")//
                .maximum(MAXIMUM_FILTER_POSITION).step(1).numberFormat("%1.0f").create();
        filterPositionsE[FILTER_POSITIONS_5] = filterPositionsP.newElement()//
                .name("filter_5_position").label("Filter 5 Position")//
                .maximum(MAXIMUM_FILTER_POSITION).step(1).numberFormat("%1.0f").create();

        factorySettingsP = newSwitchProperty() //
                .name("factory_settings").label("Factory Settings").group("Expert Configuration")//
                .timeout(0).switchRule(SwitchRules.AT_MOST_ONE).create();

        factorySettingsE = factorySettingsP.newElement().name("factory_setting").label("Factory Settings").create();

        addProperty(portP);
    }

    @Override
    public String getName() {
        return "QHY Filter Wheel (RS232)";
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
        super.processNewTextValue(property, timestamp, elementsAndValues);

        portP.processTextValue(property, elementsAndValues);
    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        if (property == factorySettingsP) {
            sendMessageToFilterWheel("SEF");

            sleep(MILLISECONDS_TIME_BEWEEN_SEND_AND_SETUP);

            getSetupPositions();

            factorySettingsP.setState(PropertyStates.OK);

            updateProperty(factorySettingsP);

        }
    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
        super.processNewNumberValue(property, timestamp, elementsAndValues);

        if (property == filterPositionsP) {
            int[] positions = new int[NUMBER_OF_FILTER_POSITIONS];
            for (int i = 0; i < filterPositionsE.length; i++) {
                positions[i] = filterPositionsE[i].getValue().intValue();
            }

            for (INDINumberElementAndValue elementsAndValue : elementsAndValues) {
                for (int h = 0; h < filterPositionsE.length; h++) {
                    if (filterPositionsE[h] == elementsAndValue.getElement()) {
                        positions[h] = elementsAndValue.getValue().intValue();
                    }
                }
            }

            sendSetupPositions(positions);

            sleep(MILLISECONDS_TIME_BEWEEN_SEND_AND_SETUP);

            getSetupPositions();
        }
    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
        // throw new UnsupportedOperationException("Not supported yet."); //To
        // change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        LOG.info("Connecting to QHY Filter Wheel");

        File port = new File(portP.getPort());
        if (!port.exists()) {
            throw new INDIException("Connection to the QHY Filter Wheel failed: port file does not exist.");
        }

        try {
            fwInput = new FileInputStream(portP.getPort());
            fwOutput = new FileOutputStream(portP.getPort());

            readingThread = new Thread(this, getName());
            readingThread.start();
        } catch (IOException e) {
            throw new INDIException("Connection to the QHY Filter Wheel failed. Check port permissions");
        }

        showFilterSlotAndNameProperties();

        addProperty(filterPositionsP);
        addProperty(factorySettingsP);

        (new Thread() {

            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                changeFilter(1);

                getSetupPositions();
            }
        }).start();

    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        LOG.info("Disconnecting from QHY Filter Wheel");

        try {
            if (readingThread != null) {
                readerEnd = true;
                readingThread = null;
            }

            sleep(MILLISECONDS_TIME_BEFORE_CLOSE);

            if (fwInput != null) {
                fwInput.close();
                fwOutput.close();
            }

            fwInput = null;
            fwOutput = null;
        } catch (IOException e) {
            LOG.error("io exception", e);
        }

        hideFilterSlotAndNameProperties();

        removeProperty(filterPositionsP);
        removeProperty(factorySettingsP);

        LOG.info("Disconnected from QHY Filter Wheel");
    }

    @Override
    public int getNumberOfFilters() {
        return NUMBER_OF_FILTER_POSITIONS;
    }

    @Override
    protected void changeFilter(int filterNumber) {
        if (filterNumber > 0 && filterNumber <= getNumberOfFilters()) {
            setBusy();
            if (readingThread != null) {
                lastAskedFilter = filterNumber;
            }

            sendMessageToFilterWheel("" + (filterNumber - 1));
        }
    }

    /**
     * Sets the positions for each filter slot.
     * 
     * @param newPositions
     *            The new positions.
     */
    private void setSetupPositions(int[] newPositions) {
        for (int i = 0; i < NUMBER_OF_FILTER_POSITIONS; i++) {
            if (newPositions[i] < 0 || newPositions[i] > MAXIMUM_FILTER_POSITION) {
                newPositions[i] = 0;
            }
            filterPositionsE[i].setValue("" + newPositions[i]);
        }

        filterPositionsP.setState(PropertyStates.OK);

        updateProperty(filterPositionsP);

    }

    /**
     * Sends the message to retrieve the positions of each slot.
     */
    private void getSetupPositions() {
        sendMessageToFilterWheel("SEG");
    }

    /**
     * Sets the position for each of the 5 filter slots.
     * 
     * @param positions
     *            The positions for each slot.
     */
    private void sendSetupPositions(int[] positions) {
        byte[] message = new byte[]{
            'S',
            'E',
            'W',
            0,
            (byte) (positions[FILTER_POSITIONS_1] >>> BITS_PER_BYTE),
            (byte) positions[FILTER_POSITIONS_1],
            (byte) (positions[FILTER_POSITIONS_2] >>> BITS_PER_BYTE),
            (byte) positions[FILTER_POSITIONS_2],
            (byte) (positions[FILTER_POSITIONS_3] >>> BITS_PER_BYTE),
            (byte) positions[FILTER_POSITIONS_3],
            (byte) (positions[FILTER_POSITIONS_4] >>> BITS_PER_BYTE),
            (byte) positions[FILTER_POSITIONS_4],
            (byte) (positions[FILTER_POSITIONS_5] >>> BITS_PER_BYTE),
            (byte) positions[FILTER_POSITIONS_5],
            (byte) (FILTER_POSITION_VALUE_6 >>> BITS_PER_BYTE),
            (byte) FILTER_POSITION_VALUE_6,
            (byte) (FILTER_POSITION_VALUE_7 >>> BITS_PER_BYTE),
            (byte) FILTER_POSITION_VALUE_7,
            (byte) (FILTER_POSITION_VALUE_8 >>> BITS_PER_BYTE),
            (byte) FILTER_POSITION_VALUE_8
        };

        sendMessageToFilterWheel(message);
    }

    /**
     * Sends a message to the filter wheel.
     * 
     * @param message
     *            The message to be sent
     */
    private void sendMessageToFilterWheel(String message) {
        sendMessageToFilterWheel(message.getBytes());
    }

    /**
     * Sends a message to the filter wheel.
     * 
     * @param message
     *            The message to be sent
     */
    private void sendMessageToFilterWheel(byte[] message) {
        try {
            for (byte element : message) {
                fwOutput.write(element);
                sleep(MILLISECONDS_TIME_BEWEEN_SEND_AND_SETUP);
            }
            fwOutput.flush();
        } catch (IOException e) {
            LOG.error("io exception", e);
        }
    }

    /**
     * Used to signal when the thread must end.
     */
    private boolean readerEnd;

    /**
     * Used to know which was the last asked filter.
     */
    private int lastAskedFilter;

    /**
     * The reader thread.
     */
    @Override
    public void run() {
        readerEnd = false;

        while (!readerEnd) {
            try {
                if (fwInput.available() > 0) {
                    byte br = (byte) fwInput.read();

                    // LOG.info("br " + ((int) br));
                    if (br == FILTER_RESPONSE_FILTER_CHANGED) {
                        filterHasBeenChanged(lastAskedFilter);
                    } else if (br == FILTER_RESPONSE_STATUS_INFO) {
                        byte[] readed = new byte[FILTER_RESPONSE_STATUS_INFO_SIZE];

                        while (fwInput.available() < FILTER_RESPONSE_STATUS_INFO_SIZE) {
                            // Wait until we got all info
                        }
                        fwInput.read(readed);
                        int[] filterPositions = new int[NUMBER_OF_FILTER_POSITIONS];

                        int index = 0;
                        filterPositions[FILTER_POSITIONS_1] = readed[index++] << BITS_PER_BYTE & HIGH_BYTE_FILTER | readed[index++] & BYTE_FILTER;
                        filterPositions[FILTER_POSITIONS_2] = readed[index++] << BITS_PER_BYTE & HIGH_BYTE_FILTER | readed[index++] & BYTE_FILTER;
                        filterPositions[FILTER_POSITIONS_3] = readed[index++] << BITS_PER_BYTE & HIGH_BYTE_FILTER | readed[index++] & BYTE_FILTER;
                        filterPositions[FILTER_POSITIONS_4] = readed[index++] << BITS_PER_BYTE & HIGH_BYTE_FILTER | readed[index++] & BYTE_FILTER;
                        filterPositions[FILTER_POSITIONS_5] = readed[index++] << BITS_PER_BYTE & HIGH_BYTE_FILTER | readed[index++] & BYTE_FILTER;

                        setSetupPositions(filterPositions);
                    }
                }
            } catch (IOException e) {
                readerEnd = true;
            }

            sleep(MILLISECONDS_TIME_BEFORE_CLOSE);
        }

        LOG.info("QHY Filter Wheel Reader Thread Ending");
    }

    /**
     * Sleeps for a certain amount of time.
     * 
     * @param milis
     *            The number of milliseconds to sleep
     */
    private void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            LOG.error("sleep interrupted", e);
        }
    }
}
