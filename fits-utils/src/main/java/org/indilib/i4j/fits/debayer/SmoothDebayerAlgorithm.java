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
 * the Smooth debayer algorithm.
 * http://www.umanitoba.ca/faculties/science/astronomy/jwest/plugins.html
 * 
 * @author Richard van Nieuwenhoven
 */
class SmoothDebayerAlgorithm extends DebayerAlgorithmImpl {

    /**
     * number 3 (just because of checkstyle.
     */
    private static final double N_3 = 3d;

    /**
     * number 4 (just because of checkstyle.
     */
    private static final double N_4 = 4d;

    /**
     * Algorithm parameter.
     */
    private double pG1 = 0;

    /**
     * Algorithm parameter.
     */
    private double pG2 = 0;

    /**
     * Algorithm parameter.
     */
    private double pG3 = 0;

    /**
     * Algorithm parameter.
     */
    private double pG4 = 0;

    /**
     * Algorithm parameter.
     */
    private double pG5 = 0;

    /**
     * Algorithm parameter.
     */
    private double pG6 = 0;

    /**
     * Algorithm parameter.
     */
    private double pG9 = 0;

    /**
     * Algorithm parameter.
     */
    private double pB1 = 0;

    /**
     * Algorithm parameter.
     */
    private double pB2 = 0;

    /**
     * Algorithm parameter.
     */
    private double pB3 = 0;

    /**
     * Algorithm parameter.
     */
    private double pB4 = 0;

    /**
     * Algorithm parameter.
     */
    private double pR1 = 0;

    /**
     * Algorithm parameter.
     */
    private double pR2 = 0;

    /**
     * Algorithm parameter.
     */
    private double pR3 = 0;

    /**
     * Algorithm parameter.
     */
    private double pR4 = 0;

    @Override
    protected void decodeGreenMiddle(ImagePixels r, ImagePixels g, ImagePixels b) {
        // Solve for green pixels first
        for (int y = 0; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                pG1 = inputImage.getPixel(x, y);
                pG2 = inputImage.getPixel(x + 2, y);
                pG3 = inputImage.getPixel(x + 1, y + 1);
                pG4 = inputImage.getPixel(x + 1, y - 1);

                g.setPixel(x, y, pG1);
                if (y == 0) {
                    g.setPixel(x + 1, y, (pG1 + pG2 + pG3) / N_3);
                } else {
                    g.setPixel(x + 1, y, (pG1 + pG2 + pG3 + pG4) / N_4);
                }
                if (x == 1) {
                    g.setPixel(x - 1, y, (pG1 + pG4 + inputImage.getPixel(x - 1, y + 1)) / N_3);
                }
            }
        }

        for (int x = 0; x < width; x += 2) {
            for (int y = 1; y < height; y += 2) {

                pG1 = inputImage.getPixel(x, y);
                pG2 = inputImage.getPixel(x + 2, y);
                pG3 = inputImage.getPixel(x + 1, y + 1);
                pG4 = inputImage.getPixel(x + 1, y - 1);

                g.setPixel(x, y, pG1);
                if (x == 0) {
                    g.setPixel(x + 1, y, (pG1 + pG2 + pG3) / N_3);
                } else {
                    g.setPixel(x + 1, y, (pG1 + pG2 + pG3 + pG4) / N_4);
                }
            }
        }

        g.setPixel(0, 0, (inputImage.getPixel(0, 1) + inputImage.getPixel(1, 0)) / 2);

        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                pB1 = inputImage.getPixel(x, y);
                pB2 = inputImage.getPixel(x + 2, y);
                pB3 = inputImage.getPixel(x, y + 2);
                pB4 = inputImage.getPixel(x + 2, y + 2);
                pG1 = g.getPixel(x, y);
                pG2 = g.getPixel(x + 2, y);
                pG3 = g.getPixel(x, y + 2);
                pG4 = g.getPixel(x + 2, y + 2);
                pG5 = g.getPixel(x + 1, y);
                pG6 = g.getPixel(x, y + 1);
                pG9 = g.getPixel(x + 1, y + 1);
                if (pG1 == 0) {
                    pG1 = 1;
                }
                if (pG2 == 0) {
                    pG2 = 1;
                }
                if (pG3 == 0) {
                    pG3 = 1;
                }
                if (pG4 == 0) {
                    pG4 = 1;
                }

                b.setPixel(x, y, pB1);
                b.setPixel(x + 1, y, pG5 / 2 * (pB1 / pG1 + pB2 / pG2));
                b.setPixel(x, y + 1, pG6 / 2 * (pB1 / pG1 + pB3 / pG3));
                b.setPixel(x + 1, y + 1, pG9 / N_4 * (pB1 / pG1 + pB3 / pG3 + pB2 / pG2 + pB4 / pG4));

            }
        }

        for (int y = 1; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                pR1 = inputImage.getPixel(x, y);
                pR2 = inputImage.getPixel(x + 2, y);
                pR3 = inputImage.getPixel(x, y + 2);
                pR4 = inputImage.getPixel(x + 2, y + 2);
                pG1 = g.getPixel(x, y);
                pG2 = g.getPixel(x + 2, y);
                pG3 = g.getPixel(x, y + 2);
                pG4 = g.getPixel(x + 2, y + 2);
                pG5 = g.getPixel(x + 1, y);
                pG6 = g.getPixel(x, y + 1);
                pG9 = g.getPixel(x + 1, y + 1);
                if (pG1 == 0) {
                    pG1 = 1;
                }
                if (pG2 == 0) {
                    pG2 = 1;
                }
                if (pG3 == 0) {
                    pG3 = 1;
                }
                if (pG4 == 0) {
                    pG4 = 1;
                }

                r.setPixel(x, y, pR1);
                r.setPixel(x + 1, y, pG5 / 2 * (pR1 / pG1 + pR2 / pG2));
                r.setPixel(x, y + 1, pG6 / 2 * (pR1 / pG1 + pR3 / pG3));
                r.setPixel(x + 1, y + 1, pG9 / N_4 * (pR1 / pG1 + pR3 / pG3 + pR2 / pG2 + pR4 / pG4));
            }
        }
    }

    @Override
    protected void decodeGreenOutside(ImagePixels r, ImagePixels g, ImagePixels b) {

        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                pG1 = inputImage.getPixel(x, y);
                pG2 = inputImage.getPixel(x + 2, y);
                pG3 = inputImage.getPixel(x + 1, y + 1);
                pG4 = inputImage.getPixel(x + 1, y - 1);

                g.setPixel(x, y, pG1);
                if (y == 0) {
                    g.setPixel(x + 1, y, (pG1 + pG2 + pG3) / N_3);
                } else {
                    g.setPixel(x + 1, y, (pG1 + pG2 + pG3 + pG4) / N_4);
                }
                if (x == 1) {
                    g.setPixel(x - 1, y, (pG1 + pG4 + inputImage.getPixel(x - 1, y + 1)) / N_3);
                }
            }
        }

        for (int y = 1; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                pG1 = inputImage.getPixel(x, y);
                pG2 = inputImage.getPixel(x + 2, y);
                pG3 = inputImage.getPixel(x + 1, y + 1);
                pG4 = inputImage.getPixel(x + 1, y - 1);

                g.setPixel(x, y, pG1);
                if (x == 0) {
                    g.setPixel(x + 1, y, (pG1 + pG2 + pG3) / N_3);
                } else {
                    g.setPixel(x + 1, y, (pG1 + pG2 + pG3 + pG4) / N_4);
                }
            }
        }

        g.setPixel(0, 0, (inputImage.getPixel(0, 1) + inputImage.getPixel(1, 0)) / 2);

        for (int y = 1; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                pB1 = inputImage.getPixel(x, y);
                pB2 = inputImage.getPixel(x + 2, y);
                pB3 = inputImage.getPixel(x, y + 2);
                pB4 = inputImage.getPixel(x + 2, y + 2);
                pG1 = g.getPixel(x, y);
                pG2 = g.getPixel(x + 2, y);
                pG3 = g.getPixel(x, y + 2);
                pG4 = g.getPixel(x + 2, y + 2);
                pG5 = g.getPixel(x + 1, y);
                pG6 = g.getPixel(x, y + 1);
                pG9 = g.getPixel(x + 1, y + 1);
                if (pG1 == 0) {
                    pG1 = 1;
                }
                if (pG2 == 0) {
                    pG2 = 1;
                }
                if (pG3 == 0) {
                    pG3 = 1;
                }
                if (pG4 == 0) {
                    pG4 = 1;
                }

                b.setPixel(x, y, pB1);
                b.setPixel(x + 1, y, pG5 / 2 * (pB1 / pG1 + pB2 / pG2));
                b.setPixel(x, y + 1, pG6 / 2 * (pB1 / pG1 + pB3 / pG3));
                b.setPixel(x + 1, y + 1, pG9 / N_4 * (pB1 / pG1 + pB3 / pG3 + pB2 / pG2 + pB4 / pG4));

            }
        }

        for (int y = 0; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                pR1 = inputImage.getPixel(x, y);
                pR2 = inputImage.getPixel(x + 2, y);
                pR3 = inputImage.getPixel(x, y + 2);
                pR4 = inputImage.getPixel(x + 2, y + 2);
                pG1 = g.getPixel(x, y);
                pG2 = g.getPixel(x + 2, y);
                pG3 = g.getPixel(x, y + 2);
                pG4 = g.getPixel(x + 2, y + 2);
                pG5 = g.getPixel(x + 1, y);
                pG6 = g.getPixel(x, y + 1);
                pG9 = g.getPixel(x + 1, y + 1);
                if (pG1 == 0) {
                    pG1 = 1;
                }
                if (pG2 == 0) {
                    pG2 = 1;
                }
                if (pG3 == 0) {
                    pG3 = 1;
                }
                if (pG4 == 0) {
                    pG4 = 1;
                }

                r.setPixel(x, y, pR1);
                r.setPixel(x + 1, y, pG5 / 2 * (pR1 / pG1 + pR2 / pG2));
                r.setPixel(x, y + 1, pG6 / 2 * (pR1 / pG1 + pR3 / pG3));
                r.setPixel(x + 1, y + 1, pG9 / N_4 * (pR1 / pG1 + pR3 / pG3 + pR2 / pG2 + pR4 / pG4));
            }
        }
    }

    @Override
    public String getName() {
        return "SmoothDebayerAlgorithm Hue";
    }

}
