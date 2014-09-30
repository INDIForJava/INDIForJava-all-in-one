package laazotea.indi.driver;

import java.text.SimpleDateFormat;
import java.util.Date;

import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIException;
import laazotea.indi.driver.annotation.InjectElement;
import laazotea.indi.driver.annotation.InjectProperty;

public class CCDDriverExtention extends INDIDriverExtention<INDICCD> {

    private final SimpleDateFormat dateFormatISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @InjectProperty(name = "FRAME", label = "Frame", group = INDICCD.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDINumberProperty imageFrame;

    @InjectElement(name = "X", label = "Left", maximumD = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameX;

    @InjectElement(name = "Y", label = "Top", maximumD = 1040d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameY;

    @InjectElement(name = "WIDTH", label = "Width", valueD = 1392d, maximumD = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameWidth;

    @InjectElement(name = "HEIGHT", label = "Height", valueD = 1392d, maximumD = 1392d, numberFormat = "%4.0f")
    protected INDINumberElement imageFrameHeigth;

    @InjectProperty(name = "FRAME_TYPE", label = "Frame Type", group = INDICCD.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDISwitchProperty frameType;

    @InjectElement(name = "FRAME_LIGHT", label = "Light", switchValue = SwitchStatus.ON)
    protected INDISwitchElement frameTypeLight;

    @InjectElement(name = "FRAME_BIAS", label = "Bias", switchValue = SwitchStatus.OFF)
    protected INDISwitchElement frameTypeBais;

    @InjectElement(name = "FRAME_DARK", label = "Dark", switchValue = SwitchStatus.OFF)
    protected INDISwitchElement frameTypeDark;

    @InjectElement(name = "FRAME_FLAT", label = "Flat", switchValue = SwitchStatus.OFF)
    protected INDISwitchElement frameTypeFlat;

    @InjectProperty(name = "EXPOSURE", label = "Expose", group = INDICCD.MAIN_CONTROL_TAB)
    protected INDINumberProperty imageExposure;

    @InjectElement(name = "CCD_EXPOSURE_VALUE", label = "Duration (s)", valueD = 1, maximumD = 36000, numberFormat = "%5.2f")
    protected INDINumberElement imageExposureDuration;

    @InjectProperty(name = "ABORT_EXPOSURE", label = "Expose Abort", group = INDICCD.MAIN_CONTROL_TAB)
    protected INDISwitchProperty abort;

    @InjectElement(name = "ABORT", label = "Abort")
    protected INDISwitchElement abortSwitch;

    @InjectProperty(name = "BINNING", label = "Binning", group = INDICCD.IMAGE_SETTINGS_TAB, saveable = true)
    protected INDINumberProperty imageBin;

    @InjectElement(name = "HOR_BIN", label = "X", valueD = 1, maximumD = 4, minimumD = 1, stepD = 1, numberFormat = "%2.0f")
    protected INDINumberElement imageBinX;

    @InjectElement(name = "VER_BIN", label = "Y", valueD = 1, maximumD = 4, minimumD = 1, stepD = 1, numberFormat = "%2.0f")
    protected INDINumberElement imageBinY;

    @InjectProperty(name = "INFO", label = "CCD Information", group = INDICCD.IMAGE_INFO_TAB, permission = PropertyPermissions.RO)
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

    @InjectProperty(name = "COMPRESSION", label = "Image", group = INDICCD.IMAGE_SETTINGS_TAB)
    protected INDISwitchProperty compress;

    @InjectElement(name = "COMPRESS", label = "Compress", switchValue = SwitchStatus.ON)
    protected INDISwitchElement compressCompress;

    @InjectElement(name = "RAW", label = "Raw")
    protected INDISwitchElement compressRaw;

    @InjectProperty(name = "CCD", label = "Image Data", group = INDICCD.IMAGE_INFO_TAB, permission = PropertyPermissions.RO)
    protected INDIBLOBProperty fits;

    @InjectElement(name = "CCD", label = "Image")
    protected INDIBLOBElement fitsImage;

    @InjectProperty(name = "RAPID_GUIDE", label = "Rapid Guide", group = INDICCD.OPTIONS_TAB, timeout = 0)
    protected INDISwitchProperty rapidGuide;

    @InjectElement(name = "ENABLE", label = "Enable")
    protected INDISwitchElement rapidGuideEnable;

    @InjectElement(name = "DISABLE", label = "Disable", switchValue = SwitchStatus.ON)
    protected INDISwitchElement rapidGuideDisable;

    @InjectProperty(name = "RAPID_GUIDE_SETUP", label = "Rapid Guide Setup", group = INDICCD.RAPIDGUIDE_TAB, timeout = 0, switchRule = SwitchRules.ANY_OF_MANY)
    protected INDISwitchProperty rapidGuideSetup;

    @InjectElement(name = "AUTO_LOOP", label = "Auto loop", switchValue = SwitchStatus.ON)
    protected INDISwitchElement rapidGuideSetupAutoLoop;

    @InjectElement(name = "SEND_IMAGE", label = "Send image")
    protected INDISwitchElement rapidGuideSetupShowMarker;

    @InjectElement(name = "SHOW_MARKER", label = "Show marker")
    protected INDISwitchElement rapidGuideSetupSendImage;

    @InjectProperty(name = "RAPID_GUIDE_DATA", label = "Rapid Guide Data", group = INDICCD.RAPIDGUIDE_TAB, permission = PropertyPermissions.RO)
    protected INDINumberProperty rapidGuideData;

    @InjectElement(name = "GUIDESTAR_X", label = "Guide star position X", maximumD = 1024, numberFormat = "%%5.2f")
    protected INDINumberElement rapidGuideDataX;

    @InjectElement(name = "GUIDESTAR_Y", label = "Guide star position Y", maximumD = 1024, numberFormat = "%5.2f")
    protected INDINumberElement rapidGuideDataY;

    @InjectElement(name = "GUIDESTAR_FIT", label = "GUIDESTAR_FIT", maximumD = 1024, numberFormat = "%5.2f")
    protected INDINumberElement rapidGuideDataFIT;

    boolean sendCompressed = false;

    int XRes; // native resolution of the ccd

    int YRes; // ditto

    int SubX; // left side of the subframe we are requesting

    int SubY; // top of the subframe requested

    int SubW; // UNBINNED width of the subframe

    int SubH; // UNBINNED height of the subframe

    int BinX = 1; // Binning requested in the x direction

    int BinY = 1; // Binning requested in the Y direction

    int NAxis = 2; // # of Axis

    float PixelSizex; // pixel size in microns, x direction

    float PixelSizey; // pixel size in microns, y direction

    int BPP = 8; // Bytes per Pixel

    boolean Interlaced = false;

    int RawFrameSize = 0;

    CCD_FRAME FrameType = CCD_FRAME.LIGHT_FRAME;

    double exposureDuration;

    Date startExposureTime;

    int lastRapidX;

    int lastRapidY;

    INDICCDImage ccdImage;

    String imageExtention = "fits";

    /**
     * @brief getXRes Get the horizontal resolution in pixels of the CCD Chip.
     * @return the horizontal resolution of the CCD Chip.
     */
    int getXRes() {
        return XRes;
    }

    /**
     * @brief Get the vertical resolution in pixels of the CCD Chip.
     * @return the horizontal resolution of the CCD Chip.
     */
    int getYRes() {
        return YRes;
    }

    /**
     * @brief getSubX Get the starting left coordinates (X) of the frame.
     * @return the starting left coordinates (X) of the image.
     */
    int getSubX() {
        return SubX;
    }

    /**
     * @brief getSubY Get the starting top coordinates (Y) of the frame.
     * @return the starting top coordinates (Y) of the image.
     */
    int getSubY() {
        return SubY;
    }

    /**
     * @brief getSubW Get the width of the frame
     * @return unbinned width of the frame
     */
    int getSubW() {
        return SubW;
    }

    /**
     * @brief getSubH Get the height of the frame
     * @return unbinned height of the frame
     */
    int getSubH() {
        return SubH;
    }

    /**
     * @brief getBinX Get horizontal binning of the CCD chip.
     * @return horizontal binning of the CCD chip.
     */
    int getBinX() {
        return BinX;
    }

    /**
     * @brief getBinY Get vertical binning of the CCD chip.
     * @return vertical binning of the CCD chip.
     */
    int getBinY() {
        return BinY;
    }

    /**
     * @brief getPixelSizeX Get horizontal pixel size in microns.
     * @return horizontal pixel size in microns.
     */
    float getPixelSizeX() {
        return PixelSizex;
    }

    /**
     * @brief getPixelSizeY Get vertical pixel size in microns.
     * @return vertical pixel size in microns.
     */
    float getPixelSizeY() {
        return PixelSizey;
    }

    /**
     * @brief getBPP Get CCD Chip depth (bits per pixel).
     * @return bits per pixel.
     */
    int getBPP() {
        return BPP;
    }

    /**
     * @brief getFrameBufferSize Get allocated frame buffer size to hold the CCD
     *        image frame.
     * @return allocated frame buffer size to hold the CCD image frame.
     */
    int getFrameBufferSize() {
        return RawFrameSize;
    }

    /**
     * @brief getExposureLeft Get exposure time left in seconds.
     * @return exposure time left in seconds.
     */
    double getExposureLeft() {
        return imageExposureDuration.getValue();
    }

    /**
     * @brief getExposureDuration Get requested exposure duration for the CCD
     *        chip in seconds.
     * @return requested exposure duration for the CCD chip in seconds.
     */
    double getExposureDuration() {
        return exposureDuration;
    }

    /**
     * @brief getExposureStartTime
     * @return exposure start time in ISO 8601 format.
     */
    String getExposureStartTime() {
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
     * @brief isCompressed
     * @return True if frame is compressed, false otherwise.
     */
    boolean isCompressed() {
        return sendCompressed;
    }

    /**
     * @brief isInterlaced
     * @return True if CCD chip is Interlaced, false otherwise.
     */
    boolean isInterlaced() {
        return Interlaced;
    }

    /**
     * @brief getFrameType
     * @return CCD Frame type
     */
    CCD_FRAME getFrameType() {
        return FrameType;
    }

    /**
     * @brief getFrameTypeName returns CCD Frame type name
     * @param fType
     *            type of frame
     * @return CCD Frame type name
     */
    String getFrameTypeName(CCD_FRAME fType) {
        return fType.toString();
    }

    /**
     * @brief Make CCD Info writable
     */
    void setCCDInfoWritable() {
        throw new UnsupportedOperationException("can not set property writable");
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
        XRes = x;
        YRes = y;

        imagePixelSizeMaxX.setValue(x);
        imagePixelSizeMaxY.setValue(y);
        this.driver.updateProperty(imagePixelSize);

        imageFrameX.setMin(0);
        imageFrameX.setMax(x - 1);
        imageFrameY.setMin(0);
        imageFrameY.setMax(y - 1);
        imageFrameWidth.setMin(1);
        imageFrameWidth.setMax(x);
        imageFrameHeigth.setMin(1);
        imageFrameHeigth.setMax(y);
        this.driver.updateProperty(imageFrame, true, null);
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
        SubX = subx;
        SubY = suby;
        SubW = subw;
        SubH = subh;

        imageFrameX.setValue(SubX);
        imageFrameY.setValue(SubY);
        imageFrameWidth.setValue(SubW);
        imageFrameHeigth.setValue(SubH);
        this.driver.updateProperty(imageFrame);
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
        BinX = hor;
        BinY = ver;

        imageBinX.setValue(BinX);
        imageBinY.setValue(BinY);
        this.driver.updateProperty(imageBin);
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
        this.driver.updateProperty(imageBin, true, null);
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
        PixelSizex = x;
        PixelSizey = y;

        imagePixelSizePixelSize.setValue(x);
        imagePixelSizePixelSizeX.setValue(x);
        imagePixelSizePixelSizeY.setValue(y);
        this.driver.updateProperty(imagePixelSize);
    }

    /**
     * @brief setCompressed Set whether a frame is compressed after exposure?
     * @param cmp
     *            If true, compress frame.
     */
    void setCompressed(boolean cmp) {

    }

    /**
     * @brief setInterlaced Set whether the CCD chip is interlaced or not?
     * @param intr
     *            If true, the CCD chip is interlaced.
     */
    void setInterlaced(boolean intr) {
        this.Interlaced = intr;
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
     * @brief setBPP Set depth of CCD chip.
     * @param bpp
     *            bits per pixel
     * @throws INDIException
     */
    void setBPP(int bpp) throws INDIException {
        BPP = bpp;
        imagePixelSizeBitPerPixel.setValue(bpp);
        this.driver.updateProperty(imagePixelSize);
    }

    /**
     * @brief setFrameType Set desired frame type for next exposure.
     * @param type
     *            desired CCD frame type.
     */
    void setFrameType(CCD_FRAME type) {

    }

    /**
     * @brief setExposureDuration Set desired CCD frame exposure duration for
     *        next exposure. You must call this function immediately before
     *        starting the actual exposure as it is used to calculate the
     *        timestamp used for the FITS header.
     * @param duration
     *            exposure duration in seconds.
     * @throws INDIException
     */
    void setExposureDuration(double duration) {
        exposureDuration = duration;
        startExposureTime = new Date();
    }

    /**
     * @brief setExposureLeft Update exposure time left. Inform the client of
     *        the new exposure time left value.
     * @param duration
     *            exposure duration left in seconds.
     */
    void setExposureLeft(double duration) throws INDIException {
        imageExposureDuration.setValue(duration);
        this.driver.updateProperty(imageExposure);
    }

    /**
     * @throws INDIException
     * @brief setExposureFailed Alert the client that the exposure failed.
     */
    void setExposureFailed() throws INDIException {
        imageExposure.setState(PropertyStates.ALERT);
        this.driver.updateProperty(imageExposure);
    }

    /**
     * @return Get number of FITS axis in image. By default 2
     */
    int getNAxis() {
        return NAxis;
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
     * @brief setImageExtension Set image exntension
     * @param ext
     *            extension (fits, jpeg, raw..etc)
     */
    void setImageExtension(String ext) {
        imageExtention = ext;
    }

    /**
     * @return Return image extension (fits, jpeg, raw..etc)
     */
    String getImageExtension() {
        return imageExtention;
    }

    /**
     * @return True if CCD is currently exposing, false otherwise.
     */
    boolean isExposing() {
        return (imageExposure.getState() == PropertyStates.BUSY);
    }

    public CCDDriverExtention(INDICCD indiccd) {
        super(indiccd);
    }
}
