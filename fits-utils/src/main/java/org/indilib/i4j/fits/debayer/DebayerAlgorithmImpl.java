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

import static org.indilib.i4j.fits.debayer.DebayerPattern.BGGR;
import static org.indilib.i4j.fits.debayer.DebayerPattern.GBRG;
import static org.indilib.i4j.fits.debayer.DebayerPattern.GRBG;
import static org.indilib.i4j.fits.debayer.DebayerPattern.RGGB;

/**
 * abstract class with the common stuff of the different debayering algorithems.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class DebayerAlgorithmImpl implements DebayerAlgorithm {

    /**
     * the bayer pattern of the input image.
     */
    protected DebayerPattern bayerPattern;

    /**
     * the input image to debayer.
     */
    protected ImagePixels inputImage;

    /**
     * the image width.
     */
    protected int width;

    /**
     * the image height.
     */
    protected int height;

    /**
     * the resuling color image.
     */
    protected RGBImagePixels rgb;

    @Override
    public final RGBImagePixels decode(DebayerPattern imageBayerPattern, ImagePixels image) {
        bayerPattern = imageBayerPattern;
        width = image.getWidth();
        height = image.getHeight();
        inputImage = image;
        rgb = new RGBImagePixels(width, height);
        // Hue
        // algorithm
        // (Edge detecting)
        if (bayerPattern == RGGB || bayerPattern == BGGR) {
            decodeGreenMiddle();
        } else if (bayerPattern == GRBG || bayerPattern == GBRG) {
            decodeGreenOutside();
        }
        return rgb;
    }

    /**
     * decode with the green pixels on the outside of the pattern.
     */
    private void decodeGreenOutside() {
        ImagePixels r;
        ImagePixels g;
        ImagePixels b;
        if (bayerPattern == GRBG) {
            b = rgb.getRed();
            g = rgb.getGreen();
            r = rgb.getBlue();
        } else if (bayerPattern == GBRG) {
            r = rgb.getRed();
            g = rgb.getGreen();
            b = rgb.getBlue();
        } else {
            throw new IllegalArgumentException();
        }
        decodeGreenOutside(r, g, b);
    }

    /**
     * decode with the green pixels on the outside of the pattern. Attention the
     * interpretation of red and blue pixels will depend on the bayernpattern.
     * 
     * @param r
     *            red result pixels
     * @param g
     *            green result pixels
     * @param b
     *            blue result pixels
     */
    protected abstract void decodeGreenOutside(ImagePixels r, ImagePixels g, ImagePixels b);

    /**
     * decode with the green pixels in the middle of the pattern. Attention the
     * interpretation of red and blue pixels will depend on the bayernpattern.
     */
    private void decodeGreenMiddle() {
        ImagePixels r;
        ImagePixels g;
        ImagePixels b;
        if (bayerPattern == RGGB) {
            b = rgb.getRed();
            g = rgb.getGreen();
            r = rgb.getBlue();
        } else if (bayerPattern == BGGR) {
            r = rgb.getRed();
            g = rgb.getGreen();
            b = rgb.getBlue();
        } else {
            throw new IllegalArgumentException();
        }
        decodeGreenMiddle(r, g, b);
    }

    /**
     * decode with the green pixels in the middle of the pattern. *
     * 
     * @param r
     *            red result pixels
     * @param g
     *            green result pixels
     * @param b
     *            blue result pixels
     */
    protected abstract void decodeGreenMiddle(ImagePixels r, ImagePixels g, ImagePixels b);
}
