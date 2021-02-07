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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

/**
 * Image pixel representation, stored as an single array of doubles.
 *
 * @author Richard van Nieuwenhoven
 */
public class ImagePixels {

    /**
     * mask to create an integer from a unsigned byte.
     */
    private static final int UNSIGNED_BYTE_MASK = 0xFF;

    /**
     * number of bits in a byte.
     */
    private static final int BYTE_BITS = 8;

    /**
     * number of bits in a short.
     */
    private static final int SHORT_BITS = 16;

    /**
     * number of bits in an integer.
     */
    private static final int INTEGER_BITS = 32;

    /**
     * the pixel values of the image.
     */
    private final double[] pixel;

    /**
     * the width of the image.
     */
    private final int width;

    /**
     * the height of the image.
     */
    private final int height;

    /**
     * constructor for the image.
     *
     * @param width  width of the image.
     * @param height height of the image.
     */
    public ImagePixels(int width, int height) {
        this.width = width;
        this.height = height;
        pixel = new double[width * height];
    }

    /**
     * @return the image width;
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the image height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * set the pixel to the specified value.
     *
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @param value the pixel value.
     */
    public void setPixel(int x, int y, double value) {
        if (value < 0) {
            value = 0;
        }
        if (x < 0 || x >= width) {
            return;
        }
        if (y < 0 || y >= height) {
            return;
        }
        pixel[y * width + x] = value;
    }

    /**
     * get the pixel value of the coordinates.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @return the pixel value.
     */
    public double getPixel(int x, int y) {
        if (x < 0 || x >= width) {
            return 0;
        }
        if (y < 0 || y >= height) {
            return 0;
        }
        return pixel[y * width + x];
    }

    /**
     * set all pixels to the image from a multy dimensional array. (the maximum
     * value is used to scale the pixel values).
     *
     * @param kernel the multi-dimensional array.
     */
    public void setPixel(Object kernel) {
        if (kernel instanceof byte[][]) {
            double range = Math.pow(2, BYTE_BITS);
            int pixelIndex = 0;
            byte[][] other = (byte[][]) kernel;
            for (byte[] element : other) {
                for (byte b : element) {
                    pixel[pixelIndex++] = (b & UNSIGNED_BYTE_MASK) / range;
                }
            }
        } else if (kernel instanceof int[][]) {
            double range = Math.pow(2, INTEGER_BITS);
            double offset = -((double) Integer.MIN_VALUE);
            int pixelIndex = 0;
            int[][] other = (int[][]) kernel;
            for (int[] element : other) {
                for (int i : element) {
                    pixel[pixelIndex++] = (offset + i) / range;
                }
            }
        } else if (kernel instanceof short[][]) {
            double range = Math.pow(2, SHORT_BITS);
            double offset = -((double) Short.MIN_VALUE);
            int pixelIndex = 0;
            short[][] other = (short[][]) kernel;
            for (short[] element : other) {
                for (short i : element) {
                    pixel[pixelIndex++] = (offset + i) / range;
                }
            }
        } else {
            throw new UnsupportedOperationException("parameter not yet supported");
        }
    }

    /**
     * @return the internal pixel array.
     */
    public double[] pixel() {
        return pixel;
    }
}
