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

class SmoothDebayerAlgorithm implements DebayerAlgorithm {

    @Override
    public String getName() {
        return "SmoothDebayerAlgorithm Hue";
    }

    public RGBImagePixels decode(DebayerRowOrder row_order, ImagePixels ip) { // SmoothDebayerAlgorithm
                                                                              // Hue
        int width = ip.getWidth();
        int height = ip.getHeight();
        // algorithm
        double G1 = 0;
        double G2 = 0;
        double G3 = 0;
        double G4 = 0;
        double G5 = 0;
        double G6 = 0;
        double G7 = 0;
        double G8 = 0;
        double G9 = 0;
        double B1 = 0;
        double B2 = 0;
        double B3 = 0;
        double B4 = 0;
        double R1 = 0;
        double R2 = 0;
        double R3 = 0;
        double R4 = 0;
        RGBImagePixels rgb = new RGBImagePixels();
        ImagePixels r = new ImagePixels(width, height);
        ImagePixels g = new ImagePixels(width, height);
        ImagePixels b = new ImagePixels(width, height);
        // Short[] pixels = ip.getPixels();

        if (row_order == RGGB || row_order == BGGR) {
            // Solve for green pixels first
            for (int y = 0; y < height; y += 2) {
                for (int x = 1; x < width; x += 2) {
                    G1 = ip.getPixel(x, y);
                    G2 = ip.getPixel(x + 2, y);
                    G3 = ip.getPixel(x + 1, y + 1);
                    G4 = ip.getPixel(x + 1, y - 1);

                    g.putPixel(x, y, G1);
                    if (y == 0)
                        g.putPixel(x + 1, y, ((G1 + G2 + G3) / 3));
                    else
                        g.putPixel(x + 1, y, ((G1 + G2 + G3 + G4) / 4));
                    if (x == 1)
                        g.putPixel(x - 1, y, ((G1 + G4 + ip.getPixel(x - 1, y + 1)) / 3));
                }
            }

            for (int x = 0; x < width; x += 2) {
                for (int y = 1; y < height; y += 2) {

                    G1 = ip.getPixel(x, y);
                    G2 = ip.getPixel(x + 2, y);
                    G3 = ip.getPixel(x + 1, y + 1);
                    G4 = ip.getPixel(x + 1, y - 1);

                    g.putPixel(x, y, G1);
                    if (x == 0)
                        g.putPixel(x + 1, y, ((G1 + G2 + G3) / 3));
                    else
                        g.putPixel(x + 1, y, ((G1 + G2 + G3 + G4) / 4));
                }
            }

            g.putPixel(0, 0, ((ip.getPixel(0, 1) + ip.getPixel(1, 0)) / 2));

            for (int y = 0; y < height; y += 2) {
                for (int x = 0; x < width; x += 2) {
                    B1 = ip.getPixel(x, y);
                    B2 = ip.getPixel(x + 2, y);
                    B3 = ip.getPixel(x, y + 2);
                    B4 = ip.getPixel(x + 2, y + 2);
                    G1 = g.getPixel(x, y);
                    G2 = g.getPixel(x + 2, y);
                    G3 = g.getPixel(x, y + 2);
                    G4 = g.getPixel(x + 2, y + 2);
                    G5 = g.getPixel(x + 1, y);
                    G6 = g.getPixel(x, y + 1);
                    G9 = g.getPixel(x + 1, y + 1);
                    if (G1 == 0)
                        G1 = 1;
                    if (G2 == 0)
                        G2 = 1;
                    if (G3 == 0)
                        G3 = 1;
                    if (G4 == 0)
                        G4 = 1;

                    b.putPixel(x, y, (B1));
                    b.putPixel(x + 1, y, ((G5 / 2 * ((B1 / G1) + (B2 / G2)))));
                    b.putPixel(x, y + 1, ((G6 / 2 * ((B1 / G1) + (B3 / G3)))));
                    b.putPixel(x + 1, y + 1, ((G9 / 4 * ((B1 / G1) + (B3 / G3) + (B2 / G2) + (B4 / G4)))));

                }
            }

            for (int y = 1; y < height; y += 2) {
                for (int x = 1; x < width; x += 2) {
                    R1 = ip.getPixel(x, y);
                    R2 = ip.getPixel(x + 2, y);
                    R3 = ip.getPixel(x, y + 2);
                    R4 = ip.getPixel(x + 2, y + 2);
                    G1 = g.getPixel(x, y);
                    G2 = g.getPixel(x + 2, y);
                    G3 = g.getPixel(x, y + 2);
                    G4 = g.getPixel(x + 2, y + 2);
                    G5 = g.getPixel(x + 1, y);
                    G6 = g.getPixel(x, y + 1);
                    G9 = g.getPixel(x + 1, y + 1);
                    if (G1 == 0)
                        G1 = 1;
                    if (G2 == 0)
                        G2 = 1;
                    if (G3 == 0)
                        G3 = 1;
                    if (G4 == 0)
                        G4 = 1;

                    r.putPixel(x, y, (R1));
                    r.putPixel(x + 1, y, ((G5 / 2 * ((R1 / G1) + (R2 / G2)))));
                    r.putPixel(x, y + 1, ((G6 / 2 * ((R1 / G1) + (R3 / G3)))));
                    r.putPixel(x + 1, y + 1, ((G9 / 4 * ((R1 / G1) + (R3 / G3) + (R2 / G2) + (R4 / G4)))));
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

            for (int y = 0; y < height; y += 2) {
                for (int x = 0; x < width; x += 2) {
                    G1 = ip.getPixel(x, y);
                    G2 = ip.getPixel(x + 2, y);
                    G3 = ip.getPixel(x + 1, y + 1);
                    G4 = ip.getPixel(x + 1, y - 1);

                    g.putPixel(x, y, G1);
                    if (y == 0)
                        g.putPixel(x + 1, y, ((G1 + G2 + G3) / 3));
                    else
                        g.putPixel(x + 1, y, ((G1 + G2 + G3 + G4) / 4));
                    if (x == 1)
                        g.putPixel(x - 1, y, ((G1 + G4 + ip.getPixel(x - 1, y + 1)) / 3));
                }
            }

            for (int y = 1; y < height; y += 2) {
                for (int x = 1; x < width; x += 2) {
                    G1 = ip.getPixel(x, y);
                    G2 = ip.getPixel(x + 2, y);
                    G3 = ip.getPixel(x + 1, y + 1);
                    G4 = ip.getPixel(x + 1, y - 1);

                    g.putPixel(x, y, G1);
                    if (x == 0)
                        g.putPixel(x + 1, y, ((G1 + G2 + G3) / 3));
                    else
                        g.putPixel(x + 1, y, ((G1 + G2 + G3 + G4) / 4));
                }
            }

            g.putPixel(0, 0, ((ip.getPixel(0, 1) + ip.getPixel(1, 0)) / 2));

            for (int y = 1; y < height; y += 2) {
                for (int x = 0; x < width; x += 2) {
                    B1 = ip.getPixel(x, y);
                    B2 = ip.getPixel(x + 2, y);
                    B3 = ip.getPixel(x, y + 2);
                    B4 = ip.getPixel(x + 2, y + 2);
                    G1 = g.getPixel(x, y);
                    G2 = g.getPixel(x + 2, y);
                    G3 = g.getPixel(x, y + 2);
                    G4 = g.getPixel(x + 2, y + 2);
                    G5 = g.getPixel(x + 1, y);
                    G6 = g.getPixel(x, y + 1);
                    G9 = g.getPixel(x + 1, y + 1);
                    if (G1 == 0)
                        G1 = 1;
                    if (G2 == 0)
                        G2 = 1;
                    if (G3 == 0)
                        G3 = 1;
                    if (G4 == 0)
                        G4 = 1;

                    b.putPixel(x, y, (B1));
                    b.putPixel(x + 1, y, ((G5 / 2 * ((B1 / G1) + (B2 / G2)))));
                    b.putPixel(x, y + 1, ((G6 / 2 * ((B1 / G1) + (B3 / G3)))));
                    b.putPixel(x + 1, y + 1, ((G9 / 4 * ((B1 / G1) + (B3 / G3) + (B2 / G2) + (B4 / G4)))));

                }
            }

            for (int y = 0; y < height; y += 2) {
                for (int x = 1; x < width; x += 2) {
                    R1 = ip.getPixel(x, y);
                    R2 = ip.getPixel(x + 2, y);
                    R3 = ip.getPixel(x, y + 2);
                    R4 = ip.getPixel(x + 2, y + 2);
                    G1 = g.getPixel(x, y);
                    G2 = g.getPixel(x + 2, y);
                    G3 = g.getPixel(x, y + 2);
                    G4 = g.getPixel(x + 2, y + 2);
                    G5 = g.getPixel(x + 1, y);
                    G6 = g.getPixel(x, y + 1);
                    G9 = g.getPixel(x + 1, y + 1);
                    if (G1 == 0)
                        G1 = 1;
                    if (G2 == 0)
                        G2 = 1;
                    if (G3 == 0)
                        G3 = 1;
                    if (G4 == 0)
                        G4 = 1;

                    r.putPixel(x, y, (R1));
                    r.putPixel(x + 1, y, ((G5 / 2 * ((R1 / G1) + (R2 / G2)))));
                    r.putPixel(x, y + 1, ((G6 / 2 * ((R1 / G1) + (R3 / G3)))));
                    r.putPixel(x + 1, y + 1, ((G9 / 4 * ((R1 / G1) + (R3 / G3) + (R2 / G2) + (R4 / G4)))));
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
