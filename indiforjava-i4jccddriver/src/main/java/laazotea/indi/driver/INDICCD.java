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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;

import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIBLOBValue;
import laazotea.indi.INDIException;
import laazotea.indi.driver.annotation.InjectExtention;
import laazotea.indi.driver.annotation.Rename;

public abstract class INDICCD extends INDIDriver implements INDIConnectionHandler, INDIGuiderInterface, CCDDriverInterface {

    private static final Logger LOG = Logger.getLogger(INDICCD.class.getName());

    protected static final String MAIN_CONTROL_TAB = "Main Control";

    protected static final String IMAGE_SETTINGS_TAB = "Image Settings";

    protected static final String IMAGE_INFO_TAB = "Image Info";

    protected static final String GUIDE_HEAD_TAB = "Guider Head";

    protected static final String GUIDE_CONTROL_TAB = "Guider Control";

    protected static final String RAPIDGUIDE_TAB = "Rapid Guide";

    protected static final String OPTIONS_TAB = "Options";

    final INDINumberProperty temperature;

    private final INDINumberElement temperatureTemp;

    private final INDISwitchProperty upload;

    protected final INDISwitchElement uploadClient;

    protected final INDISwitchElement uploadLocal;

    protected final INDISwitchElement uploadBoth;

    private final INDITextProperty uploadSettings;

    private final INDITextElement uploadSettingsDir;

    private final INDITextElement uploadSettingsPrefix;

    private final INDINumberProperty eqn;

    private final INDINumberElement eqnRa;

    private final INDINumberElement eqnDec;

    private final INDITextProperty activeDevice;

    private final INDITextElement activeDeviceTelescope;

    private final INDITextElement activeDeviceFocuser;

    @InjectExtention(prefix = "CCD_", rename = {
        @Rename(name = "CCD", to = "CCD0")
    })
    private CCDDriverExtention primaryCCD;

    @InjectExtention(prefix = "GUIDER_", rename = {
        @Rename(name = "CCD", to = "CCD1")
    })
    private CCDDriverExtention guiderCCD;

    protected final Capability capability = new Capability();

    protected double RA = -1000d;

    protected double Dec = -1000d;

    @InjectExtention(group = GUIDE_CONTROL_TAB)
    private INDIGuiderExtention guider;

    public INDICCD(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        primaryCCD.setDriverInterface(this);
        guiderCCD.setDriverInterface(new CCDDriverInterface() {

            @Override
            public boolean abortExposure() {
                return abortGuideExposure();
            }

            @Override
            public boolean startExposure(double duration) {
                return startGuideExposure(duration);
            }

            @Override
            public boolean updateCCDBin(int binX, int binY) {
                return updateGuiderBin(binX, binY);
            }

            @Override
            public boolean updateCCDFrame(int x, int y, int w, int h) {
                return updateGuiderFrame(x, y, w, h);
            }

            @Override
            public boolean updateCCDFrameType(CCD_FRAME frameType) {
                return updateGuiderFrameType(frameType);
            }
        });
        // CCD Temperature
        this.temperature = new INDINumberProperty(this, "CCD_TEMPERATURE", "Temperature", INDICCD.MAIN_CONTROL_TAB, IDLE, RW, 60);
        this.temperatureTemp = new INDINumberElement(this.temperature, "CCD_TEMPERATURE_VALUE", "Temperature (C)", 0d, -50d, 50d, 0d, "%5.2f");

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
        this.guider.setGuiderInterface(this);
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        primaryCCD.connect();
        if (capability.hasGuideHead) {
            guiderCCD.connect();
        }
        if (capability.hasCooler)
            addProperty(temperature);

        if (capability.hasST4Port) {
            this.guider.connect();
        }
        addProperty(activeDevice);
        addProperty(upload);

        if (uploadSettingsDir.getValue() == null) {
            uploadSettingsDir.setValue(System.getProperty("user.home"));
        }
        addProperty(uploadSettings);
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        primaryCCD.disconnect();
        if (capability.hasGuideHead) {
            guiderCCD.disconnect();
        }
        if (capability.hasCooler)
            removeProperty(temperature);
        if (capability.hasST4Port) {
            this.guider.disconnect();
        }
        removeProperty(activeDevice);
        removeProperty(upload);
        removeProperty(uploadSettings);
    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {

    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
        // This is for our device
        // Now lets see if it's something we process here

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
    public abstract boolean startExposure(double duration);

    protected File getFileWithIndex(String extension) throws IOException {
        String dir = uploadSettingsDir.getValue();
        String prefix = uploadSettingsPrefix.getValue();

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
        maxNumber++;
        File fp = new File(uploadSettingsDir.getValue(), uploadSettingsPrefix.getValue().replace("XX", Integer.toString(maxNumber)) + "." + extension);
        return fp;
    }

    /**
     * Abort ongoing exposure
     * 
     * @return true is abort is successful, false otherwise.
     */
    public abstract boolean abortExposure();

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
    public abstract boolean updateCCDFrame(int x, int y, int w, int h);

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
    public abstract boolean updateCCDBin(int hor, int ver);

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
    public abstract boolean updateCCDFrameType(CCD_FRAME fType);

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

    /**
     * Uploads target Chip exposed buffer as FITS to the client. Dervied classes
     * should call this functon when an exposure is complete. This function must
     * be implemented in the child class
     * 
     * @param targetChip
     *            chip that contains upload image data
     */
    protected boolean exposureComplete(CCDDriverExtention targetChip) {
        if (targetChip == primaryCCD) {
            return primaryCCD.exposureComplete();
        } else {
            return guiderCCD.exposureComplete();
        }
    }
}
