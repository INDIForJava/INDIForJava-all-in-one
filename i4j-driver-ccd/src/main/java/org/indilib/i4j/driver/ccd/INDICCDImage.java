package org.indilib.i4j.driver.ccd;

/*
 * #%L
 * INDI for Java Abstract CCD Driver
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

import java.io.DataOutputStream;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;

/**
 * This class represends an captured ccd images. it will handle any needed
 * conversions and hides away the image processing from the driver itself. This
 * is an abstract class that has apropriate subclasses as innerclasses for the
 * different bit-per-pixel types.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class INDICCDImage {

    /**
     * create a ccd image with the specified size and bpp.
     * 
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     * @param bpp
     *            the bits per pixel of the image.
     */
    private INDICCDImage(int width, int height, int bpp) {
        this.width = width;
        this.height = height;
        this.bpp = bpp;
    }

    /**
     * If the image has less or equal 8 bit per pixel the data fits in a byte
     * array.
     */
    private static final class INDI8BitCCDImage extends INDICCDImage {

        /**
         * the max bit per pixel supported by this class.
         */
        private static final int MAX_BPP = 8;

        /**
         * create a ccd image with the specified size and bpp.
         * 
         * @param width
         *            the width of the image
         * @param height
         *            the height of the image
         * @param bpp
         *            the bits per pixel of the image.
         */
        private INDI8BitCCDImage(int width, int height, int bpp) {
            super(width, height, bpp);
        }

        /**
         * the image data.
         */
        private byte[] imageData;

        @Override
        Object getImageData() {
            return imageData;
        }
    }

    /**
     * If the image has more than 8 but less or equal 16 bit per pixel the data
     * fits in a short array.
     */
    private static final class INDI16BitCCDImage extends INDICCDImage {

        /**
         * the max bit per pixel supported by this class.
         */
        private static final int MAX_BPP = 16;

        /**
         * create a ccd image with the specified size and bpp.
         * 
         * @param width
         *            the width of the image
         * @param height
         *            the height of the image
         * @param bpp
         *            the bits per pixel of the image.
         */
        public INDI16BitCCDImage(int width, int height, int bpp) {
            super(width, height, bpp);
        }

        /**
         * the image data.
         */
        private short[] imageData;

        @Override
        Object getImageData() {
            return imageData;
        }
    }

    /**
     * If the image has more than 16 but less or equal 32 bit per pixel the data
     * fits in a int array.
     */
    private static final class INDI32BitCCDImage extends INDICCDImage {

        /**
         * the max bit per pixel supported by this class.
         */
        private static final int MAX_BPP = 32;

        /**
         * create a ccd image with the specified size and bpp.
         * 
         * @param width
         *            the width of the image
         * @param height
         *            the height of the image
         * @param bpp
         *            the bits per pixel of the image.
         */
        public INDI32BitCCDImage(int width, int height, int bpp) {
            super(width, height, bpp);
        }

        /**
         * the image data.
         */
        private int[] imageData;

        @Override
        Object getImageData() {
            return imageData;
        }

    }

    /**
     * the image width.
     */
    protected final int width;

    /**
     * the image height.
     */
    protected final int height;

    /**
     * bits per pixel.
     */
    protected final int bpp;

    /**
     * the fits representation.
     */
    private Fits f;

    /**
     * convert the current imageData to a fits image.
     * 
     * @throws FitsException
     *             if the image could not be converted
     */
    private void convertToFits() throws FitsException {
        f = new Fits();
        BasicHDU imageFits = FitsFactory.HDUFactory(getImageData());
        f.addHDU(imageFits);
    }

    /**
     * @return the primitive array of the image.
     */
    abstract Object getImageData();

    /**
     * create a ccd image with the specified size and bpp.
     * 
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     * @param bpp
     *            the bits per pixel of the image.
     * @return the newly created image.
     */
    public static INDICCDImage createImage(int width, int height, int bpp) {
        if (bpp <= INDI8BitCCDImage.MAX_BPP) {
            return new INDI8BitCCDImage(width, height, bpp);
        } else if (bpp <= INDI16BitCCDImage.MAX_BPP) {
            return new INDI16BitCCDImage(width, height, bpp);
        } else if (bpp <= INDI32BitCCDImage.MAX_BPP) {
            return new INDI32BitCCDImage(width, height, bpp);
        } else {
            throw new IllegalArgumentException("not supported bits per pixel " + bpp);
        }
    }

    /**
     * @return the fits image representing the current data.
     */
    public Fits asFitsImage() {
        if (f == null) {
            try {
                convertToFits();
            } catch (FitsException e) {
                throw new IllegalStateException("Fits image could not be created!", e);
            }
        }
        return f;
    }

    /**
     * write the ccd image to the output stream.
     * 
     * @param os
     *            the output stream
     * @param left
     *            start in x
     * @param top
     *            start in y
     * @param subwidth
     *            width in pixel
     * @param subheigth
     *            height in pixel
     * @param extension
     *            the file extension (currently only fits allowed.
     * @throws FitsException
     *             if the file could not be written.
     */
    public void write(DataOutputStream os, int left, int top, int subwidth, int subheigth, String extension) throws FitsException {
        if ("fits".equals(extension)) {
            asFitsImage().write(os);
        } else {
            throw new IllegalArgumentException("extention " + extension + " not supported");
        }
    }
}
