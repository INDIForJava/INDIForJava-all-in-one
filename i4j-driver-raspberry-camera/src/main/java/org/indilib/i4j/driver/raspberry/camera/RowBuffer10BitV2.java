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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;

import org.indilib.i4j.driver.ccd.INDICCDImage.PixelIterator;

/**
 * This class handles the 10 bit raw format of the raspberry pi camera, it
 * converts one row of pixels from the 10 bit compact form an integer array. The
 * raspberry camera stores the pixel values ba adding a byte every four bytes
 * that contains the low level two bis per byte preceeding.
 * 
 * @author Richard van Nieuwenhoven
 *
 */
public final class RowBuffer10BitV2 {

    /**
     * byte space for the pixels of one row.
     */
    private final byte[] buffer = new byte[CameraConstands.ROWSIZE + 1];

    /**
     * the integer pixel values.
     */
    protected final short[] pixels = new short[CameraConstands.HPIXELS];

    /**
     * read one row from the raw image. and convert it to 16 bit values. move
     * the values so that the least significant 6 bits are always 0 and not the
     * high 6 bits.
     * 
     * @param from
     *            the stream of the raw image.
     * @throws IOException
     *             if something went wrong.
     */
    public void read(InputStream from) throws IOException {
        from.read(this.buffer, 0, CameraConstands.ROWSIZE);
        int pix = 0;
        int bindex = 0;
        while (pix < CameraConstands.HPIXELS) {
            /*
             * decode 4x10 bit from 5 bytes
             */
            int byte0 = this.buffer[bindex++] & 0xFF;
            int byte1 = this.buffer[bindex++] & 0xFF;
            int byte2 = this.buffer[bindex++] & 0xFF;
            int byte3 = this.buffer[bindex++] & 0xFF;
            int split = this.buffer[bindex++] & 0xFF;

            this.pixels[pix++] = (short) (byte0 << 8 | (split & 0b11000000) << 0);
            this.pixels[pix++] = (short) (byte1 << 8 | (split & 0b00110000) << 2);
            this.pixels[pix++] = (short) (byte2 << 8 | (split & 0b00001100) << 4);
            this.pixels[pix++] = (short) (byte3 << 8 | (split & 0b00000011) << 6);
        }
    }

    /**
     * write the pixels to the pixel iterator.
     * 
     * @param iterator
     *            the iterator to write the pixel to.
     */
    public void writeTo(PixelIterator iterator) {
        for (int x = 0; x < CameraConstands.HPIXELS; x++) {
            iterator.setPixel(pixels[x]);
        }
    }

}
