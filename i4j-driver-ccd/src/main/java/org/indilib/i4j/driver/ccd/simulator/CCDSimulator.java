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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class CCDSimulator extends INDICCDDriver {

    /**
     * The logger to log the messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CCDSimulator.class);

    class Camera implements Runnable {

        double exposure = Double.NaN;

        Random random = new Random(System.currentTimeMillis());

        boolean stop = false;

        double temperature = 20.0d;

        double theTargetTemperature = Double.NaN;

        int heigth;

        int width;

        BufferedImage stdImage;

        public Camera() {
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
                    Thread.sleep(100L);
                    if (connectionExtension.isConnected()) {
                        updateTemperature(100L, count);
                        updateExposure(100L, count);
                        count = (count + 1) % 10;
                    }
                } catch (InterruptedException e) {
                    LOG.error("camera thread interrrupted", e);
                }

            }
        }

        private void updateExposure(long sleeptime, int count) {
            if (!Double.isNaN(exposure)) {
                exposure = exposure - sleeptime;
            }
            if (count == 0) {
                primaryCCD.setExposureLeft(Math.max(0, exposure) / 1000d);
            }
            if (exposure < 0) {
                exposure = Double.NaN;
                INDICCDImage newCcdImage = INDICCDImage.createImage(width, heigth, 8, ImageType.COLOR);
                Raster stdData = stdImage.getData();
                int[] pixel = new int[3];
                PixelIterator pixelIter = newCcdImage.iteratePixel();
                for (int y = 0; y < heigth; y++) {
                    for (int x = 0; x < width; x++) {
                        stdData.getPixel(x, y, pixel);
                        pixelIter.setPixel(pixel[0], pixel[1], pixel[2]);
                    }
                }
                primaryCCD.setFrameBuffer(newCcdImage);
                exposureComplete(primaryCCD);
            }
        }

        protected void updateTemperature(long sleeptime, int count) {
            double tempDiff = (random.nextDouble() - 0.5d) / 20d;
            if (!Double.isNaN(theTargetTemperature)) {
                if (theTargetTemperature > temperature) {
                    tempDiff += 0.1d;
                } else {
                    tempDiff -= 0.1d;
                }
            }
            temperature += tempDiff;
            if (count == 0) {
                CCDSimulator.this.temperatureTemp.setValue(temperature);
                updateProperty(CCDSimulator.this.temperature);
            }
        }
    }

    Camera camera = new Camera();

    public CCDSimulator(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        new Thread(camera, "camera").start();
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
        camera.exposure = duration * 1000d;
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
        return false;
    }

    @Override
    public void addFITSKeywords(BasicHDU fitsHeader) {

    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}
