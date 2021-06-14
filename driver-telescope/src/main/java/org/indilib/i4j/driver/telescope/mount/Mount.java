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
 * The virtual telescope mount with two encoder axis.
 * 
 * @param <AxisWithEncoderType>
 *            the type for the axis.
 */
public class Mount<AxisWithEncoderType extends AxisWithEncoder> {

    /**
     * the maximum altitude value.
     */
    private static final double MAXIMUM_LEGAL_ALT_VALUE = 190d;

    /**
     * the minimum legal altitude value.
     */
    private static final double MINIMUM_LEGAL_ALT_VALUE = -10d;

    /**
     * The horizontal encoder.
     */
    protected final AxisWithEncoderType horizontalAxis;

    /**
     * The vertical encoder.
     */
    protected final AxisWithEncoderType verticalAxis;

    /**
     * The constructor of the mount.
     */
    public Mount() {
        horizontalAxis = createHorizontalAxis();
        verticalAxis = createVerticalAxis();
        limitVerticalAxis();
    }

    /**
     * limit the vertical axis, to not to far below the horizon.
     */
    public void limitVerticalAxis() {
        verticalAxis.setValidRange(MINIMUM_LEGAL_ALT_VALUE, MAXIMUM_LEGAL_ALT_VALUE);
    }

    /**
     * free the vertical axis, attention this may block your scope.
     */
    public void freeVerticalAxis() {
        verticalAxis.setValidRange(Double.NaN, Double.NaN);
    }

    /**
     * point the telescope to the specified coordinates and try to reach the
     * position in the alloted time. Attention the mount will continue to go in
     * that direction even if the coordinates are reached, so update the
     * direction again before the alloted time is over.
     * 
     * @param horizontalDegrees
     *            the horizontal axis coordinates.
     * @param verticalDegrees
     *            the vertical axis coordinates.
     * @param secondsInFuture
     *            the alloted time to reach the coordinates.
     */
    public void gotoWithSpeed(double horizontalDegrees, double verticalDegrees, double secondsInFuture) {
        horizontalAxis.gotoWithSpeed(horizontalDegrees, secondsInFuture);
        verticalAxis.gotoWithSpeed(verticalDegrees, secondsInFuture);
    }

    /**
     * Stop any motion as soon as possible.
     */
    public void stop() {
        horizontalAxis.stop();
        verticalAxis.stop();
    }

    /**
     * @return an encoder to use for the horizontal axis.
     */
    @SuppressWarnings("unchecked")
    protected AxisWithEncoderType createHorizontalAxis() {
        return (AxisWithEncoderType) new AxisWithEncoder();
    }

    /**
     * @return an encoder to use for the vertical axis.
     */
    @SuppressWarnings("unchecked")
    protected AxisWithEncoderType createVerticalAxis() {
        return (AxisWithEncoderType) new AxisWithEncoder();
    }

    /**
     * @return the position of the horizontal axis in degrees relative to the
     *         zero position. (value range 0 to 360)
     */
    public double getHorizontalPosition() {
        return horizontalAxis.getCurrentPosition();
    }

    /**
     * @return the position of the vertical axis in degrees relative to the zero
     *         position. (value range 0 to 360)
     */
    public double getVerticalPosition() {
        return verticalAxis.getCurrentPosition();
    }

}
