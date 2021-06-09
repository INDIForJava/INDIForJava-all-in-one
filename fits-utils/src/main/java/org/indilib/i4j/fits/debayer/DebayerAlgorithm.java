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
 * Algorithem to debayer a raw sensor image.
 *
 * @author Richard van Nieuwenhoven
 */
interface DebayerAlgorithm {

    /**
     * decode (debayer) the image and return the color image. the colors are
     * represented by doubles so every possible bit per pixel should be possible
     * to debayer without loss of data. The instance is not thread save so do
     * not cache the algorithm.
     *
     * @param imageBayerPattern the bayer pattern of the input image
     * @param image             the input image.
     * @return the image debayered (an image per color)
     */
    RGBImagePixels decode(DebayerPattern imageBayerPattern, ImagePixels image);

    /**
     * @return the name of the algorithm.
     */
    String getName();
}
