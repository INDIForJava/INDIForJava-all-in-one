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

import org.indilib.i4j.driver.serial.INDISerialPortExtension;
import org.indilib.i4j.driver.telescope.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexStarGtMount extends Mount<NexStarGtAxisWithEncoder> {

    /**
     * The logger for any messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(NexStarGtMount.class);

    private final INDISerialPortExtension serialExt;

    public NexStarGtMount(INDISerialPortExtension serialExt) {
        super();
        this.serialExt = serialExt;
        serialExt.setBaudrate(4800);
        serialExt.setDatabits(8);
        serialExt.setStopbits(1);
        serialExt.setParity(0);
        serialExt.getOpenSerialPort();
        scale(MAX_ENCODER_VALUE);
        resetCounters();
    }

    @Override
    protected NexStarGtAxisWithEncoder createHorizontalAxis() {
        return new NexStarGtAxisWithEncoder(this, MOVE_LEFT_AZIMUTH, MOVE_RIGHT_AZIMUTH, STOP_GOTO_AZIMUTH, READ_ENCODER_AZIMUTH);
    }

    @Override
    protected NexStarGtAxisWithEncoder createVerticalAxis() {
        return new NexStarGtAxisWithEncoder(this, MOVE_DOWN_ALTITUDE, MOVE_UP_ALTITUDE, STOP_GOTO_ALTITUDE, READ_ENCODER_ALTITUDE);
    }

    private static final int STOP_GOTO_AZIMUTH = 0x05;

    private static final int STOP_GOTO_ALTITUDE = 0x19;

    private static final int GET_STATUS = 0x0D;

    private static final int RESET_ENCODER_AZIMUTH = 0x03;

    private static final int RESET_ENCODER_ALTITUDE = 0x17;

    private static final int MOVE_DOWN_ALTITUDE = 0x1b;

    private static final int MOVE_LEFT_AZIMUTH = 0x07;

    private static final int READ_ENCODER_ALTITUDE = 0x15;

    private static final int READ_ENCODER_AZIMUTH = 0x01;

    private static final int SCALE_ALTITUDE = 0x20;

    private static final int SCALE_AZIMUTH = 0x0C;

    private static final int MOVE_UP_ALTITUDE = 0x1A;

    private static final int MOVE_RIGHT_AZIMUTH = 0x06;

    // whats' the best scale, experimenting now
    public static final int MAX_ENCODER_VALUE = 0x100000;

    public boolean isBusy() {
        synchronized (this) {
            serialExt.sendByte(GET_STATUS, true);
            return serialExt.readByte() == 0x00;
        }
    }

    protected int read3ByteInt() {
        try {
            int count = 0;
            while (serialExt.getOpenSerialPort().getInputBufferBytesCount() < 3) {
                count++;
                Thread.sleep(50L);
                if (count > 10) {
                    return -1;
                }
            }
        } catch (Exception e) {

        }
        byte[] bytes = serialExt.readByte(3);
        return (bytes[0] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | bytes[2] & 0xFF;
    }

    private void resetCounters() {
        synchronized (this) {
            serialExt.sendByte(RESET_ENCODER_AZIMUTH, true);
            serialExt.sendByte(RESET_ENCODER_ALTITUDE, true);
        }
    }

    private void scale(int maximumValue) {
        byte[] send = new byte[4];
        send[0] = (byte) SCALE_AZIMUTH;
        write3ByteValueInCommandArray(send, maximumValue);
        synchronized (this) {
            serialExt.sendBytes(send, true);
        }
        send[0] = (byte) SCALE_ALTITUDE;
        write3ByteValueInCommandArray(send, maximumValue);
        synchronized (this) {
            serialExt.sendBytes(send, true);
        }
    }

    protected void write3ByteValueInCommandArray(byte[] send, int value) {
        send[3] = (byte) (value & 0xFF);
        send[2] = (byte) (value >> 8 & 0xFF);
        send[1] = (byte) (value >> 16 & 0xFF);
    }

    protected void sendBytes(byte[] bytes) {
        serialExt.sendBytes(bytes, true);
    }

    protected void sendByte(byte aByte) {
        serialExt.sendByte(aByte, true);
    }

    protected void sendByte(int aByte) {
        serialExt.sendByte(aByte, true);
    }

    public void update() {
        horizontalAxis.update();
        verticalAxis.update();
        if (LOG.isDebugEnabled()) {
            LOG.debug("nexstar positions hor:" + horizontalAxis + " ver: " + verticalAxis);
        }
    }
}
