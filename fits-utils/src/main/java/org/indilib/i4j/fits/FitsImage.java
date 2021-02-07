package org.indilib.i4j.fits;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.header.Standard;
import nom.tam.fits.header.extra.MaxImDLExt;
import org.indilib.i4j.fits.debayer.AdaptiveDebayerAlgorithm;
import org.indilib.i4j.fits.debayer.DebayerPattern;
import org.indilib.i4j.fits.debayer.ImagePixels;
import org.indilib.i4j.fits.debayer.RGBImagePixels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Array;

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
 * Utility functions for fits images.
 *
 * @author Richard van Nieuwenhoven
 */
public final class FitsImage {

    /**
     * A logger for the errors.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FitsImage.class);

    /**
     * utility class.
     */
    private FitsImage() {

    }

    /**
     * convert the bytes to a fits image.
     *
     * @param bytes the bytes to convert
     * @return the fits image or null if the image could not be read or the
     * format is not correct.
     */
    public static Fits asImage(byte[] bytes) {
        try {
            Fits fitsImage = new Fits(new ByteArrayInputStream(bytes));
            fitsImage.read();
            return fitsImage;
        } catch (Exception e) {
            LOG.error("could not convert bytes to fits image", e);
            return null;
        }
    }

    /**
     * convert a fits image to a java displayable image, debayer it if
     * Necessary.
     *
     * @param fitsImage the image to convert
     * @return the java buffered image
     */
    public static BufferedImage asJavaImage(Fits fitsImage) {
        try {
            BasicHDU<?> oneImage = fitsImage.getHDU(0);
            String bayerpat = oneImage.getHeader().getStringValue(MaxImDLExt.BAYERPAT);
            int height = oneImage.getHeader().getIntValue(Standard.NAXISn.n(1));
            int width = oneImage.getHeader().getIntValue(Standard.NAXISn.n(2));
            int[] axis = oneImage.getAxes();
            RGBImagePixels result;
            if (axis.length == 2 && bayerpat != null && !bayerpat.trim().isEmpty()) {
                // RAW image
                ImagePixels ip = new ImagePixels(width, height);
                ip.setPixel(oneImage.getKernel());
                result = new AdaptiveDebayerAlgorithm().decode(DebayerPattern.valueOf(bayerpat), ip);
            } else if (axis.length == 2) {
                // GRAY Image
                result = new RGBImagePixels(width, height);
                result.getGreen().setPixel(oneImage.getKernel());
                result.setBlue(result.getGreen());
                result.setRed(result.getGreen());
            } else {
                // COLOR Image
                result = new RGBImagePixels(width, height);
                Object red = Array.get(oneImage.getKernel(), 0);
                result.getRed().setPixel(red);
                Object green = Array.get(oneImage.getKernel(), 1);
                result.getGreen().setPixel(green);
                Object blue = Array.get(oneImage.getKernel(), 2);
                result.getBlue().setPixel(blue);
            }
            return result.asImage();
        } catch (Exception e) {
            LOG.error("could not fits image to java image", e);
            return null;
        }
    }
}