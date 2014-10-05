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

import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

public abstract class INDICCDImage {

    public class INDI16BitCCDImage extends INDICCDImage {

        private short[][] imageData;
    }

    public class INDI32BitCCDImage extends INDICCDImage {

        private int[][] imageData;

    }

    public class INDI8BitCCDImage extends INDICCDImage {

        private byte[][] imageData;

    }

    private int width;

    private int height;

    private Fits f;

    private void convertToFits() {

    }

    public Fits asFitsImage() {
        if (f == null) {
            convertToFits();
        }
        return f;
    }

    public void write(DataOutputStream os, int left, int top, int width, int heigth, String extention) throws FitsException {
        if ("fits".equals(extention)) {
            asFitsImage().write(os);
        } else {
            // todo
        }
    }
}
