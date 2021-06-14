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
 * the Replicate debayer algorithm.
 * http://www.umanitoba.ca/faculties/science/astronomy/jwest/plugins.html
 * 
 * @author Richard van Nieuwenhoven
 */
class ReplicateDebayerAlgorithm extends DebayerAlgorithmImpl {

    /**
     * Algorithm parameter.
     */
    private double one = 0;

    @Override
    protected void decodeGreenMiddle(ImagePixels r, ImagePixels g, ImagePixels b) {
        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                b.setPixel(x, y, one);
                b.setPixel(x + 1, y, one);
                b.setPixel(x, y + 1, one);
                b.setPixel(x + 1, y + 1, one);
            }
        }
        for (int y = 1; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                r.setPixel(x, y, one);
                r.setPixel(x + 1, y, one);
                r.setPixel(x, y + 1, one);
                r.setPixel(x + 1, y + 1, one);
            }
        }
        for (int y = 0; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                g.setPixel(x, y, one);
                g.setPixel(x + 1, y, one);
            }
        }
        for (int y = 1; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                g.setPixel(x, y, one);
                g.setPixel(x + 1, y, one);
            }
        }
    }

    @Override
    protected void decodeGreenOutside(ImagePixels r, ImagePixels g, ImagePixels b) {
        for (int y = 1; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                b.setPixel(x, y, one);
                b.setPixel(x + 1, y, one);
                b.setPixel(x, y + 1, one);
                b.setPixel(x + 1, y + 1, one);
            }
        }
        for (int y = 0; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                r.setPixel(x, y, one);
                r.setPixel(x + 1, y, one);
                r.setPixel(x, y + 1, one);
                r.setPixel(x + 1, y + 1, one);
            }
        }
        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                g.setPixel(x, y, one);
                g.setPixel(x + 1, y, one);
            }
        }
        for (int y = 1; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                one = inputImage.getPixel(x, y);
                g.setPixel(x, y, one);
                g.setPixel(x + 1, y, one);
            }
        }
    }

    @Override
    public String getName() {
        return "Replication";
    }
}
