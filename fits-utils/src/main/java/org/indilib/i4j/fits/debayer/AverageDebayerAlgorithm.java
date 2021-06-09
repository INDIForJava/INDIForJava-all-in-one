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
 * the average debayer algorithm.
 * http://www.umanitoba.ca/faculties/science/astronomy/jwest/plugins.html
 *
 * @author Richard van Nieuwenhoven
 */
class AverageDebayerAlgorithm extends DebayerAlgorithmImpl {

    /**
     * number 4 (just because of checkstyle.
     */
    private static final int N_4 = 4;
    /**
     * Algorithm parameter.
     */
    private double one = 0;
    /**
     * Algorithm parameter.
     */
    private double two = 0;
    /**
     * Algorithm parameter.
     */
    private double three = 0;
    /**
     * Algorithm parameter.
     */
    private double four = 0;

    @Override
    public String getName() {
        return "Bilinear";
    }

    @Override
    protected void decodeGreenMiddle(ImagePixels r, ImagePixels g, ImagePixels b) {
        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                two = inputImage.getPixel(x + 2, y);
                three = inputImage.getPixel(x, y + 2);
                four = inputImage.getPixel(x + 2, y + 2);
                b.setPixel(x, y, one);
                b.setPixel(x + 1, y, (one + two) / 2);
                b.setPixel(x, y + 1, (one + three) / 2);
                b.setPixel(x + 1, y + 1, (one + two + three + four) / N_4);
            }
        }
        for (int y = 1; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                two = inputImage.getPixel(x + 2, y);
                three = inputImage.getPixel(x, y + 2);
                four = inputImage.getPixel(x + 2, y + 2);
                r.setPixel(x, y, one);
                r.setPixel(x + 1, y, (one + two) / 2);
                r.setPixel(x, y + 1, (one + three) / 2);
                r.setPixel(x + 1, y + 1, (one + two + three + four) / N_4);
            }
        }
        for (int y = 0; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                two = inputImage.getPixel(x + 2, y);
                three = inputImage.getPixel(x + 1, y + 1);
                four = inputImage.getPixel(x + 1, y - 1);
                g.setPixel(x, y, one);
                g.setPixel(x + 1, y, (one + two + three + four) / N_4);
            }
        }
        for (int y = 1; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                two = inputImage.getPixel(x + 2, y);
                three = inputImage.getPixel(x + 1, y + 1);
                four = inputImage.getPixel(x + 1, y - 1);

                g.setPixel(x, y, one);
                g.setPixel(x + 1, y, (one + two + three + four) / N_4);
            }
        }
    }

    @Override
    protected void decodeGreenOutside(ImagePixels r, ImagePixels g, ImagePixels b) {
        for (int y = 1; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                two = inputImage.getPixel(x + 2, y);
                three = inputImage.getPixel(x, y + 2);
                four = inputImage.getPixel(x + 2, y + 2);
                b.setPixel(x, y, one);
                b.setPixel(x + 1, y, (one + two) / 2);
                b.setPixel(x, y + 1, (one + three) / 2);
                b.setPixel(x + 1, y + 1, (one + two + three + four) / N_4);
            }
        }
        for (int y = 0; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                two = inputImage.getPixel(x + 2, y);
                three = inputImage.getPixel(x, y + 2);
                four = inputImage.getPixel(x + 2, y + 2);
                r.setPixel(x, y, one);
                r.setPixel(x + 1, y, (one + two) / 2);
                r.setPixel(x, y + 1, (one + three) / 2);
                r.setPixel(x + 1, y + 1, (one + two + three + four) / N_4);
            }
        }

        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                two = inputImage.getPixel(x + 2, y);
                three = inputImage.getPixel(x + 1, y + 1);
                four = inputImage.getPixel(x + 1, y - 1);
                g.setPixel(x, y, one);
                g.setPixel(x + 1, y, (one + two + three + four) / N_4);
            }
        }
        for (int y = 1; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                two = inputImage.getPixel(x + 2, y);
                three = inputImage.getPixel(x + 1, y + 1);
                four = inputImage.getPixel(x + 1, y - 1);
                g.setPixel(x, y, one);
                g.setPixel(x + 1, y, (one + two + three + four) / N_4);
            }
        }
    }
}
