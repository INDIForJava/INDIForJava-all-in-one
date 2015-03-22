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

import static org.indilib.i4j.properties.INDIStandardElement.UPLOAD_BOTH;
import static org.indilib.i4j.properties.INDIStandardElement.UPLOAD_CLIENT;
import static org.indilib.i4j.properties.INDIStandardElement.UPLOAD_DIR;
import static org.indilib.i4j.properties.INDIStandardElement.UPLOAD_LOCAL;
import static org.indilib.i4j.properties.INDIStandardElement.UPLOAD_PREFIX;
import static org.indilib.i4j.properties.INDIStandardProperty.UPLOAD_MODE;
import static org.indilib.i4j.properties.INDIStandardProperty.UPLOAD_SETTINGS;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.FileUtils;
import org.indilib.i4j.INDIException;
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
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.annotation.Rename;
import org.indilib.i4j.driver.connection.INDIConnectionHandler;
import org.indilib.i4j.driver.event.NumberEvent;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.driver.event.TextEvent;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the abstract cdd driver, all ccd drivers should subclass it. the
 * initial verion of the Functionality is a port of the c++ driver.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class INDICCDDriver extends INDIDriver implements INDIConnectionHandler, INDICCDDriverInterface {

    /**
     * The minimum temperature for the ccd chip in degrees celcius.
     */
    private static final double MAXIMUM_CCD_TEMPERATURE = 50d;

    /**
     * The maximum temperature for the ccd chip in degrees celcius.
     */
    private static final double MINIMAL_CCD_TEMPERATURE = -50d;

    /**
     * The logger to log the messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDICCDDriver.class);

    /**
     * The property tab for the image settings..
     */
    protected static final String IMAGE_SETTINGS_TAB = "Image Settings";

    /**
     * The property tab for the image information.
     */
    protected static final String IMAGE_INFO_TAB = "Image Info";

    /**
     * The temperature of the ccd chip.
     */
    @InjectProperty(name = "CCD_TEMPERATURE", label = "Temperature", group = INDIDriver.GROUP_MAIN_CONTROL)
    protected INDINumberProperty temperature;

    /**
     * The temperature of the ccd chip.
     */
    @InjectElement(name = "CCD_TEMPERATURE_VALUE", label = "Temperature (C)", minimum = MINIMAL_CCD_TEMPERATURE, maximum = MAXIMUM_CCD_TEMPERATURE, numberFormat = "%5.2f")
    protected INDINumberElement temperatureTemp;

    /**
     * The upload mode for the images, if they should be saved on the server or
     * send to the client.
     */
    @InjectProperty(std = UPLOAD_MODE, label = "Upload", group = INDIDriver.GROUP_OPTIONS, timeout = 0, saveable = true)
    private INDISwitchProperty upload;

    /**
     * should the images be send to the client?
     */
    @InjectElement(std = UPLOAD_CLIENT, label = "Client", switchValue = SwitchStatus.ON)
    private INDISwitchElement uploadClient;

    /**
     * should the images be stored locally (where the driver resides)?
     */
    @InjectElement(std = UPLOAD_LOCAL, label = "Local")
    private INDISwitchElement uploadLocal;

    /**
     * should the images be send to the client and stored locally (where the
     * driver resides).
     */
    @InjectElement(std = UPLOAD_BOTH, label = "Both")
    private INDISwitchElement uploadBoth;

    /**
     * Upload directory and filenames for local storage.
     */
    @InjectProperty(std = UPLOAD_SETTINGS, label = "Upload Settings", group = INDIDriver.GROUP_OPTIONS, saveable = true)
    private INDITextProperty uploadSettings;

    /**
     * Upload directory for local storage.
     */
    @InjectElement(std = UPLOAD_DIR, label = "Dir")
    private INDITextElement uploadSettingsDir;

    /**
     * The prefix for the images to store. (The "XX" part of the name will be
     * replaced by a number).
     */
    @InjectElement(std = UPLOAD_PREFIX, label = "Prefix", textValue = "IMAGE_XX")
    private INDITextElement uploadSettingsPrefix;

    /**
     * the CCD extention, where the real ccd interfacing happens, this is the
     * primary ccd.
     */
    @InjectExtension(prefix = "CCD_", rename = {
        @Rename(name = "CCD", to = "CCD1")
    })
    protected INDICCDDriverExtension primaryCCD;

    /**
     * the CCD extention, where the real guider ccd interfacing happens, this is
     * the guider ccd.
     */
    @InjectExtension(prefix = "GUIDER_", rename = {
        @Rename(name = "CCD", to = "CCD2")
    })
    protected INDICCDDriverExtension guiderCCD;

    /**
     * a capability object with booleans that specify what this dirver can and
     * can not do.
     */
    private final Capability capability = defineCapabilities();

    /**
     * The CCD driver constructor, all subclasses must call this. All local
     * event handlers are here attached to the properties.
     * 
     * @param connection
     *            the indi connection to the server.
     */
    public INDICCDDriver(INDIConnection connection) {
        super(connection);
        primaryCCD.setDriverInterface(this);
        guiderCCD.setDriverInterface(createGuiderDriverHandler());
        temperature.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newTemperatureValue(elementsAndValues);
            }
        });
        upload.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newUploadValue(elementsAndValues);
            }
        });
        uploadSettings.setEventHandler(new TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                property.setValues(elementsAndValues);
                updateProperty(property);
            }
        });
    }

    /**
     * the client send a new value for the temperature.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newTemperatureValue(INDINumberElementAndValue[] elementsAndValues) {
        temperature.setValues(elementsAndValues);
        Boolean rc = setTemperature(temperatureTemp.getValue());
        if (rc == null) {
            temperature.setState(PropertyStates.ALERT);
        } else if (rc.booleanValue()) {
            temperature.setState(PropertyStates.OK);
        } else {
            temperature.setState(PropertyStates.BUSY);
        }
        updateProperty(temperature);
    }

    /**
     * the client send a new value for the upload directory.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newUploadValue(INDISwitchElementAndValue[] elementsAndValues) {
        upload.setValues(elementsAndValues);
        upload.setState(PropertyStates.OK);

        updateProperty(upload);
        if (uploadClient.isOn()) {
            LOG.debug(String.format("Upload settings set to client only."));
        } else if (uploadLocal.isOn()) {
            LOG.debug(String.format("Upload settings set to local only."));
        } else {
            LOG.debug(String.format("Upload settings set to client and local."));
        }
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
     * should call this functon when an exposure is complete.
     * 
     * @param targetChip
     *            chip that contains upload image data
     * @return true if the operation was successful.
     */
    protected boolean exposureComplete(INDICCDDriverExtension targetChip) {
        if (targetChip == primaryCCD) {
            return primaryCCD.exposureComplete();
        } else {
            return guiderCCD.exposureComplete();
        }
    }

    /**
     * calculate a unique non existent filename to save an image localy.
     * 
     * @param extension
     *            the file extension to use (file format)
     * @return the file to use to store the next image.
     * @throws IOException
     *             if the driver could not read on the filesystem.
     */
    protected File getFileWithIndex(String extension) throws IOException {
        String dir = uploadSettingsDir.getValue();
        String prefix = uploadSettingsPrefix.getValue();

        int indexOfXX = prefix.indexOf("XX");
        String uptoXX = prefix.substring(0, indexOfXX);
        String afterXX = prefix.substring(indexOfXX + 2) + "." + extension;

        int maxNumber = 0;
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            for (File file : dirFile.listFiles()) {
                if (file.getName().startsWith(uptoXX) && file.getName().endsWith(afterXX)) {
                    int number = Integer.parseInt(file.getName().substring(indexOfXX, file.getName().lastIndexOf('.')));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                }
            }
        } else {
            if (!dirFile.mkdirs()) {
                LOG.info("Could not create directoty: " + dir);
            }
        }
        maxNumber++;
        File fp = new File(dir, prefix.replace("XX", Integer.toString(maxNumber)) + "." + extension);
        return fp;
    }

    /**
     * Set CCD temperature. Upon returning false, the property becomes BUSY.
     * Once the temperature reaches the requested value, change the state to OK.
     * This must be implemented in the child class
     * 
     * @param theTargetTemperature
     *            CCD temperature in degrees celcius.
     * @return true or false if setting the temperature call to the hardware is
     *         successful. null if an error is encountered. Return false if
     *         setting the temperature to the requested value takes time. Return
     *         true if setting the temperature to the requested value is
     *         complete.
     */
    protected abstract Boolean setTemperature(double theTargetTemperature);

    /**
     * @return a collection of capabilities of this driver.
     */
    protected Capability capability() {
        return capability;
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        primaryCCD.connect();
        if (capability.hasGuideHead()) {
            guiderCCD.connect();
        }
        if (capability.hasCooler()) {
            addProperty(temperature);
        }
        addProperty(upload);

        if (uploadSettingsDir.getValue() == null || uploadSettingsDir.getValue().trim().isEmpty()) {
            uploadSettingsDir.setValue(new File(FileUtils.getI4JBaseDirectory(), "images").getAbsolutePath());
        }
        addProperty(uploadSettings);
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        primaryCCD.disconnect();
        if (capability.hasGuideHead()) {
            guiderCCD.disconnect();
        }
        if (capability.hasCooler()) {
            removeProperty(temperature);
        }
        removeProperty(upload);
        removeProperty(uploadSettings);
    }

    /**
     * @return true if the images should be saved locally (where the driver
     *         resides).
     */
    public boolean shouldSaveImage() {
        return uploadLocal.isOn() || uploadBoth.isOn();
    }

    /**
     * @return true if the image should be send to the client.
     */
    public boolean shouldSendImage() {
        return uploadClient.isOn() || uploadBoth.isOn();
    }

}
