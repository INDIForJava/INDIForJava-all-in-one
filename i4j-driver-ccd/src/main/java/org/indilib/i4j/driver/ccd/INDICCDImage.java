package org.indilib.i4j.driver.ccd;

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
