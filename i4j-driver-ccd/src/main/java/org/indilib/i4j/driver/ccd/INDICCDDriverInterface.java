package org.indilib.i4j.driver.ccd;

import nom.tam.fits.BasicHDU;

/*
 * #%L
 * INDI for Java Abstract CCD Driver
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

/**
 * The driver specific functions are encapsulated in this interface. Normally
 * the driver implements this interface but when a ccd-driver has multiple chips
 * it must have a way to handle the functionality on a per chip basis.
 */
public interface INDICCDDriverInterface {

    /**
     * Abort the current exposure.
     * 
     * @return true is successful
     */
    boolean abortExposure();

    /**
     * start an exposure with the specified duration.
     * 
     * @param duration
     *            the duration in seconds.
     * @return true is successful
     */
    boolean startExposure(double duration);

    /**
     * The ccd binning was updated. perform the necessary operations.
     * 
     * @param binX
     *            the binning in x axis
     * @param binY
     *            the binning in y axis
     * @return true is successful
     */
    boolean updateCCDBin(int binX, int binY);

    /**
     * the subframe size was updated. perform the necessary operations.
     * 
     * @param x
     *            the start x
     * @param y
     *            the start y
     * @param width
     *            the width
     * @param height
     *            the height
     * @return true is successful
     */
    boolean updateCCDFrame(int x, int y, int width, int height);

    /**
     * the frame type was updated. perform the necessary operations.
     * 
     * @param frameType
     *            the new frametype
     * @return true is successful
     */
    boolean updateCCDFrameType(CcdFrame frameType);

    /**
     * add any additinal fits header information to the fits image.
     * 
     * @param fitsHeader
     *            the header to write the attributes.
     */
    void addFITSKeywords(BasicHDU fitsHeader);

}
