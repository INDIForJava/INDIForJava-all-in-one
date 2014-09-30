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

import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIBLOBValue;
import laazotea.indi.INDIException;
import laazotea.indi.driver.annotation.InjectExtention;
import laazotea.indi.driver.annotation.Rename;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.HeaderCardException;

public abstract class INDICCD extends INDIDriver implements INDIConnectionHandler, INDIGuiderInterface {

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

    @InjectExtention(prefix = "CCD_", rename = {
        @Rename(name = "CCD", to = "CCD0")
    })
    private CCDDriverExtention primaryCCD;

    @InjectExtention(prefix = "GUIDER_", rename = {
        @Rename(name = "CCD", to = "CCD1")
    })
    private CCDDriverExtention guiderCCD;

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

    @InjectExtention(group = GUIDE_CONTROL_TAB)
    private INDIGuiderExtention guider;

    public INDICCD(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);

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
            this.guider.connect();
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
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
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
            this.guider.disconnect();
        }
        removeProperty(primaryCCD.frameType);
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
    protected boolean exposureComplete(CCDDriverExtention targetChip) {
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

    protected void uploadFile(CCDDriverExtention targetChip, INDICCDImage ccdImage, boolean sendImage, boolean saveImage) throws Exception {
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
    protected void addFITSKeywords(CCDDriverExtention targetChip, BasicHDU fitsHeader) throws HeaderCardException {
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
