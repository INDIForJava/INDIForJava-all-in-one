package org.indilib.i4j.driver.ccd;

/*
 * #%L INDI for Java Abstract CCD Driver %% Copyright (C) 2013 - 2014
 * indiforjava %% This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Lesser Public License for more details. You should have
 * received a copy of the GNU General Lesser Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>. #L%
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.HeaderCardException;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIBLOBElement;
import org.indilib.i4j.driver.INDIBLOBProperty;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INDICCDDriverExtension extends INDIDriverExtension<INDICCDDriver> {

    private static final Logger LOG = LoggerFactory.getLogger(INDICCDDriverExtension.class);

    private final SimpleDateFormat dateFormatISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @InjectProperty(name = "FRAME", label = "Frame", group = INDICCDDriver.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDINumberProperty imageFrame;

    @InjectElement(name = "X", label = "Left", maximum = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameX;

    @InjectElement(name = "Y", label = "Top", maximum = 1040d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameY;

    @InjectElement(name = "WIDTH", label = "Width", numberValue = 1392d, maximum = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameWidth;

    @InjectElement(name = "HEIGHT", label = "Height", numberValue = 1392d, maximum = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameHeigth;

    @InjectProperty(name = "FRAME_TYPE", label = "Frame Type", group = INDICCDDriver.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDISwitchProperty frameType;

    @InjectElement(name = "FRAME_LIGHT", label = "Light", switchValue = SwitchStatus.ON)
    protected INDISwitchElement frameTypeLight;

    @InjectElement(name = "FRAME_BIAS", label = "Bias", switchValue = SwitchStatus.OFF)
    protected INDISwitchElement frameTypeBais;

    @InjectElement(name = "FRAME_DARK", label = "Dark", switchValue = SwitchStatus.OFF)
    protected INDISwitchElement frameTypeDark;

    @InjectElement(name = "FRAME_FLAT", label = "Flat", switchValue = SwitchStatus.OFF)
    protected INDISwitchElement frameTypeFlat;

    @InjectProperty(name = "EXPOSURE", label = "Expose", group = INDICCDDriver.MAIN_CONTROL_TAB)
    protected INDINumberProperty imageExposure;

    @InjectElement(name = "CCD_EXPOSURE_VALUE", label = "Duration (s)", numberValue = 1, maximum = 36000, numberFormat = "%5.2f")
    protected INDINumberElement imageExposureDuration;

    @InjectProperty(name = "ABORT_EXPOSURE", label = "Expose Abort", group = INDICCDDriver.MAIN_CONTROL_TAB)
    protected INDISwitchProperty abort;

    @InjectElement(name = "ABORT", label = "Abort")
    protected INDISwitchElement abortSwitch;

    @InjectProperty(name = "BINNING", label = "Binning", group = INDICCDDriver.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDINumberProperty imageBin;

    @InjectElement(name = "HOR_BIN", label = "X", numberValue = 1, maximum = 4, minimum = 1, step = 1, numberFormat = "%2.0f")
    protected INDINumberElement imageBinX;

    @InjectElement(name = "VER_BIN", label = "Y", numberValue = 1, maximum = 4, minimum = 1, step = 1, numberFormat = "%2.0f")
    protected INDINumberElement imageBinY;

    @InjectProperty(name = "INFO", label = "CCD Information", group = INDICCDDriver.IMAGE_INFO_TAB, permission = PropertyPermissions.RO)
    protected INDINumberProperty imagePixelSize;

    @InjectElement(name = "CCD_MAX_X", label = "Resolution x", numberValue = 1392, maximum = 16000, minimum = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizeMaxX;

    @InjectElement(name = "CCD_MAX_Y", label = "Resolution y", numberValue = 1392, maximum = 16000, minimum = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizeMaxY;

    @InjectElement(name = "CCD_PIXEL_SIZE", label = "Pixel size (um)", numberValue = 6.45, maximum = 40, minimum = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizePixelSize;

    @InjectElement(name = "CCD_PIXEL_SIZE_X", label = "Pixel size X", numberValue = 6.45, maximum = 40, minimum = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizePixelSizeX;

    @InjectElement(name = "CCD_PIXEL_SIZE_Y", label = "Pixel size Y", numberValue = 6.45, maximum = 40, minimum = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizePixelSizeY;

    @InjectElement(name = "CCD_BITSPERPIXEL", label = "Bits per pixel", numberValue = 8, maximum = 64, minimum = 8, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizeBitPerPixel;

    @InjectProperty(name = "COMPRESSION", label = "Image", group = INDICCDDriver.IMAGE_SETTINGS_TAB)
    protected INDISwitchProperty compress;

    @InjectElement(name = "COMPRESS", label = "Compress", switchValue = SwitchStatus.ON)
    protected INDISwitchElement compressCompress;

    @InjectElement(name = "RAW", label = "Raw")
    protected INDISwitchElement compressRaw;

    @InjectProperty(name = "CCD", label = "Image Data", group = INDICCDDriver.IMAGE_INFO_TAB, permission = PropertyPermissions.RO)
    protected INDIBLOBProperty fits;

    @InjectElement(name = "CCD", label = "Image")
    protected INDIBLOBElement fitsImage;

    private INDICCDDriverInterface driverInterface;

    /**
     * True if frame is compressed, false otherwise
     */
    private boolean sendCompressed = false;

    /**
     * native horizontal resolution of the ccd
     */
    private int xResolution;

    /**
     * native vertical resolution of the ccd
     */
    private int yResolution;

    /**
     * the starting left coordinates (X) of the image
     */
    private int subframeX;

    /**
     * the starting top coordinates (Y) of the image.
     */
    private int subframeY;

    /**
     * UNBINNED width of the subframe
     */
    private int subframeWidth;

    /**
     * UNBINNED height of the subframe
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
     * the fits header naxis
     */
    private int fitsNAxis = 2;

    /**
     * horizontal pixel size in microns.
     */
    private float pixelSizeX;

    /**
     * vertical pixel size in microns.
     */
    private float pixelSizeY;

    private int bitsPerPixel = 8; // Bytes per Pixel

    /**
     * desired frame type for next exposure
     */
    private CcdFrame currentFrameType = CcdFrame.LIGHT_FRAME;

    /**
     * requested exposure duration for the CCD chip in seconds.
     */
    private double exposureDuration;

    private Date startExposureTime;

    private INDICCDImage ccdImage;

    private String imageExtension = "fits";

    private float exposureTime = 0.0f;

    private double ra = Double.NaN;

    private double dec = Double.NaN;

    protected boolean autoLoop = false;

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
     * @param fptr
     *            pointer to a valid FITS file.
     * @param targetChip
     *            The target chip to extract the keywords from.
     * @throws HeaderCardException
     */
    private void addFITSKeywords(BasicHDU fitsHeader) throws HeaderCardException {
        fitsHeader.addValue("EXPTIME", exposureDuration, "Total Exposure Time (s)");

        if (currentFrameType == CcdFrame.DARK_FRAME)
            fitsHeader.addValue("DARKTIME", exposureDuration, "Total Exposure Time (s)");

        fitsHeader.addValue("PIXSIZE1", pixelSizeX, "Pixel Size 1 (microns)");
        fitsHeader.addValue("PIXSIZE2", pixelSizeY, "Pixel Size 2 (microns)");
        fitsHeader.addValue("XBINNING", binningX, "Binning factor in width");
        fitsHeader.addValue("YBINNING", binningY, "Binning factor in height");
        fitsHeader.addValue("FRAME", currentFrameType.fitsValue(), "Frame Type");

        if (fitsNAxis == 2) {
            // should de done in the image processing
            // fitsHeader.addValue("DATAMIN", &min_val, "Minimum value");
            // fitsHeader.addValue("DATAMAX", &max_val, "Maximum value");
        }

        if (!Double.isNaN(ra) && !Double.isNaN(dec)) {
            fitsHeader.addValue("OBJCTRA", ra, "Object RA");
            fitsHeader.addValue("OBJCTDEC", dec, "Object DEC");
        }

        fitsHeader.addValue("INSTRUME", driver.getName(), "CCD Name");
        fitsHeader.addValue("DATE-OBS", getExposureStartTime(), "UTC start date of observation");

    }

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

    private void newCompressedValue(INDISwitchElementAndValue[] elementsAndValues) {
        compress.setValues(elementsAndValues);
        if (compressCompress.getValue() == SwitchStatus.ON) {
            sendCompressed = true;
        } else {
            sendCompressed = false;
        }
        updateProperty(compress);
    }

    private void newFrameTypeValue(INDISwitchElementAndValue[] elementsAndValues) {
        frameType.setValues(elementsAndValues);
        frameType.setState(PropertyStates.OK);
        if (frameTypeLight.getValue() == SwitchStatus.ON) {
            currentFrameType = CcdFrame.LIGHT_FRAME;
        } else if (frameTypeBais.getValue() == SwitchStatus.ON) {
            currentFrameType = CcdFrame.BIAS_FRAME;
            if (!capability().hasShutter()) {
                LOG.info("The CCD does not have a shutter. Cover the camera in order to take a bias frame.");
            }
        } else if (frameTypeDark.getValue() == SwitchStatus.ON) {
            currentFrameType = CcdFrame.DARK_FRAME;
            if (!capability().hasShutter()) {
                LOG.info("The CCD does not have a shutter. Cover the camera in order to take a dark frame.");
            }
        } else if (frameTypeFlat.getValue() == SwitchStatus.ON) {
            currentFrameType = CcdFrame.FLAT_FRAME;
        }
        if (!driverInterface.updateCCDFrameType(currentFrameType))
            frameType.setState(PropertyStates.ALERT);
        updateProperty(frameType);
    }

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

        } else
            imageBin.setState(PropertyStates.ALERT);
        updateProperty(imageBin);
        return;
    }

    private void newImageExposureValue(INDINumberElementAndValue[] elementsAndValues) {
        imageExposure.setValues(elementsAndValues);
        if (imageExposure.getState() == PropertyStates.BUSY) {
            driverInterface.abortExposure();
        }
        startExposureTime = new Date();
        if (driverInterface.startExposure(exposureDuration))
            imageExposure.setState(PropertyStates.BUSY);
        else
            imageExposure.setState(PropertyStates.ALERT);
        updateProperty(imageExposure);
    }

    private void newImageFrameValue(INDINumberElementAndValue[] elementsAndValues) {
        imageFrame.setValues(elementsAndValues);
        imageFrame.setState(PropertyStates.OK);
        String message =
                String.format("Requested CCD Frame is %4d,%4d %4d x %4d", imageFrameX.getIntValue(), imageFrameY.getIntValue(), imageFrameWidth.getIntValue(),
                        imageFrameHeigth.getIntValue());
        LOG.info(message);

        if (!driverInterface.updateCCDFrame(imageFrameX.getIntValue(), imageFrameY.getIntValue(), imageFrameWidth.getIntValue(), imageFrameHeigth.getIntValue()))
            imageFrame.setState(PropertyStates.ALERT);
        updateProperty(imageFrame, message);
    }

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
     * @brief setBin Set CCD Chip binnig
     * @param hor
     *            Horizontal binning.
     * @param ver
     *            Vertical binning.
     * @throws INDIException
     */
    private void setBin(int hor, int ver) throws INDIException {
        binningX = hor;
        binningY = ver;

        imageBinX.setValue(binningX);
        imageBinY.setValue(binningY);
        this.updateProperty(imageBin);
    }

    /**
     * @brief setBPP Set pixel depth of CCD chip.
     * @param bpp
     *            bits per pixel
     * @throws INDIException
     */
    private void setBitsPerPixel(int bpp) throws INDIException {
        bitsPerPixel = bpp;
        imagePixelSizeBitPerPixel.setValue(bpp);
        this.updateProperty(imagePixelSize);
    }

    /**
     * @brief setFrame Set desired frame resolutoin for an exposure.
     * @param subx
     *            Left position.
     * @param suby
     *            Top position.
     * @param subw
     *            unbinned width of the frame.
     * @param subh
     *            unbinned height of the frame.
     * @throws INDIException
     */
    private void setFrame(int subx, int suby, int subw, int subh) throws INDIException {
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
     * @brief setPixelSize Set CCD Chip pixel size
     * @param x
     *            Horziontal pixel size in microns.
     * @param y
     *            Vertical pixel size in microns.
     * @throws INDIException
     */
    private void setPixelSize(float x, float y) throws INDIException {
        pixelSizeX = x;
        pixelSizeY = y;

        imagePixelSizePixelSize.setValue(x);
        imagePixelSizePixelSizeX.setValue(x);
        imagePixelSizePixelSizeY.setValue(y);
        this.updateProperty(imagePixelSize);
    }

    /**
     * @brief setResolution set CCD Chip resolution
     * @param x
     *            width
     * @param y
     *            height
     * @throws INDIException
     */
    private void setResolution(int x, int y) throws INDIException {
        xResolution = x;
        yResolution = y;

        imagePixelSizeMaxX.setValue(x);
        imagePixelSizeMaxY.setValue(y);
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
        if (capability().canAbort())
            addProperty(this.abort);
        if (!capability().canSubFrame()) {
            this.imageFrame.setPermission(PropertyPermissions.RO);
        }
        addProperty(this.imageFrame);
        if (capability().canBin())
            addProperty(this.imageBin);
        addProperty(this.imagePixelSize);
        addProperty(this.compress);
        addProperty(this.fits);
        addProperty(this.frameType);

    }

    @Override
    public void disconnect() {
        removeProperty(this.imageExposure);
        if (capability().canAbort())
            removeProperty(this.abort);
        if (!capability().canSubFrame()) {
            this.imageFrame.setPermission(PropertyPermissions.RO);
        }
        removeProperty(this.imageFrame);
        if (capability().canBin())
            removeProperty(this.imageBin);
        removeProperty(this.imagePixelSize);
        removeProperty(this.compress);
        removeProperty(this.fits);
        removeProperty(this.frameType);

    }

    /**
     * Uploads target Chip exposed buffer as FITS to the client.
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
        if (autoLoop) {
            imageExposureDuration.setValue(exposureTime);
            imageExposure.setState(PropertyStates.BUSY);
            if (driverInterface.startExposure(exposureTime))
                imageExposure.setState(PropertyStates.BUSY);
            else {
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
     * @param xf
     *            X pixel size in microns.
     * @param yf
     *            Y pixel size in microns.
     */
    public void setCCDParams(int x, int y, int bpp, float xf, float yf) throws INDIException {
        setResolution(x, y);
        setFrame(0, 0, x, y);
        setBin(1, 1);
        setPixelSize(xf, yf);
        setBitsPerPixel(bpp);
    }

    public void setDriverInterface(INDICCDDriverInterface driverInterface) {
        this.driverInterface = driverInterface;
    }

    /**
     * @throws INDIException
     * @brief setExposureFailed Alert the client that the exposure failed.
     */
    public void setExposureFailed() throws INDIException {
        imageExposure.setState(PropertyStates.ALERT);
        this.updateProperty(imageExposure);
    }

    /**
     * @brief setExposureLeft Update exposure time left. Inform the client of
     *        the new exposure time left value.
     * @param duration
     *            exposure duration left in seconds.
     */
    public void setExposureLeft(double duration) throws INDIException {
        imageExposureDuration.setValue(duration);
        this.updateProperty(imageExposure);
    }

    /**
     * @brief setFrameBuffer Set raw frame buffer pointer.
     * @param buffer
     *            pointer to frame buffer /note CCD Chip allocates the frame
     *            buffer internally once SetFrameBufferSize is called with
     *            allocMem set to true which is the default behavior. If you
     *            allocated the memory yourself (i.e. allocMem is false), then
     *            you must call this function to set the pointer to the raw
     *            frame buffer.
     */
    public void setFrameBuffer(INDICCDImage ccdImage) {
        this.ccdImage = ccdImage;
    }

    /**
     * @brief setImageExtension Set image exntension
     * @param ext
     *            extension (fits, jpeg, raw..etc)
     */
    public void setImageExtension(String ext) {
        imageExtension = ext;
    }

    /**
     * @brief setMaxBin Set Maximum CCD Chip binning
     * @param max_hor
     *            Maximum horizontal binning
     * @param max_ver
     *            Maximum vertical binning
     * @throws INDIException
     */
    public void setMaxBin(int max_hor, int max_ver) throws INDIException {
        imageBinX.setMax(max_hor);
        imageBinY.setMax(max_ver);
        this.updateProperty(imageBin, true, null);
    }

    public void setScopeDirection(double ra, double dec) {
        this.ra = ra;
        this.dec = dec;
    }

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
}
