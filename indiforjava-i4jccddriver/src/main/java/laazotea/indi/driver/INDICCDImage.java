package laazotea.indi.driver;

import java.io.DataOutputStream;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

public abstract class INDICCDImage {

    private int width;

    private int height;

    private Fits f;

    public class INDI8BitCCDImage extends INDICCDImage {

        private byte[][] imageData;

        @Override
        public int[][] subImageInt(int minx, int miny, int maxx, int maxy) {
            int[][] subImage = new int[maxx - maxx][maxy - maxy];
            int subY = 0;
            for (int y = miny; y <= maxy; y++) {
                int subX = 0;
                for (int x = minx; x <= maxx; x++) {
                    subImage[subX][subY] = imageData[x][y] & 0xFF;
                    subX++;
                }
                subY++;
            }
            return subImage;
        }

        @Override
        public void drawMarker(int ix, int iy) {
            int xmin = Math.max(ix - 10, 0);
            int xmax = Math.min(ix + 10, width - 1);
            int ymin = Math.max(iy - 10, 0);
            int ymax = Math.min(iy + 10, height - 1);

            // fprintf(stderr, "%d %d %d %d\n", xmin, xmax, ymin, ymax);

            if (ymin > 0) {
                for (int x = xmin; x <= xmax; x++) {
                    imageData[x][ymin] = (byte) 200;
                }
            }

            if (xmin > 0) {
                for (int y = ymin; y <= ymax; y++) {
                    imageData[xmin][y] = (byte) 200;
                }
            }

            if (xmax < width - 1) {
                for (int y = ymin; y <= ymax; y++) {
                    imageData[xmax][y] = (byte) 200;
                }
            }

            if (ymax < height - 1) {
                for (int x = xmin; x <= xmax; x++)
                    imageData[x][xmax] = (byte) 200;
            }
        }
    }

    public class INDI16BitCCDImage extends INDICCDImage {

        private short[][] imageData;

        @Override
        public int[][] subImageInt(int minx, int miny, int maxx, int maxy) {
            int[][] subImage = new int[maxx - maxx][maxy - maxy];
            int subY = 0;
            for (int y = miny; y <= maxy; y++) {
                int subX = 0;
                for (int x = minx; x <= maxx; x++) {
                    subImage[subX][subY] = imageData[x][y] & 0xFFFF;
                    subX++;
                }
                subY++;
            }
            return subImage;
        }

        @Override
        public void drawMarker(int ix, int iy) {
            int xmin = Math.max(ix - 10, 0);
            int xmax = Math.min(ix + 10, width - 1);
            int ymin = Math.max(iy - 10, 0);
            int ymax = Math.min(iy + 10, height - 1);

            // fprintf(stderr, "%d %d %d %d\n", xmin, xmax, ymin, ymax);

            if (ymin > 0) {
                for (int x = xmin; x <= xmax; x++) {
                    imageData[x][ymin] = (short) 50000;
                }
            }

            if (xmin > 0) {
                for (int y = ymin; y <= ymax; y++) {
                    imageData[xmin][y] = (short) 50000;
                }
            }

            if (xmax < width - 1) {
                for (int y = ymin; y <= ymax; y++) {
                    imageData[xmax][y] = (short) 50000;
                }
            }

            if (ymax < height - 1) {
                for (int x = xmin; x <= xmax; x++)
                    imageData[x][xmax] = (short) 50000;
            }
        }
    }

    public class INDI32BitCCDImage extends INDICCDImage {

        private int[][] imageData;

        @Override
        public int[][] subImageInt(int minx, int miny, int maxx, int maxy) {
            // here we probalby must shift bits to enable unsigned int's
            throw new UnsupportedOperationException("not yet implemented");
        }

        @Override
        public void drawMarker(int ix, int iy) {
            // here we probalby must shift bits to enable unsigned int's
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    public abstract int[][] subImageInt(int minx, int miny, int maxx, int maxy);

    public abstract void drawMarker(int ix, int iy);

    public Fits asFitsImage() {
        if (f == null) {
            convertToFits();
        }
        return f;
    }

    private void convertToFits() {

    }

    public void write(DataOutputStream os,String extention) throws FitsException {
        if ("fits".equals(extention)) {
            asFitsImage().write(os);
        }else {
            //todo
        }
    }
}
