package org.indilib.i4j.driver.ccd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.indilib.i4j.driver.INDIBLOBElementAndValue;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIConnectionHandler;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectExtention;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.annotation.Rename;
import org.indilib.i4j.driver.event.NumberEvent;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.driver.event.TextEvent;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIException;

public abstract class INDICCDDriver extends INDIDriver implements INDIConnectionHandler, INDIGuiderInterface, INDICCDDriverInterface {

    private static final Logger LOG = Logger.getLogger(INDICCDDriver.class.getName());

    protected static final String MAIN_CONTROL_TAB = "Main Control";

    protected static final String IMAGE_SETTINGS_TAB = "Image Settings";

    protected static final String IMAGE_INFO_TAB = "Image Info";

    protected static final String GUIDE_HEAD_TAB = "Guider Head";

    protected static final String GUIDE_CONTROL_TAB = "Guider Control";

    protected static final String RAPIDGUIDE_TAB = "Rapid Guide";

    protected static final String OPTIONS_TAB = "Options";

    @InjectProperty(name = "CCD_TEMPERATURE", label = "Temperature", group = INDICCDDriver.MAIN_CONTROL_TAB)
    protected INDINumberProperty temperature;

    @InjectElement(name = "CCD_TEMPERATURE_VALUE", label = "Temperature (C)", minimumD = -50d, maximumD = 50d, numberFormat = "%5.2f")
    private INDINumberElement temperatureTemp;

    @InjectProperty(name = "UPLOAD_MODE", label = "Upload", group = INDICCDDriver.OPTIONS_TAB, timeout = 0, saveable = true)
    private INDISwitchProperty upload;

    @InjectElement(name = "UPLOAD_CLIENT", label = "Client", switchValue = SwitchStatus.ON)
    private INDISwitchElement uploadClient;

    @InjectElement(name = "UPLOAD_LOCAL", label = "Local")
    private INDISwitchElement uploadLocal;

    @InjectElement(name = "UPLOAD_BOTH", label = "Both")
    private INDISwitchElement uploadBoth;

    @InjectProperty(name = "UPLOAD_SETTINGS", label = "Upload Settings", group = INDICCDDriver.OPTIONS_TAB, saveable = true)
    private INDITextProperty uploadSettings;

    @InjectElement(name = "UPLOAD_DIR", label = "Dir")
    private INDITextElement uploadSettingsDir;

    @InjectElement(name = "UPLOAD_PREFIX", label = "Prefix", valueT = "IMAGE_XX")
    private INDITextElement uploadSettingsPrefix;

    @InjectProperty(name = "ACTIVE_DEVICES", label = "Snoop devices", group = INDICCDDriver.OPTIONS_TAB, saveable = true)
    private INDITextProperty activeDevice;

    @InjectElement(name = "ACTIVE_TELESCOPE", label = "Telescope", valueT = "Telescope Simulator")
    private INDITextElement activeDeviceTelescope;

    @InjectElement(name = "ACTIVE_FOCUSER", label = "Focuser", valueT = "Focuser Simulator")
    private INDITextElement activeDeviceFocuser;

    @InjectExtention(prefix = "CCD_", rename = {
        @Rename(name = "CCD", to = "CCD0")
    })
    protected INDICCDDriverExtention primaryCCD;

    @InjectExtention(prefix = "GUIDER_", rename = {
        @Rename(name = "CCD", to = "CCD1")
    })
    protected INDICCDDriverExtention guiderCCD;

    private final Capability capability = defineCapabilities();

    @InjectExtention(group = GUIDE_CONTROL_TAB)
    private INDIGuiderExtention guider;

    public INDICCDDriver(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        primaryCCD.setDriverInterface(this);
        guiderCCD.setDriverInterface(createGuiderDriverHandler());
        this.guider.setGuiderInterface(this);
        this.temperature.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newTemperatureValue(elementsAndValues);
            }
        });
        this.upload.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newUploadValue(elementsAndValues);
            }
        });
        this.activeDevice.setEventHandler(new TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                newActiveDeviceValue(elementsAndValues);
            }

        });
        this.uploadSettings.setEventHandler(new TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                property.setValues(elementsAndValues);
                try {
                    updateProperty(property);
                } catch (INDIException e) {
                }
            }
        });
    }

    private void connectToTelescope() {
        // TODO open a indiclient connection to the scope
    }

    private void disconnectFromTelescope() {
        // TODO open a indiclient connection to the fokuser
    }

    private void newActiveDeviceValue(INDITextElementAndValue[] elementsAndValues) {
        activeDevice.setState(PropertyStates.OK);
        activeDevice.setValues(elementsAndValues);
        try {
            updateProperty(activeDevice);
        } catch (INDIException e) {
        }

        // IDSnoopDevice(ActiveDeviceT[0].text,"EQUATORIAL_EOD_COORD");

        // Tell children active devices was updated.
        activeDevicesUpdated();
    }

    private void newTemperatureValue(INDINumberElementAndValue[] elementsAndValues) {
        temperature.setValues(elementsAndValues);
        Boolean rc = setTemperature(temperatureTemp.getValue());
        if (rc == null)
            temperature.setState(PropertyStates.ALERT);
        else if (rc.booleanValue())
            temperature.setState(PropertyStates.OK);
        else
            temperature.setState(PropertyStates.BUSY);
        try {
            updateProperty(temperature);
        } catch (INDIException e) {
        }
    }

    private void newUploadValue(INDISwitchElementAndValue[] elementsAndValues) {
        upload.setValues(elementsAndValues);
        upload.setState(PropertyStates.OK);

        try {
            updateProperty(upload);
        } catch (INDIException e) {
        }
        if (uploadClient.getValue() == SwitchStatus.ON)
            LOG.log(Level.FINE, String.format("Upload settings set to client only."));
        else if (uploadLocal.getValue() == SwitchStatus.ON)
            LOG.log(Level.FINE, String.format("Upload settings set to local only."));
        else
            LOG.log(Level.FINE, String.format("Upload settings set to client and local."));
    }

    /**
     * the active telescope was changed!
     */
    protected void activeDevicesUpdated() {
        // disconnect the indi-client and reconnect to the new active device.
    }

    /**
     * @return a handler instance for the guider. mandatory if the ccd support a
     *         guider head.
     */
    protected INDICCDDriverInterface createGuiderDriverHandler() {
        return null;
    }

    /**
     * @return a new Capability object that defines the capabilities of this ccd
     *         driver.
     */
    protected abstract Capability defineCapabilities();

    /**
     * Uploads target Chip exposed buffer as FITS to the client. Dervied classes
     * should call this functon when an exposure is complete. This function must
     * be implemented in the child class
     * 
     * @param targetChip
     *            chip that contains upload image data
     */
    protected boolean exposureComplete(INDICCDDriverExtention targetChip) {
        if (targetChip == primaryCCD) {
            return primaryCCD.exposureComplete();
        } else {
            return guiderCCD.exposureComplete();
        }
    }

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
     * Guide easward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    abstract protected boolean guideEast(float ms);

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
     * Guide westward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    abstract protected boolean guideWest(float ms);

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
     * Abort ongoing exposure
     * 
     * @return true is abort is successful, false otherwise.
     */
    public abstract boolean abortExposure();

    public Capability capability() {
        return capability;
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        primaryCCD.connect();
        if (capability.hasGuideHead()) {
            guiderCCD.connect();
        }
        if (capability.hasCooler())
            addProperty(temperature);

        if (capability.hasST4Port()) {
            this.guider.connect();
        }
        addProperty(activeDevice);
        addProperty(upload);

        if (uploadSettingsDir.getValue() == null) {
            uploadSettingsDir.setValue(System.getProperty("user.home"));
        }
        addProperty(uploadSettings);
        connectToTelescope();
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        primaryCCD.disconnect();
        if (capability.hasGuideHead()) {
            guiderCCD.disconnect();
        }
        if (capability.hasCooler())
            removeProperty(temperature);
        if (capability.hasST4Port()) {
            this.guider.disconnect();
        }
        removeProperty(activeDevice);
        removeProperty(upload);
        removeProperty(uploadSettings);
        disconnectFromTelescope();
    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {

    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {

    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
    }

    public boolean shouldSaveImage() {
        return uploadLocal.getValue() == SwitchStatus.ON || uploadBoth.getValue() == SwitchStatus.ON;
    }

    public boolean shouldSendImage() {
        return uploadClient.getValue() == SwitchStatus.ON || uploadBoth.getValue() == SwitchStatus.ON;
    }

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
     * INDICCD calls this function when CCD frame type needs to be updated in
     * the hardware.The CCD hardware layer may either set the frame type when
     * this function is called, or (optionally) before an exposure is started.
     * 
     * @param fType
     *            Frame type
     * @return true is CCD chip update is successful, false otherwise.
     */
    public abstract boolean updateCCDFrameType(CcdFrame fType);
}
