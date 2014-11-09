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
import nom.tam.fits.HeaderCardException;

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
     * axis value for the third layer.
     */
    private static final int COLOR_AXIS3 = 3;

    /**
     * naxis for a grayscale image.
     */
    private static final int GRAY_SCALE_NAXIS = 2;

    /**
     * naxis for a color image.
     */
    private static final int COLOR_SCALE_NAXIS = 3;

    /**
     * the maximum value of a short.
     */
    private static final int MAX_SHORT_VALUE = 32768;

    /**
     * the maximum value of a byte.
     */
    private static final int MAX_BYTE_VALUE = 255;

    /**
     * integer to convert a unsigned byte.
     */
    private static final int UNSIGNED_BYTE = 0xFF;

    /**
     * integer to convert a unsigned short.
     */
    private static final int UNSIGNED_SHORT = 0xFFFF;

    /**
     * color type of the image.
     */
    public enum ImageType {
        /**
         * grayscale image.
         */
        GRAY_SCALE(1),
        /**
         * rgb image.
         */
        COLOR(3),
        /**
         * raw image. needs debayering.
         */
        RAW(1);

        /**
         * how many values for the 3 axis.
         */
        private final int axis3;

        /**
         * internal constructor.
         * 
         * @param axis3
         *            the number of values for the 3 axis.
         */
        private ImageType(int axis3) {
            this.axis3 = axis3;
        }
    }

    /**
     * Iterator to interate over the pixels.
     */
    public abstract static class PixelIterator {

        /**
         * index of the pixel in the first layer.
         */
        protected int index;

        /**
         * index of the pixel in the second layer.
         */
        protected int indexLayer2;

        /**
         * index of the pixel in the thirt layer.
         */
        protected int indexLayer3;

        /**
         * line width of the image.
         */
        protected final int width;

        /**
         * create a pixel iterator over an image array.
         * 
         * @param width
         *            the width of the image.
         * @param heigth
         *            the heigth of the image.
         */
        private PixelIterator(int width, int heigth) {
            this.width = width;
            this.indexLayer2 = width * heigth;
            this.indexLayer3 = this.indexLayer2 * 2;
        }

        /**
         * set the pixel value of the first layer.
         * 
         * @param value
         *            the new value for the pixel.
         */
        public abstract void setPixel(int value);

        /**
         * set the pixel value of the first layer.
         * 
         * @param value
         *            the new value for the pixel.
         */
        public abstract void setPixel(byte value);

        /**
         * set the pixel value of the first layer.
         * 
         * @param value
         *            the new value for the pixel.
         */
        public abstract void setPixel(short value);

        /**
         * set the pixel values of the different colors.
         * 
         * @param red
         *            the value for the first layer
         * @param green
         *            the value for the second layer
         * @param blue
         *            the value for the third layer
         */
        public abstract void setPixel(int red, int green, int blue);

        /**
         * set the pixel values of the different colors.
         * 
         * @param red
         *            the value for the first layer
         * @param green
         *            the value for the second layer
         * @param blue
         *            the value for the third layer
         */
        public abstract void setPixel(byte red, byte green, byte blue);

        /**
         * set the pixel values of the different colors.
         * 
         * @param red
         *            the value for the first layer
         * @param green
         *            the value for the second layer
         * @param blue
         *            the value for the third layer
         */
        public abstract void setPixel(short red, short green, short blue);

        /**
         * skip over the next pixel.
         */
        public void nextPixel() {
            index++;
            indexLayer2++;
            indexLayer3++;
        }

        /**
         * skip over the next pixel line.
         */
        public void nextLine() {
            index = ((index + 1) % width) * width + width;
            indexLayer2 = ((indexLayer2 + 1) % width) * width + width;
            indexLayer3 = ((indexLayer3 + 1) % width) * width + width;
        }
    }

    /**
     * type of the image.
     */
    protected ImageType type;

    /**
     * create a ccd image with the specified size and bpp.
     * 
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     * @param bpp
     *            the bits per pixel of the image.
     * @param type
     *            type of the image.
     */
    private INDICCDImage(int width, int height, int bpp, ImageType type) {
        this.width = width;
        this.height = height;
        this.bpp = bpp;
        this.type = type;
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
         * @param type
         *            type of the image.
         */
        private INDI8BitCCDImage(int width, int height, int bpp, ImageType type) {
            super(width, height, bpp, type);
        }

        /**
         * the image data.
         */
        private byte[] imageData;

        @Override
        Object getImageData() {
            return imageData;
        }

        @Override
        public PixelIterator iteratePixel() {
            imageData = new byte[width * height * type.axis3];
            return new PixelIterator(width, height) {

                @Override
                public void setPixel(int value) {
                    imageData[index++] = (byte) (value & UNSIGNED_BYTE);
                }

                @Override
                public void setPixel(int red, int green, int blue) {
                    imageData[index++] = (byte) (red & UNSIGNED_BYTE);
                    imageData[indexLayer2++] = (byte) (green & UNSIGNED_BYTE);
                    imageData[indexLayer3++] = (byte) (blue & UNSIGNED_BYTE);

                }

                @Override
                public void setPixel(short value) {
                    imageData[index++] = (byte) (value & UNSIGNED_BYTE);
                }

                @Override
                public void setPixel(short red, short green, short blue) {
                    imageData[index++] = (byte) (red & UNSIGNED_BYTE);
                    imageData[indexLayer2++] = (byte) (green & UNSIGNED_BYTE);
                    imageData[indexLayer3++] = (byte) (blue & UNSIGNED_BYTE);

                }

                @Override
                public void setPixel(byte value) {
                    imageData[index++] = value;
                }

                @Override
                public void setPixel(byte red, byte green, byte blue) {
                    imageData[index++] = red;
                    imageData[indexLayer2++] = green;
                    imageData[indexLayer3++] = blue;

                }

            };
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
         * @param type
         *            the type of the image.
         */
        public INDI16BitCCDImage(int width, int height, int bpp, ImageType type) {
            super(width, height, bpp, type);
        }

        /**
         * the image data.
         */
        private int[] imageData;

        @Override
        Object getImageData() {
            return imageData;
        }

        @Override
        public PixelIterator iteratePixel() {
            imageData = new int[width * height * type.axis3];
            return new PixelIterator(width, height) {

                @Override
                public void setPixel(int value) {
                    imageData[index++] = value;
                }

                @Override
                public void setPixel(int red, int green, int blue) {
                    imageData[index++] = red;
                    imageData[indexLayer2++] = green;
                    imageData[indexLayer3++] = blue;

                }

                @Override
                public void setPixel(short value) {
                    imageData[index++] = value;
                }

                @Override
                public void setPixel(short red, short green, short blue) {
                    imageData[index++] = red;
                    imageData[indexLayer2++] = green;
                    imageData[indexLayer3++] = blue;

                }

                @Override
                public void setPixel(byte value) {
                    imageData[index++] = (value & UNSIGNED_BYTE);
                }

                @Override
                public void setPixel(byte red, byte green, byte blue) {
                    imageData[index++] = (red & UNSIGNED_BYTE);
                    imageData[indexLayer2++] = (green & UNSIGNED_BYTE);
                    imageData[indexLayer3++] = (blue & UNSIGNED_BYTE);

                }
            };
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
         * @param type
         *            type of the image.
         */
        public INDI32BitCCDImage(int width, int height, int bpp, ImageType type) {
            super(width, height, bpp, type);
        }

        /**
         * the image data.
         */
        private int[] imageData;

        @Override
        Object getImageData() {
            return imageData;
        }

        @Override
        public PixelIterator iteratePixel() {
            imageData = new int[width * height * type.axis3];
            return new PixelIterator(width, height) {

                @Override
                public void setPixel(int value) {
                    imageData[index++] = value;
                }

                @Override
                public void setPixel(int red, int green, int blue) {
                    imageData[index++] = red;
                    imageData[indexLayer2++] = green;
                    imageData[indexLayer3++] = blue;

                }

                @Override
                public void setPixel(short value) {
                    imageData[index++] = value & UNSIGNED_SHORT;
                }

                @Override
                public void setPixel(short red, short green, short blue) {
                    imageData[index++] = red & UNSIGNED_SHORT;
                    imageData[indexLayer2++] = green & UNSIGNED_SHORT;
                    imageData[indexLayer3++] = blue & UNSIGNED_SHORT;

                }

                @Override
                public void setPixel(byte value) {
                    imageData[index++] = value & UNSIGNED_BYTE;
                }

                @Override
                public void setPixel(byte red, byte green, byte blue) {
                    imageData[index++] = red & UNSIGNED_BYTE;
                    imageData[indexLayer2++] = green & UNSIGNED_BYTE;
                    imageData[indexLayer3++] = blue & UNSIGNED_BYTE;

                }
            };
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
        addFitsAttributes(imageFits);
        f.addHDU(imageFits);
    }

    /**
     * add the standard fits attributes to the image.
     * 
     * @param imageFits
     *            the fits image to add the attributes.
     * @throws HeaderCardException
     *             if the header got illegal
     */
    private void addFitsAttributes(BasicHDU imageFits) throws HeaderCardException {
        imageFits.addValue("HISTORY", "FITS image created by i4j", "");
        imageFits.addValue("SIMPLE", "T", "");
        imageFits.addValue("BITPIX", bpp, "");
        imageFits.addValue("NAXIS", type == ImageType.COLOR ? COLOR_SCALE_NAXIS : GRAY_SCALE_NAXIS, "");
        imageFits.addValue("NAXIS1", width, "");
        imageFits.addValue("NAXIS2", height, "");
        if (type == ImageType.COLOR) {
            imageFits.addValue("NAXIS3", COLOR_AXIS3, "");
        }
        imageFits.addValue("DATAMIN", 0, "");
        if (bpp == INDI8BitCCDImage.MAX_BPP) {
            imageFits.addValue("DATAMAX", MAX_BYTE_VALUE, "");
        } else if (bpp == INDI16BitCCDImage.MAX_BPP) {
            imageFits.addValue("DATAMAX", MAX_SHORT_VALUE, "");
        } else {
            throw new IllegalArgumentException("unknown bits per pixel");
        }

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
     * @param type
     *            the type of the image.
     * @return the newly created image.
     */
    public static INDICCDImage createImage(int width, int height, int bpp, ImageType type) {
        if (bpp <= INDI8BitCCDImage.MAX_BPP) {
            return new INDI8BitCCDImage(width, height, bpp, type);
        } else if (bpp <= INDI16BitCCDImage.MAX_BPP) {
            return new INDI16BitCCDImage(width, height, bpp, type);
        } else if (bpp <= INDI32BitCCDImage.MAX_BPP) {
            return new INDI32BitCCDImage(width, height, bpp, type);
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

    /**
     * @return an iterator to iterate over the pixels.
     */
    public abstract PixelIterator iteratePixel();
}
