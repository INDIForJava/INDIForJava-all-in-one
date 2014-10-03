package laazotea.indi.driver;

import static laazotea.indi.Constants.PropertyStates.IDLE;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;

import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIBLOBValue;
import laazotea.indi.INDIException;
import laazotea.indi.driver.annotation.InjectElement;
import laazotea.indi.driver.annotation.InjectProperty;
import laazotea.indi.driver.event.NumberEvent;
import laazotea.indi.driver.event.SwitchEvent;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.HeaderCardException;

public class CCDDriverExtention extends INDIDriverExtention<INDICCDDriver> {

    private static final Logger LOG = Logger.getLogger(CCDDriverExtention.class.getName());

    static double P0 = 0.906, P1 = 0.584, P2 = 0.365, P3 = 0.117, P4 = 0.049, P5 = -0.05, P6 = -0.064, P7 = -0.074, P8 = -0.094;

    static private int[][] PROXIMITY_MATRIX = {
        {
            8,
            8,
            8,
            8,
            8,
            8,
            8,
            8,
            8
        },
        {
            8,
            8,
            8,
            7,
            6,
            7,
            8,
            8,
            8
        },
        {
            8,
            8,
            5,
            4,
            3,
            4,
            5,
            8,
            8
        },
        {
            8,
            7,
            4,
            2,
            1,
            2,
            4,
            8,
            8
        },
        {
            8,
            6,
            3,
            1,
            0,
            1,
            3,
            6,
            8
        },
        {
            8,
            7,
            4,
            2,
            1,
            2,
            4,
            8,
            8
        },
        {
            8,
            8,
            5,
            4,
            3,
            4,
            5,
            8,
            8
        },
        {
            8,
            8,
            8,
            7,
            6,
            7,
            8,
            8,
            8
        },
        {
            8,
            8,
            8,
            8,
            8,
            8,
            8,
            8,
            8
        }
    };

    private final SimpleDateFormat dateFormatISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @InjectProperty(name = "FRAME", label = "Frame", group = INDICCDDriver.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDINumberProperty imageFrame;

    @InjectElement(name = "X", label = "Left", maximumD = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameX;

    @InjectElement(name = "Y", label = "Top", maximumD = 1040d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameY;

    @InjectElement(name = "WIDTH", label = "Width", valueD = 1392d, maximumD = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameWidth;

    @InjectElement(name = "HEIGHT", label = "Height", valueD = 1392d, maximumD = 1392d, numberFormat = "%4.0f")
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

    @InjectElement(name = "CCD_EXPOSURE_VALUE", label = "Duration (s)", valueD = 1, maximumD = 36000, numberFormat = "%5.2f")
    protected INDINumberElement imageExposureDuration;

    @InjectProperty(name = "ABORT_EXPOSURE", label = "Expose Abort", group = INDICCDDriver.MAIN_CONTROL_TAB)
    protected INDISwitchProperty abort;

    @InjectElement(name = "ABORT", label = "Abort")
    protected INDISwitchElement abortSwitch;

    @InjectProperty(name = "BINNING", label = "Binning", group = INDICCDDriver.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDINumberProperty imageBin;

    @InjectElement(name = "HOR_BIN", label = "X", valueD = 1, maximumD = 4, minimumD = 1, stepD = 1, numberFormat = "%2.0f")
    protected INDINumberElement imageBinX;

    @InjectElement(name = "VER_BIN", label = "Y", valueD = 1, maximumD = 4, minimumD = 1, stepD = 1, numberFormat = "%2.0f")
    protected INDINumberElement imageBinY;

    @InjectProperty(name = "INFO", label = "CCD Information", group = INDICCDDriver.IMAGE_INFO_TAB, permission = PropertyPermissions.RO)
    protected INDINumberProperty imagePixelSize;

    @InjectElement(name = "CCD_MAX_X", label = "Resolution x", valueD = 1392, maximumD = 16000, minimumD = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizeMaxX;

    @InjectElement(name = "CCD_MAX_Y", label = "Resolution y", valueD = 1392, maximumD = 16000, minimumD = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizeMaxY;

    @InjectElement(name = "CCD_PIXEL_SIZE", label = "Pixel size (um)", valueD = 6.45, maximumD = 40, minimumD = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizePixelSize;

    @InjectElement(name = "CCD_PIXEL_SIZE_X", label = "Pixel size X", valueD = 6.45, maximumD = 40, minimumD = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizePixelSizeX;

    @InjectElement(name = "CCD_PIXEL_SIZE_Y", label = "Pixel size Y", valueD = 6.45, maximumD = 40, minimumD = 1, numberFormat = "%4.0f")
    protected INDINumberElement imagePixelSizePixelSizeY;

    @InjectElement(name = "CCD_BITSPERPIXEL", label = "Bits per pixel", valueD = 8, maximumD = 64, minimumD = 8, numberFormat = "%4.0f")
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

    @InjectProperty(name = "RAPID_GUIDE", label = "Rapid Guide", group = INDICCDDriver.OPTIONS_TAB, timeout = 0)
    protected INDISwitchProperty rapidGuide;

    @InjectElement(name = "ENABLE", label = "Enable")
    protected INDISwitchElement rapidGuideEnable;

    @InjectElement(name = "DISABLE", label = "Disable", switchValue = SwitchStatus.ON)
    protected INDISwitchElement rapidGuideDisable;

    @InjectProperty(name = "RAPID_GUIDE_SETUP", label = "Rapid Guide Setup", group = INDICCDDriver.RAPIDGUIDE_TAB, timeout = 0, switchRule = SwitchRules.ANY_OF_MANY)
    protected INDISwitchProperty rapidGuideSetup;

    @InjectElement(name = "AUTO_LOOP", label = "Auto loop", switchValue = SwitchStatus.ON)
    protected INDISwitchElement rapidGuideSetupAutoLoop;

    @InjectElement(name = "SEND_IMAGE", label = "Send image")
    protected INDISwitchElement rapidGuideSetupShowMarker;

    @InjectElement(name = "SHOW_MARKER", label = "Show marker")
    protected INDISwitchElement rapidGuideSetupSendImage;

    @InjectProperty(name = "RAPID_GUIDE_DATA", label = "Rapid Guide Data", group = INDICCDDriver.RAPIDGUIDE_TAB, permission = PropertyPermissions.RO)
    protected INDINumberProperty rapidGuideData;

    @InjectElement(name = "GUIDESTAR_X", label = "Guide star position X", maximumD = 1024, numberFormat = "%%5.2f")
    protected INDINumberElement rapidGuideDataX;

    @InjectElement(name = "GUIDESTAR_Y", label = "Guide star position Y", maximumD = 1024, numberFormat = "%5.2f")
    protected INDINumberElement rapidGuideDataY;

    @InjectElement(name = "GUIDESTAR_FIT", label = "GUIDESTAR_FIT", maximumD = 1024, numberFormat = "%5.2f")
    protected INDINumberElement rapidGuideDataFIT;

    private CCDDriverInterface driverInterface;

    private boolean sendCompressed = false;

    private int xResolution; // native resolution of the ccd

    private int yResolution; // ditto

    private int subframeX; // left side of the subframe we are requesting

    private int subframeY; // top of the subframe requested

    private int subframeWidth; // UNBINNED width of the subframe

    private int subframeHeight; // UNBINNED height of the subframe

    private int binningX = 1; // Binning requested in the x direction

    private int binningY = 1; // Binning requested in the Y direction

    private int NAxis = 2; // # of Axis

    private float pixelSizeX; // pixel size in microns, x direction

    private float pixelSizeY; // pixel size in microns, y direction

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

    private int lastRapidX;

    private int lastRapidY;

    private INDICCDImage ccdImage;

    private String imageExtention = "fits";

    private boolean rapidGuideEnabled = false;

    private boolean autoLoop = false;

    private boolean sendImage = false;

    private boolean showMarker = false;

    private float exposureTime = 0.0f;

    private double ra = Double.NaN;

    private double dec = Double.NaN;

    public CCDDriverExtention(INDICCDDriver indiccd) {
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
        rapidGuideData.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newRapidGuideDataValue(elementsAndValues);
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

        rapidGuide.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newRapidGuideValue(elementsAndValues);
            }

        });

        rapidGuideSetup.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newRapidGuideSetupValue(elementsAndValues);
            }
        });

    }

    private Capability capability() {
        return driver.capability();
    }

    private void newAbortValue() {
        abortSwitch.setValue(SwitchStatus.OFF);
        if (driverInterface.abortExposure()) {
            abort.setState(PropertyStates.OK);
            imageExposure.setState(IDLE);
            imageExposureDuration.setValue(0.0);
        } else {
            abort.setState(PropertyStates.ALERT);
            imageExposure.setState(PropertyStates.ALERT);
        }
        try {
            updateProperty(abort);
            updateProperty(imageExposure);
        } catch (INDIException e) {
        }
    }

    private void newCompressedValue(INDISwitchElementAndValue[] elementsAndValues) {
        compress.setValues(elementsAndValues);
        if (compressCompress.getValue() == SwitchStatus.ON) {
            sendCompressed = true;
        } else {
            sendCompressed = false;
        }
        try {
            updateProperty(compress);
        } catch (INDIException e) {
        }
    }

    private void newFrameTypeValue(INDISwitchElementAndValue[] elementsAndValues) {
        frameType.setValues(elementsAndValues);
        frameType.setState(PropertyStates.OK);
        if (frameTypeLight.getValue() == SwitchStatus.ON) {
            currentFrameType = CcdFrame.LIGHT_FRAME;
        } else if (frameTypeBais.getValue() == SwitchStatus.ON) {
            currentFrameType = CcdFrame.BIAS_FRAME;
            if (!capability().hasShutter()) {
                LOG.log(Level.FINE, "The CCD does not have a shutter. Cover the camera in order to take a bias frame.");
            }
        } else if (frameTypeDark.getValue() == SwitchStatus.ON) {
            currentFrameType = CcdFrame.DARK_FRAME;
            if (!capability().hasShutter()) {
                LOG.log(Level.FINE, "The CCD does not have a shutter. Cover the camera in order to take a dark frame.");
            }
        } else if (frameTypeFlat.getValue() == SwitchStatus.ON) {
            currentFrameType = CcdFrame.FLAT_FRAME;
        }
        if (!driverInterface.updateCCDFrameType(currentFrameType))
            frameType.setState(PropertyStates.ALERT);
        try {
            updateProperty(frameType);
        } catch (INDIException e) {
        }
    }

    private void newImageBinValue(INDINumberElementAndValue[] elementsAndValues) {
        // We are being asked to set camera binning
        if (!capability().canBin()) {
            imageBin.setState(PropertyStates.ALERT);
            try {
                updateProperty(imageBin);
            } catch (INDIException e) {
            }
            return;
        }
        imageBin.setValues(elementsAndValues);
        binningX = imageBinX.getIntValue();
        binningY = imageBinY.getIntValue();

        if (driverInterface.updateCCDBin(binningX, binningY)) {
            imageBin.setState(PropertyStates.OK);

        } else
            imageBin.setState(PropertyStates.ALERT);
        try {
            updateProperty(imageBin);
        } catch (INDIException e) {
        }
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
        try {
            updateProperty(imageExposure);
        } catch (INDIException e) {
        }
    }

    private void newImageFrameValue(INDINumberElementAndValue[] elementsAndValues) {
        imageFrame.setValues(elementsAndValues);

        imageFrame.setState(PropertyStates.OK);

        LOG.log(Level.FINE, String.format("Requested CCD Frame is %4.0f,%4.0f %4.0f x %4.0f", imageFrameX.getIntValue(), imageFrameY.getIntValue(),
                imageFrameWidth.getIntValue(), imageFrameHeigth.getIntValue()));

        if (!driverInterface.updateCCDFrame(imageFrameX.getIntValue(), imageFrameY.getIntValue(), imageFrameWidth.getIntValue(), imageFrameHeigth.getIntValue()))
            imageFrame.setState(PropertyStates.ALERT);
        try {
            updateProperty(imageFrame);
        } catch (INDIException e) {
        }
    }

    private void newImagePixelSize(INDINumberElementAndValue[] elementsAndValues) {
        imagePixelSize.setValues(elementsAndValues);
        imagePixelSize.setState(PropertyStates.OK);
        xResolution = imagePixelSizeMaxX.getIntValue();
        yResolution = imagePixelSizeMaxY.getIntValue();
        pixelSizeX = imagePixelSizePixelSizeX.getValue().floatValue();
        pixelSizeY = imagePixelSizePixelSizeY.getValue().floatValue();
        try {
            updateProperty(imagePixelSize);
        } catch (INDIException e) {
        }
    }

    private void newRapidGuideDataValue(INDINumberElementAndValue[] elementsAndValues) {
        rapidGuideData.setState(PropertyStates.OK);
        rapidGuideData.setValues(elementsAndValues);
        try {
            updateProperty(rapidGuideData);
        } catch (INDIException e) {
        }
    }

    private void newRapidGuideSetupValue(INDISwitchElementAndValue[] elementsAndValues) {
        rapidGuideSetup.setValues(elementsAndValues);
        rapidGuideSetup.setState(PropertyStates.OK);

        autoLoop = rapidGuideSetupAutoLoop.getValue() == SwitchStatus.ON;
        sendImage = rapidGuideSetupSendImage.getValue() == SwitchStatus.ON;
        showMarker = rapidGuideSetupShowMarker.getValue() == SwitchStatus.ON;

        try {
            updateProperty(rapidGuideSetup);
        } catch (INDIException e) {
        }
    }

    private void newRapidGuideValue(INDISwitchElementAndValue[] elementsAndValues) {
        rapidGuide.setValues(elementsAndValues);
        rapidGuide.setState(PropertyStates.OK);
        rapidGuideEnabled = (rapidGuideEnable.getValue() == SwitchStatus.ON);

        if (rapidGuideEnabled) {
            driver.addProperty(rapidGuideSetup);
            driver.addProperty(rapidGuideData);
        } else {
            driver.removeProperty(rapidGuideSetup);
            driver.removeProperty(rapidGuideData);
        }

        try {
            updateProperty(rapidGuide);
        } catch (INDIException e) {
        }
    }

    /**
     * @brief getBinX Get horizontal binning of the CCD chip.
     * @return horizontal binning of the CCD chip.
     */
    int getBinX() {
        return binningX;
    }

    /**
     * @brief getBinY Get vertical binning of the CCD chip.
     * @return vertical binning of the CCD chip.
     */
    int getBinY() {
        return binningY;
    }

    /**
     * @brief getBPP Get CCD Chip depth (bits per pixel).
     * @return bits per pixel.
     */
    int getBPP() {
        return bitsPerPixel;
    }

    /**
     * @brief getExposureLeft Get exposure time left in seconds.
     * @return exposure time left in seconds.
     */
    double getExposureLeft() {
        return imageExposureDuration.getValue();
    }

    /**
     * @brief getExposureStartTime
     * @return exposure start time in ISO 8601 format.
     */
    private String getExposureStartTime() {
        return dateFormatISO8601.format(startExposureTime);
    }

    /**
     * @brief getFrameBuffer Get raw frame buffer of the CCD chip.
     * @return raw frame buffer of the CCD chip.
     */
    INDICCDImage getFrameBuffer() {
        return ccdImage;
    }

    /**
     * @brief getFrameTypeName returns CCD Frame type name
     * @param fType
     *            type of frame
     * @return CCD Frame type name
     */
    String getFrameTypeName(CcdFrame fType) {
        return fType.toString();
    }

    /**
     * @return Return image extension (fits, jpeg, raw..etc)
     */
    public String getImageExtension() {
        return imageExtention;
    }

    /**
     * @return Get number of FITS axis in image. By default 2
     */
    int getNAxis() {
        return NAxis;
    }

    /**
     * @brief getPixelSizeX Get horizontal pixel size in microns.
     * @return horizontal pixel size in microns.
     */
    float getPixelSizeX() {
        return pixelSizeX;
    }

    /**
     * @brief getPixelSizeY Get vertical pixel size in microns.
     * @return vertical pixel size in microns.
     */
    float getPixelSizeY() {
        return pixelSizeY;
    }

    /**
     * @brief getSubH Get the height of the frame
     * @return unbinned height of the frame
     */
    int getSubH() {
        return subframeHeight;
    }

    /**
     * @brief getSubW Get the width of the frame
     * @return unbinned width of the frame
     */
    int getSubW() {
        return subframeWidth;
    }

    /**
     * @brief getSubX Get the starting left coordinates (X) of the frame.
     * @return the starting left coordinates (X) of the image.
     */
    int getSubX() {
        return subframeX;
    }

    /**
     * @brief getSubY Get the starting top coordinates (Y) of the frame.
     * @return the starting top coordinates (Y) of the image.
     */
    int getSubY() {
        return subframeY;
    }

    /**
     * @brief getXRes Get the horizontal resolution in pixels of the CCD Chip.
     * @return the horizontal resolution of the CCD Chip.
     */
    int getXRes() {
        return xResolution;
    }

    /**
     * @brief Get the vertical resolution in pixels of the CCD Chip.
     * @return the horizontal resolution of the CCD Chip.
     */
    int getYRes() {
        return yResolution;
    }

    /**
     * @brief isCompressed
     * @return True if frame is compressed, false otherwise.
     */
    boolean isCompressed() {
        return sendCompressed;
    }

    /**
     * @return True if CCD is currently exposing, false otherwise.
     */
    boolean isExposing() {
        return (imageExposure.getState() == PropertyStates.BUSY);
    }

    /**
     * @brief setBin Set CCD Chip binnig
     * @param hor
     *            Horizontal binning.
     * @param ver
     *            Vertical binning.
     * @throws INDIException
     */
    void setBin(int hor, int ver) throws INDIException {
        binningX = hor;
        binningY = ver;

        imageBinX.setValue(binningX);
        imageBinY.setValue(binningY);
        this.updateProperty(imageBin);
    }

    /**
     * @brief setBPP Set depth of CCD chip.
     * @param bpp
     *            bits per pixel
     * @throws INDIException
     */
    void setBPP(int bpp) throws INDIException {
        bitsPerPixel = bpp;
        imagePixelSizeBitPerPixel.setValue(bpp);
        this.updateProperty(imagePixelSize);
    }

    /**
     * @brief Make CCD Info writable
     */
    void setCCDInfoWritable() {
        throw new UnsupportedOperationException("can not set property writable");
    }

    /**
     * @brief setCompressed Set whether a frame is compressed after exposure?
     * @param cmp
     *            If true, compress frame.
     */
    void setCompressed(boolean cmp) {

    }

    /**
     * @throws INDIException
     * @brief setExposureFailed Alert the client that the exposure failed.
     */
    void setExposureFailed() throws INDIException {
        imageExposure.setState(PropertyStates.ALERT);
        this.updateProperty(imageExposure);
    }

    /**
     * @brief setExposureLeft Update exposure time left. Inform the client of
     *        the new exposure time left value.
     * @param duration
     *            exposure duration left in seconds.
     */
    void setExposureLeft(double duration) throws INDIException {
        imageExposureDuration.setValue(duration);
        this.updateProperty(imageExposure);
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
    void setFrame(int subx, int suby, int subw, int subh) throws INDIException {
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
     * @brief setFrameBuffer Set raw frame buffer pointer.
     * @param buffer
     *            pointer to frame buffer /note CCD Chip allocates the frame
     *            buffer internally once SetFrameBufferSize is called with
     *            allocMem set to true which is the default behavior. If you
     *            allocated the memory yourself (i.e. allocMem is false), then
     *            you must call this function to set the pointer to the raw
     *            frame buffer.
     */
    void setFrameBuffer(INDICCDImage ccdImage) {
        this.ccdImage = ccdImage;
    }

    /**
     * @brief setFrameBufferSize Set desired frame buffer size. The function
     *        will allocate memory accordingly. The frame size depends on the
     *        desired frame resolution (Left, Top, Width, Height), depth of the
     *        CCD chip (bpp), and binning settings. You must set the frame size
     *        any time any of the prior parameters gets updated.
     * @param nbuf
     *            size of buffer in bytes.
     * @param allocMem
     *            if True, it will allocate memory of nbut size bytes.
     */
    void setFrameBufferSize(int nbuf) {
        throw new UnsupportedOperationException("sould not be nessesary");
    }

    /**
     * @brief setImageExtension Set image exntension
     * @param ext
     *            extension (fits, jpeg, raw..etc)
     */
   public void setImageExtension(String ext) {
        imageExtention = ext;
    }

    /**
     * @brief setMaxBin Set Maximum CCD Chip binning
     * @param max_hor
     *            Maximum horizontal binning
     * @param max_ver
     *            Maximum vertical binning
     * @throws INDIException
     */
    void setMaxBin(int max_hor, int max_ver) throws INDIException {
        imageBinX.setMax(max_hor);
        imageBinY.setMax(max_ver);
        this.updateProperty(imageBin, true, null);
    }

    /**
     * @brief setNAxis Set FITS number of axis
     * @param value
     *            number of axis
     */
    void setNAxis(int value) {
        NAxis = value;
    }

    /**
     * @brief setPixelSize Set CCD Chip pixel size
     * @param x
     *            Horziontal pixel size in microns.
     * @param y
     *            Vertical pixel size in microns.
     * @throws INDIException
     */
    void setPixelSize(float x, float y) throws INDIException {
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
    void setResolution(int x, int y) throws INDIException {
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

        fitsHeader.addValue("PIXSIZE1", getPixelSizeX(), "Pixel Size 1 (microns)");
        fitsHeader.addValue("PIXSIZE2", getPixelSizeY(), "Pixel Size 2 (microns)");
        fitsHeader.addValue("XBINNING", binningX, "Binning factor in width");
        fitsHeader.addValue("YBINNING", binningY, "Binning factor in height");
        fitsHeader.addValue("FRAME", currentFrameType.fitsValue(), "Frame Type");

        if (getNAxis() == 2) {
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

    /**
     * Uploads target Chip exposed buffer as FITS to the client.
     */
    protected boolean exposureComplete() {
        boolean sendImage = driver.shouldSendImage();
        boolean saveImage = driver.shouldSaveImage();
        boolean showMarker = false;
        boolean sendData = false;
        boolean autoLoop = false;
        if (rapidGuideEnabled) {
            autoLoop = this.autoLoop;
            sendImage = this.sendImage;
            showMarker = this.showMarker;
            sendData = true;
            saveImage = false;
        }
        if (sendData) {
            rapidGuideData.setState(PropertyStates.BUSY);
            int width = getSubW() / getBinX();
            int height = getSubH() / getBinY();
            INDICCDImage ccdImage = getFrameBuffer();
            // this calculation stuff should move to a separate utility class or
            // the ccdimage class, and done in a much more java way.
            int i[] = new int[9];
            int ix = 0, iy = 0;
            double average, fit, bestFit = 0;
            int minx = 4;
            int maxx = width - 4;
            int miny = 4;
            int maxy = height - 4;
            if (lastRapidX > 0 && lastRapidY > 0) {
                minx = Math.max(lastRapidX - 20, 4);
                maxx = Math.min(lastRapidX + 20, width - 4);
                miny = Math.max(lastRapidY - 20, 4);
                maxy = Math.min(lastRapidY + 20, height - 4);
            }
            int[][] src = ccdImage.subImageInt(minx - 4, miny - 4, maxx + 4, maxy + 4);
            for (int y = 4; y < (src.length - 4); y++) {
                for (int x = 4; x < (src[x].length - 4); x++) {
                    i[0] = i[1] = i[2] = i[3] = i[4] = i[5] = i[6] = i[7] = i[8] = 0;
                    for (int My = 0; My < PROXIMITY_MATRIX.length; My++) {
                        for (int Mx = 0; Mx < PROXIMITY_MATRIX[My].length; Mx++) {
                            i[PROXIMITY_MATRIX[My][Mx]] = src[y + My - 4][x + Mx - 4];
                        }
                    }
                    average = (i[0] + i[1] + i[2] + i[3] + i[4] + i[5] + i[6] + i[7] + i[8]) / 85.0;
                    fit = P0 * (i[0] - average) + //
                            P1 * (i[1] - 4 * average) + //
                            P2 * (i[2] - 4 * average) + //
                            P3 * (i[3] - 4 * average) + //
                            P4 * (i[4] - 8 * average) + //
                            P5 * (i[5] - 4 * average) + //
                            P6 * (i[6] - 4 * average) + //
                            P7 * (i[7] - 8 * average) + //
                            P8 * (i[8] - 48 * average);
                    if (bestFit < fit) {
                        bestFit = fit;
                        ix = x;
                        iy = y;
                    }
                }
            }
            double sumX = 0;
            double sumY = 0;
            double total = 0;
            for (int y = iy - 4; y <= iy + 4; y++) {
                for (int x = ix - 4; x <= ix + 4; x++) {
                    double w = src[y][x];
                    sumX += x * w;
                    sumY += y * w;
                    total += w;
                }
            }

            ix = ix + (minx - 4);
            iy = iy + (miny - 4);

            rapidGuideDataX.setValue(ix);
            rapidGuideDataY.setValue(iy);
            rapidGuideDataFIT.setValue(bestFit);
            lastRapidX = ix;
            lastRapidY = iy;
            if (bestFit > 50) {
                if (total > 0) {
                    rapidGuideDataX.setValue(sumX / total);
                    rapidGuideDataY.setValue(sumY / total);
                    rapidGuideData.setState(PropertyStates.OK);

                    LOG.log(Level.FINE, String.format("Guide Star X: %g Y: %g FIT: %g", rapidGuideDataX.getValue(), rapidGuideDataY.getValue(), rapidGuideDataFIT.getValue()));
                } else {
                    rapidGuideData.setState(PropertyStates.ALERT);
                    lastRapidX = lastRapidY = -1;
                }
            } else {
                rapidGuideData.setState(PropertyStates.ALERT);
                lastRapidX = lastRapidY = -1;
            }
            try {
                updateProperty(rapidGuideData);
            } catch (INDIException e) {
            }

            if (showMarker) {
                ccdImage.drawMarker(ix, iy);
            }
            if (sendImage || saveImage) {
                try {
                    if ("fits".equals((getImageExtension()))) {
                        Fits f = ccdImage.asFitsImage();
                        addFITSKeywords(f.getHDU(0));
                    }
                    uploadFile(sendImage, saveImage);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "could not send or save image", e);
                    return false;
                }
            }
        }
        imageExposure.setState(PropertyStates.OK);
        try {
            updateProperty(imageExposure);
        } catch (INDIException e) {
        }
        if (autoLoop) {
            imageExposureDuration.setValue(exposureTime);
            imageExposure.setState(PropertyStates.BUSY);
            if (driverInterface.startExposure(exposureTime))
                imageExposure.setState(PropertyStates.BUSY);
            else {
                LOG.log(Level.SEVERE, "Autoloop: Primary CCD Exposure Error!");
                imageExposure.setState(PropertyStates.ALERT);
            }
            try {
                updateProperty(imageExposure);
            } catch (INDIException e) {
            }
        }
        return true;
    }

    protected void uploadFile(boolean sendImage, boolean saveImage) throws Exception {
        if (saveImage) {
            File fp = driver.getFileWithIndex(getImageExtension());
            try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fp)))) {
                ccdImage.write(os, getImageExtension());
            }
        }
        if (sendImage) {
            if (sendCompressed) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new BufferedOutputStream(out))))) {
                    ccdImage.write(os, getImageExtension());
                }
                fitsImage.setValue(new INDIBLOBValue(out.toByteArray(), "." + getImageExtension() + ".z"));
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(out))) {
                    ccdImage.write(os, getImageExtension());
                }
                fitsImage.setValue(new INDIBLOBValue(out.toByteArray(), "." + getImageExtension()));
            }
            fits.setState(PropertyStates.OK);
            try {
                updateProperty(fits);
            } catch (INDIException e) {
            }
        }
    }

    @Override
    public void connect() {
        driver.addProperty(this.imageExposure);
        if (capability().canAbort())
            driver.addProperty(this.abort);
        if (!capability().canSubFrame()) {
            this.imageFrame.setPermission(PropertyPermissions.RO);
        }
        driver.addProperty(this.imageFrame);
        if (capability().canBin())
            driver.addProperty(this.imageBin);
        driver.addProperty(this.imagePixelSize);
        driver.addProperty(this.compress);
        driver.addProperty(this.fits);
        driver.addProperty(this.frameType);
        driver.addProperty(this.rapidGuide);
        if (rapidGuideEnabled) {
            driver.addProperty(this.rapidGuideSetup);
            driver.addProperty(this.rapidGuideData);
        }
    }

    @Override
    public void disconnect() {
        driver.removeProperty(this.imageExposure);
        if (capability().canAbort())
            driver.removeProperty(this.abort);
        if (!capability().canSubFrame()) {
            this.imageFrame.setPermission(PropertyPermissions.RO);
        }
        driver.removeProperty(this.imageFrame);
        if (capability().canBin())
            driver.removeProperty(this.imageBin);
        driver.removeProperty(this.imagePixelSize);
        driver.removeProperty(this.compress);
        driver.removeProperty(this.fits);
        driver.removeProperty(this.frameType);
        driver.removeProperty(this.rapidGuide);
        if (rapidGuideEnabled) {
            driver.removeProperty(this.rapidGuideSetup);
            driver.removeProperty(this.rapidGuideData);
        }
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
        setBPP(bpp);
    }

    public void setDriverInterface(CCDDriverInterface driverInterface) {
        this.driverInterface = driverInterface;
    }

    public void setScopeDirection(double ra, double dec) {
        this.ra = ra;
        this.dec = dec;
    }
}
