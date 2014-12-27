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
        for (int y = 0; y < this.height; y += 2) {
            for (int x = 1; x < this.width; x += 2) {
                this.pG1 = this.inputImage.getPixel(x, y);
                this.pG2 = this.inputImage.getPixel(x + 2, y);
                this.pG3 = this.inputImage.getPixel(x + 1, y + 1);
                this.pG4 = this.inputImage.getPixel(x + 1, y - 1);

                g.setPixel(x, y, this.pG1);
                if (y == 0) {
                    g.setPixel(x + 1, y, (this.pG1 + this.pG2 + this.pG3) / N_3);
                } else {
                    g.setPixel(x + 1, y, (this.pG1 + this.pG2 + this.pG3 + this.pG4) / N_4);
                }
                if (x == 1) {
                    g.setPixel(x - 1, y, (this.pG1 + this.pG4 + this.inputImage.getPixel(x - 1, y + 1)) / N_3);
                }
            }
        }

        for (int x = 0; x < this.width; x += 2) {
            for (int y = 1; y < this.height; y += 2) {

                this.pG1 = this.inputImage.getPixel(x, y);
                this.pG2 = this.inputImage.getPixel(x + 2, y);
                this.pG3 = this.inputImage.getPixel(x + 1, y + 1);
                this.pG4 = this.inputImage.getPixel(x + 1, y - 1);

                g.setPixel(x, y, this.pG1);
                if (x == 0) {
                    g.setPixel(x + 1, y, (this.pG1 + this.pG2 + this.pG3) / N_3);
                } else {
                    g.setPixel(x + 1, y, (this.pG1 + this.pG2 + this.pG3 + this.pG4) / N_4);
                }
            }
        }

        g.setPixel(0, 0, (this.inputImage.getPixel(0, 1) + this.inputImage.getPixel(1, 0)) / 2);

        for (int y = 0; y < this.height; y += 2) {
            for (int x = 0; x < this.width; x += 2) {
                this.pB1 = this.inputImage.getPixel(x, y);
                this.pB2 = this.inputImage.getPixel(x + 2, y);
                this.pB3 = this.inputImage.getPixel(x, y + 2);
                this.pB4 = this.inputImage.getPixel(x + 2, y + 2);
                this.pG1 = g.getPixel(x, y);
                this.pG2 = g.getPixel(x + 2, y);
                this.pG3 = g.getPixel(x, y + 2);
                this.pG4 = g.getPixel(x + 2, y + 2);
                this.pG5 = g.getPixel(x + 1, y);
                this.pG6 = g.getPixel(x, y + 1);
                this.pG9 = g.getPixel(x + 1, y + 1);
                if (this.pG1 == 0) {
                    this.pG1 = 1;
                }
                if (this.pG2 == 0) {
                    this.pG2 = 1;
                }
                if (this.pG3 == 0) {
                    this.pG3 = 1;
                }
                if (this.pG4 == 0) {
                    this.pG4 = 1;
                }

                b.setPixel(x, y, this.pB1);
                b.setPixel(x + 1, y, this.pG5 / 2 * (this.pB1 / this.pG1 + this.pB2 / this.pG2));
                b.setPixel(x, y + 1, this.pG6 / 2 * (this.pB1 / this.pG1 + this.pB3 / this.pG3));
                b.setPixel(x + 1, y + 1, this.pG9 / N_4 * (this.pB1 / this.pG1 + this.pB3 / this.pG3 + this.pB2 / this.pG2 + this.pB4 / this.pG4));

            }
        }

        for (int y = 1; y < this.height; y += 2) {
            for (int x = 1; x < this.width; x += 2) {
                this.pR1 = this.inputImage.getPixel(x, y);
                this.pR2 = this.inputImage.getPixel(x + 2, y);
                this.pR3 = this.inputImage.getPixel(x, y + 2);
                this.pR4 = this.inputImage.getPixel(x + 2, y + 2);
                this.pG1 = g.getPixel(x, y);
                this.pG2 = g.getPixel(x + 2, y);
                this.pG3 = g.getPixel(x, y + 2);
                this.pG4 = g.getPixel(x + 2, y + 2);
                this.pG5 = g.getPixel(x + 1, y);
                this.pG6 = g.getPixel(x, y + 1);
                this.pG9 = g.getPixel(x + 1, y + 1);
                if (this.pG1 == 0) {
                    this.pG1 = 1;
                }
                if (this.pG2 == 0) {
                    this.pG2 = 1;
                }
                if (this.pG3 == 0) {
                    this.pG3 = 1;
                }
                if (this.pG4 == 0) {
                    this.pG4 = 1;
                }

                r.setPixel(x, y, this.pR1);
                r.setPixel(x + 1, y, this.pG5 / 2 * (this.pR1 / this.pG1 + this.pR2 / this.pG2));
                r.setPixel(x, y + 1, this.pG6 / 2 * (this.pR1 / this.pG1 + this.pR3 / this.pG3));
                r.setPixel(x + 1, y + 1, this.pG9 / N_4 * (this.pR1 / this.pG1 + this.pR3 / this.pG3 + this.pR2 / this.pG2 + this.pR4 / this.pG4));
            }
        }
    }

    @Override
    protected void decodeGreenOutside(ImagePixels r, ImagePixels g, ImagePixels b) {

        for (int y = 0; y < this.height; y += 2) {
            for (int x = 0; x < this.width; x += 2) {
                this.pG1 = this.inputImage.getPixel(x, y);
                this.pG2 = this.inputImage.getPixel(x + 2, y);
                this.pG3 = this.inputImage.getPixel(x + 1, y + 1);
                this.pG4 = this.inputImage.getPixel(x + 1, y - 1);

                g.setPixel(x, y, this.pG1);
                if (y == 0) {
                    g.setPixel(x + 1, y, (this.pG1 + this.pG2 + this.pG3) / N_3);
                } else {
                    g.setPixel(x + 1, y, (this.pG1 + this.pG2 + this.pG3 + this.pG4) / N_4);
                }
                if (x == 1) {
                    g.setPixel(x - 1, y, (this.pG1 + this.pG4 + this.inputImage.getPixel(x - 1, y + 1)) / N_3);
                }
            }
        }

        for (int y = 1; y < this.height; y += 2) {
            for (int x = 1; x < this.width; x += 2) {
                this.pG1 = this.inputImage.getPixel(x, y);
                this.pG2 = this.inputImage.getPixel(x + 2, y);
                this.pG3 = this.inputImage.getPixel(x + 1, y + 1);
                this.pG4 = this.inputImage.getPixel(x + 1, y - 1);

                g.setPixel(x, y, this.pG1);
                if (x == 0) {
                    g.setPixel(x + 1, y, (this.pG1 + this.pG2 + this.pG3) / N_3);
                } else {
                    g.setPixel(x + 1, y, (this.pG1 + this.pG2 + this.pG3 + this.pG4) / N_4);
                }
            }
        }

        g.setPixel(0, 0, (this.inputImage.getPixel(0, 1) + this.inputImage.getPixel(1, 0)) / 2);

        for (int y = 1; y < this.height; y += 2) {
            for (int x = 0; x < this.width; x += 2) {
                this.pB1 = this.inputImage.getPixel(x, y);
                this.pB2 = this.inputImage.getPixel(x + 2, y);
                this.pB3 = this.inputImage.getPixel(x, y + 2);
                this.pB4 = this.inputImage.getPixel(x + 2, y + 2);
                this.pG1 = g.getPixel(x, y);
                this.pG2 = g.getPixel(x + 2, y);
                this.pG3 = g.getPixel(x, y + 2);
                this.pG4 = g.getPixel(x + 2, y + 2);
                this.pG5 = g.getPixel(x + 1, y);
                this.pG6 = g.getPixel(x, y + 1);
                this.pG9 = g.getPixel(x + 1, y + 1);
                if (this.pG1 == 0) {
                    this.pG1 = 1;
                }
                if (this.pG2 == 0) {
                    this.pG2 = 1;
                }
                if (this.pG3 == 0) {
                    this.pG3 = 1;
                }
                if (this.pG4 == 0) {
                    this.pG4 = 1;
                }

                b.setPixel(x, y, this.pB1);
                b.setPixel(x + 1, y, this.pG5 / 2 * (this.pB1 / this.pG1 + this.pB2 / this.pG2));
                b.setPixel(x, y + 1, this.pG6 / 2 * (this.pB1 / this.pG1 + this.pB3 / this.pG3));
                b.setPixel(x + 1, y + 1, this.pG9 / N_4 * (this.pB1 / this.pG1 + this.pB3 / this.pG3 + this.pB2 / this.pG2 + this.pB4 / this.pG4));

            }
        }

        for (int y = 0; y < this.height; y += 2) {
            for (int x = 1; x < this.width; x += 2) {
                this.pR1 = this.inputImage.getPixel(x, y);
                this.pR2 = this.inputImage.getPixel(x + 2, y);
                this.pR3 = this.inputImage.getPixel(x, y + 2);
                this.pR4 = this.inputImage.getPixel(x + 2, y + 2);
                this.pG1 = g.getPixel(x, y);
                this.pG2 = g.getPixel(x + 2, y);
                this.pG3 = g.getPixel(x, y + 2);
                this.pG4 = g.getPixel(x + 2, y + 2);
                this.pG5 = g.getPixel(x + 1, y);
                this.pG6 = g.getPixel(x, y + 1);
                this.pG9 = g.getPixel(x + 1, y + 1);
                if (this.pG1 == 0) {
                    this.pG1 = 1;
                }
                if (this.pG2 == 0) {
                    this.pG2 = 1;
                }
                if (this.pG3 == 0) {
                    this.pG3 = 1;
                }
                if (this.pG4 == 0) {
                    this.pG4 = 1;
                }

                r.setPixel(x, y, this.pR1);
                r.setPixel(x + 1, y, this.pG5 / 2 * (this.pR1 / this.pG1 + this.pR2 / this.pG2));
                r.setPixel(x, y + 1, this.pG6 / 2 * (this.pR1 / this.pG1 + this.pR3 / this.pG3));
                r.setPixel(x + 1, y + 1, this.pG9 / N_4 * (this.pR1 / this.pG1 + this.pR3 / this.pG3 + this.pR2 / this.pG2 + this.pR4 / this.pG4));
            }
        }
    }

    @Override
    public String getName() {
        return "SmoothDebayerAlgorithm Hue";
    }

}
