package org.indilib.i4j.fits.debayer;

/*
 * #%L
 * INDI for Java Utilities for the fits image format
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
 * This enum represents the different kind of supported bayering patterns.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum DebayerPattern {
    /**
     * This pattern represents.
     * 
     * <pre>
     * R-G-R-G
     * G-B-G-B
     * R-G-R-G
     * G-B-G-B
     * </pre>
     */
    RGGB,
    /**
     * This pattern represents.
     * 
     * <pre>
     * B-G-B-G
     * G-R-G-R
     * B-G-B-G
     * G-R-G-R
     * </pre>
     */
    BGGR,
    /**
     * This pattern represents.
     * 
     * <pre>
     * G-R-G-R
     * B-G-B-G
     * G-R-G-R
     * B-G-B-G
     * </pre>
     */
    GRBG,
    /**
     * This pattern represents.
     * 
     * <pre>
     * G-B-G-B
     * R-G-R-G
     * G-B-G-B
     * R-G-R-G
     * </pre>
     */
    GBRG;

}
