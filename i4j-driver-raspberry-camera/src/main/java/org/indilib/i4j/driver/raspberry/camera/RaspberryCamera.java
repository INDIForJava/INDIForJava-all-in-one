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
import java.util.HashMap;
import java.util.Map;

import nom.tam.fits.BasicHDU;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
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
import org.indilib.i4j.driver.raspberry.camera.image.RowBuffer10Bit;
import org.indilib.i4j.fits.StandardFitsHeader;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the raspberry pi camera driver, it takes onyl raw images that must be
 * debayered. Exposures up to 6 seconds are possible. Change the other options
 * only when you know what you are doing.
 * 
 * @author Richard van Nieuwenhoven
 */
public class RaspberryCamera extends INDICCDDriver {

    /**
     * The logger to log the messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RaspberryCamera.class);

    /**
     * The temperature of the ccd chip.
     */
    @InjectProperty(name = "CAMERA_OPTIONS", label = "options", group = INDIDriver.GROUP_OPTIONS)
    protected INDITextProperty cameraOptions;

    /**
     * The sharpness setting..
     * 
     * @see CameraOption#sharpness
     */
    @InjectElement(name = "SHARPNESS", label = "Sharpness")
    protected INDITextElement sharpness;

    /**
     * The contrast setting..
     * 
     * @see CameraOption#contrast
     */
    @InjectElement(name = "CONTRAST", label = "Contrast")
    protected INDITextElement contrast;

    /**
     * The brightness setting..
     * 
     * @see CameraOption#brightness
     */
    @InjectElement(name = "BRIGHTNESS", label = "Brightness")
    protected INDITextElement brightness;

    /**
     * The saturation setting..
     * 
     * @see CameraOption#saturation
     */
    @InjectElement(name = "SATURATION", label = "Saturation")
    protected INDITextElement saturation;

    /**
     * The iso setting..
     * 
     * @see CameraOption#ISO
     */
    @InjectElement(name = "ISO", label = "ISO", textValue = "800")
    protected INDITextElement iso;

    /**
     * The awbgains setting..
     * 
     * @see CameraOption#awbgains
     */
    @InjectElement(name = "AWBGAINS", label = "AWB Gains", textValue = "1,1")
    protected INDITextElement awbgains;

    /**
     * The timeout setting..
     * 
     * @see CameraOption#timeout
     */
    @InjectElement(name = "TIMEOUT", label = "timeout", textValue = "180000")
    protected INDITextElement timeout;

    /**
     * The bayer pattern this camera has, the value will be saved.
     */
    @InjectProperty(name = "BAYERPAT", label = "bayer pattern", group = INDIDriver.GROUP_OPTIONS, saveable = true)
    protected INDITextProperty bayerpatP;

    /**
     * Bayering pattern, it seems that it can be different per camera.
     */
    @InjectElement(label = "bayer pattern", textValue = CameraConstands.RASPBERRY_CAM_BAYER_PATTERN)
    protected INDITextElement bayerpat;

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

    /**
     * the camera controll process (starting capturing extraxting the raw image
     * error logging).
     */
    private CameraControl control;

    /**
     * the loop count that was used at the moment capturing started.
     */
    private double originalLoopCount;

    /**
     * Constructor for the camera driver.
     * 
     * @param connection
     *            the indi connection to the server.
     */
    public RaspberryCamera(INDIConnection connection) {
        super(connection);
        primaryCCD.setCCDParams(CameraConstands.HPIXELS, CameraConstands.VPIXELS, //
                CameraConstands.RASPBERRY_CAM_BITS_PER_PIXEL, //
                CameraConstands.PIXEL_SIZE, CameraConstands.PIXEL_SIZE);
        cameraOptions.setEventHandler(new org.indilib.i4j.driver.event.TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                property.setValues(elementsAndValues);
                for (INDITextElement element : property) {
                    checkOptionValue(element);
                }
                property.setState(PropertyStates.OK);
                updateProperty(property);
            }
        });
        loopCountP.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                property.setValues(elementsAndValues);
                property.setState(PropertyStates.OK);
                updateProperty(property);
            }
        });
        bayerpatP.setEventHandler(new org.indilib.i4j.driver.event.TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                property.setValues(elementsAndValues);
                property.setState(PropertyStates.OK);
                updateProperty(property);
            }
        });
    }

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

    /**
     * filter the specified option values, allow only numbers kommas and points.
     * 
     * @param element
     *            the element to check the value.
     */
    protected void checkOptionValue(INDITextElement element) {
        String value = element.getValue();
        StringBuffer result = new StringBuffer();
        for (char character : value.toCharArray()) {
            if (Character.isDigit(character) || character == '.' || character == ',') {
                result.append(character);
            }
        }
        if (!value.equals(result.toString())) {
            element.setValue(result.toString());
        }
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
        // BGGR states the doku reality proves different..
        Map<String, Object> result = new HashMap<String, Object>();
        String bayerPattern = CameraConstands.RASPBERRY_CAM_BAYER_PATTERN;
        if (bayerpat.getValue() != null && !bayerpat.getValue().trim().isEmpty()) {
            bayerPattern = bayerpat.getValue();
        }
        result.put(StandardFitsHeader.BAYERPAT, bayerPattern);
        return result;
    }

    @Override
    public String getName() {
        return RaspberryCamera.class.getSimpleName();
    }

    @Override
    public boolean startExposure(double duration) {
        originalLoopCount = loopCount.getValue();
        if (control == null) {
            control = new CameraControl() {

                @Override
                protected synchronized void imageCaptured(RawImage capturedImage) {
                    primaryCCD.setAutoLoop(false);
                    if (loopCount.getIntValue() == 1) {
                        stop();
                        control = null;
                    } else {
                        loopCount.setValue(loopCount.getValue() - 1d);
                    }
                    updateProperty(loopCountP);
                    primaryCCD.setFrameBuffer(convertRawImageToINDIImage(capturedImage));
                    exposureComplete(primaryCCD);
                    // reinitialize the system
                    if (control == null) {
                        primaryCCD.setAutoLoop(true);
                        loopCount.setValue(originalLoopCount);
                    }
                }

            };
            control.addOption(CameraOption.sharpness, sharpness.getValue()) //
                    .addOption(CameraOption.contrast, contrast.getValue()) //
                    .addOption(CameraOption.brightness, brightness.getValue()) //
                    .addOption(CameraOption.saturation, saturation.getValue()) //
                    .addOption(CameraOption.ISO, iso.getValue()) //
                    .addOption(CameraOption.awbgains, awbgains.getValue()) //
                    .addOption(CameraOption.timeout, timeout.getValue()) //
                    .start();
            try {
                control.capture(duration);
            } catch (Exception e) {
                LOG.error("could not start exposure", e);
                return false;
            }
        }
        return true;
    }

    /**
     * convert the 10 bit raw raspberry image to a CCD image of shorts.
     * 
     * @param capturedImage
     *            the raw raspberry image.
     * @return the converted ccd image.
     */
    protected INDICCDImage convertRawImageToINDIImage(RawImage capturedImage) {
        INDICCDImage newCcdImage = INDICCDImage.createImage(CameraConstands.VPIXELS, CameraConstands.HPIXELS, CameraConstands.RASPBERRY_CAM_BITS_PER_PIXEL, ImageType.RAW);
        try {
            RowBuffer10Bit row = new RowBuffer10Bit();
            PixelIterator iterator = newCcdImage.iteratePixel();
            ByteArrayInputStream inData = new ByteArrayInputStream(capturedImage.getRawData());
            for (int y = 0; y < CameraConstands.VPIXELS; y++) {
                row.copy10BitPixelRowToIterator(inData, iterator);
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
        if (frameType == CcdFrame.TRI_COLOR_FRAME) {
            return false;
        }
        // ready for all other frame types
        return true;
    }
}
