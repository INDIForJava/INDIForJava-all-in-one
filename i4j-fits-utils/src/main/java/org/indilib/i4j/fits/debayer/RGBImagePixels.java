package org.indilib.i4j.fits.debayer;

import java.awt.Color;
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

class RGBImagePixels {

    private ImagePixels red;

    public ImagePixels getRed() {
        return red;
    }

    public ImagePixels getBlue() {
        return blue;
    }

    public ImagePixels getGreen() {
        return green;
    }

    public void setRed(ImagePixels red) {
        this.red = red;
    }

    public void setBlue(ImagePixels blue) {
        this.blue = blue;
    }

    public void setGreen(ImagePixels green) {
        this.green = green;
    }

    private ImagePixels blue;

    private ImagePixels green;

    public Object getColors(int bitsPerPixel) {
        double[] redPixel = red.pixel();
        double[] greenPixel = green.pixel();
        double[] bluePixel = blue.pixel();
        if (bitsPerPixel == 8) {
            double max = Math.pow(2, 8);
            byte[] value = new byte[redPixel.length + greenPixel.length + bluePixel.length];
            int pixelIndex = 0;
            for (int index = 0; index < redPixel.length; index++) {
                value[pixelIndex++] = (byte) (redPixel[index] * max);
            }
            for (int index = 0; index < greenPixel.length; index++) {
                value[pixelIndex++] = (byte) (greenPixel[index] * max);
            }
            for (int index = 0; index < bluePixel.length; index++) {
                value[pixelIndex++] = (byte) (bluePixel[index] * max);
            }
            return value;
        } else if (bitsPerPixel == 16) {
            double max = Math.pow(2, 16);
            short[] value = new short[redPixel.length + greenPixel.length + bluePixel.length];
            int pixelIndex = 0;
            for (int index = 0; index < redPixel.length; index++) {
                value[pixelIndex++] = (short) (redPixel[index] * max);
            }
            for (int index = 0; index < greenPixel.length; index++) {
                value[pixelIndex++] = (short) (greenPixel[index] * max);
            }
            for (int index = 0; index < bluePixel.length; index++) {
                value[pixelIndex++] = (short) (bluePixel[index] * max);
            }
            return value;
        } else if (bitsPerPixel == 32) {
            throw new UnsupportedOperationException();
        }
        throw new UnsupportedOperationException();
    }

    public BufferedImage asImage() {
        double[] redPixel = red.pixel();
        double[] greenPixel = green.pixel();
        double[] bluePixel = blue.pixel();
        double max = Math.pow(2, 8);
        int x = 0;
        int y = 0;
        BufferedImage result = new BufferedImage(red.getWidth(), red.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int index = 0; index < redPixel.length; index++) {
            int red = Math.min((int) (redPixel[index] * max), 255);
            int green = Math.min((int) (greenPixel[index] * max), 255);
            int blue = Math.min((int) (bluePixel[index] * max), 255);
            result.setRGB(x, y, new Color(red, green, blue).getRGB());
            x++;
            if (x >= this.red.getWidth()) {
                x = 0;
                y++;
            }
        }
        return result;
    }

}
