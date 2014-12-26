package org.indilib.i4j.driver.raspberry.camera.image;

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
import org.indilib.i4j.driver.raspberry.camera.CameraConstands;

/**
 * This class handles the 10 bit raw format of the raspberry pi camera, it
 * converts one row of pixels from the 10 bit compact form an integer array. The
 * raspberry camera stores the pixel values ba adding a byte every four bytes
 * that contains the low level two bis per byte preceeding.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class RowBuffer10Bit {

    /**
     * number of pixels in 5 raw image bytes.
     */
    private static final int PIXELS_PER_5_BYTES = 4;

    /**
     * how many bits to shift the fourth 2 to the first 2.
     */
    private static final int SHIFT_FOURTH_2_TO_FIRST_2 = 6;

    /**
     * how many bits to shift the third 2 to the first 2.
     */
    private static final int SHIFT_THIRD_2_TO_FIRST_2 = 4;

    /**
     * how many bits to shift the second 2 to the first 2.
     */
    private static final int SHIFT_SECOND_2_TO_FIRST_2 = 2;

    /**
     * Bit mast for the fourth 2 bytes.
     */
    private static final int SPLIT_BYTE_FROUTH_2 = 0b00000011;

    /**
     * Bit mast for the third 2 bytes.
     */
    private static final int SPLIT_BYTE_THIRD_2 = 0b00001100;

    /**
     * Bit mast for the second 2 bytes.
     */
    private static final int SPLIT_BYTE_SECOND_2 = 0b00110000;

    /**
     * Bit mast for the first 2 bytes.
     */
    private static final int SPLIT_BYTE_FIRST_2 = 0b11000000;

    /**
     * number of bits in a byte.
     */
    private static final int BITS_IN_BYTE = 8;

    /**
     * mask to access a byte.
     */
    private static final int BYTE_MASK = 0xFF;

    /**
     * byte space for the pixels of one row.
     */
    private final byte[] buffer = new byte[CameraConstands.ROWSIZE + 1];

    /**
     * read one row from the raw image.
     * 
     * @param from
     *            the stream of the raw image.
     * @param iterator
     *            the pixel iterator to fill
     * @throws IOException
     *             if something went wrong.
     */
    public void copy10BitPixelRowToIterator(InputStream from, PixelIterator iterator) throws IOException {
        from.read(this.buffer, 0, CameraConstands.ROWSIZE);
        int pix = 0;
        int bindex = 0;
        while (pix < CameraConstands.HPIXELS) {
            /*
             * decode 4x10 bit from 5 bytes
             */
            int byte0 = this.buffer[bindex++] & BYTE_MASK;
            int byte1 = this.buffer[bindex++] & BYTE_MASK;
            int byte2 = this.buffer[bindex++] & BYTE_MASK;
            int byte3 = this.buffer[bindex++] & BYTE_MASK;
            int split = this.buffer[bindex++] & BYTE_MASK;

            iterator.setPixel(byte0 << BITS_IN_BYTE | (split & SPLIT_BYTE_FIRST_2));
            iterator.setPixel(byte1 << BITS_IN_BYTE | (split & SPLIT_BYTE_SECOND_2) << SHIFT_SECOND_2_TO_FIRST_2);
            iterator.setPixel(byte2 << BITS_IN_BYTE | (split & SPLIT_BYTE_THIRD_2) << SHIFT_THIRD_2_TO_FIRST_2);
            iterator.setPixel(byte3 << BITS_IN_BYTE | (split & SPLIT_BYTE_FROUTH_2) << SHIFT_FOURTH_2_TO_FIRST_2);
            pix += PIXELS_PER_5_BYTES;
        }
    }
}
