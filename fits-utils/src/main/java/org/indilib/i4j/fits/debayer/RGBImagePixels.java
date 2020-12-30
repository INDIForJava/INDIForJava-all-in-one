package org.indilib.i4j.fits.debayer;

import java.awt.*;
import java.awt.image.BufferedImage;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
/**
 * This class wrappes a color image consisting of a read a green and a blue
 * image.
 * 
 * @author Richard van Nieuwenhoven
 */
public class RGBImagePixels {

    /**
     * number of bits per integer.
     */
    private static final int BITS_PER_INTEGER = 32;

    /**
     * number of bits per short.
     */
    private static final int BITS_PER_SHORT = 16;

    /**
     * number of bits per byte.
     */
    private static final int BITS_PER_BYTE = 8;

    /**
     * the red pixels.
     */
    private ImagePixels red;

    /**
     * the blue pixels.
     */
    private ImagePixels blue;

    /**
     * the green pixels.
     */
    private ImagePixels green;

    /**
     * construct an image of the given size.
     * 
     * @param width
     *            the width of the image
     * @param height
     *            the heigth of the image.
     */
    public RGBImagePixels(int width, int height) {
        red = new ImagePixels(width, height);
        green = new ImagePixels(width, height);
        blue = new ImagePixels(width, height);
    }

    /**
     * @return the red pixels
     */
    public ImagePixels getRed() {
        return red;
    }

    /**
     * @return the blue pixels
     */
    public ImagePixels getBlue() {
        return blue;
    }

    /**
     * @return the green pixels
     */
    public ImagePixels getGreen() {
        return green;
    }

    /**
     * set the red pixels.
     * 
     * @param red
     *            the new red pixels
     */
    public void setRed(ImagePixels red) {
        this.red = red;
    }

    /**
     * set the blue pixels.
     * 
     * @param blue
     *            the new blue pixels
     */
    public void setBlue(ImagePixels blue) {
        this.blue = blue;
    }

    /**
     * set the green pixels.
     * 
     * @param green
     *            the new green pixels
     */
    public void setGreen(ImagePixels green) {
        this.green = green;
    }

    /**
     * create an rgb color array in the requested bit per pixel depth. currently
     * only 8, 16 and 32 bit pixel depth supported. Attention the primitives are
     * used unsigned!
     * 
     * @param bitsPerPixel
     *            the pixel depth to use
     * @return the array of color pixels.
     */
    public Object getColors(int bitsPerPixel) {
        double[] redPixel = red.pixel();
        double[] greenPixel = green.pixel();
        double[] bluePixel = blue.pixel();
        if (bitsPerPixel == BITS_PER_BYTE) {
            double max = Math.pow(2, BITS_PER_BYTE) - 1d;
            byte[] value = new byte[redPixel.length + greenPixel.length + bluePixel.length];
            int pixelIndex = 0;
            for (double element : redPixel) {
                value[pixelIndex++] = (byte) scalePixel(element, max);
            }
            for (double element : greenPixel) {
                value[pixelIndex++] = (byte) scalePixel(element, max);
            }
            for (double element : bluePixel) {
                value[pixelIndex++] = (byte) scalePixel(element, max);
            }
            return value;
        } else if (bitsPerPixel == BITS_PER_SHORT) {
            double max = Short.MAX_VALUE;
            double min = Short.MIN_VALUE;
            short[] value = new short[redPixel.length + greenPixel.length + bluePixel.length];
            int pixelIndex = 0;
            for (double element : redPixel) {
                value[pixelIndex++] = (short) scalePixel(element, min, max);
            }
            for (double element : greenPixel) {
                value[pixelIndex++] = (short) scalePixel(element, min, max);
            }
            for (double element : bluePixel) {
                value[pixelIndex++] = (short) scalePixel(element, min, max);
            }
            return value;
        } else if (bitsPerPixel == BITS_PER_INTEGER) {
            double max = Math.pow(2, BITS_PER_INTEGER) - 1d;
            int[] value = new int[redPixel.length + greenPixel.length + bluePixel.length];
            int pixelIndex = 0;
            for (double element : redPixel) {
                value[pixelIndex++] = (int) scalePixel(element, max);
            }
            for (double element : greenPixel) {
                value[pixelIndex++] = (int) scalePixel(element, max);
            }
            for (double element : bluePixel) {
                value[pixelIndex++] = (int) scalePixel(element, max);
            }
            return value;
        }
        throw new UnsupportedOperationException();
    }

    /**
     * scale a 0 to 1 double pixel to the new range, rounding it to a long.
     * 
     * @param pixel
     *            the pixel to scale
     * @param max
     *            the max value.
     * @return the pixel value in the new range.
     */
    private long scalePixel(double pixel, double max) {
        return Math.round(Math.min(pixel * max, max));
    }

    /**
     * scale a 0 to 1 double pixel to the new range, rounding it to a long.
     * 
     * @param pixel
     *            the pixel to scale
     * @param min
     *            the max value.
     * @param max
     *            the max value.
     * @return the pixel value in the new range.
     */
    private long scalePixel(double pixel, double min, double max) {
        return Math.round(Math.min(pixel * (max - min) + min, max));
    }

    /**
     * @return a 8 bit buffered image that represents this image, mainly for
     *         direkt display
     */
    public BufferedImage asImage() {
        double[] redPixel = red.pixel();
        double[] greenPixel = green.pixel();
        double[] bluePixel = blue.pixel();
        double max = Math.pow(2, BITS_PER_BYTE) - 1d;
        int x = 0;
        int y = 0;
        BufferedImage result = new BufferedImage(red.getWidth(), red.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int index = 0; index < redPixel.length; index++) {
            int redPixelValue = (int) scalePixel(redPixel[index], max);
            int greenPixelValue = (int) scalePixel(bluePixel[index], max);
            int bluePixelValue = (int) scalePixel(greenPixel[index], max);
            result.setRGB(x, y, new Color(redPixelValue, greenPixelValue, bluePixelValue).getRGB());
            x++;
            if (x >= red.getWidth()) {
                x = 0;
                y++;
            }
        }
        return result;
    }

}
