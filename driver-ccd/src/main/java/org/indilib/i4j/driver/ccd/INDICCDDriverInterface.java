package org.indilib.i4j.driver.ccd;

import java.util.Map;

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
     * Start exposing primary CCD chip. This function must be implemented in the
     * child class
     * 
     * @param duration
     *            Duration in seconds
     * @return true if OK and exposure will take some time to complete, false on
     *         error.
     */
    boolean startExposure(double duration);

    /**
     * INDICCD calls this function when CCD Binning needs to be updated in the
     * hardware. Derived classes should implement this function
     * 
     * @param binX
     *            Horizontal binning.
     * @param binY
     *            Vertical binning.
     * @return true is CCD chip update is successful, false otherwise.
     */
    boolean updateCCDBin(int binX, int binY);

    /**
     * INDICCD calls this function when CCD Frame dimension needs to be updated
     * in the hardware. Derived classes should implement this function
     * 
     * @param x
     *            Subframe X coordinate in pixels.
     * @param y
     *            Subframe Y coordinate in pixels.
     * @param width
     *            Subframe width in pixels.
     * @param height
     *            Subframe height in pixels. \note (0,0) is defined as most
     *            left, top pixel in the subframe.
     * @return true is CCD chip update is successful, false otherwise.
     */
    boolean updateCCDFrame(int x, int y, int width, int height);

    /**
     * INDICCD calls this function when CCD frame type needs to be updated in
     * the hardware.The CCD hardware layer may either set the frame type when
     * this function is called, or (optionally) before an exposure is started.
     * 
     * @param frameType
     *            Frame type
     * @return true is CCD chip update is successful, false otherwise.
     */
    boolean updateCCDFrameType(CcdFrame frameType);

    /**
     * get a map of any additinal fits header information to the fits image. if
     * no extra atts needed keep it null.
     * 
     * @param fitsHeader
     *            the orignal header with the existing attributes.
     * @return null or a map with the new header attributes.
     */
    Map<String, Object> getExtraFITSKeywords(BasicHDU fitsHeader);

}
