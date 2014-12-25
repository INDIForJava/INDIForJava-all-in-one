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

import static org.indilib.i4j.fits.debayer.DebayerRowOrder.BGGR;
import static org.indilib.i4j.fits.debayer.DebayerRowOrder.GBGR;
import static org.indilib.i4j.fits.debayer.DebayerRowOrder.GRBG;
import static org.indilib.i4j.fits.debayer.DebayerRowOrder.RGGB;

class ReplicateDebayerAlgorithm implements DebayerAlgorithm {

    @Override
    public String getName() {
        return "Replication";
    }

    public RGBImagePixels decode(DebayerRowOrder row_order, ImagePixels ip) { // Replication
        int width = ip.getWidth();
        int height = ip.getHeight();
        // algorithm
        double one = 0;
        RGBImagePixels rgb = new RGBImagePixels();
        ImagePixels r = new ImagePixels(width, height);
        ImagePixels g = new ImagePixels(width, height);
        ImagePixels b = new ImagePixels(width, height);
        // Short[] pixels = ip.getPixels();

        if (row_order == RGGB || row_order == BGGR) {
            for (int y = 0; y < height; y += 2) {
                for (int x = 0; x < width; x += 2) {
                    one = ip.getPixel(x, y);
                    b.putPixel(x, y, one);
                    b.putPixel(x + 1, y, one);
                    b.putPixel(x, y + 1, one);
                    b.putPixel(x + 1, y + 1, one);
                }
            }

            for (int y = 1; y < height; y += 2) {
                for (int x = 1; x < width; x += 2) {
                    one = ip.getPixel(x, y);
                    r.putPixel(x, y, one);
                    r.putPixel(x + 1, y, one);
                    r.putPixel(x, y + 1, one);
                    r.putPixel(x + 1, y + 1, one);
                }
            }

            for (int y = 0; y < height; y += 2) {
                for (int x = 1; x < width; x += 2) {
                    one = ip.getPixel(x, y);
                    g.putPixel(x, y, one);
                    g.putPixel(x + 1, y, one);
                }
            }

            for (int y = 1; y < height; y += 2) {
                for (int x = 0; x < width; x += 2) {
                    one = ip.getPixel(x, y);
                    g.putPixel(x, y, one);
                    g.putPixel(x + 1, y, one);
                }
            }

            if (row_order == RGGB) {
                rgb.setRed(b);
                rgb.setGreen(g);
                rgb.setBlue(r);
            } else if (row_order == BGGR) {
                rgb.setRed(r);
                rgb.setGreen(g);
                rgb.setBlue(b);
            }
        }

        else if (row_order == GRBG || row_order == GBGR) {
            for (int y = 1; y < height; y += 2) {
                for (int x = 0; x < width; x += 2) {
                    one = ip.getPixel(x, y);
                    b.putPixel(x, y, one);
                    b.putPixel(x + 1, y, one);
                    b.putPixel(x, y + 1, one);
                    b.putPixel(x + 1, y + 1, one);
                }
            }
            for (int y = 0; y < height; y += 2) {
                for (int x = 1; x < width; x += 2) {
                    one = ip.getPixel(x, y);
                    r.putPixel(x, y, one);
                    r.putPixel(x + 1, y, one);
                    r.putPixel(x, y + 1, one);
                    r.putPixel(x + 1, y + 1, one);
                }
            }
            for (int y = 0; y < height; y += 2) {
                for (int x = 0; x < width; x += 2) {
                    one = ip.getPixel(x, y);
                    g.putPixel(x, y, one);
                    g.putPixel(x + 1, y, one);
                }
            }
            for (int y = 1; y < height; y += 2) {
                for (int x = 1; x < width; x += 2) {
                    one = ip.getPixel(x, y);
                    g.putPixel(x, y, one);
                    g.putPixel(x + 1, y, one);
                }
            }
            if (row_order == GRBG) {
                rgb.setRed(b);
                rgb.setGreen(g);
                rgb.setBlue(r);
            } else if (row_order == GBGR) {
                rgb.setRed(r);
                rgb.setGreen(g);
                rgb.setBlue(b);
            }
        }

        return rgb;

    }
}
