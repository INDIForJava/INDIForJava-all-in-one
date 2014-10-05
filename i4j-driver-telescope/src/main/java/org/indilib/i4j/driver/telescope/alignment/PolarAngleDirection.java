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
 * * The direction of measurement of a polar angle. The following are
 * conventions for some coordinate systems - Declination is measured
 * FROM_AZIMUTHAL_PLANE. - Altitude is measured FROM_AZIMUTHAL_PLANE. - Altitude
 * in libnova horizontal coordinates is measured FROM_AZIMUTHAL_PLANE.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum PolarAngleDirection {
    /**
     * Angle is measured down from the polar axis
     */
    FROM_POLAR_AXIS,
    /**
     * Angle is measured upwards from the azimuthal plane
     */
    FROM_AZIMUTHAL_PLANE
}
