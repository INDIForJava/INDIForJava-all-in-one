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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DeflaterOutputStream;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.HeaderCardException;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.driver.INDIBLOBElement;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.NumberEvent;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.fits.StandardFitsHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the extension for handling one ccd chip image.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDICCDDriverExtension extends INDIDriverExtension<INDICCDDriver> {

    /**
     * The logger to use.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDICCDDriverExtension.class);

    /**
     * the default bits per pixel of an ccd.
     */
    private static final int DEFAULT_BITS_PER_PIXEL = 8;

    /**
     * max string length in fit header.
     */
    private static final int MAX_STRING_LENGTH_IN_FITS_HEADER = 67;

    /**
     * the date/time fomat iso-8601 as a simple date formatter.
     */
    private final SimpleDateFormat dateFormatISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * This property describes the sub frame of the ccd to capture.
     */
    @InjectProperty(name = "FRAME", label = "Frame", group = INDICCDDriver.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDINumberProperty imageFrame;

    /**
     * The starting x point in pixel of the subframe.
     */
    @InjectElement(name = "X", label = "Left", maximum = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameX;

    /**
     * The starting y point in pixel of the subframe.
     */
    @InjectElement(name = "Y", label = "Top", maximum = 1040d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameY;

    /**
     * The width in pixel of the subframe.
     */
    @InjectElement(name = "WIDTH", label = "Width", numberValue = 1392d, maximum = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameWidth;

    /**
     * The height in pixel of the subframe.
     */
    @InjectElement(name = "HEIGHT", label = "Height", numberValue = 1392d, maximum = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameHeigth;

    /**
     * The frame type, the next exposure will be {@link CcdFrame}.
     */
    @InjectProperty(name = "FRAME_TYPE", label = "Frame Type", group = INDICCDDriver.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDISwitchProperty frameType;

    /**
     * It will be a nomal light frame.
     */
    @InjectElement(name = "FRAME_LIGHT", label = "Light", switchValue = SwitchStatus.ON)
    protected INDISwitchElement frameTypeLight;

    /**
     * It will be a bias frame {@link CcdFrame#BIAS_FRAME}.
     */
    @InjectElement(name = "FRAME_BIAS", label = "Bias")
    protected INDISwitchElement frameTypeBais;

    /**
     * It will be a dark frame {@link CcdFrame#DARK_FRAME}.
     */
    @InjectElement(name = "FRAME_DARK", label = "Dark")
    protected INDISwitchElement frameTypeDark;

    /**
     * It will be a flat frame {@link CcdFrame#FLAT_FRAME}.
     */
    @InjectElement(name = "FRAME_FLAT", label = "Flat")
    protected INDISwitchElement frameTypeFlat;

    /**
     * The exposure time of the next image in seconds, a new value will also
     * start the exposure.
     */
    @InjectProperty(name = "EXPOSURE", label = "Expose", group = INDIDriver.GROUP_MAIN_CONTROL)
    protected INDINumberProperty imageExposure;

    /**
     * The exposure time of the next image in seconds, a new value will also
     * start the exposure.
     */
    @InjectElement(name = "CCD_EXPOSURE_VALUE", label = "Duration (s)", numberValue = 1, maximum = 36000, numberFormat = "%5.2f")
    protected INDINumberElement imageExposureDuration;

    /**
     * abort the current exposure.
     */
    @InjectProperty(name = "ABORT_EXPOSURE", label = "Expose Abort", group = INDIDriver.GROUP_MAIN_CONTROL)
    protected INDISwitchProperty abort;

    /**
     * abort the current exposure.
     */
    @InjectElement(name = "ABORT", label = "Abort")
    protected INDISwitchElement abortSwitch;

    /**
     * how may pixels should be binned together to get a higher sensibility.
     */
    @InjectProperty(name = "BINNING", label = "Binning", group = INDICCDDriver.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDINumberProperty imageBin;

    /**
     * how may pixels should be binned together in the x axis to get a higher
     * sensibility.
     */
    @InjectElement(name = "HOR_BIN", label = "X", numberValue = 1, maximum = 4, minimum = 1, step = 1, numberFormat = "%2.0f")
    protected INDINumberElement imageBinX;

    /**
     * how may pixels should be binned together in the y axis to get a higher
     * sensibility.
     */
    @InjectElement(name = "VER_BIN", label = "Y", numberValue = 1, maximum = 4, minimum = 1, step = 1, numberFormat = "%2.0f")
    protected INDINumberElement imageBinY;

    /**
     * Some informations about the ccd sensor itself.
     */
    @InjectProperty(name = "INFO", label = "CCD Information", group = INDICCDDriver.IMAGE_INFO_TAB, permission = PropertyPermissions.RO)
    protected INDINumberProperty imagePixelSize;

    /**
     * the number of pixels in the x axis the sensor has.
     */
    @InjectElement(name = "CCD_MAX_X", label = "Resolution x", numberValue = 1392, maximum = 16000, minimum = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizeMaxX;

    /**
     * the number of pixels in the y axis the sensor has.
     */
    @InjectElement(name = "CCD_MAX_Y", label = "Resolution y", numberValue = 1392, maximum = 16000, minimum = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizeMaxY;

    /**
     * the size of one pixel in microns in the bothe axis.
     */
    @InjectElement(name = "CCD_PIXEL_SIZE", label = "Pixel size (um)", numberValue = 6.45, maximum = 40, minimum = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizePixelSize;

    /**
     * the size of one pixel in microns in the x axis.
     */
    @InjectElement(name = "CCD_PIXEL_SIZE_X", label = "Pixel size X", numberValue = 6.45, maximum = 40, minimum = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizePixelSizeX;

    /**
     * the size of one pixel in microns in the y axis.
     */
    @InjectElement(name = "CCD_PIXEL_SIZE_Y", label = "Pixel size Y", numberValue = 6.45, maximum = 40, minimum = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizePixelSizeY;

    /**
     * how many bits per pixel does the sensor support (or bether how many bits
     * has the analog digital converter of the ccd chip).
     */
    @InjectElement(name = "CCD_BITSPERPIXEL", label = "Bits per pixel", numberValue = 8, maximum = 64, minimum = 8, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizeBitPerPixel;

    /**
     * what type of compression sould be used?
     */
    @InjectProperty(name = "COMPRESSION", label = "Image", group = INDICCDDriver.IMAGE_SETTINGS_TAB)
    protected INDISwitchProperty compress;

    /**
     * perform a zip compression on the image blob.
     */
    @InjectElement(name = "COMPRESS", label = "Compress", switchValue = SwitchStatus.ON)
    protected INDISwitchElement compressCompress;

    /**
     * send the image as it is. No compression used.
     */
    @InjectElement(name = "RAW", label = "Raw")
    protected INDISwitchElement compressRaw;

    /**
     * The image data itself, over this property the image will be send.
     */
    @InjectProperty(name = "CCD", label = "Image Data", group = INDICCDDriver.IMAGE_INFO_TAB, permission = PropertyPermissions.RO)
    protected INDIBLOBProperty fits;

    /**
     * The image data itself, over this property the image will be send.
     */
    @InjectElement(name = "CCD", label = "Image")
    protected INDIBLOBElement fitsImage;

    /**
     * should a new exposure start as soon as the old one is ready?
     */
    @InjectProperty(name = "AUTO_LOOP", label = "Auto loop", group = INDIDriver.GROUP_MAIN_CONTROL, switchRule = SwitchRules.AT_MOST_ONE)
    protected INDISwitchProperty autoLoopProp;

    /**
     * should a new exposure start as soon as the old one is ready?
     */
    @InjectElement(name = "AUTO_LOOP", label = "Auto loop")
    protected INDISwitchElement autoLoop;

    /**
     * The driver specific functions are encapsulated in this interface.
     * Normally the driver implements this interface but when a ccd-driver has
     * multiple chips it must have a way to handle the functionality on a per
     * chip basis.
     */
    private INDICCDDriverInterface driverInterface;

    /**
     * True if frame is compressed, false otherwise.
     */
    private boolean sendCompressed = false;

    /**
     * native horizontal resolution of the ccd.
     */
    private int xResolution;

    /**
     * native vertical resolution of the ccd.
     */
    private int yResolution;

    /**
     * the starting left coordinates (X) of the image.
     */
    private int subframeX;

    /**
     * the starting top coordinates (Y) of the image.
     */
    private int subframeY;

    /**
     * UNBINNED width of the subframe.
     */
    private int subframeWidth;

    /**
     * UNBINNED height of the subframe.
     */
    private int subframeHeight;

    /**
     * horizontal binning of the CCD chip.
     */
    private int binningX = 1;

    /**
     * vertical binning of the CCD chip.
     */
    private int binningY = 1;

    /**
     * horizontal pixel size in microns.
     */
    private float pixelSizeX;

    /**
     * vertical pixel size in microns.
     */
    private float pixelSizeY;

    /**
     * the bits per pixel for this sensor.
     */
    private int bitsPerPixel = DEFAULT_BITS_PER_PIXEL;

    /**
     * desired frame type for next exposure.
     */
    private CcdFrame currentFrameType = CcdFrame.LIGHT_FRAME;

    /**
     * requested exposure duration for the CCD chip in seconds.
     */
    private double exposureDuration;

    /**
     * the exact time the last exposure was started.
     */
    private Date startExposureTime;

    /**
     * the current image that was taken.
     */
    private INDICCDImage ccdImage;

    /**
     * the current image extension, fits is recommended!
     */
    private String imageExtension = "fits";

    /**
     * the current exposure time.
     */
    private float exposureTime = 0.0f;

    /**
     * Constructor of the extension, you should really know what you are doing
     * if you call this yourself. Better to let it be used by the injector.
     * 
     * @param indiccd
     *            the ccd driver to attach this extension to.
     */
    public INDICCDDriverExtension(INDICCDDriver indiccd) {
        super(indiccd);
        imageExposure.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newImageExposureValue(elementsAndValues);
            }
        });
        imageBin.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newImageBinValue(elementsAndValues);

            }

        });
        imageFrame.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newImageFrameValue(elementsAndValues);
            }
        });
        imagePixelSize.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newImagePixelSize(elementsAndValues);
            }
        });
        abort.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newAbortValue();
            }

        });
        compress.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newCompressedValue(elementsAndValues);
            }
        });
        frameType.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newFrameTypeValue(elementsAndValues);
            }
        });
        this.autoLoopProp.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                autoLoopProp.setValues(elementsAndValues);
                if (autoLoop.isOff()) {
                    autoLoopProp.setState(PropertyStates.IDLE);
                } else {
                    autoLoopProp.setState(PropertyStates.OK);
                }
                updateProperty(autoLoopProp);
            }
        });

    }

    /**
     * Add FITS keywords to a fits file. In additional to the standard FITS
     * keywords, this function write the following keywords the FITS file:
     * <ul>
     * <li>EXPTIME: Total Exposure Time (s)</li>
     * <li>DARKTIME (if applicable): Total Exposure Time (s)</li>
     * <li>PIXSIZE1: Pixel Size 1 (microns)</li>
     * <li>PIXSIZE2: Pixel Size 2 (microns)</li>
     * <li>BINNING: Binning HOR x VER</li>
     * <li>FRAME: Frame Type</li>
     * <li>DATAMIN: Minimum value</li>
     * <li>DATAMAX: Maximum value</li>
     * <li>INSTRUME: CCD Name</li>
     * <li>DATE-OBS: UTC start date of observation</li>
     * </ul>
     * To add additional information, override this function in the child class
     * and ensure to call INDICCD::addFITSKeywords.
     * 
     * @param fitsHeader
     *            the fits header definition to extend the headers
     * @throws HeaderCardException
     *             if the header becomes invalid.
     */
    private void addFITSKeywords(BasicHDU fitsHeader) throws HeaderCardException {
        fitsHeader.addValue(StandardFitsHeader.EXPTIME, exposureDuration, "Total Exposure Time (s)");

        if (currentFrameType == CcdFrame.DARK_FRAME) {
            fitsHeader.addValue("DARKTIME", exposureDuration, "Total Exposure Time (s)");
        }
        fitsHeader.addValue(StandardFitsHeader.PIXSIZE1, pixelSizeX, "Pixel Size x axis (microns)");
        fitsHeader.addValue(StandardFitsHeader.PIXSIZE2, pixelSizeY, "Pixel Size y axis (microns)");
        fitsHeader.addValue(StandardFitsHeader.XPIXSZ, pixelSizeX, "Pixel Size x axis (microns)");
        fitsHeader.addValue(StandardFitsHeader.YPIXSZ, pixelSizeY, "Pixel Size y axis (microns)");
        fitsHeader.addValue(StandardFitsHeader.XBINNING, binningX, "Binning factor in width");
        fitsHeader.addValue(StandardFitsHeader.YBINNING, binningY, "Binning factor in height");
        fitsHeader.addValue("FRAME", currentFrameType.fitsValue(), "Frame Type");

        fitsHeader.addValue(StandardFitsHeader.INSTRUME, driver.getName(), "CCD Name");
        fitsHeader.addValue(StandardFitsHeader.DATE_OBS, getExposureStartTime(), "UTC start date of observation");

        Map<String, Object> attributes = driverInterface.getExtraFITSKeywords(fitsHeader);
        if (attributes != null) {
            for (Entry<String, Object> attribute : attributes.entrySet()) {
                if (attribute.getValue() instanceof Date) {
                    fitsHeader.addValue(attribute.getKey(), this.dateFormatISO8601.format((Date) attribute.getValue()), "");
                } else if (attribute.getValue() instanceof String) {
                    String stringValue = (String) attribute.getValue();
                    String comment = "";
                    if (stringValue.length() > MAX_STRING_LENGTH_IN_FITS_HEADER) {
                        comment = stringValue;
                        stringValue = "value in comment";
                    }
                    fitsHeader.addValue(attribute.getKey(), stringValue, comment);
                } else if (attribute.getValue() instanceof Integer) {
                    fitsHeader.addValue(attribute.getKey(), (Integer) attribute.getValue(), "");
                } else if (attribute.getValue() instanceof Double) {
                    fitsHeader.addValue(attribute.getKey(), (Double) attribute.getValue(), "");
                } else if (attribute.getValue() instanceof Boolean) {
                    fitsHeader.addValue(attribute.getKey(), (Boolean) attribute.getValue(), "");
                } else {
                    throw new IllegalArgumentException("unknown bits per pixel");
                }
            }
        }

    }

    /**
     * @return the capabilities of the current driver.
     */
    private Capability capability() {
        return driver.capability();
    }

    /**
     * @brief getExposureStartTime
     * @return exposure start time in ISO 8601 format.
     */
    private String getExposureStartTime() {
        return dateFormatISO8601.format(startExposureTime);
    }

    /**
     * an abort was triggered from the client.
     */
    private void newAbortValue() {
        abortSwitch.setValue(SwitchStatus.OFF);
        if (driverInterface.abortExposure()) {
            abort.setState(PropertyStates.OK);
            imageExposure.setState(PropertyStates.IDLE);
            imageExposureDuration.setValue(0.0);
        } else {
            abort.setState(PropertyStates.ALERT);
            imageExposure.setState(PropertyStates.ALERT);
        }
        updateProperty(abort);
        updateProperty(imageExposure);
    }

    /**
     * the compressed property was changed on the client.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newCompressedValue(INDISwitchElementAndValue[] elementsAndValues) {
        compress.setValues(elementsAndValues);
        if (compressCompress.isOn()) {
            sendCompressed = true;
        } else {
            sendCompressed = false;
        }
        updateProperty(compress);
    }

    /**
     * the current frame type was changed on the client.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newFrameTypeValue(INDISwitchElementAndValue[] elementsAndValues) {
        frameType.setValues(elementsAndValues);
        frameType.setState(PropertyStates.OK);
        String message = null;
        if (frameTypeLight.isOn()) {
            currentFrameType = CcdFrame.LIGHT_FRAME;
        } else if (frameTypeBais.isOn()) {
            currentFrameType = CcdFrame.BIAS_FRAME;
            if (!capability().hasShutter()) {
                message = "The CCD does not have a shutter. Cover the camera in order to take a bias frame.";
                LOG.info(message);
            }
        } else if (frameTypeDark.isOn()) {
            currentFrameType = CcdFrame.DARK_FRAME;
            if (!capability().hasShutter()) {
                message = "The CCD does not have a shutter. Cover the camera in order to take a dark frame.";
                LOG.info(message);
            }
        } else if (frameTypeFlat.isOn()) {
            currentFrameType = CcdFrame.FLAT_FRAME;
        }
        if (!driverInterface.updateCCDFrameType(currentFrameType)) {
            frameType.setState(PropertyStates.ALERT);
        }
        updateProperty(frameType, message);
    }

    /**
     * the binning properties where changed on the client.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newImageBinValue(INDINumberElementAndValue[] elementsAndValues) {
        // We are being asked to set camera binning
        if (!capability().canBin()) {
            imageBin.setState(PropertyStates.ALERT);
            updateProperty(imageBin);
            return;
        }
        imageBin.setValues(elementsAndValues);
        binningX = imageBinX.getIntValue();
        binningY = imageBinY.getIntValue();

        if (driverInterface.updateCCDBin(binningX, binningY)) {
            imageBin.setState(PropertyStates.OK);
        } else {
            imageBin.setState(PropertyStates.ALERT);
        }
        updateProperty(imageBin);
        return;
    }

    /**
     * the exposure was changed on the client, attention this also starts the
     * exposure itself.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newImageExposureValue(INDINumberElementAndValue[] elementsAndValues) {
        imageExposure.setValues(elementsAndValues);
        if (imageExposure.getState() == PropertyStates.BUSY) {
            driverInterface.abortExposure();
        }
        exposureDuration = imageExposureDuration.getValue();
        startExposureTime = new Date();
        if (driverInterface.startExposure(exposureDuration)) {
            imageExposure.setState(PropertyStates.BUSY);
        } else {
            imageExposure.setState(PropertyStates.ALERT);
        }
        updateProperty(imageExposure);
    }

    /**
     * the subframe specification was changed on the client.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newImageFrameValue(INDINumberElementAndValue[] elementsAndValues) {
        String message = null;
        if (capability().canSubFrame()) {
            imageFrame.setValues(elementsAndValues);
            imageFrame.setState(PropertyStates.OK);
            message = String.format("Requested CCD Frame is %4d,%4d %4d x %4d", //
                    imageFrameX.getIntValue(), imageFrameY.getIntValue(), imageFrameWidth.getIntValue(), imageFrameHeigth.getIntValue());
            LOG.info(message);
            if (!driverInterface.updateCCDFrame(imageFrameX.getIntValue(), imageFrameY.getIntValue(), imageFrameWidth.getIntValue(), imageFrameHeigth.getIntValue())) {
                imageFrame.setState(PropertyStates.ALERT);
            }
        } else {
            message = "sub frame is not supported!";
            LOG.info(message);
            imageFrame.setState(PropertyStates.OK);
        }
        updateProperty(imageFrame, message);
    }

    /**
     * the pixel sizes where changed on the client.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newImagePixelSize(INDINumberElementAndValue[] elementsAndValues) {
        imagePixelSize.setValues(elementsAndValues);
        imagePixelSize.setState(PropertyStates.OK);
        xResolution = imagePixelSizeMaxX.getIntValue();
        yResolution = imagePixelSizeMaxY.getIntValue();
        pixelSizeX = imagePixelSizePixelSizeX.getValue().floatValue();
        pixelSizeY = imagePixelSizePixelSizeY.getValue().floatValue();
        updateProperty(imagePixelSize);
    }

    /**
     * Set CCD Chip binnig.
     * 
     * @param hor
     *            Horizontal binning.
     * @param ver
     *            Vertical binning.
     */
    private void setBin(int hor, int ver) {
        binningX = hor;
        binningY = ver;

        imageBinX.setValue(binningX);
        imageBinY.setValue(binningY);
        this.updateProperty(imageBin);
    }

    /**
     * Set pixel depth of CCD chip.
     * 
     * @param bpp
     *            bits per pixel
     */
    private void setBitsPerPixel(int bpp) {
        bitsPerPixel = bpp;
        imagePixelSizeBitPerPixel.setValue(bitsPerPixel);
        this.updateProperty(imagePixelSize);
    }

    /**
     * Set desired frame resolutoin for an exposure.
     * 
     * @param subx
     *            Left position.
     * @param suby
     *            Top position.
     * @param subw
     *            unbinned width of the frame.
     * @param subh
     *            unbinned height of the frame.
     */
    private void setFrame(int subx, int suby, int subw, int subh) {
        subframeX = subx;
        subframeY = suby;
        subframeWidth = subw;
        subframeHeight = subh;

        imageFrameX.setValue(subframeX);
        imageFrameY.setValue(subframeY);
        imageFrameWidth.setValue(subframeWidth);
        imageFrameHeigth.setValue(subframeHeight);
        this.updateProperty(imageFrame);
    }

    /**
     * Set CCD Chip pixel size.
     * 
     * @param x
     *            Horziontal pixel size in microns.
     * @param y
     *            Vertical pixel size in microns.
     */
    private void setPixelSize(float x, float y) {
        pixelSizeX = x;
        pixelSizeY = y;

        imagePixelSizePixelSize.setValue(x);
        imagePixelSizePixelSizeX.setValue(x);
        imagePixelSizePixelSizeY.setValue(y);
        this.updateProperty(imagePixelSize);
    }

    /**
     * set CCD Chip resolution.
     * 
     * @param x
     *            width
     * @param y
     *            height
     */
    private void setResolution(int x, int y) {
        xResolution = x;
        yResolution = y;

        imagePixelSizeMaxX.setValue(xResolution);
        imagePixelSizeMaxY.setValue(yResolution);
        this.updateProperty(imagePixelSize);

        imageFrameX.setMin(0);
        imageFrameX.setMax(x - 1);
        imageFrameY.setMin(0);
        imageFrameY.setMax(y - 1);
        imageFrameWidth.setMin(1);
        imageFrameWidth.setMax(x);
        imageFrameHeigth.setMin(1);
        imageFrameHeigth.setMax(y);
        this.updateProperty(imageFrame, true, null);
    }

    @Override
    public void connect() {
        addProperty(this.imageExposure);
        if (capability().canAbort()) {
            addProperty(this.abort);
        }
        if (!capability().canSubFrame()) {
            this.imageFrame.setPermission(PropertyPermissions.RO);
        }
        addProperty(this.imageFrame);
        if (capability().canBin()) {
            addProperty(this.imageBin);
        }
        addProperty(this.imagePixelSize);
        addProperty(this.compress);
        addProperty(this.fits);
        addProperty(this.frameType);
        addProperty(this.autoLoopProp);
    }

    @Override
    public void disconnect() {
        removeProperty(this.imageExposure);
        if (capability().canAbort()) {
            removeProperty(this.abort);
        }
        if (!capability().canSubFrame()) {
            this.imageFrame.setPermission(PropertyPermissions.RO);
        }
        removeProperty(this.imageFrame);
        if (capability().canBin()) {
            removeProperty(this.imageBin);
        }
        removeProperty(this.imagePixelSize);
        removeProperty(this.compress);
        removeProperty(this.fits);
        removeProperty(this.frameType);
        removeProperty(this.autoLoopProp);
    }

    /**
     * Uploads target Chip exposed buffer as FITS to the client. Dervied classes
     * should call this functon when an exposure is complete.
     * 
     * @return true if the operation was successful.
     */
    public boolean exposureComplete() {
        boolean sendImage = driver.shouldSendImage();
        boolean saveImage = driver.shouldSaveImage();
        if (sendImage || saveImage) {
            try {
                if ("fits".equals((getImageExtension()))) {
                    Fits f = ccdImage.asFitsImage();
                    addFITSKeywords(f.getHDU(0));
                }
                uploadFile(sendImage, saveImage);
            } catch (Exception e) {
                LOG.error("could not send or save image", e);
                return false;
            }
        }
        imageExposure.setState(PropertyStates.OK);
        updateProperty(imageExposure);
        if (autoLoop.isOn()) {
            imageExposureDuration.setValue(exposureTime);
            imageExposure.setState(PropertyStates.BUSY);
            if (driverInterface.startExposure(exposureTime)) {
                imageExposure.setState(PropertyStates.BUSY);
            } else {
                LOG.error("Autoloop: CCD Exposure Error!");
                imageExposure.setState(PropertyStates.ALERT);
            }
            updateProperty(imageExposure);
        }
        return true;
    }

    /**
     * @return Return image extension (fits, jpeg, raw..etc)
     */
    public String getImageExtension() {
        return imageExtension;
    }

    /**
     * @return True if CCD is currently exposing, false otherwise.
     */
    public boolean isExposing() {
        return (imageExposure.getState() == PropertyStates.BUSY);
    }

    /**
     * Setup CCD paramters for the CCD. Child classes call this function to
     * update CCD paramaters
     * 
     * @param x
     *            Frame X coordinates in pixels.
     * @param y
     *            Frame Y coordinates in pixels.
     * @param bpp
     *            Bits Per Pixels.
     * @param xPixelSize
     *            X pixel size in microns.
     * @param yPixelsize
     *            Y pixel size in microns.
     */
    public void setCCDParams(int x, int y, int bpp, float xPixelSize, float yPixelsize) {
        setResolution(x, y);
        setFrame(0, 0, x, y);
        setBin(1, 1);
        setPixelSize(xPixelSize, yPixelsize);
        setBitsPerPixel(bpp);
    }

    /**
     * sets the ccd driver interface for this ccd. this should only be changed
     * for non primary ccd's.
     * 
     * @param driverInterface
     *            the implementation of the ccd handling.
     */
    public void setDriverInterface(INDICCDDriverInterface driverInterface) {
        this.driverInterface = driverInterface;
    }

    /**
     * Alert the client that the exposure failed.
     */
    public void setExposureFailed() {
        imageExposure.setState(PropertyStates.ALERT);
        this.updateProperty(imageExposure);
    }

    /**
     * Update exposure time left. Inform the client of the new exposure time
     * left value.
     * 
     * @param duration
     *            exposure duration left in seconds.
     */
    public void setExposureLeft(double duration) {
        imageExposureDuration.setValue(duration);
        this.updateProperty(imageExposure);
    }

    /**
     * Set raw frame buffer pointer.
     * 
     * @param newCcdImage
     *            the captured ccd image.
     */
    public void setFrameBuffer(INDICCDImage newCcdImage) {
        this.ccdImage = newCcdImage;
    }

    /**
     * Set image extension.
     * 
     * @param ext
     *            extension (fits, jpeg, raw..etc)
     */
    public void setImageExtension(String ext) {
        imageExtension = ext;
    }

    /**
     * Set Maximum CCD Chip binning.
     * 
     * @param maxHor
     *            Maximum horizontal binning
     * @param maxVer
     *            Maximum vertical binning
     */
    public void setMaxBin(int maxHor, int maxVer) {
        imageBinX.setMax(maxHor);
        imageBinY.setMax(maxVer);
        this.updateProperty(imageBin, true, null);
    }

    /**
     * Upload a new exposure image to the appropriate places.
     * 
     * @param sendImage
     *            should the image be send to the client.
     * @param saveImage
     *            should the image be saved locally (where the driver resides)
     * @throws Exception
     *             if something went wrong with the transmission or the saving
     *             of the file.
     */
    public void uploadFile(boolean sendImage, boolean saveImage) throws Exception {

        if (saveImage) {
            File fp = driver.getFileWithIndex(getImageExtension());
            try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fp)))) {
                ccdImage.write(os, this.subframeX, this.subframeY, this.subframeWidth, this.subframeHeight, getImageExtension());
            }
        }
        if (sendImage) {
            if (sendCompressed) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new BufferedOutputStream(out))))) {
                    ccdImage.write(os, this.subframeX, this.subframeY, this.subframeWidth, this.subframeHeight, getImageExtension());
                }
                fitsImage.setValue(new INDIBLOBValue(out.toByteArray(), "." + getImageExtension() + ".z"));
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(out))) {
                    ccdImage.write(os, this.subframeX, this.subframeY, this.subframeWidth, this.subframeHeight, getImageExtension());
                }
                fitsImage.setValue(new INDIBLOBValue(out.toByteArray(), "." + getImageExtension()));
            }
            fits.setState(PropertyStates.OK);
            updateProperty(fits);
        }
    }

    /**
     * @return desired frame type for next exposure.
     */
    public CcdFrame getCurrentFrameType() {
        return currentFrameType;
    }

    /**
     * @return the horizontal binning of the CCD chip.
     */
    public int getBinningX() {
        return binningX;
    }

    /**
     * @return the vertical binning of the CCD chip.
     */
    public int getBinningY() {
        return binningY;
    }

    /**
     * set the auto loop property.
     * 
     * @param value
     *            is the autoloop is on or off.
     */
    public void setAutoLoop(boolean value) {
        if (value) {
            autoLoop.setOn();
        } else {
            autoLoop.setOff();
        }
    }
}
