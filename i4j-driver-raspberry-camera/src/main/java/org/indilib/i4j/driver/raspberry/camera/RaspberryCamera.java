package org.indilib.i4j.driver.raspberry.camera;

/*
 * #%L
 * INDI for Java Driver for the Raspberry pi camera
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import nom.tam.fits.BasicHDU;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.ccd.Capability;
import org.indilib.i4j.driver.ccd.CcdFrame;
import org.indilib.i4j.driver.ccd.INDICCDDriver;
import org.indilib.i4j.driver.ccd.INDICCDImage;
import org.indilib.i4j.driver.ccd.INDICCDImage.ImageType;
import org.indilib.i4j.driver.ccd.INDICCDImage.PixelIterator;
import org.indilib.i4j.driver.event.NumberEvent;
import org.indilib.i4j.driver.raspberry.camera.image.RawImage;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaspberryCamera extends INDICCDDriver {

    /**
     * The logger to log the messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RaspberryCamera.class);

    /**
     * The temperature of the ccd chip.
     */
    @InjectProperty(name = "CAMERA_OPTIONS", label = "options", group = INDIDriver.GROUP_OPTIONS)
    protected INDINumberProperty cameraOptions;

    /**
     * The sharpness setting..
     * 
     * @see CameraOption#sharpness
     */
    @InjectElement(name = "SHARPNESS", label = "Sharpness", minimum = -100d, maximum = 100d, numberFormat = "%3.0f")
    protected INDINumberElement sharpness;

    /**
     * The contrast setting..
     * 
     * @see CameraOption#contrast
     */
    @InjectElement(name = "CONTRAST", label = "Contrast", minimum = -100d, maximum = 100d, numberFormat = "%3.0f")
    protected INDINumberElement contrast;

    /**
     * The brightness setting..
     * 
     * @see CameraOption#brightness
     */
    @InjectElement(name = "BRIGHTNESS", label = "Brightness", minimum = -100d, maximum = 100d, numberFormat = "%3.0f")
    protected INDINumberElement brightness;

    /**
     * The saturation setting..
     * 
     * @see CameraOption#saturation
     */
    @InjectElement(name = "SATURATION", label = "Saturation", minimum = -100d, maximum = 100d, numberFormat = "%3.0f")
    protected INDINumberElement saturation;

    /**
     * The iso setting..
     * 
     * @see CameraOption#ISO
     */
    @InjectElement(name = "ISO", label = "ISO", minimum = 0d, maximum = 10000d, numberFormat = "%4.0f", numberValue = 800)
    protected INDINumberElement iso;

    /**
     * The awbgains setting..
     * 
     * @see CameraOption#awbgains
     */
    @InjectElement(name = "AWBGAINS", label = "AWB Gains", minimum = 0d, maximum = 10000d, numberFormat = "%4.2f", numberValue = 1.1)
    protected INDINumberElement awbgains;

    /**
     * The number of images to take per exposure.
     */
    @InjectProperty(name = "LOOP_COUNT", label = "Number of captures of one exposure", group = INDIDriver.GROUP_OPTIONS)
    protected INDINumberProperty loopCountP;

    /**
     * The saturation setting..
     * 
     * @see CameraOption#saturation
     */
    @InjectElement(name = "LOOP_COUNT_ELEMENT", label = "count", minimum = 1d, maximum = 1000d, numberFormat = "%3.0f", numberValue = 1d)
    protected INDINumberElement loopCount;

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        super.driverConnect(timestamp);
        addProperty(cameraOptions);
        addProperty(loopCountP);
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        super.driverDisconnect(timestamp);
        removeProperty(cameraOptions);
        removeProperty(loopCountP);
    }

    private CameraControl control;

    public RaspberryCamera(INDIConnection connection) {
        super(connection);
        primaryCCD.setCCDParams(2592, 1944, 10, 1.4f, 1.4f);
        cameraOptions.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                property.setValues(elementsAndValues);
                property.setState(PropertyStates.OK);
                updateProperty(property);
            }
        });
    }

    @Override
    protected Capability defineCapabilities() {
        return new Capability().canAbort(true)//
                .canBin(false)//
                .canSubFrame(false)//
                .hasCooler(false)//
                .hasGuideHead(false)//
                .hasShutter(false);
    }

    @Override
    public boolean abortExposure() {
        // should never be called because capability deactivated.
        return false;
    }

    @Override
    protected Boolean setTemperature(double theTargetTemperature) {
        // should never be called because capability deactivated.
        return null;
    }

    @Override
    public boolean updateCCDBin(int binX, int binY) {
        // should never be called because capability deactivated.
        return false;
    }

    @Override
    public boolean updateCCDFrame(int x, int y, int width, int height) {
        // should never be called because capability deactivated.
        return false;
    }

    @Override
    public Map<String, Object> getExtraFITSKeywords(BasicHDU fitsHeader) {
        return null;
    }

    @Override
    public String getName() {
        return RaspberryCamera.class.getSimpleName();
    }

    private double originalLoopCount;

    @Override
    public boolean startExposure(double duration) {
        originalLoopCount = loopCount.getValue();
        if (control == null) {
            control = new CameraControl() {

                @Override
                protected synchronized void imageCaptured(RawImage capturedImage) {
                    if (loopCount.getIntValue() == 1) {
                        stop();
                        control = null;
                        loopCount.setValue(originalLoopCount);
                        primaryCCD.setAutoLoop(true);
                    } else {
                        loopCount.setValue(loopCount.getValue() - 1d);
                        primaryCCD.setAutoLoop(false);
                    }
                    updateProperty(loopCountP);
                    primaryCCD.setFrameBuffer(convertRawImageToINDIImage(capturedImage));
                    exposureComplete(primaryCCD);
                }

            };
            control.start();
            try {
                control.capture(duration);
            } catch (Exception e) {
                LOG.error("could not start exposure", e);
                return false;
            }
        }
        return true;
    }

    protected INDICCDImage convertRawImageToINDIImage(RawImage capturedImage) {
        INDICCDImage newCcdImage = INDICCDImage.createImage(CameraConstands.VPIXELS, CameraConstands.HPIXELS, 10, ImageType.RAW);
        try {
            RowBuffer10Bit row = new RowBuffer10Bit();
            PixelIterator iterator = newCcdImage.iteratePixel();
            ByteArrayInputStream inData = new ByteArrayInputStream(capturedImage.getRawData());
            for (int y = 0; y < CameraConstands.VPIXELS; y++) {
                row.read(inData);
                row.writeTo(iterator);
            }
            newCcdImage.iteratorComplete(iterator);
            newCcdImage.getExtraFitsHeaders().putAll(capturedImage.getFitsAttributes());

        } catch (IOException e) {
            LOG.error("image incomplete because of exception", e);
        }
        return newCcdImage;
    };

    @Override
    public boolean updateCCDFrameType(CcdFrame frameType) {
        // ready for all frame types
        return true;
    }
}
