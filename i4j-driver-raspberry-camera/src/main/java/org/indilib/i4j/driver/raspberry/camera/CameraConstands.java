package org.indilib.i4j.driver.raspberry.camera;

/*
 * #%L
 * INDI for Java Driver for the Raspberry pi camera
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

public class CameraConstands {

    public static final int RAWBLOCKSIZE = 6404097;

    public static final int HEADERSIZE = 32769;

    /**
     * number of bytes per row of pixels, including 24 'other' bytes at end
     */
    public static final int ROWSIZE = 3264;

    /**
     * number of horizontal pixels on OV5647
     */
    public static final int HPIXELS = 2592;

    public static final int IMAGE_X_SIZE = CameraConstands.HPIXELS / 2;

    /**
     * number of vertical pixels on OV5647 sensor
     */
    public static final int VPIXELS = 1944;

    public static final int IMAGE_Y_SIZE = CameraConstands.VPIXELS / 2;

}
