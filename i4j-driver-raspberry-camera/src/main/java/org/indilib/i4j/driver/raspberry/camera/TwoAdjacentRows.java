package org.indilib.i4j.driver.raspberry.camera;

/*
 * #%L
 * INDI for Java Driver for the Raspberry pi camera
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class TwoAdjacentRows {

    private final RowBuffer10Bit firstRow = new RowBuffer10Bit();

    private final RowBuffer10Bit secondRow = new RowBuffer10Bit();

    public void read(InputStream in) throws IOException {
        this.firstRow.read(in);
        this.secondRow.read(in);
    }

    public void write16BitFitsImages(int y, short[][][] image) {
        int[] firstPixels = this.firstRow.pixels;
        int[] secondPixels = this.secondRow.pixels;
        int offset = 0;
        for (int x = 0; x < CameraConstands.HPIXELS; x += 2) {
            image[0][y][offset] = (short) (secondPixels[x] >> 1);
            image[1][y][offset] = (short) (firstPixels[x] >> 1);
            image[2][y][offset++] = (short) (secondPixels[x + 1] >> 1);
            // image[y][imagex] = (short) firstPixels[x + 1];
        }
    }

    public void write16BitRGB(DataOutputStream data) throws IOException {
        int[] firstPixels = this.firstRow.pixels;
        int[] secondPixels = this.secondRow.pixels;

        for (int x = 0; x < CameraConstands.HPIXELS; x += 2) {
            data.writeShort(secondPixels[x]);
            data.writeShort(firstPixels[x]);
            data.writeShort(firstPixels[x + 1]);
            // int g2 = secondPixels[x + 1];
        }
    }

    public void writeRawFitsImages(int y, short[][] image) {
        int[] firstPixels = this.firstRow.pixels;
        int[] secondPixels = this.secondRow.pixels;
        for (int x = 0; x < CameraConstands.HPIXELS; x++) {
            image[y][x] = (short) (firstPixels[x] >> 1);
            image[y + 1][x] = (short) (secondPixels[x] >> 1);
        }
    }

    public void writeRGB24FitsImages(int y, byte[][][] image) {
        int[] firstPixels = this.firstRow.pixels;
        int[] secondPixels = this.secondRow.pixels;
        int offset = 0;
        for (int x = 0; x < CameraConstands.HPIXELS; x += 2) {
            image[0][y][offset] = (byte) (secondPixels[x] >> 8);
            image[1][y][offset] = (byte) (firstPixels[x] >> 8);
            image[2][y][offset++] = (byte) (secondPixels[x + 1] >> 8);
            // image[y][imagex] = (short) firstPixels[x + 1];
        }
    }
}
