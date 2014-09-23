package laazotea.indi.driver;

import static laazotea.indi.Constants.PropertyPermissions.RW;
import static laazotea.indi.Constants.PropertyStates.IDLE;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.HeaderCardException;

public abstract class INDICCD extends INDIDriver implements INDIConnectionHandler, INDIGuiderInterface {

    private static final Logger LOG = Logger.getLogger(INDICCD.class.getName());

    protected enum CCD_FRAME {
        LIGHT_FRAME("Light"),
        BIAS_FRAME("Bias"),
        DARK_FRAME("Dark"),
        FLAT_FRAME("Flat Field");

        private CCD_FRAME(String fitsName) {
            this.fitsName = fitsName;
        }

        private final String fitsName;

        public String fitsValue() {
            return fitsName;
        }

    }

    protected enum CCD_FRAME_INDEX {
        FRAME_X,
        FRAME_Y,
        FRAME_W,
        FRAME_H
    }

    protected enum CCD_BIN_INDEX {
        BIN_W,
        BIN_H
    }

    protected enum CCD_INFO_INDEX {
        CCD_MAX_X,
        CCD_MAX_Y,
        CCD_PIXEL_SIZE,
        CCD_PIXEL_SIZE_X,
        CCD_PIXEL_SIZE_Y,
        CCD_BITSPERPIXEL
    }

    protected static class Capability {

        public boolean isHasGuideHead() {
            return hasGuideHead;
        }

        public void setHasGuideHead(boolean hasGuideHead) {
            this.hasGuideHead = hasGuideHead;
        }

        public boolean isHasST4Port() {
            return hasST4Port;
        }

        public void setHasST4Port(boolean hasST4Port) {
            this.hasST4Port = hasST4Port;
        }

        public boolean isHasShutter() {
            return hasShutter;
        }

        public void setHasShutter(boolean hasShutter) {
            this.hasShutter = hasShutter;
        }

        public boolean isHasCooler() {
            return hasCooler;
        }

        public void setHasCooler(boolean hasCooler) {
            this.hasCooler = hasCooler;
        }

        public boolean isCanBin() {
            return canBin;
        }

        public void setCanBin(boolean canBin) {
            this.canBin = canBin;
        }

        public boolean isCanSubFrame() {
            return canSubFrame;
        }

        public void setCanSubFrame(boolean canSubFrame) {
            this.canSubFrame = canSubFrame;
        }

        public boolean isCanAbort() {
            return canAbort;
        }

        public void setCanAbort(boolean canAbort) {
            this.canAbort = canAbort;
        }

        private boolean hasGuideHead = false;

        private boolean hasST4Port = false;

        private boolean hasShutter = false;

        private boolean hasCooler = false;

        private boolean canBin = false;

        private boolean canSubFrame = false;

        private boolean canAbort = false;
    }

    protected class CCD {

        private final SimpleDateFormat dateFormatISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        private final INDINumberProperty imageFrame;

        private final INDINumberElement imageFrameX;

        private final INDINumberElement imageFrameY;

        private final INDINumberElement imageFrameWidth;

        private final INDINumberElement imageFrameHeigth;

        private final INDISwitchProperty frameType;

        private final INDISwitchElement frameTypeLight;

        private final INDISwitchElement frameTypeDark;

        private final INDISwitchElement frameTypeFlat;

        private final INDISwitchElement frameTypeBais;

        private final INDINumberProperty imageExposure;

        private final INDINumberElement imageExposureDuration;

        private final INDISwitchProperty abort;

        private final INDISwitchElement abortSwitch;

        private final INDINumberProperty imageBin;

        private final INDINumberElement imageBinX;

        private final INDINumberElement imageBinY;

        private final INDINumberProperty imagePixelSize;

        private final INDINumberElement imagePixelSizeMaxX;

        private final INDINumberElement imagePixelSizeMaxY;

        private final INDINumberElement imagePixelSizePixelSize;

        private final INDINumberElement imagePixelSizePixelSizeX;

        private final INDINumberElement imagePixelSizePixelSizeY;

        private final INDINumberElement imagePixelSizeBitPerPixel;

        private final INDISwitchProperty compress;

        private final INDISwitchElement compressCompress;

        private final INDISwitchElement compressRaw;

        private boolean sendCompressed = false;

        private final INDIBLOBProperty fits;

        private final INDIBLOBElement fitsImage;

        private final INDISwitchProperty rapidGuide;

        private final INDISwitchElement rapidGuideEnable;

        private final INDISwitchElement rapidGuideDisable;

        private final INDISwitchProperty rapidGuideSetup;

        private final INDISwitchElement rapidGuideSetupAutoLoop;

        private final INDISwitchElement rapidGuideSetupShowMarker;

        private final INDISwitchElement rapidGuideSetupSendImage;

        private final INDINumberProperty rapidGuideData;

        private final INDINumberElement rapidGuideDataX;

        private final INDINumberElement rapidGuideDataY;

        private final INDINumberElement rapidGuideDataFIT;

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
         * @brief getXRes Get the horizontal resolution in pixels of the CCD
         *        Chip.
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
         * @brief getFrameBufferSize Get allocated frame buffer size to hold the
         *        CCD image frame.
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
         * @brief getExposureDuration Get requested exposure duration for the
         *        CCD chip in seconds.
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
         *            allocated the memory yourself (i.e. allocMem is false),
         *            then you must call this function to set the pointer to the
         *            raw frame buffer.
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
            updateProperty(imagePixelSize);

            imageFrameX.setMin(0);
            imageFrameX.setMax(x - 1);
            imageFrameY.setMin(0);
            imageFrameY.setMax(y - 1);
            imageFrameWidth.setMin(1);
            imageFrameWidth.setMax(x);
            imageFrameHeigth.setMin(1);
            imageFrameHeigth.setMax(y);
            updateProperty(imageFrame, true, null);
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
            updateProperty(imageFrame);
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
            updateProperty(imageBin);
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
            updateProperty(imageBin, true, null);
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
            updateProperty(imagePixelSize);
        }

        /**
         * @brief setCompressed Set whether a frame is compressed after
         *        exposure?
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
         *        will allocate memory accordingly. The frame size depends on
         *        the desired frame resolution (Left, Top, Width, Height), depth
         *        of the CCD chip (bpp), and binning settings. You must set the
         *        frame size any time any of the prior parameters gets updated.
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
            updateProperty(imagePixelSize);
        }

        /**
         * @brief setFrameType Set desired frame type for next exposure.
         * @param type
         *            desired CCD frame type.
         */
        void setFrameType(CCD_FRAME type) {

        }

        /**
         * @brief setExposureDuration Set desired CCD frame exposure duration
         *        for next exposure. You must call this function immediately
         *        before starting the actual exposure as it is used to calculate
         *        the timestamp used for the FITS header.
         * @param duration
         *            exposure duration in seconds.
         * @throws INDIException
         */
        void setExposureDuration(double duration) {
            exposureDuration = duration;
            startExposureTime = new Date();
        }

        /**
         * @brief setExposureLeft Update exposure time left. Inform the client
         *        of the new exposure time left value.
         * @param duration
         *            exposure duration left in seconds.
         */
        void setExposureLeft(double duration) throws INDIException {
            imageExposureDuration.setValue(duration);
            updateProperty(imageExposure);
        }

        /**
         * @throws INDIException
         * @brief setExposureFailed Alert the client that the exposure failed.
         */
        void setExposureFailed() throws INDIException {
            imageExposure.setState(PropertyStates.ALERT);
            updateProperty(imageExposure);
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

        public CCD(String prefix, int nr, InputStream inputStream, OutputStream outputStream) {
            this.imageFrame = new INDINumberProperty(INDICCD.this, prefix + "FRAME", "Frame", INDICCD.IMAGE_SETTINGS_TAB, IDLE, RW, 60);
            this.imageFrameX = new INDINumberElement(INDICCD.this.temperature, "X", "Left", 0, 0, 1392, 0, "%4.0f");
            this.imageFrameY = new INDINumberElement(INDICCD.this.temperature, "Y", "Top", 0, 0, 1040, 0, "%4.0f");
            this.imageFrameWidth = new INDINumberElement(INDICCD.this.temperature, "WIDTH", "Width", 1392, 0, 1392, 0, "%4.0f");
            this.imageFrameHeigth = new INDINumberElement(INDICCD.this.temperature, "HEIGHT", "Height", 1392, 0, 1392, 0, "%4.0f");
            this.imageFrame.setSaveable(true);

            this.frameType = new INDISwitchProperty(INDICCD.this, prefix + "FRAME_TYPE", "Frame Type", INDICCD.IMAGE_SETTINGS_TAB, IDLE, RW, 60, SwitchRules.ONE_OF_MANY);
            this.frameTypeLight = new INDISwitchElement(this.frameType, "FRAME_LIGHT", "Light", SwitchStatus.ON);
            this.frameTypeBais = new INDISwitchElement(this.frameType, "FRAME_BIAS", "Bias", SwitchStatus.OFF);
            this.frameTypeDark = new INDISwitchElement(this.frameType, "FRAME_DARK", "Dark", SwitchStatus.OFF);
            this.frameTypeFlat = new INDISwitchElement(this.frameType, "FRAME_FLAT", "Flat", SwitchStatus.OFF);

            this.imageExposure = new INDINumberProperty(INDICCD.this, prefix + "EXPOSURE", "Expose", INDICCD.MAIN_CONTROL_TAB, IDLE, RW, 60);
            this.imageExposureDuration = new INDINumberElement(this.imageExposure, "CCD_EXPOSURE_VALUE", "Duration (s)", 1, 0, 36000, 0, "%5.2f");

            this.abort = new INDISwitchProperty(INDICCD.this, prefix + "ABORT_EXPOSURE", "Expose Abort", INDICCD.MAIN_CONTROL_TAB, IDLE, RW, 60, SwitchRules.ONE_OF_MANY);
            this.abortSwitch = new INDISwitchElement(this.abort, "ABORT", "Abort", SwitchStatus.OFF);

            this.imageBin = new INDINumberProperty(INDICCD.this, prefix + "BINNING", "Binning", INDICCD.IMAGE_SETTINGS_TAB, IDLE, RW, 60);
            this.imageBinX = new INDINumberElement(this.imageBin, "HOR_BIN", "X", 1, 4, 1, 1, "%2.0f");
            this.imageBinY = new INDINumberElement(this.imageBin, "VER_BIN", "X", 1, 4, 1, 1, "%2.0f");
            this.imageBin.setSaveable(true);

            this.imagePixelSize = new INDINumberProperty(INDICCD.this, prefix + "INFO", "CCD Information", INDICCD.IMAGE_INFO_TAB, IDLE, PropertyPermissions.RO, 60);
            this.imagePixelSizeMaxX = new INDINumberElement(this.imagePixelSize, "CCD_MAX_X", "Resolution x", 1392.0, 1, 16000, 0, "%4.0f");
            this.imagePixelSizeMaxY = new INDINumberElement(this.imagePixelSize, "CCD_MAX_Y", "Resolution y", 1392.0, 1, 16000, 0, "%4.0f");
            this.imagePixelSizePixelSize = new INDINumberElement(this.imagePixelSize, "CCD_PIXEL_SIZE", "Pixel size (um)", 6.45, 1, 40, 0, "%4.0f");
            this.imagePixelSizePixelSizeX = new INDINumberElement(this.imagePixelSize, "CCD_PIXEL_SIZE_X", "Pixel size X", 6.45, 1, 40, 0, "%4.0f");
            this.imagePixelSizePixelSizeY = new INDINumberElement(this.imagePixelSize, "CCD_PIXEL_SIZE_Y", "Pixel size Y", 6.45, 1, 40, 0, "%4.0f");
            this.imagePixelSizeBitPerPixel = new INDINumberElement(this.imagePixelSize, "CCD_BITSPERPIXEL", "Bits per pixel", 8, 8, 64, 0, "%4.0f");

            this.compress = new INDISwitchProperty(INDICCD.this, prefix + "COMPRESSION", "Image", INDICCD.IMAGE_SETTINGS_TAB, IDLE, RW, 60, SwitchRules.ONE_OF_MANY);
            this.compressCompress = new INDISwitchElement(this.compress, "COMPRESS", "Compress", SwitchStatus.ON);
            this.compressRaw = new INDISwitchElement(this.compress, "RAW", "Raw", SwitchStatus.OFF);
            this.compress.setSaveable(true);
            this.sendCompressed = true;

            this.fits = new INDIBLOBProperty(INDICCD.this, "CCD" + nr, "Image Data", INDICCD.IMAGE_INFO_TAB, IDLE, PropertyPermissions.RO, 60);
            this.fitsImage = new INDIBLOBElement(this.fits, "CCD" + nr, "Image");

            this.rapidGuide = new INDISwitchProperty(INDICCD.this, prefix + "RAPID_GUIDE", "Rapid Guide", INDICCD.OPTIONS_TAB, IDLE, RW, 0, SwitchRules.ONE_OF_MANY);
            this.rapidGuideEnable = new INDISwitchElement(this.rapidGuide, "ENABLE", "Enable", SwitchStatus.OFF);
            this.rapidGuideDisable = new INDISwitchElement(this.rapidGuide, "DISABLE", "Disable", SwitchStatus.ON);

            this.rapidGuideSetup =
                    new INDISwitchProperty(INDICCD.this, prefix + "RAPID_GUIDE_SETUP", "Rapid Guide Setup", INDICCD.RAPIDGUIDE_TAB, IDLE, RW, 0, SwitchRules.ANY_OF_MANY);
            this.rapidGuideSetupAutoLoop = new INDISwitchElement(this.rapidGuideSetup, "AUTO_LOOP", "Auto loop", SwitchStatus.ON);
            this.rapidGuideSetupSendImage = new INDISwitchElement(this.rapidGuideSetup, "SEND_IMAGE", "Send image", SwitchStatus.OFF);
            this.rapidGuideSetupShowMarker = new INDISwitchElement(this.rapidGuideSetup, "SHOW_MARKER", "Show marker", SwitchStatus.OFF);

            this.rapidGuideData =
                    new INDINumberProperty(INDICCD.this, prefix + "RAPID_GUIDE_DATA", "Rapid Guide Data", INDICCD.RAPIDGUIDE_TAB, IDLE, PropertyPermissions.RO, 60);
            this.rapidGuideDataX = new INDINumberElement(this.imagePixelSize, "GUIDESTAR_X", "Guide star position X", 0, 0, 1024, 0, "%5.2f");
            this.rapidGuideDataY = new INDINumberElement(this.imagePixelSize, "GUIDESTAR_Y", "Guide star position Y", 0, 0, 1024, 0, "%5.2f");
            this.rapidGuideDataFIT = new INDINumberElement(this.imagePixelSize, "GUIDESTAR_FIT", "Guide star fit", 0, 0, 1024, 0, "%5.2f");
        }
    }

    protected static final String MAIN_CONTROL_TAB = "Main Control";

    protected static final String IMAGE_SETTINGS_TAB = "Image Settings";

    protected static final String IMAGE_INFO_TAB = "Image Info";

    protected static final String GUIDE_HEAD_TAB = "Guider Head";

    protected static final String GUIDE_CONTROL_TAB = "Guider Control";

    protected static final String RAPIDGUIDE_TAB = "Rapid Guide";

    protected static final String OPTIONS_TAB = "Options";

    private final INDINumberProperty temperature;

    private final INDINumberElement temperatureTemp;

    private final INDISwitchProperty upload;

    private final INDISwitchElement uploadClient;

    private final INDISwitchElement uploadLocal;

    private final INDISwitchElement uploadBoth;

    private final INDITextProperty uploadSettings;

    private final INDITextElement uploadSettingsDir;

    private final INDITextElement uploadSettingsPrefix;

    private final INDINumberProperty eqn;

    private final INDINumberElement eqnRa;

    private final INDINumberElement eqnDec;

    private final INDITextProperty activeDevice;

    private final INDITextElement activeDeviceTelescope;

    private final INDITextElement activeDeviceFocuser;

    private final CCD primaryCCD;

    private final CCD guiderCCD;

    private final Capability capability = new Capability();

    private double RA = -1000d;

    private double Dec = -1000d;

    private boolean InExposure = false;

    private boolean InGuideExposure = false;

    private boolean RapidGuideEnabled = false;

    private boolean GuiderRapidGuideEnabled = false;

    private boolean AutoLoop = false;

    private boolean GuiderAutoLoop = false;

    private boolean SendImage = false;

    private boolean GuiderSendImage = false;

    private boolean ShowMarker = false;

    private boolean GuiderShowMarker = false;

    private float ExposureTime = 0.0f;

    private float GuiderExposureTime = 0.0f;

    private final INDIGuider guider;

    public INDICCD(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);

        // CCD Temperature
        this.temperature = new INDINumberProperty(this, "CCD_TEMPERATURE", "Temperature", INDICCD.MAIN_CONTROL_TAB, IDLE, RW, 60);
        this.temperatureTemp = new INDINumberElement(this.temperature, "CCD_TEMPERATURE_VALUE", "Temperature (C)", 0d, -50d, 50d, 0d, "%5.2f");

        // PRIMARY CCD Init
        this.primaryCCD = new CCD("CCD_", 1, inputStream, outputStream);

        // GUIDER CCD Init
        this.guiderCCD = new CCD("GUIDER_", 2, inputStream, outputStream);

        // CCD Class Init
        this.upload = new INDISwitchProperty(this, "UPLOAD_MODE", "Upload", INDICCD.OPTIONS_TAB, IDLE, RW, 0, SwitchRules.ONE_OF_MANY);
        this.uploadClient = new INDISwitchElement(this.upload, "UPLOAD_CLIENT", "Client", SwitchStatus.ON);
        this.uploadLocal = new INDISwitchElement(this.upload, "UPLOAD_LOCAL", "Local", SwitchStatus.OFF);
        this.uploadBoth = new INDISwitchElement(this.upload, "UPLOAD_BOTH", "Both", SwitchStatus.OFF);
        this.upload.setSaveable(true);

        this.uploadSettings = new INDITextProperty(this, "UPLOAD_SETTINGS", "Upload Settings", INDICCD.OPTIONS_TAB, IDLE, RW, 60);
        this.uploadSettingsDir = new INDITextElement(this.uploadSettings, "UPLOAD_DIR", "Dir", "");
        this.uploadSettingsPrefix = new INDITextElement(this.uploadSettings, "UPLOAD_PREFIX", "Prefix", "IMAGE_XX");
        this.uploadSettings.setSaveable(true);

        this.activeDevice = new INDITextProperty(this, "ACTIVE_DEVICES", "Snoop devices", INDICCD.OPTIONS_TAB, IDLE, RW, 60);
        this.activeDeviceTelescope = new INDITextElement(this.uploadSettings, "ACTIVE_TELESCOPE", "Telescope", "Telescope Simulator");
        this.activeDeviceFocuser = new INDITextElement(this.uploadSettings, "ACTIVE_FOCUSER", "Focuser", "Focuser Simulator");
        this.activeDevice.setSaveable(true);

        this.eqn = new INDINumberProperty(this, "EQUATORIAL_EOD_COORD", "EQ Coord", INDICCD.MAIN_CONTROL_TAB, IDLE, RW, 60);
        this.eqnRa = new INDINumberElement(this.eqn, "RA", "Ra (hh:mm:ss)", 0, 0, 24, 0, "%010.6m");
        this.eqnDec = new INDINumberElement(this.eqn, "DEC", "Dec (dd:mm:ss)", 0, -90, 90, 0, "%010.6m");

        // IDSnoopDevice(ActiveDeviceT[0].text,"EQUATORIAL_EOD_COORD");
        this.guider = new INDIGuider(this, this, GUIDE_CONTROL_TAB);
        updateProperties();
    }

    protected void updateProperties() {
        if (isConnected()) {
            addProperty(primaryCCD.imageExposure);

            if (capability.canAbort)
                addProperty(primaryCCD.abort);
            if (!capability.canSubFrame) {
                primaryCCD.imageFrame.setPermission(PropertyPermissions.RO);
            }
            addProperty(primaryCCD.imageFrame);
            if (capability.canBin)
                addProperty(primaryCCD.imageBin);

            if (capability.hasGuideHead) {
                addProperty(guiderCCD.imageExposure);
                if (capability.canAbort)
                    addProperty(guiderCCD.abort);
                if (!capability.canSubFrame)
                    guiderCCD.imageFrame.setPermission(PropertyPermissions.RO);
                addProperty(guiderCCD.imageFrame);
            }

            if (capability.hasCooler)
                addProperty(temperature);

            addProperty(primaryCCD.imagePixelSize);
            if (capability.hasGuideHead) {
                addProperty(guiderCCD.imagePixelSize);
                if (capability.canBin)
                    addProperty(guiderCCD.imageBin);
            }
            addProperty(primaryCCD.compress);
            addProperty(primaryCCD.fits);
            if (capability.hasGuideHead) {
                addProperty(guiderCCD.compress);
                addProperty(guiderCCD.fits);
            }
            if (capability.hasST4Port) {
                addProperty(this.guider.getGuideNS());
                addProperty(this.guider.getGuideWE());
            }
            addProperty(primaryCCD.frameType);

            if (capability.hasGuideHead)
                addProperty(guiderCCD.frameType);

            addProperty(primaryCCD.rapidGuide);

            if (capability.hasGuideHead)
                addProperty(guiderCCD.rapidGuide);

            if (RapidGuideEnabled) {
                addProperty(primaryCCD.rapidGuideSetup);
                addProperty(primaryCCD.rapidGuideData);
            }
            if (GuiderRapidGuideEnabled) {
                addProperty(guiderCCD.rapidGuideSetup);
                addProperty(guiderCCD.rapidGuideData);
            }
            addProperty(activeDevice);
            addProperty(upload);

            if (uploadSettingsDir.getValue() == null) {
                uploadSettingsDir.setValue(System.getProperty("user.home"));
            }
            addProperty(uploadSettings);
        } else {
            removeProperty(primaryCCD.imageFrame);
            removeProperty(primaryCCD.imagePixelSize);

            if (capability.canBin)
                removeProperty(primaryCCD.imageBin);

            removeProperty(primaryCCD.imageExposure);
            if (capability.canAbort)
                removeProperty(primaryCCD.abort);
            removeProperty(primaryCCD.fits);
            removeProperty(primaryCCD.compress);
            removeProperty(primaryCCD.rapidGuide);
            if (RapidGuideEnabled) {
                removeProperty(primaryCCD.rapidGuideSetup);
                removeProperty(primaryCCD.rapidGuideData);
            }
            if (capability.hasGuideHead) {
                removeProperty(guiderCCD.imageExposure);
                if (capability.canAbort)
                    removeProperty(guiderCCD.abort);
                removeProperty(guiderCCD.imageFrame);
                removeProperty(guiderCCD.imagePixelSize);

                removeProperty(guiderCCD.fits);
                if (capability.canBin)
                    removeProperty(guiderCCD.imageBin);
                removeProperty(guiderCCD.compress);
                removeProperty(guiderCCD.frameType);
                removeProperty(guiderCCD.rapidGuide);
                if (GuiderRapidGuideEnabled) {
                    removeProperty(guiderCCD.rapidGuideSetup);
                    removeProperty(guiderCCD.rapidGuideData);
                }
            }
            if (capability.hasCooler)
                removeProperty(temperature);
            if (capability.hasST4Port) {
                removeProperty(this.guider.getGuideNS());
                removeProperty(this.guider.getGuideWE());
            }
            removeProperty(primaryCCD.frameType);
            removeProperty(activeDevice);
            removeProperty(upload);
            removeProperty(uploadSettings);
        }
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        updateProperties();
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        updateProperties();
    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {

    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
        // This is for our device
        // Now lets see if it's something we process here
        if (property == primaryCCD.imageExposure) {
            property.setValues(elementsAndValues);
            if (property.getState() == PropertyStates.BUSY)
                abortExposure();
            if (startExposure(primaryCCD.exposureDuration))
                primaryCCD.imageExposure.setState(PropertyStates.BUSY);
            else
                primaryCCD.imageExposure.setState(PropertyStates.ALERT);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }
        if (property == guiderCCD.imageExposure) {
            property.setValues(elementsAndValues);

            if (property.getState() == PropertyStates.BUSY)
                abortGuideExposure();
            if (startGuideExposure(guiderCCD.exposureDuration))
                guiderCCD.imageExposure.setState(PropertyStates.BUSY);
            else
                guiderCCD.imageExposure.setState(PropertyStates.ALERT);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == primaryCCD.imageBin) {
            // We are being asked to set camera binning
            if (!canBin()) {
                primaryCCD.imageBin.setState(PropertyStates.ALERT);
                try {
                    updateProperty(property);
                } catch (INDIException e) {
                }
                return;
            }
            property.setValues(elementsAndValues);
            primaryCCD.BinX = primaryCCD.imageBinX.getIntValue();
            primaryCCD.BinY = primaryCCD.imageBinY.getIntValue();

            if (updateCCDBin(primaryCCD.BinX, primaryCCD.BinY)) {
                primaryCCD.imageBin.setState(PropertyStates.OK);

            } else
                primaryCCD.imageBin.setState(PropertyStates.ALERT);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
            return;
        }

        if (property == guiderCCD.imageBin) {
            // We are being asked to set camera binning
            if (!canBin()) {
                guiderCCD.imageBin.setState(PropertyStates.ALERT);
                try {
                    updateProperty(property);
                } catch (INDIException e) {
                }
                return;
            }
            property.setValues(elementsAndValues);
            guiderCCD.BinX = guiderCCD.imageBinX.getIntValue();
            guiderCCD.BinY = guiderCCD.imageBinY.getIntValue();

            if (updateGuiderBin(guiderCCD.BinX, guiderCCD.BinY)) {
                guiderCCD.imageBin.setState(PropertyStates.OK);
            } else
                guiderCCD.imageBin.setState(PropertyStates.ALERT);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
            return;
        }

        if (property == primaryCCD.imageFrame) {
            property.setValues(elementsAndValues);

            primaryCCD.imageFrame.setState(PropertyStates.OK);

            LOG.log(Level.FINE, String.format("Requested CCD Frame is %4.0f,%4.0f %4.0f x %4.0f", primaryCCD.imageFrameX.getIntValue(), primaryCCD.imageFrameY.getIntValue(),
                    primaryCCD.imageFrameWidth.getIntValue(), primaryCCD.imageFrameHeigth.getIntValue()));

            if (!updateCCDFrame(primaryCCD.imageFrameX.getIntValue(), primaryCCD.imageFrameY.getIntValue(), primaryCCD.imageFrameWidth.getIntValue(),
                    primaryCCD.imageFrameHeigth.getIntValue()))
                primaryCCD.imageFrame.setState(PropertyStates.ALERT);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == guiderCCD.imageFrame) {
            property.setValues(elementsAndValues);

            guiderCCD.imageFrame.setState(PropertyStates.OK);

            LOG.log(Level.FINE, String.format("Requested CCD Frame is %4.0f,%4.0f %4.0f x %4.0f", guiderCCD.imageFrameX.getIntValue(), guiderCCD.imageFrameY.getIntValue(),
                    guiderCCD.imageFrameWidth.getIntValue(), guiderCCD.imageFrameHeigth.getIntValue()));

            if (!updateCCDFrame(guiderCCD.imageFrameX.getIntValue(), guiderCCD.imageFrameY.getIntValue(), guiderCCD.imageFrameWidth.getIntValue(),
                    guiderCCD.imageFrameHeigth.getIntValue()))
                guiderCCD.imageFrame.setState(PropertyStates.ALERT);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == primaryCCD.rapidGuideData) {
            primaryCCD.rapidGuideData.setState(PropertyStates.OK);
            property.setValues(elementsAndValues);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == guiderCCD.rapidGuideData) {
            guiderCCD.rapidGuideData.setState(PropertyStates.OK);
            property.setValues(elementsAndValues);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == guider.getGuideNS() || property == guider.getGuideNS()) {
            try {
                guider.processNewNumberValue(property, timestamp, elementsAndValues);
            } catch (INDIException e) {
            }
        }

        // CCD TEMPERATURE:
        if (property == temperature) {
            property.setValues(elementsAndValues);
            Boolean rc = setTemperature(temperatureTemp.getValue());
            if (rc == null)
                temperature.setState(PropertyStates.ALERT);
            else if (rc.booleanValue())
                temperature.setState(PropertyStates.OK);
            else
                temperature.setState(PropertyStates.BUSY);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        // Primary CCD Info
        if (property == primaryCCD.imagePixelSize) {
            property.setValues(elementsAndValues);
            primaryCCD.imagePixelSize.setState(PropertyStates.OK);
            primaryCCD.XRes = primaryCCD.imagePixelSizeMaxX.getIntValue();
            primaryCCD.YRes = primaryCCD.imagePixelSizeMaxY.getIntValue();
            primaryCCD.PixelSizex = primaryCCD.imagePixelSizePixelSizeX.getValue().floatValue();
            primaryCCD.PixelSizey = primaryCCD.imagePixelSizePixelSizeY.getValue().floatValue();
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        // Guide CCD Info
        if (property == guiderCCD.imagePixelSize) {
            property.setValues(elementsAndValues);
            guiderCCD.imagePixelSize.setState(PropertyStates.OK);
            guiderCCD.XRes = guiderCCD.imagePixelSizeMaxX.getIntValue();
            guiderCCD.YRes = guiderCCD.imagePixelSizeMaxY.getIntValue();
            guiderCCD.PixelSizex = guiderCCD.imagePixelSizePixelSizeX.getValue().floatValue();
            guiderCCD.PixelSizey = guiderCCD.imagePixelSizePixelSizeY.getValue().floatValue();
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        if (property == upload) {
            property.setValues(elementsAndValues);
            property.setState(PropertyStates.OK);

            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
            if (uploadClient.getValue() == SwitchStatus.ON)
                LOG.log(Level.FINE, String.format("Upload settings set to client only."));
            else if (uploadLocal.getValue() == SwitchStatus.ON)
                LOG.log(Level.FINE, String.format("Upload settings set to local only."));
            else
                LOG.log(Level.FINE, String.format("Upload settings set to client and local."));

        }

        if (property == primaryCCD.abort) {
            primaryCCD.abortSwitch.setValue(SwitchStatus.OFF);
            if (abortExposure()) {
                primaryCCD.abort.setState(PropertyStates.OK);
                primaryCCD.imageExposure.setState(IDLE);
                primaryCCD.imageExposureDuration.setValue(0.0);
            } else {
                primaryCCD.abort.setState(PropertyStates.ALERT);
                primaryCCD.imageExposure.setState(PropertyStates.ALERT);
            }
            try {
                updateProperty(primaryCCD.abort);
                updateProperty(primaryCCD.imageExposure);
            } catch (INDIException e) {
            }
        }

        if (property == guiderCCD.abort) {
            guiderCCD.abortSwitch.setValue(SwitchStatus.OFF);
            if (abortGuideExposure()) {
                guiderCCD.abort.setState(PropertyStates.OK);
                guiderCCD.imageExposure.setState(IDLE);
                guiderCCD.imageExposureDuration.setValue(0.0);
            } else {
                guiderCCD.abort.setState(PropertyStates.ALERT);
                guiderCCD.imageExposure.setState(PropertyStates.ALERT);
            }
            try {
                updateProperty(guiderCCD.abort);
                updateProperty(guiderCCD.imageExposure);
            } catch (INDIException e) {
            }
        }

        if (property == primaryCCD.compress) {

            property.setValues(elementsAndValues);

            if (primaryCCD.compressCompress.getValue() == SwitchStatus.ON) {
                primaryCCD.sendCompressed = true;
            } else {
                primaryCCD.sendCompressed = false;
            }
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == guiderCCD.compress) {
            property.setValues(elementsAndValues);

            if (guiderCCD.compressCompress.getValue() == SwitchStatus.ON) {
                guiderCCD.sendCompressed = true;
            } else {
                guiderCCD.sendCompressed = false;
            }
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == primaryCCD.frameType) {
            property.setValues(elementsAndValues);
            primaryCCD.frameType.setState(PropertyStates.OK);

            if (primaryCCD.frameTypeLight.getValue() == SwitchStatus.ON) {
                primaryCCD.setFrameType(CCD_FRAME.LIGHT_FRAME);
            } else if (primaryCCD.frameTypeBais.getValue() == SwitchStatus.ON) {
                primaryCCD.setFrameType(CCD_FRAME.BIAS_FRAME);
                if (capability.hasShutter == false) {
                    LOG.log(Level.FINE, "The CCD does not have a shutter. Cover the camera in order to take a bias frame.");
                }
            } else if (primaryCCD.frameTypeDark.getValue() == SwitchStatus.ON) {
                primaryCCD.setFrameType(CCD_FRAME.DARK_FRAME);
                if (capability.hasShutter == false) {
                    LOG.log(Level.FINE, "The CCD does not have a shutter. Cover the camera in order to take a dark frame.");
                }
            } else if (primaryCCD.frameTypeFlat.getValue() == SwitchStatus.ON) {
                primaryCCD.setFrameType(CCD_FRAME.FLAT_FRAME);
            }
            if (updateCCDFrameType(primaryCCD.getFrameType()) == false)
                primaryCCD.frameType.setState(PropertyStates.ALERT);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == guiderCCD.frameType) {
            property.setValues(elementsAndValues);
            guiderCCD.frameType.setState(PropertyStates.OK);

            if (guiderCCD.frameTypeLight.getValue() == SwitchStatus.ON) {
                guiderCCD.setFrameType(CCD_FRAME.LIGHT_FRAME);
            } else if (guiderCCD.frameTypeBais.getValue() == SwitchStatus.ON) {
                guiderCCD.setFrameType(CCD_FRAME.BIAS_FRAME);
                if (capability.hasShutter == false) {
                    LOG.log(Level.FINE, "The CCD does not have a shutter. Cover the camera in order to take a bias frame.");
                }
            } else if (guiderCCD.frameTypeDark.getValue() == SwitchStatus.ON) {
                guiderCCD.setFrameType(CCD_FRAME.DARK_FRAME);
                if (capability.hasShutter == false) {
                    LOG.log(Level.FINE, "The CCD does not have a shutter. Cover the camera in order to take a dark frame.");
                }
            } else if (guiderCCD.frameTypeFlat.getValue() == SwitchStatus.ON) {
                guiderCCD.setFrameType(CCD_FRAME.FLAT_FRAME);
            }
            if (updateCCDFrameType(guiderCCD.getFrameType()) == false)
                guiderCCD.frameType.setState(PropertyStates.ALERT);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == primaryCCD.rapidGuide) {
            property.setValues(elementsAndValues);
            primaryCCD.rapidGuide.setState(PropertyStates.OK);
            RapidGuideEnabled = (primaryCCD.rapidGuideEnable.getValue() == SwitchStatus.ON);

            if (RapidGuideEnabled) {
                addProperty(primaryCCD.rapidGuideSetup);
                addProperty(primaryCCD.rapidGuideData);
            } else {
                removeProperty(primaryCCD.rapidGuideSetup);
                removeProperty(primaryCCD.rapidGuideData);
            }

            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == guiderCCD.rapidGuide) {
            property.setValues(elementsAndValues);
            guiderCCD.rapidGuide.setState(PropertyStates.OK);
            RapidGuideEnabled = (guiderCCD.rapidGuideEnable.getValue() == SwitchStatus.ON);

            if (RapidGuideEnabled) {
                addProperty(guiderCCD.rapidGuideSetup);
                addProperty(guiderCCD.rapidGuideData);
            } else {
                removeProperty(guiderCCD.rapidGuideSetup);
                removeProperty(guiderCCD.rapidGuideData);
            }
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == primaryCCD.rapidGuideSetup) {
            property.setValues(elementsAndValues);
            primaryCCD.rapidGuideSetup.setState(PropertyStates.OK);

            AutoLoop = primaryCCD.rapidGuideSetupAutoLoop.getValue() == SwitchStatus.ON;
            SendImage = primaryCCD.rapidGuideSetupSendImage.getValue() == SwitchStatus.ON;
            ShowMarker = primaryCCD.rapidGuideSetupShowMarker.getValue() == SwitchStatus.ON;

            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

        if (property == guiderCCD.rapidGuideSetup) {
            property.setValues(elementsAndValues);
            guiderCCD.rapidGuideSetup.setState(PropertyStates.OK);

            AutoLoop = guiderCCD.rapidGuideSetupAutoLoop.getValue() == SwitchStatus.ON;
            SendImage = guiderCCD.rapidGuideSetupSendImage.getValue() == SwitchStatus.ON;
            ShowMarker = guiderCCD.rapidGuideSetupShowMarker.getValue() == SwitchStatus.ON;

            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
        // This is for our device
        // Now lets see if it's something we process here
        if (property == activeDevice) {
            activeDevice.setState(PropertyStates.OK);
            property.setValues(elementsAndValues);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }

            // IDSnoopDevice(ActiveDeviceT[0].text,"EQUATORIAL_EOD_COORD");

            // Tell children active devices was updated.
            activeDevicesUpdated();
        }

        if (property == uploadSettings) {
            property.setValues(elementsAndValues);
            try {
                updateProperty(property);
            } catch (INDIException e) {
            }
        }

    }

    /**
     * the active telescope was changed!
     */
    protected void activeDevicesUpdated() {

    }

    /**
     * @return True if CCD can abort exposure. False otherwise.
     */
    protected boolean canAbort() {
        return capability.canAbort;
    }

    /**
     * @return True if CCD supports binning. False otherwise.
     */
    protected boolean canBin() {
        return capability.canBin;
    }

    /**
     * @return True if CCD supports subframing. False otherwise.
     */
    protected boolean canSubFrame() {
        return capability.canSubFrame;
    }

    /**
     * @return True if CCD has guide head. False otherwise.
     */
    protected boolean hasGuideHead() {
        return capability.hasGuideHead;
    }

    /**
     * @return True if CCD has mechanical or electronic shutter. False
     *         otherwise.
     */
    protected boolean hasShutter() {
        return capability.hasShutter;
    }

    /**
     * @return True if CCD has ST4 port for guiding. False otherwise.
     */
    protected boolean hasST4Port() {
        return capability.hasST4Port;
    }

    /**
     * @return True if CCD has cooler and temperature can be controlled. False
     *         otherwise.
     */
    protected boolean hasCooler() {
        return capability.hasCooler;
    }

    /**
     * Set CCD temperature. Upon returning false, the property becomes BUSY.
     * Once the temperature reaches the requested value, change the state to OK.
     * This must be implemented in the child class
     * 
     * @param temperature
     *            CCD temperature in degrees celcius.
     * @return true or false if setting the temperature call to the hardware is
     *         successful. null if an error is encountered. Return false if
     *         setting the temperature to the requested value takes time. Return
     *         true if setting the temperature to the requested value is
     *         complete.
     */
    abstract protected Boolean setTemperature(double temperature);

    /**
     * Start exposing primary CCD chip. This function must be implemented in the
     * child class
     * 
     * @param duration
     *            Duration in seconds
     * @return true if OK and exposure will take some time to complete, false on
     *         error.
     */
    abstract protected boolean startExposure(double duration);

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

    /**
     * Uploads target Chip exposed buffer as FITS to the client. Dervied classes
     * should call this functon when an exposure is complete. This function must
     * be implemented in the child class
     * 
     * @param targetChip
     *            chip that contains upload image data
     */
    protected boolean exposureComplete(CCD targetChip) {
        boolean sendImage = (uploadClient.getValue() == SwitchStatus.ON || uploadBoth.getValue() == SwitchStatus.ON);
        boolean saveImage = (uploadLocal.getValue() == SwitchStatus.ON || uploadBoth.getValue() == SwitchStatus.ON);
        boolean showMarker = false;
        boolean sendData = false;
        boolean autoLoop = false;
        if (RapidGuideEnabled && targetChip == primaryCCD) {
            autoLoop = AutoLoop;
            sendImage = SendImage;
            showMarker = ShowMarker;
            sendData = true;
            saveImage = false;
        }
        if (GuiderRapidGuideEnabled && targetChip == guiderCCD) {
            autoLoop = GuiderAutoLoop;
            sendImage = GuiderSendImage;
            showMarker = GuiderShowMarker;
            sendData = true;
            saveImage = false;
        }
        if (sendData) {
            targetChip.rapidGuideData.setState(PropertyStates.BUSY);
            int width = targetChip.getSubW() / targetChip.getBinX();
            int height = targetChip.getSubH() / targetChip.getBinY();
            INDICCDImage ccdImage = targetChip.getFrameBuffer();
            int i[] = new int[9];
            int ix = 0, iy = 0;
            double average, fit, bestFit = 0;
            int minx = 4;
            int maxx = width - 4;
            int miny = 4;
            int maxy = height - 4;
            if (targetChip.lastRapidX > 0 && targetChip.lastRapidY > 0) {
                minx = Math.max(targetChip.lastRapidX - 20, 4);
                maxx = Math.min(targetChip.lastRapidX + 20, width - 4);
                miny = Math.max(targetChip.lastRapidY - 20, 4);
                maxy = Math.min(targetChip.lastRapidY + 20, height - 4);
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

            targetChip.rapidGuideDataX.setValue(ix);
            targetChip.rapidGuideDataY.setValue(iy);
            targetChip.rapidGuideDataFIT.setValue(bestFit);
            targetChip.lastRapidX = ix;
            targetChip.lastRapidY = iy;
            if (bestFit > 50) {
                if (total > 0) {
                    targetChip.rapidGuideDataX.setValue(sumX / total);
                    targetChip.rapidGuideDataY.setValue(sumY / total);
                    targetChip.rapidGuideData.setState(PropertyStates.OK);

                    LOG.log(Level.FINE, String.format("Guide Star X: %g Y: %g FIT: %g", targetChip.rapidGuideDataX.getValue(), targetChip.rapidGuideDataY.getValue(),
                            targetChip.rapidGuideDataFIT.getValue()));
                } else {
                    targetChip.rapidGuideData.setState(PropertyStates.ALERT);
                    targetChip.lastRapidX = targetChip.lastRapidY = -1;
                }
            } else {
                targetChip.rapidGuideData.setState(PropertyStates.ALERT);
                targetChip.lastRapidX = targetChip.lastRapidY = -1;
            }
            try {
                updateProperty(targetChip.rapidGuideData);
            } catch (INDIException e) {
            }

            if (showMarker) {
                ccdImage.drawMarker(ix, iy);
            }
            if (sendImage || saveImage) {
                try {
                    if ("fits".equals((targetChip.getImageExtension()))) {
                        Fits f = ccdImage.asFitsImage();
                        addFITSKeywords(targetChip, f.getHDU(0));
                    }
                    uploadFile(targetChip, ccdImage, sendImage, saveImage);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "could not send or save image", e);
                    return false;
                }
            }
        }
        targetChip.imageExposure.setState(PropertyStates.OK);
        try {
            updateProperty(targetChip.imageExposure);
        } catch (INDIException e) {
        }
        if (autoLoop) {
            targetChip.imageExposureDuration.setValue(primaryCCD == targetChip ? ExposureTime : GuiderExposureTime);
            targetChip.imageExposure.setState(PropertyStates.BUSY);
            if (primaryCCD == targetChip ? startExposure(ExposureTime) : startGuideExposure(GuiderExposureTime))
                targetChip.imageExposure.setState(PropertyStates.BUSY);
            else {
                LOG.log(Level.SEVERE, "Autoloop: Primary CCD Exposure Error!");
                targetChip.imageExposure.setState(PropertyStates.ALERT);
            }
            try {
                updateProperty(targetChip.imageExposure);
            } catch (INDIException e) {
            }
        }
        return true;
    }

    private int getFileIndex(String dir, String prefix, String extension) throws IOException {
        int indexOfXX = prefix.indexOf("XX");
        String uptoXX = prefix.substring(0, indexOfXX);
        String afterXX = prefix.substring(indexOfXX + 2) + "." + extension;

        int maxNumber = 0;
        for (File file : new File(dir).listFiles()) {
            if (file.getName().startsWith(uptoXX) && file.getName().endsWith(afterXX)) {
                int number = Integer.valueOf(file.getName().substring(indexOfXX));
                if (number > maxNumber) {
                    maxNumber = number;
                }
            }
        }
        return maxNumber;
    }

    protected void uploadFile(CCD targetChip, INDICCDImage ccdImage, boolean sendImage, boolean saveImage) throws Exception {
        if (saveImage) {
            int maxIndex = getFileIndex(uploadSettingsDir.getValue(), uploadSettingsPrefix.getValue(), targetChip.getImageExtension());
            maxIndex++;
            File fp = new File(uploadSettingsDir.getValue(), uploadSettingsPrefix.getValue().replace("XX", Integer.toString(maxIndex)) + "." + targetChip.getImageExtension());
            try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fp)))) {
                ccdImage.write(os, targetChip.getImageExtension());
            }
        }
        if (sendImage) {
            if (targetChip.sendCompressed) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new BufferedOutputStream(out))))) {
                    ccdImage.write(os, targetChip.getImageExtension());
                }
                targetChip.fitsImage.setValue(new INDIBLOBValue(out.toByteArray(), "." + targetChip.getImageExtension() + ".z"));
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(out))) {
                    ccdImage.write(os, targetChip.getImageExtension());
                }
                targetChip.fitsImage.setValue(new INDIBLOBValue(out.toByteArray(), "." + targetChip.getImageExtension()));
            }
            targetChip.fits.setState(PropertyStates.OK);
            try {
                updateProperty(targetChip.fits);
            } catch (INDIException e) {
            }
        }
    }

    /**
     * Abort ongoing exposure
     * 
     * @return true is abort is successful, false otherwise.
     */
    abstract protected boolean abortExposure();

    /**
     * Start exposing guide CCD chip
     * 
     * @param duration
     *            Duration in seconds
     * @return true if OK and exposure will take some time to complete, false on
     *         error.
     */
    abstract protected boolean startGuideExposure(double duration);

    /**
     * Abort ongoing exposure
     * 
     * @return true is abort is successful, false otherwise.
     */
    abstract protected boolean abortGuideExposure();

    /**
     * INDICCD calls this function when CCD Frame dimension needs to be updated
     * in the hardware. Derived classes should implement this function
     * 
     * @param x
     *            Subframe X coordinate in pixels.
     * @param y
     *            Subframe Y coordinate in pixels.
     * @param w
     *            Subframe width in pixels.
     * @param h
     *            Subframe height in pixels. \note (0,0) is defined as most
     *            left, top pixel in the subframe.
     * @return true is CCD chip update is successful, false otherwise.
     */
    abstract protected boolean updateCCDFrame(int x, int y, int w, int h);

    /**
     * INDICCD calls this function when Guide head frame dimension is updated by
     * the client. Derived classes should implement this function
     * 
     * @param x
     *            Subframe X coordinate in pixels.
     * @param y
     *            Subframe Y coordinate in pixels.
     * @param w
     *            Subframe width in pixels.
     * @param h
     *            Subframe height in pixels. \note (0,0) is defined as most
     *            left, top pixel in the subframe.
     * @return true is CCD chip update is successful, false otherwise.
     */
    abstract protected boolean updateGuiderFrame(int x, int y, int w, int h);

    /**
     * INDICCD calls this function when CCD Binning needs to be updated in the
     * hardware. Derived classes should implement this function
     * 
     * @param hor
     *            Horizontal binning.
     * @param ver
     *            Vertical binning.
     * @return true is CCD chip update is successful, false otherwise.
     */
    abstract protected boolean updateCCDBin(int hor, int ver);

    /**
     * INDICCD calls this function when Guide head binning is updated by the
     * client. Derived classes should implement this function
     * 
     * @param hor
     *            Horizontal binning.
     * @param ver
     *            Vertical binning.
     * @return true is CCD chip update is successful, false otherwise.
     */
    abstract protected boolean updateGuiderBin(int hor, int ver);

    /**
     * INDICCD calls this function when CCD frame type needs to be updated in
     * the hardware.The CCD hardware layer may either set the frame type when
     * this function is called, or (optionally) before an exposure is started.
     * 
     * @param fType
     *            Frame type
     * @return true is CCD chip update is successful, false otherwise.
     */
    abstract protected boolean updateCCDFrameType(CCD_FRAME fType);

    /**
     * INDICCD calls this function when Guide frame type is updated by the
     * client.The CCD hardware layer may either set the frame type when this
     * function is called, or (optionally) before an exposure is started.
     * 
     * @param fType
     *            Frame type
     * @return true is CCD chip update is successful, false otherwise.
     */
    abstract protected boolean updateGuiderFrameType(CCD_FRAME fType);

    /**
     * Setup CCD paramters for primary CCD. Child classes call this function to
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
    abstract protected void setCCDParams(int x, int y, int bpp, float xf, float yf);

    /**
     * Setup CCD paramters for guide head CCD. Child classes call this function
     * to update CCD paramaters
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
    abstract protected void setGuiderParams(int x, int y, int bpp, float xf, float yf);

    /**
     * Guide northward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return True if successful, false otherwise.
     */
    abstract protected boolean guideNorth(float ms);

    /**
     * Guide southward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    abstract protected boolean guideSouth(float ms);

    /**
     * Guide easward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    abstract protected boolean guideEast(float ms);

    /**
     * Guide westward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    abstract protected boolean guideWest(float ms);

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
    protected void addFITSKeywords(CCD targetChip, BasicHDU fitsHeader) throws HeaderCardException {
        fitsHeader.addValue("EXPTIME", targetChip.exposureDuration, "Total Exposure Time (s)");

        if (targetChip.getFrameType() == CCD_FRAME.DARK_FRAME)
            fitsHeader.addValue("DARKTIME", targetChip.exposureDuration, "Total Exposure Time (s)");

        fitsHeader.addValue("PIXSIZE1", targetChip.getPixelSizeX(), "Pixel Size 1 (microns)");
        fitsHeader.addValue("PIXSIZE2", targetChip.getPixelSizeY(), "Pixel Size 2 (microns)");
        fitsHeader.addValue("XBINNING", targetChip.BinX, "Binning factor in width");
        fitsHeader.addValue("YBINNING", targetChip.BinY, "Binning factor in height");
        fitsHeader.addValue("FRAME", targetChip.getFrameType().fitsValue(), "Frame Type");

        if (targetChip.getNAxis() == 2) {
            // should de done in the image processing
            // fitsHeader.addValue("DATAMIN", &min_val, "Minimum value");
            // fitsHeader.addValue("DATAMAX", &max_val, "Maximum value");
        }

        if (RA > -999d && Dec > -999d) {
            fitsHeader.addValue("OBJCTRA", this.RA, "Object RA");
            fitsHeader.addValue("OBJCTDEC", this.Dec, "Object DEC");
        }

        fitsHeader.addValue("INSTRUME", getName(), "CCD Name");
        fitsHeader.addValue("DATE-OBS", targetChip.getExposureStartTime(), "UTC start date of observation");

    }

    void SetCCDParams(int x, int y, int bpp, float xf, float yf) throws INDIException {
        primaryCCD.setResolution(x, y);
        primaryCCD.setFrame(0, 0, x, y);
        primaryCCD.setBin(1, 1);
        primaryCCD.setPixelSize(xf, yf);
        primaryCCD.setBPP(bpp);

    }

    void SetGuiderParams(int x, int y, int bpp, float xf, float yf) throws INDIException {
        capability.hasGuideHead = true;

        guiderCCD.setResolution(x, y);
        guiderCCD.setFrame(0, 0, x, y);
        guiderCCD.setPixelSize(xf, yf);
        guiderCCD.setBPP(bpp);

    }

}
