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

import org.indilib.i4j.driver.telescope.mount.AxisWithEncoder;

public class NexStarGtAxisWithEncoder extends AxisWithEncoder {

    private static final double CONVERSION_OF_NEXTSTAR_SPEED_TO_SPEED_PER_SEC = 10d;

    private final NexStarGtMount mount;

    private final int moveMinusCommand;

    private final int movePlusCommand;

    private final int stopCommand;

    private final int readEncoderCommand;

    public NexStarGtAxisWithEncoder(NexStarGtMount mount, int moveMinusCommand, int movePlusCommand, int stopCommand, int readEncoderCommand) {
        this.mount = mount;
        this.moveMinusCommand = moveMinusCommand;
        this.movePlusCommand = movePlusCommand;
        this.stopCommand = stopCommand;
        this.readEncoderCommand = readEncoderCommand;
        this.minimum = 0;
        this.maximum = NexStarGtMount.MAX_ENCODER_VALUE;
    }

    @Override
    protected void setSpeedInTicksPerSecond(double speedInTicksPerSecond) {
        super.setSpeedInTicksPerSecond(speedInTicksPerSecond);
        byte[] send = new byte[4];
        int value = (int) (speedInTicksPerSecond * CONVERSION_OF_NEXTSTAR_SPEED_TO_SPEED_PER_SEC);
        if (value < 0) {
            send[0] = (byte) moveMinusCommand;
            value = Math.abs(value);
        } else {
            send[0] = (byte) movePlusCommand;
        }
        mount.write3ByteValueInCommandArray(send, value);
        synchronized (mount) {
            mount.sendBytes(send);
        }
    }

    @Override
    public void stop() {
        synchronized (mount) {
            mount.sendByte(stopCommand);
        }
        super.stop();
    }

    protected void update() {
        synchronized (mount) {
            mount.sendByte(readEncoderCommand);
            setPosition(mount.read3ByteInt());
        }
    }

    /**
     * Calculated this max 12500 ticks per second with 360 degrees = 0xFFFFFF. 1
     * degree = 46603 ticks. result 12500/46603 = 0,268223076 degrees per second
     */
    @Override
    protected double getMaximumSpeed(double distanceInDegrees) {
        return 0.268d;
    }
}
