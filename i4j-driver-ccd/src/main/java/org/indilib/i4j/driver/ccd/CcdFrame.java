package org.indilib.i4j.driver.ccd;

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
 * The frame type of the ccd image.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum CcdFrame {
    /**
     * This is a normal ccd image.
     */
    LIGHT_FRAME("Light"),
    /**
     * This is a bias frame with is an image obtained from an opto-electronic
     * image sensor, with no actual exposure time. The image so obtained only
     * contains noise due to the electronics that elaborate the sensor data, and
     * not noise from charge accumulation (e.g. from dark current) within the
     * sensor itself.
     */
    BIAS_FRAME("Bias"),
    /**
     * This is a dark frame. A dark frame is an image captured with the sensor
     * in the dark, essentially just an image of noise in an image sensor. A
     * dark frame, or an average of several dark frames, can then be subtracted
     * from subsequent images to correct for fixed-pattern noise such as that
     * caused by dark current. Dark-frame subtraction has been done for some
     * time in scientific imaging; many newer consumer digital cameras offer it
     * as an option, or may do it automatically for exposures beyond a certain
     * time.
     */
    DARK_FRAME("Dark"),
    /**
     * This is a flat field frame. In order for an astrophotographer to capture
     * a light frame, he or she must place a light source over the imaging
     * instrument's objective lens such that the light source emanates evenly
     * through the users optics. The photographer must then adjust the exposure
     * of their imaging device (CCD or DSLR camera) so that when the histogram
     * of the image is viewed, a peak reaching about 40–70% of the dynamic range
     * (Maximum range of values for a changeable quantity. In this case its
     * pixel value) of the imaging device is seen.
     */
    FLAT_FRAME("Flat Field");

    /**
     * The fits attribute name for this frame type.
     */
    private final String fitsName;

    /**
     * internal constructor.
     * 
     * @param fitsName
     *            the fits attribute name for this frame type.
     */
    private CcdFrame(String fitsName) {
        this.fitsName = fitsName;
    }

    /**
     * @return The fits attribute name for this frame type.
     */
    public String fitsValue() {
        return fitsName;
    }

}
