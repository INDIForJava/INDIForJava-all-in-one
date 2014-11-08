package org.indilib.i4j.driver.telescope.mount;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
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

/**
 * AxisWithEncoder class for one axis.
 */
public class AxisWithEncoder {

    /**
     * number of milliseconds per second.
     */
    private static final double MILLISECONDS_PER_SECOND = 1000d;

    /**
     * how many seconds in a minute.
     */
    private static final double SECONDS_PER_MINUTE = 60d;

    /**
     * the default minimum encoder value.
     */
    private static final long DEFAULT_MIN_ENCODER = 0L;

    /**
     * the default maximum encoder value.
     */
    private static final long DEFAULT_MAX_ENCODER = 100000L;

    /**
     * how many degrees make a full circle.
     */
    private static final double FULL_CIRCLE_IN_DEGREES = 360d;

    /**
     * minimum position of the encoder.
     */
    protected long minimum = DEFAULT_MIN_ENCODER;

    /**
     * maximum position of the encoder. defaults to 10000.
     */
    protected long maximum = DEFAULT_MAX_ENCODER;

    /**
     * the current position.
     */
    private long position;

    /**
     * @return the current position.
     */
    protected long getPosition() {
        return position;
    }

    /**
     * set the current position.
     * 
     * @param position
     *            the new position value.
     */
    protected void setPosition(long position) {
        this.position = position;
        this.positionTimeMs = System.currentTimeMillis();
    }

    /**
     * the time in milliseconds when the last position was requested from the
     * real encoder.
     */
    private long positionTimeMs;

    /**
     * the zero degrees position.
     */
    protected long zeroPosition;

    /**
     * the current speed.
     */
    protected double speedInTicksPerSecond;

    /**
     * delta in degrees, for manual differences.
     */
    protected double delta;

    /**
     * goto the position with the specified degrees, and try to get there in the
     * alloted time frame. Attention the axis will continue to go in that
     * direction even if the coordinates are reached, so update the direction
     * again before the alloted time is over.
     * 
     * @param degrees
     *            the position relative to the zero position.(value range 0 to
     *            360)
     * @param secondsInFuture
     *            the alloted time to get there.
     */
    public void gotoWithSpeed(double degrees, double secondsInFuture) {
        double degreesWithDelta = degrees + delta;
        double range = maximum - minimum;
        double posZeroOne = degreeRange(degreesWithDelta) / FULL_CIRCLE_IN_DEGREES;
        double futurePos = zeroPosition + range * posZeroOne;

        while (futurePos > maximum) {
            futurePos = futurePos - range;
        }
        while (futurePos < minimum) {
            futurePos = futurePos + range;
        }
        double difference = futurePos - position;
        // check if the other direction would be faster.
        if (Math.abs(difference) > (range / 2d)) {
            // ok, the other way around is faster.
            if (difference > 0) {
                difference = -(range - difference);
            } else {
                difference = range + difference;
            }
        }
        double speed = difference / secondsInFuture;

        // regulate the speed depending on the distance.
        double maximumSpeed = getMaximumSpeed(Math.abs(degreeRange(degreesWithDelta) - getCurrentPosition()));

        if (speed > 0) {
            speed = Math.min(speed, maximumSpeed);
        } else {
            speed = Math.max(speed, -maximumSpeed);
        }

        setSpeedInTicksPerSecond(speed);
    }

    /**
     * Maximum speed in ticks per second when the distance to the point is as
     * specified. this should be overloaded to get a more smooth aproch.
     * Defaults to 1 minute to make a full circle.
     * 
     * @param distanceInDegrees
     *            distance in degrees
     * @return the maximum speed
     */
    protected double getMaximumSpeed(double distanceInDegrees) {
        double range = maximum - minimum;
        return range / SECONDS_PER_MINUTE;
    }

    /**
     * limit the degrees to a value range between 0 and 360.
     * 
     * @param degrees
     *            the value to limit
     * @return the value in the range.
     */
    private double degreeRange(double degrees) {
        double result = degrees;
        while (result > FULL_CIRCLE_IN_DEGREES) {
            result = result - FULL_CIRCLE_IN_DEGREES;
        }
        while (result < 0) {
            result = result + FULL_CIRCLE_IN_DEGREES;
        }
        return result;
    }

    /**
     * set the speed of the axis in ticks per second.
     * 
     * @param speedInTicksPerSecond
     *            the new speed
     */
    protected void setSpeedInTicksPerSecond(double speedInTicksPerSecond) {
        this.speedInTicksPerSecond = speedInTicksPerSecond;
    }

    /**
     * Stop the axis as soon as possible.
     */
    public void stop() {
        setSpeedInTicksPerSecond(0);
    }

    /**
     * @return get the current speed.
     */
    protected double getSpeedInTicksPerSecond() {
        return speedInTicksPerSecond;
    }

    /**
     * @return the current position of the Axis in degrees relative to the zero
     *         position. (value range 0 to 360)
     */
    protected double getCurrentPosition() {
        double range = maximum - minimum;
        double secondsSinsLastPositionUpdate = (System.currentTimeMillis() - positionTimeMs) / MILLISECONDS_PER_SECOND;
        double currentPosition = position + secondsSinsLastPositionUpdate * speedInTicksPerSecond;
        double positionRelativeToZero = currentPosition - zeroPosition;
        return degreeRange(((positionRelativeToZero / range) * FULL_CIRCLE_IN_DEGREES) - delta);
    }

}
