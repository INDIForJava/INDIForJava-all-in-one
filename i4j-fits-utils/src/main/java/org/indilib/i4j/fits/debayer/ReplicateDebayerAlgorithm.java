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
        for (int y = 0; y < this.height; y += 2) {
            for (int x = 0; x < this.width; x += 2) {
                this.one = this.inputImage.getPixel(x, y);
                b.setPixel(x, y, this.one);
                b.setPixel(x + 1, y, this.one);
                b.setPixel(x, y + 1, this.one);
                b.setPixel(x + 1, y + 1, this.one);
            }
        }
        for (int y = 1; y < this.height; y += 2) {
            for (int x = 1; x < this.width; x += 2) {
                this.one = this.inputImage.getPixel(x, y);
                r.setPixel(x, y, this.one);
                r.setPixel(x + 1, y, this.one);
                r.setPixel(x, y + 1, this.one);
                r.setPixel(x + 1, y + 1, this.one);
            }
        }
        for (int y = 0; y < this.height; y += 2) {
            for (int x = 1; x < this.width; x += 2) {
                this.one = this.inputImage.getPixel(x, y);
                g.setPixel(x, y, this.one);
                g.setPixel(x + 1, y, this.one);
            }
        }
        for (int y = 1; y < this.height; y += 2) {
            for (int x = 0; x < this.width; x += 2) {
                this.one = this.inputImage.getPixel(x, y);
                g.setPixel(x, y, this.one);
                g.setPixel(x + 1, y, this.one);
            }
        }
    }

    @Override
    protected void decodeGreenOutside(ImagePixels r, ImagePixels g, ImagePixels b) {
        for (int y = 1; y < this.height; y += 2) {
            for (int x = 0; x < this.width; x += 2) {
                this.one = this.inputImage.getPixel(x, y);
                b.setPixel(x, y, this.one);
                b.setPixel(x + 1, y, this.one);
                b.setPixel(x, y + 1, this.one);
                b.setPixel(x + 1, y + 1, this.one);
            }
        }
        for (int y = 0; y < this.height; y += 2) {
            for (int x = 1; x < this.width; x += 2) {
                this.one = this.inputImage.getPixel(x, y);
                r.setPixel(x, y, this.one);
                r.setPixel(x + 1, y, this.one);
                r.setPixel(x, y + 1, this.one);
                r.setPixel(x + 1, y + 1, this.one);
            }
        }
        for (int y = 0; y < this.height; y += 2) {
            for (int x = 0; x < this.width; x += 2) {
                this.one = this.inputImage.getPixel(x, y);
                g.setPixel(x, y, this.one);
                g.setPixel(x + 1, y, this.one);
            }
        }
        for (int y = 1; y < this.height; y += 2) {
            for (int x = 1; x < this.width; x += 2) {
                this.one = this.inputImage.getPixel(x, y);
                g.setPixel(x, y, this.one);
                g.setPixel(x + 1, y, this.one);
            }
        }
    }

    @Override
    public String getName() {
        return "Replication";
    }
}
