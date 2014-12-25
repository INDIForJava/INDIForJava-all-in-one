package org.indilib.i4j.fits;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Array;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;

import org.indilib.i4j.fits.debayer.AdaptiveDebayerAlgorithm;
import org.indilib.i4j.fits.debayer.DebayerRowOrder;
import org.indilib.i4j.fits.debayer.ImagePixels;
import org.indilib.i4j.fits.debayer.RGBImagePixels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class FitsImage {

    /**
     * A logger for the errors.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FitsImage.class);

    /**
     * convert the bytes to a fits image.
     * 
     * @param bytes
     *            the bytes to convert
     * @return the fits image or null if the image could not be read or the
     *         format is not correct.
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
     * nessesary.
     * 
     * @param fitsImage
     *            the image to convert
     * @return the java buffered image
     */
    public static BufferedImage asJavaImage(Fits fitsImage) {
        try {
            BasicHDU oneImage = fitsImage.getHDU(0);
            String bayerpat = oneImage.getHeader().getStringValue(StandardFitsHeader.BAYERPAT);
            int[] axis = oneImage.getAxes();
            RGBImagePixels result;
            double datamax = oneImage.getHeader().getDoubleValue(StandardFitsHeader.DATAMAX);
            if (axis.length == 2 && bayerpat != null && !bayerpat.trim().isEmpty()) {
                // RAW image
                ImagePixels ip = new ImagePixels(axis[1], axis[0]);
                ip.setPixel(oneImage.getKernel(), datamax);
                result = new AdaptiveDebayerAlgorithm().decode(DebayerRowOrder.valueOf(bayerpat), ip);
            } else if (axis.length == 2) {
                // GRAY Image
                ImagePixels ip = new ImagePixels(axis[1], axis[0]);
                ip.setPixel(oneImage.getKernel(), datamax);
                result = new RGBImagePixels();
                result.setBlue(ip);
                result.setGreen(ip);
                result.setRed(ip);
            } else {
                // COLOR Image
                result = new RGBImagePixels();
                Object red = Array.get(oneImage.getKernel(), 0);
                ImagePixels ip = new ImagePixels(axis[1], axis[0]);
                ip.setPixel(red, datamax);
                result.setRed(ip);
                Object green = Array.get(oneImage.getKernel(), 1);
                ip = new ImagePixels(axis[1], axis[0]);
                ip.setPixel(green, datamax);
                result.setGreen(ip);
                Object blue = Array.get(oneImage.getKernel(), 2);
                ip = new ImagePixels(axis[1], axis[0]);
                ip.setPixel(blue, datamax);
                result.setBlue(ip);
            }
            return result.asImage();
        } catch (Exception e) {
            LOG.error("could not fits image to java image", e);
            return null;
        }
    }

}
