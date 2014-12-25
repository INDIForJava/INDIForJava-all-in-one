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

public class ImagePixels {

    private final double[] pixel;

    private final int width;

    private final int height;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ImagePixels(int width, int height) {
        this.width = width;
        this.height = height;
        pixel = new double[width * height];
    }

    public void putPixel(int x, int y, double value) {
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

    public double getPixel(int x, int y) {
        if (x < 0 || x >= width) {
            return 0;
        }
        if (y < 0 || y >= height) {
            return 0;
        }
        return pixel[y * width + x];
    }

    public void setPixel(Object kernel, double datamax) {
        if (kernel instanceof int[][]) {
            double max = Math.min(Math.pow(2, 32), datamax > 1d ? datamax : Double.MAX_VALUE);
            int pixelIndex = 0;
            int[][] other = (int[][]) kernel;
            for (int index1 = 0; index1 < other.length; index1++) {
                for (int index2 = 0; index2 < other[index1].length; index2++) {
                    pixel[pixelIndex++] = ((double) other[index1][index2]) / max;
                }
            }
        } else if (kernel instanceof short[][]) {
            double max = Math.min(Math.pow(2, 16), datamax > 1d ? datamax : Double.MAX_VALUE);
            int pixelIndex = 0;
            short[][] other = (short[][]) kernel;
            for (int index1 = 0; index1 < other.length; index1++) {
                for (int index2 = 0; index2 < other[index1].length; index2++) {
                    pixel[pixelIndex++] = ((double) (other[index1][index2] & 0xFFFF)) / max;
                }
            }
        }
    }

    public double[] pixel() {
        return pixel;
    }
}
