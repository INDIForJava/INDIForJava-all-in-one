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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import nom.tam.fits.*;

import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static nom.tam.fits.header.Standard.*;

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
     * the maximum length of a fits header card.
     */
    private static final int MAX_FITS_HEADERCARD_VALUE_LENGTH = 70;

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
     * the maximum value of a byte.
     */
    private static final int MAX_BYTE_VALUE = 255;

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
     * Iterator to interate over the pixels. Attention this class depends on the
     * fact that the minimum pixel value is 0 and the maximum is dependent on
     * the bits per pixel info. So no negative values.
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
            indexLayer2 = width * heigth;
            indexLayer3 = indexLayer2 * 2;
        }

        /**
         * max value of of a pixel.
         */
        private int maxPixelValue = Integer.MIN_VALUE;

        /**
         * min value of of a pixel.
         */
        private int minPixelValue = Integer.MAX_VALUE;

        /**
         * set the pixel value of the first layer.
         * 
         * @param value
         *            the new value for the pixel.
         */
        public abstract void setPixel(int value);

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
            index = (index + 1) % width * width + width;
            indexLayer2 = (indexLayer2 + 1) % width * width + width;
            indexLayer3 = (indexLayer3 + 1) % width * width + width;
        }

        /**
         * check the max pixel value, an record the new max and min value.
         * Attention the range adaption is not pressent here. so do only enter
         * values between 0 and Integer.MAX_VALUE
         * 
         * @param value
         *            the value to check
         * @return the unchanged original value
         */
        protected final int rangeCheck(int value) {
            maxPixelValue = Math.max(maxPixelValue, value);
            minPixelValue = Math.min(minPixelValue, value);
            return value;
        }

        /**
         * check the max pixel value, an record the new max and min value. and
         * convert the range to Short.MIN_VALUE and Short.MAX_VALUE
         * 
         * @param value
         *            the value to check
         * @return the unchanged original value
         */
        protected final short rangeCheckShort(int value) {
            int shortValue = value + Short.MIN_VALUE;
            if (shortValue < Short.MIN_VALUE) {
                shortValue = Short.MIN_VALUE;
                minPixelValue = Short.MIN_VALUE;
            } else if (shortValue > Short.MAX_VALUE) {
                shortValue = Short.MAX_VALUE;
                maxPixelValue = Short.MAX_VALUE;
            } else if (shortValue > maxPixelValue) {
                maxPixelValue = shortValue;
            } else if (shortValue < minPixelValue) {
                minPixelValue = shortValue;
            }
            return (short) shortValue;
        }

        /**
         * check the max pixel value, an record the new max and min value.
         * assume that bytes are unsigned, so no range adaption
         * 
         * @param value
         *            the value to check
         * @return the unchanged original value
         */
        protected final byte rangeCheckByte(int value) {
            value = Math.min(value, MAX_BYTE_VALUE);
            maxPixelValue = Math.max(maxPixelValue, value);
            minPixelValue = Math.min(minPixelValue, value);
            return (byte) value;
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
    public static class INDI8BitCCDImage extends INDICCDImage {

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
         * @param type
         *            type of the image.
         */
        public INDI8BitCCDImage(int width, int height, ImageType type) {
            super(width, height, MAX_BPP, type);
        }

        /**
         * the image data.
         */
        protected byte[] imageData;

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
                    imageData[index++] = rangeCheckByte(value);
                }

                @Override
                public void setPixel(int red, int green, int blue) {
                    imageData[index++] = rangeCheckByte(red);
                    imageData[indexLayer2++] = rangeCheckByte(green);
                    imageData[indexLayer3++] = rangeCheckByte(blue);
                }

            };
        }
    }

    /**
     * If the image has more than 8 but less or equal 16 bit per pixel the data
     * fits in a short array.
     */
    private static class INDI16BitCCDImage extends INDICCDImage {

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
         * @param type
         *            the type of the image.
         */
        public INDI16BitCCDImage(int width, int height, ImageType type) {
            super(width, height, MAX_BPP, type);
        }

        /**
         * the image data.
         */
        protected short[] imageData;

        @Override
        Object getImageData() {
            return imageData;
        }

        @Override
        public PixelIterator iteratePixel() {
            imageData = new short[width * height * type.axis3];
            return new PixelIterator(width, height) {

                @Override
                public void setPixel(int value) {
                    imageData[index++] = rangeCheckShort(value);
                }

                @Override
                public void setPixel(int red, int green, int blue) {
                    imageData[index++] = rangeCheckShort(red);
                    imageData[indexLayer2++] = rangeCheckShort(green);
                    imageData[indexLayer3++] = rangeCheckShort(blue);
                }

            };
        }
    }

    /**
     * If the image has more than 16 but less or equal 32 bit per pixel the data
     * fits in a int array.
     */
    private static class INDI32BitCCDImage extends INDICCDImage {

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
         * @param type
         *            type of the image.
         */
        public INDI32BitCCDImage(int width, int height, ImageType type) {
            super(width, height, MAX_BPP, type);
        }

        /**
         * the image data.
         */
        protected int[] imageData;

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
                    imageData[index++] = rangeCheck(value);
                }

                @Override
                public void setPixel(int red, int green, int blue) {
                    imageData[index++] = rangeCheck(red);
                    imageData[indexLayer2++] = rangeCheck(green);
                    imageData[indexLayer3++] = rangeCheck(blue);
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
     * the maximum value of a pixel.
     */
    private float maxPixelValue = Float.MIN_VALUE;

    /**
     * the maximum value of a pixel.
     */
    private float minPixelValue = Float.MAX_VALUE;

    /**
     * extra fits headers to include.
     */
    private Map<String, Object> extraFitsHeaders;

    /**
     * convert the current imageData to a fits image.
     * 
     * @throws FitsException
     *             if the image could not be converted
     */
    private void convertToFits() throws FitsException {
        f = new Fits();
        BasicHDU<?> imageFits = FitsFactory.HDUFactory(getImageData());
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
    private void addFitsAttributes(BasicHDU<?> imageFits) throws HeaderCardException {
        imageFits.addValue(HISTORY, "FITS image created by i4j");
        imageFits.addValue(SIMPLE, "T");
        imageFits.addValue(BITPIX, bpp);
        imageFits.addValue(NAXIS, type == ImageType.COLOR ? COLOR_SCALE_NAXIS : GRAY_SCALE_NAXIS);
        imageFits.addValue(NAXISn.n(1), height);
        imageFits.addValue(NAXISn.n(2), width);
        if (type == ImageType.COLOR) {
            imageFits.addValue(NAXISn.n(3), COLOR_AXIS3);
        }
        if (maxPixelValue > minPixelValue) {
            imageFits.addValue(DATAMAX, maxPixelValue);
            imageFits.addValue(DATAMIN, minPixelValue);
        }
        if (extraFitsHeaders != null) {
            for (Entry<String, Object> header : extraFitsHeaders.entrySet()) {
                if (header.getValue() instanceof Double) {
                    imageFits.addValue(header.getKey(), (Double) header.getValue(), "");
                } else if (header.getValue() instanceof Boolean) {
                    imageFits.addValue(header.getKey(), (Boolean) header.getValue(), "");
                } else if (header.getValue() instanceof Integer) {
                    imageFits.addValue(header.getKey(), (Integer) header.getValue(), "");
                } else if (header.getValue() != null) {
                    String stringValue = header.getValue().toString();
                    if (stringValue.length() > MAX_FITS_HEADERCARD_VALUE_LENGTH) {
                        imageFits.addValue(header.getKey(), stringValue.substring(0, MAX_FITS_HEADERCARD_VALUE_LENGTH), stringValue);
                    } else {
                        imageFits.addValue(header.getKey(), stringValue, "");
                    }
                }
            }
        }
    }

    /**
     * @return an initalized map of values for fits headers. please only use
     *         this for image headers directly from the chip.
     */
    public Map<String, Object> getExtraFitsHeaders() {
        if (extraFitsHeaders == null) {
            extraFitsHeaders = new HashMap<>();
        }
        return extraFitsHeaders;
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
            return new INDI8BitCCDImage(width, height, type);
        } else if (bpp <= INDI16BitCCDImage.MAX_BPP) {
            return new INDI16BitCCDImage(width, height, type);
        } else if (bpp <= INDI32BitCCDImage.MAX_BPP) {
            return new INDI32BitCCDImage(width, height, type);
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
     * @param subWidth
     *            width in pixel
     * @param subHeigth
     *            height in pixel
     * @param extension
     *            the file extension (currently only fits allowed.
     * @throws FitsException
     *             if the file could not be written.
     */
    public void write(DataOutputStream os, int left, int top, int subWidth, int subHeigth, String extension) throws FitsException {
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

    /**
     * Iterator reached its end, we can take some statistics.
     * 
     * @param iterator
     *            the iterator.
     */
    public void iteratorComplete(PixelIterator iterator) {
        maxPixelValue = iterator.maxPixelValue;
        minPixelValue = iterator.minPixelValue;
    }
}
