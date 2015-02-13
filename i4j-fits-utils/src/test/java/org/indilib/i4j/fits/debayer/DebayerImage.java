package org.indilib.i4j.fits.debayer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Standard;
import nom.tam.fits.header.extra.MaxImDLExt;
import nom.tam.util.Cursor;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

/**
 * This class is a refactored class from a JImage plugin. originating:
 * http://www.umanitoba.ca/faculties/science/astronomy/jwest/plugins.html all
 * credit for the debayering algorithms go there.
 * 
 * @author Richard van Nieuwnehoven
 */
public class DebayerImage {

    public static void main(String[] args) throws FileNotFoundException, FitsException, IOException {
        Fits result = new DebayerImage().decode(new FileInputStream("src/test/resources/raspberry2.fits"), new AverageDebayerAlgorithm());

        DataOutputStream os = new DataOutputStream(new FileOutputStream("target/result1.fits"));
        result.write(os);
        os.close();

        result = new DebayerImage().decode(new FileInputStream("src/test/resources/raspberry2.fits"), new AdaptiveDebayerAlgorithm());

        os = new DataOutputStream(new FileOutputStream("target/result2.fits"));
        result.write(os);
        os.close();

        result = new DebayerImage().decode(new FileInputStream("src/test/resources/raspberry2.fits"), new ReplicateDebayerAlgorithm());

        os = new DataOutputStream(new FileOutputStream("target/result3.fits"));
        result.write(os);
        os.close();

        result = new DebayerImage().decode(new FileInputStream("src/test/resources/raspberry2.fits"), new SmoothDebayerAlgorithm());

        os = new DataOutputStream(new FileOutputStream("target/result4.fits"));
        result.write(os);
        os.close();
    }

    public Fits decode(InputStream in, DebayerAlgorithm algorithm) throws FitsException, IOException {

        Fits fitsImage = new Fits(in);
        fitsImage.read();
        Fits colorImage = new Fits();

        int count = fitsImage.getNumberOfHDUs();
        for (int index = 0; index < count; index++) {
            BasicHDU oneImage = fitsImage.getHDU(index);
            String bayerpat = oneImage.getHeader().getStringValue(MaxImDLExt.BAYERPAT);
            int[] axis = oneImage.getAxes();
            if (axis.length == 2 && bayerpat != null && !bayerpat.trim().isEmpty()) {
                // DebayerRowOrder row_order =
                // DebayerRowOrder.valueOfFits(bayerpat);
                DebayerPattern row_order = DebayerPattern.GRBG;

                ImagePixels ip = new ImagePixels(axis[1], axis[0]);
                ip.setPixel(oneImage.getKernel());
                RGBImagePixels result = algorithm.decode(row_order, ip);

                ImageIO.write(result.asImage(), "png", new File("target/result.png"));

                BasicHDU colorHDU = FitsFactory.HDUFactory(result.getColors(oneImage.getBitPix()));
                colorImage.addHDU(colorHDU);
                colorHDU.getHeader().setNaxes(3);
                colorHDU.getHeader().setNaxis(3, 3);
                Cursor iter = oneImage.getHeader().iterator();
                while (iter.hasNext()) {
                    HeaderCard headerCard = (HeaderCard) iter.next();
                    if (headerCard.getKey().equals(MaxImDLExt.BAYERPAT) || headerCard.getKey().equals(Standard.NAXIS)) {
                        // ignore
                    } else {
                        colorHDU.getHeader().addLine(headerCard);
                    }
                }
            }
        }

        return colorImage;

    }
}
