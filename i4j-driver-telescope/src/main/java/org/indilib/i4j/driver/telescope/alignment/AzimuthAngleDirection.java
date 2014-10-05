package org.indilib.i4j.driver.telescope.alignment;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
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
 * * The direction of measurement of an azimuth angle. The following are the
 * conventions for some coordinate systems. - Right Ascension is measured
 * ANTI_CLOCKWISE from the vernal equinox. - Local Hour Angle is measured
 * CLOCKWISE from the observer's meridian. - Greenwich Hour Angle is measured
 * CLOCKWISE from the Greenwich meridian. - Azimuth (as in Altitude Azimuth
 * coordinate systems ) is often measured CLOCKWISE\n from north. But ESO FITS
 * (Clockwise from South) and SDSS FITS(Anticlockwise from South)\n have
 * different conventions. Horizontal coordinates in libnova are measured
 * clockwise from south. alignment subsystem.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum AzimuthAngleDirection {
    /**
     * Angle is measured clockwise
     */
    CLOCKWISE,
    /**
     * Angle is measured anti clockwise
     */
    ANTI_CLOCKWISE
}
