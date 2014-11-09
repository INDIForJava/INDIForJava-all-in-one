package org.indilib.i4j.driver.ccd.simulator;

/*
 * #%L
 * INDI for Java Abstract CCD Driver
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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import nom.tam.fits.BasicHDU;

import org.indilib.i4j.driver.ccd.Capability;
import org.indilib.i4j.driver.ccd.CcdFrame;
import org.indilib.i4j.driver.ccd.INDICCDDriver;
import org.indilib.i4j.driver.ccd.INDICCDImage;
import org.indilib.i4j.driver.ccd.INDICCDImage.ImageType;
import org.indilib.i4j.driver.ccd.INDICCDImage.PixelIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This driver simulates a ccd dirver by sending always sending the same pictute
 * when an exposure is ready.
 * 
 * @author Richard van Nieuwenhoven
 */
public class CCDSimulator extends INDICCDDriver {

    /**
     * The logger to log the messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CCDSimulator.class);

    /**
     * the pixel size of the simulated pixels.
     */
    private static final int SIMULATED_PIXEL_SIZE = 20;

    /**
     * the number of bits per pixel color.
     */
    private static final int BITS_PER_PIXEL_COLOR = 8;

    /**
     * the number of milliseconds per second.
     */
    private static final double MILLISECONDS_PER_SECOND = 1000d;

    /**
     * The simulated camera. running as a thread- runnable.
     */
    final class Camera implements Runnable {

        /**
         * the random part is 1/20 of a degree.
         */
        private static final double TEMPERATURE_RANDOM_PART = 20d;

        /**
         * the random value is between 0 and 1. shift it so that it is between
         * -0.5 and +0.5.
         */
        private static final double RANDOM_VALUE_MOVE = 0.5d;

        /**
         * with every loop the temperature will com this step closer to the
         * target value.
         */
        private static final double TEMPERATURE_STEPPING = 0.1d;

        /**
         * the maximum value of a byte.
         */
        private static final int MAXIMUM_BYTE_VALUE = 255;

        /**
         * how many values are there per color.
         */
        private static final int VALUES_PER_COLOR = 3;

        /**
         * update the properties not in every loop but in every x'th interation.
         */
        private static final int PROPERTY_UPDATE_EVERY_X_LOOPS = 10;

        /**
         * sleep time for 1/10 of a second.
         */
        private static final long ONE_TENTH_OF_A_SECOND = 100L;

        /**
         * as soon as this value is a real number the count down starts.
         */
        private double exposure = Double.NaN;

        /**
         * random seed to give some reality.
         */
        private Random random = new Random(System.currentTimeMillis());

        /**
         * stop the camera.
         */
        private boolean stop = false;

        /**
         * the current (simulated) temperature.
         */
        private double temperature = 0.0d;

        /**
         * the target temperature to reach.
         */
        private double theTargetTemperature = Double.NaN;

        /**
         * the camera vertical resolution.
         */
        private final int heigth;

        /**
         * the camera horizontal resolution.
         */
        private final int width;

        /**
         * the image to send.
         */
        private BufferedImage stdImage;

        /**
         * constructor of the camera.
         */
        private Camera() {
            try {
                stdImage = ImageIO.read(CCDSimulator.class.getResourceAsStream("default.jpg"));
                heigth = stdImage.getHeight();
                width = stdImage.getWidth();
            } catch (IOException e) {
                throw new IllegalStateException("This driver can only function with its default image", e);
            }
        }

        @Override
        public void run() {
            int count = 0;
            while (!stop) {
                try {
                    Thread.sleep(ONE_TENTH_OF_A_SECOND);
                    if (connectionExtension.isConnected()) {
                        updateTemperature(count == 0);
                        updateExposure(ONE_TENTH_OF_A_SECOND, count == 0);
                        count = (count + 1) % PROPERTY_UPDATE_EVERY_X_LOOPS;
                    }
                } catch (InterruptedException e) {
                    LOG.error("camera thread interrrupted", e);
                }

            }
        }

        /**
         * update the exposure left value. and send the image if the rest
         * reaches 0.
         * 
         * @param sleeptime
         *            the sleep time per loop
         * @param updateProperty
         *            send the property values to the client.
         */
        private void updateExposure(long sleeptime, boolean updateProperty) {
            if (!Double.isNaN(exposure)) {
                exposure = exposure - sleeptime;
            }
            if (updateProperty) {
                primaryCCD.setExposureLeft(Math.max(0, exposure) / MILLISECONDS_PER_SECOND);
            }
            if (exposure < 0) {
                sendImage();
            }
        }

        /**
         * send the image to the client.
         */
        private void sendImage() {
            exposure = Double.NaN;
            INDICCDImage newCcdImage = INDICCDImage.createImage(width, heigth, BITS_PER_PIXEL_COLOR, ImageType.COLOR);
            Raster stdData = stdImage.getData();
            int[] pixel = new int[VALUES_PER_COLOR];
            PixelIterator pixelIter = newCcdImage.iteratePixel();

            for (int y = 0; y < heigth; y++) {
                for (int x = 0; x < width; x++) {
                    if (primaryCCD.getCurrentFrameType() == CcdFrame.FLAT_FRAME) {
                        stdData.getPixel(x, y, pixel);
                        pixelIter.setPixel(MAXIMUM_BYTE_VALUE, MAXIMUM_BYTE_VALUE, MAXIMUM_BYTE_VALUE);
                    } else if (primaryCCD.getCurrentFrameType() == CcdFrame.DARK_FRAME || primaryCCD.getCurrentFrameType() == CcdFrame.BIAS_FRAME) {
                        stdData.getPixel(x, y, pixel);
                        pixelIter.setPixel(0, 0, 0);
                    } else {
                        stdData.getPixel(x, y, pixel);
                        pixelIter.setPixel(pixel[0], pixel[1], pixel[2]);
                    }
                }
            }
            primaryCCD.setFrameBuffer(newCcdImage);
            exposureComplete(primaryCCD);
        }

        /**
         * update the temperature field.
         * 
         * @param updateProperty
         *            should the client property be updated?
         */
        protected void updateTemperature(boolean updateProperty) {
            double tempDiff = (random.nextDouble() - RANDOM_VALUE_MOVE) / TEMPERATURE_RANDOM_PART;
            if (!Double.isNaN(theTargetTemperature)) {
                if (theTargetTemperature > temperature) {
                    tempDiff += TEMPERATURE_STEPPING;
                } else {
                    tempDiff -= TEMPERATURE_STEPPING;
                }
            }
            temperature += tempDiff;
            if (updateProperty) {
                CCDSimulator.this.temperatureTemp.setValue(temperature);
                updateProperty(CCDSimulator.this.temperature);
            }
        }
    }

    /**
     * the simulated camera.
     */
    private final Camera camera = new Camera();

    /**
     * standard constructor for the simulated ccd driver.
     * 
     * @param inputStream
     *            The stream from which to read messages.
     * @param outputStream
     *            The stream to which to write the messages.
     */
    public CCDSimulator(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        new Thread(camera, "camera").start();
        primaryCCD.setCCDParams(camera.width, camera.heigth, BITS_PER_PIXEL_COLOR, SIMULATED_PIXEL_SIZE, SIMULATED_PIXEL_SIZE);
    }

    @Override
    protected Capability defineCapabilities() {
        Capability capabilities = new Capability();
        capabilities.canAbort(true);
        capabilities.canBin(true);
        capabilities.canSubFrame(true);
        capabilities.hasCooler(true);
        capabilities.hasGuideHead(false);
        capabilities.hasShutter(true);
        return capabilities;
    }

    @Override
    protected Boolean setTemperature(double theTargetTemperature) {
        camera.theTargetTemperature = theTargetTemperature;
        return true;
    }

    @Override
    public boolean abortExposure() {
        return false;
    }

    @Override
    public boolean startExposure(double duration) {
        camera.exposure = duration * MILLISECONDS_PER_SECOND;
        return true;
    }

    @Override
    public boolean updateCCDBin(int hor, int ver) {
        return false;
    }

    @Override
    public boolean updateCCDFrame(int x, int y, int w, int h) {
        return false;
    }

    @Override
    public boolean updateCCDFrameType(CcdFrame fType) {
        return true;
    }

    @Override
    public Map<String, Object> getExtraFITSKeywords(BasicHDU fitsHeader) {
        return null;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}
