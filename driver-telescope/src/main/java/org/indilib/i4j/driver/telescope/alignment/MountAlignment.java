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
 * Describe the alignment of a telescope axis. This is normally used to
 * differentiate between equatorial mounts in differnet hemispheres and altaz or
 * dobsonian mounts.
 */
public enum MountAlignment {
    /**
     * the zenith as base alignment.
     */
    ZENITH,
    /**
     * the north pole as base alignment.
     */
    NORTH_CELESTIAL_POLE,
    /**
     * the south pole as base alignment.
     */
    SOUTH_CELESTIAL_POLE;
}
