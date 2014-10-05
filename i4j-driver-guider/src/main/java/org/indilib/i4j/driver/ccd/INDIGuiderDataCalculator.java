package org.indilib.i4j.driver.ccd;

/*
 * #%L
 * INDI for Java Guider extention - inactive
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
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


import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INDIGuiderDataCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(INDIGuiderDataCalculator.class);

    private static final double P0 = 0.906, P1 = 0.584, P2 = 0.365, P3 = 0.117, P4 = 0.049, P5 = -0.05, P6 = -0.064, P7 = -0.074, P8 = -0.094;

    private static final int[][] PROXIMITY_MATRIX = {
        {
            8,
            8,
            8,
            8,
            8,
            8,
            8,
            8,
            8
        },
        {
            8,
            8,
            8,
            7,
            6,
            7,
            8,
            8,
            8
        },
        {
            8,
            8,
            5,
            4,
            3,
            4,
            5,
            8,
            8
        },
        {
            8,
            7,
            4,
            2,
            1,
            2,
            4,
            8,
            8
        },
        {
            8,
            6,
            3,
            1,
            0,
            1,
            3,
            6,
            8
        },
        {
            8,
            7,
            4,
            2,
            1,
            2,
            4,
            8,
            8
        },
        {
            8,
            8,
            5,
            4,
            3,
            4,
            5,
            8,
            8
        },
        {
            8,
            8,
            8,
            7,
            6,
            7,
            8,
            8,
            8
        },
        {
            8,
            8,
            8,
            8,
            8,
            8,
            8,
            8,
            8
        }
    };

    private final INDINumberProperty rapidGuideData;

    private final INDINumberElement rapidGuideDataX;

    private final INDINumberElement rapidGuideDataY;

    private final INDINumberElement rapidGuideDataFIT;

    private int lastRapidX;

    private int lastRapidY;

    public INDIGuiderDataCalculator(INDINumberProperty rapidGuideData, INDINumberElement rapidGuideDataX, INDINumberElement rapidGuideDataY,
            INDINumberElement rapidGuideDataFIT) {
        this.rapidGuideData = rapidGuideData;
        this.rapidGuideDataX = rapidGuideDataX;
        this.rapidGuideDataY = rapidGuideDataY;
        this.rapidGuideDataFIT = rapidGuideDataFIT;
    }

    public void detectGuideData(int width, int height, INDICCDImage ccdImage, boolean showMarker) {
        rapidGuideData.setState(PropertyStates.BUSY);
        int i[] = new int[9];
        int ix = 0, iy = 0;
        double average, fit, bestFit = 0;
        int minx = 4;
        int maxx = width - 4;
        int miny = 4;
        int maxy = height - 4;
        if (lastRapidX > 0 && lastRapidY > 0) {
            minx = Math.max(lastRapidX - 20, 4);
            maxx = Math.min(lastRapidX + 20, width - 4);
            miny = Math.max(lastRapidY - 20, 4);
            maxy = Math.min(lastRapidY + 20, height - 4);
        }
        int[][] src = ccdImage.subImageInt(minx - 4, miny - 4, maxx + 4, maxy + 4);
        for (int y = 4; y < (src.length - 4); y++) {
            for (int x = 4; x < (src[x].length - 4); x++) {
                i[0] = i[1] = i[2] = i[3] = i[4] = i[5] = i[6] = i[7] = i[8] = 0;
                for (int My = 0; My < PROXIMITY_MATRIX.length; My++) {
                    for (int Mx = 0; Mx < PROXIMITY_MATRIX[My].length; Mx++) {
                        i[PROXIMITY_MATRIX[My][Mx]] = src[y + My - 4][x + Mx - 4];
                    }
                }
                average = (i[0] + i[1] + i[2] + i[3] + i[4] + i[5] + i[6] + i[7] + i[8]) / 85.0;
                fit = P0 * (i[0] - average) + //
                        P1 * (i[1] - 4 * average) + //
                        P2 * (i[2] - 4 * average) + //
                        P3 * (i[3] - 4 * average) + //
                        P4 * (i[4] - 8 * average) + //
                        P5 * (i[5] - 4 * average) + //
                        P6 * (i[6] - 4 * average) + //
                        P7 * (i[7] - 8 * average) + //
                        P8 * (i[8] - 48 * average);
                if (bestFit < fit) {
                    bestFit = fit;
                    ix = x;
                    iy = y;
                }
            }
        }
        double sumX = 0;
        double sumY = 0;
        double total = 0;
        for (int y = iy - 4; y <= iy + 4; y++) {
            for (int x = ix - 4; x <= ix + 4; x++) {
                double w = src[y][x];
                sumX += x * w;
                sumY += y * w;
                total += w;
            }
        }

        ix = ix + (minx - 4);
        iy = iy + (miny - 4);

        rapidGuideDataX.setValue(ix);
        rapidGuideDataY.setValue(iy);
        rapidGuideDataFIT.setValue(bestFit);
        lastRapidX = ix;
        lastRapidY = iy;
        if (bestFit > 50) {
            if (total > 0) {
                rapidGuideDataX.setValue(sumX / total);
                rapidGuideDataY.setValue(sumY / total);
                rapidGuideData.setState(PropertyStates.OK);

                LOG.debug(String.format("Guide Star X: %g Y: %g FIT: %g", rapidGuideDataX.getValue(), rapidGuideDataY.getValue(), rapidGuideDataFIT.getValue()));
            } else {
                rapidGuideData.setState(PropertyStates.ALERT);
                lastRapidX = lastRapidY = -1;
            }
        } else {
            rapidGuideData.setState(PropertyStates.ALERT);
            lastRapidX = lastRapidY = -1;
        }
        if (showMarker) {
            ccdImage.drawMarker(ix, iy);
        }
    }

}
