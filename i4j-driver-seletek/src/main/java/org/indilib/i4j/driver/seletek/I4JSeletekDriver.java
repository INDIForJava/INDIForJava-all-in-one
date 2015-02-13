package org.indilib.i4j.driver.seletek;

/*
 * #%L
 * INDI for Java Driver for the Seletek
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

import static org.indilib.i4j.Constants.PropertyPermissions.RO;
import static org.indilib.i4j.Constants.PropertyStates.OK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.indilib.i4j.Constants.LightStates;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDILightElement;
import org.indilib.i4j.driver.INDILightProperty;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDIPortProperty;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchOneOfManyProperty;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.connection.INDIConnectionHandler;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that acts as a INDI for Java Driver for the Seletek (by Lunatico
 * [http://lunatico.es]).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class I4JSeletekDriver extends INDIDriver implements INDIConnectionHandler {

    /**
     * the maximum temperature for the property.
     */
    private static final int MAXIMUM_TEMPERATUR = 100;

    /**
     * the minimum temperature for the property.
     */
    private static final int MINIMUM_TEMPERATUR = -50;

    /**
     * the temperature stepping for the property.
     */
    private static final double STEP_TEMPERATUR = 0.1;

    /**
     * index in the seletek version string of the build number.
     */
    private static final int SELETEK_VERSION_BUILT_INDEX = 3;

    /**
     * index in the seletek version string of the sub version number.
     */
    private static final int SELETEK_VERSION_SUB_VERSION_INDEX = 2;

    /**
     * index in the seletek version string of the main version number.
     */
    private static final int SELETEK_VERSION_MAIN_VERSION_INDEX = 1;

    /**
     * index in the seletek version string of the model type.
     */
    private static final int SELETEK_VERSION_TYPE_INDEX = 0;

    /**
     * minus 15 degrees.
     */
    private static final int MINUS_15_DEGREES = -15;

    /**
     * the maximum power value.
     */
    protected static final int MAX_POWER_VALUE = 1023;

    /**
     * the number of milliseconds to wait before closing the ports.
     */
    protected static final int MILLISECONDS_TO_WAIT_BEFORE_CLOSE_PORTS = 200;

    /**
     * logger to use.
     */
    private static final Logger LOG = LoggerFactory.getLogger(I4JSeletekDriver.class);

    /**
     * The Port Property.
     */
    private INDIPortProperty portP;

    /**
     * The Information Property.
     */
    private INDITextProperty seletekInfoP;

    /**
     * The Seletek Version Element.
     */
    private INDITextElement seletekVersionE;

    /**
     * The Seletek Serial Number Element.
     */
    private INDITextElement seletekSerialNumberE;

    /**
     * The Main Device Property.
     */
    private INDISwitchOneOfManyProperty mainDeviceP;

    /**
     * The Exp Device Property.
     */
    private INDISwitchOneOfManyProperty expDeviceP;

    /**
     * The Third Device Property.
     */
    private INDISwitchOneOfManyProperty thirdDeviceP;

    /**
     * The Temperature Sensors Property.
     */
    private INDINumberProperty temperatureSensorsP;

    /**
     * The internal temperature Element.
     */
    private INDINumberElement internalTemperatureE;

    /**
     * The external temperature Eleent.
     */
    private INDINumberElement externalTemperatureE;

    /**
     * The Power OK Property.
     */
    private INDILightProperty powerOkP;

    /**
     * The Power Ok Element.
     */
    private INDILightElement powerOkE;

    /**
     * The input stream from which to read from the Seletek.
     */
    private FileInputStream seletekInput;

    /**
     * The output stream to write to the Seletek.
     */
    private FileOutputStream seletekOutput;

    /**
     * The thread that reads the messages from the Seletek.
     */
    private SeletekReadingThread readingThread;

    /**
     * The thread that asks for the temperatures and sernsors of the Seletek.
     */
    private SeletekSensorStatusRequesterThread sensorStatusThread;

    /**
     * Readed internal temperatures.
     */
    private double[] internalTemperatures;

    /**
     * The current internal temperature.
     */
    private int currentInternalTemperature;

    /**
     * Readed external temperatures.
     */
    private double[] externalTemperatures;

    /**
     * The current external temerature.
     */
    private int currentExternalTemperature;

    /**
     * The main subdriver.
     */
    private INDIDriver mainSubdriver;

    /**
     * The exp subdriver.
     */
    private INDIDriver expSubdriver;

    /**
     * The third subdriver.
     */
    private INDIDriver thirdSubdriver;

    /**
     * Constructs an instance of a <code>I4JSeletekDriver</code> with a
     * particular <code>inputStream</code> from which to read the incoming
     * messages (from clients) and a <code>outputStream</code> to write the
     * messages to the clients.
     * 
     * @param connection
     *            the indi connection to the server.
     */
    public I4JSeletekDriver(INDIConnection connection) {
        super(connection);

        internalTemperatures = new double[SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS];
        externalTemperatures = new double[SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS];
        currentInternalTemperature = 0;
        currentExternalTemperature = 0;

        portP = INDIPortProperty.create(this, "/dev/ttyUSB0");

        seletekInfoP = newTextProperty().name("seletekinfo").label("Seletek Info").group(INDIDriver.GROUP_MAIN_CONTROL).state(OK).permission(RO).create();

        seletekVersionE = seletekInfoP.newElement().name("version").label("Version").textValue("?").create();
        seletekSerialNumberE = seletekInfoP.newElement().name("serialnumber").label("Serial Number").textValue("N/A").create();

        temperatureSensorsP = newNumberProperty().name("temperatures").label("Temperatures").group("Device Sensors").permission(RO).create();
        internalTemperatureE = temperatureSensorsP.newElement().name("internalTemperature").label("Internal")//
                .minimum(MINIMUM_TEMPERATUR).maximum(MAXIMUM_TEMPERATUR).step(STEP_TEMPERATUR).numberFormat("%1.1f").create();
        externalTemperatureE = temperatureSensorsP.newElement().name("externalTemperature").label("External")//
                .minimum(MINIMUM_TEMPERATUR).maximum(MAXIMUM_TEMPERATUR).step(STEP_TEMPERATUR).numberFormat("%1.1f").create();

        powerOkP = newLightProperty().name("power").label("Power").group("Device Sensors").create();
        powerOkE = powerOkP.newElement().name("power").label("Power").create();

        mainDeviceP = newProperty(INDISwitchOneOfManyProperty.class).saveable(true).name("mainDevice").label("Main Device").group(INDIDriver.GROUP_MAIN_CONTROL).create();
        mainDeviceP.newElement().name("None").switchValue(SwitchStatus.ON).create();
        mainDeviceP.newElement().name("Focuser").create();

        expDeviceP = newProperty(INDISwitchOneOfManyProperty.class).saveable(true).name("expDevice").label("Exp. Device").group(INDIDriver.GROUP_MAIN_CONTROL).create();
        expDeviceP.newElement().name("None").switchValue(SwitchStatus.ON).create();
        expDeviceP.newElement().name("Focuser").create();

        thirdDeviceP = newProperty(INDISwitchOneOfManyProperty.class).saveable(true).name("thirdDevice").label("Third Device").group(INDIDriver.GROUP_MAIN_CONTROL).create();
        thirdDeviceP.newElement().name("None").switchValue(SwitchStatus.ON).create();
        thirdDeviceP.newElement().name("Focuser").create();

        this.addProperty(portP);
    }

    @Override
    protected void propertiesRequested() {
        if (mainSubdriver != null) {
            mainSubdriver.sendAllProperties();
        }

        if (expSubdriver != null) {
            expSubdriver.sendAllProperties();
        }

        if (thirdSubdriver != null) {
            thirdSubdriver.sendAllProperties();
        }
    }

    @Override
    public String getName() {
        return "Seletek Armadillo (by Lunatico)";
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
        if (property == portP) {
            portP.processTextValue(property, elementsAndValues);
        }
    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        if (property == mainDeviceP) {
            int newPos = mainDeviceP.getSelectedIndex(elementsAndValues);

            if (mainDeviceP.getSelectedIndex() != newPos) {
                destroyMainSubdriver();

                mainDeviceP.setSelectedIndex(elementsAndValues);

                if (mainDeviceP.getSelectedValue().compareTo("Focuser") == 0) {
                    mainSubdriver = new SeletekFocuser(super.getINDIConnection(), 0, this);
                    registerSubdriver(mainSubdriver);
                }
            }
            mainDeviceP.setState(PropertyStates.OK);

            updateProperty(mainDeviceP);

        }

        if (property == expDeviceP) {
            int newPos = expDeviceP.getSelectedIndex(elementsAndValues);

            if (expDeviceP.getSelectedIndex() != newPos) {
                destroyExpSubdriver();

                expDeviceP.setSelectedIndex(elementsAndValues);

                if (expDeviceP.getSelectedValue().compareTo("Focuser") == 0) {
                    expSubdriver = new SeletekFocuser(super.getINDIConnection(), 1, this);
                    registerSubdriver(expSubdriver);
                }
            }
            expDeviceP.setState(PropertyStates.OK);

            updateProperty(expDeviceP);

        }

        if (property == thirdDeviceP) {
            int newPos = thirdDeviceP.getSelectedIndex(elementsAndValues);

            if (thirdDeviceP.getSelectedIndex() != newPos) {
                destroyThirdSubdriver();

                thirdDeviceP.setSelectedIndex(elementsAndValues);

                if (thirdDeviceP.getSelectedValue().compareTo("Focuser") == 0) {
                    thirdSubdriver = new SeletekFocuser(super.getINDIConnection(), 2, this);
                    registerSubdriver(thirdSubdriver);
                }
            }
            thirdDeviceP.setState(PropertyStates.OK);

            updateProperty(thirdDeviceP);

        }
    }

    /**
     * Destroys the Main subdriver.
     */
    private void destroyMainSubdriver() {
        if (mainSubdriver != null) {
            unregisterSubdriver(mainSubdriver);
            mainSubdriver.isBeingDestroyed();
            mainSubdriver = null;
        }
    }

    /**
     * Destroys the Exp subdriver.
     */
    private void destroyExpSubdriver() {
        if (expSubdriver != null) {
            unregisterSubdriver(expSubdriver);
            expSubdriver.isBeingDestroyed();
            expSubdriver = null;
        }
    }

    /**
     * Destroys the Third subdriver.
     */
    private void destroyThirdSubdriver() {
        if (thirdSubdriver != null) {
            unregisterSubdriver(thirdSubdriver);
            thirdSubdriver.isBeingDestroyed();
            thirdSubdriver = null;
        }
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        LOG.info("Connecting to Seletek");

        File port = new File(portP.getPort());
        if (!port.exists()) {
            throw new INDIException("Connection to the Seletek failed: port file does not exist.");
        }

        try {
            seletekInput = new FileInputStream(portP.getPort());
            seletekOutput = new FileOutputStream(portP.getPort());

            readingThread = new SeletekReadingThread(this, seletekInput);
            readingThread.start();

            sensorStatusThread = new SeletekSensorStatusRequesterThread(this);
            sensorStatusThread.start();
        } catch (IOException e) {
            throw new INDIException("Connection to the Seletek failed. Check port permissions");
        }

        this.addProperty(seletekInfoP);
        this.addProperty(mainDeviceP);
        this.addProperty(expDeviceP);
        this.addProperty(temperatureSensorsP);
        this.addProperty(powerOkP);

        if (mainDeviceP.getSelectedValue().compareTo("Focuser") == 0) {
            mainSubdriver = new SeletekFocuser(super.getINDIConnection(), 0, this);
            registerSubdriver(mainSubdriver);
        }

        if (expDeviceP.getSelectedValue().compareTo("Focuser") == 0) {
            expSubdriver = new SeletekFocuser(super.getINDIConnection(), 1, this);
            registerSubdriver(expSubdriver);
        }

        askForSeletekVersion();
        askForSerialNumber();
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        LOG.info("Disconnecting from Seletek");
        try {
            if (readingThread != null) {
                readingThread.stopReading();
                sensorStatusThread.stopRequesting();
            }

            destroyMainSubdriver();
            destroyExpSubdriver();
            destroyThirdSubdriver();

            Utils.sleep(MILLISECONDS_TO_WAIT_BEFORE_CLOSE_PORTS);

            if (seletekInput != null) {
                seletekInput.close();
                seletekOutput.close();
            }

            seletekInput = null;
            seletekOutput = null;
        } catch (IOException e) {
            LOG.error("io exception durin close", e);
        }

        this.removeProperty(seletekInfoP);
        this.removeProperty(mainDeviceP);
        this.removeProperty(expDeviceP);
        this.removeProperty(thirdDeviceP);
        this.removeProperty(temperatureSensorsP);
        this.removeProperty(powerOkP);

        LOG.info("Disconnected from Seletek");
    }

    /**
     * Sends the command to the Seletek to obtain its version.
     */
    private void askForSeletekVersion() {
        sendCommandToSeletek("!seletek version#");
    }

    /**
     * Sends the command to the Seletek to get its serial number.
     */
    private void askForSerialNumber() {
        sendCommandToSeletek("!seletek getsernum#");
    }

    /**
     * Sends the command to the Seletek to get the internal termperature sensor
     * reading.
     */
    protected void askForInternalTemperature() {
        sendCommandToSeletek("!read temps 0#");
    }

    /**
     * Sends the command to the Seletek to get the external termperature sensor
     * reading.
     */
    protected void askForExternalTemperature() {
        sendCommandToSeletek("!read temps 1#");
    }

    /**
     * Sends the command to the Seletek to get information about its power
     * status.
     */
    protected void askForPowerOk() {
        sendCommandToSeletek("!seletek powok#");
    }

    /**
     * Sends the command to the Seletek to get the position of a focuser.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     */
    protected void getStepperPos(int port) {
        sendCommandToSeletek("!step getpos " + port + "#");
    }

    /**
     * Sends the command to the Seletek to set the focuser speed.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     * @param speed
     *            The new speed for the focuser
     */
    protected void setStepperSpeed(int port, int speed) {
        if (speed < 0) {
            speed = 0;
        }
        // TODO: a java doc explination would be very good here.
        int newSpeed = 21 - speed * 2 + 5;

        sendCommandToSeletek("!step speed " + port + " " + newSpeed + "#");
    }

    /**
     * Sends the command to the Seletek to ask the focuser to go to a particular
     * position.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     * @param position
     *            The desired position for the focuser
     */
    protected void stepperGotoAbs(int port, int position) {
        sendCommandToSeletek("!step goto " + port + " " + position + "#");
    }

    /**
     * Sends the command to the Seletek to stop the focuser.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     */
    protected void stopStepper(int port) {
        sendCommandToSeletek("!step stop " + port + "#");
    }

    /**
     * Sends the command to the Seletek to set the wire mode of the focuser.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     * @param wireMode
     *            0 - Lunático, 1 - Lunático Inverted, 2 - RF/Moonlite, 3 -
     *            RF/Moonlite Inverted
     */
    protected void setStepperWireMode(int port, int wireMode) {
        sendCommandToSeletek("!step wiremode " + port + " " + wireMode + "#");
    }

    /**
     * Sends the command to the Seletek to set the model of the focuser.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     * @param model
     *            0 - Unipolar, 1 - Bipolar, 2 - DC, 3 - Step and Dir
     */
    protected void setStepperModel(int port, int model) {
        sendCommandToSeletek("!step model " + port + " " + model + "#");
    }

    /**
     * Sends the command to the Seletek to set the focuser to half step.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     * @param halfStep
     *            <code>true</code> if the ficuser must be set at half step
     *            mode.
     */
    protected void setStepperHalfStep(int port, boolean halfStep) {
        int h = 0;
        if (halfStep) {
            h = 1;
        }

        sendCommandToSeletek("!step halfstep " + port + " " + h + "#");
    }

    /**
     * Sends the command to the Seletek to set the moving power for the focuser.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     * @param power
     *            The new power for the focuser when moving
     */
    void setStepperMovePower(int port, int power) {
        power = Math.max(power, 0);
        power = Math.min(power, MAX_POWER_VALUE);
        sendCommandToSeletek("!step movepow " + port + " " + power + "#");
    }

    /**
     * Sends the command to the Seletek to set the stopped power for the
     * focuser.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     * @param power
     *            The new power for the focuser when stopped
     */
    void setStepperStopPower(int port, int power) {
        power = Math.max(power, 0);
        power = Math.min(power, MAX_POWER_VALUE);
        sendCommandToSeletek("!step stoppow " + port + " " + power + "#");
    }

    /**
     * Sends a command to the Seletek.
     * 
     * @param command
     *            The command to be set to the Seletek
     */
    private synchronized void sendCommandToSeletek(String command) {
        try {
            seletekOutput.write(command.getBytes());
            seletekOutput.flush();
        } catch (IOException e) {
            LOG.error("io exception", e);
        }
    }

    /**
     * Parses the messages from the Seletek.
     * 
     * @param buffer
     *            The messages from the Seletek
     * @return The messages that have still to be parsed (this function is
     *         called recursively)
     */
    protected String parseSeletekMessages(String buffer) {
        int initPos = buffer.indexOf("!");

        if (initPos == -1) { // Discard message
            return "";
        }

        if (initPos != 0) {
            buffer = buffer.substring(initPos);
        }

        // initPos is always 0;
        int endPos = buffer.indexOf("#");

        if (endPos == -1) {
            return buffer;
        }

        int dotsPos = buffer.indexOf(":");

        String portS = buffer.substring(dotsPos - 1, dotsPos);
        int port = 0;
        if (portS.compareTo("1") == 0) {
            port = 1;
        }

        String returnParam = buffer.substring(dotsPos + 1, endPos);

        if (buffer.startsWith("!seletek version:")) {
            parseVersion(returnParam);
        } else if (buffer.startsWith("!seletek getsernum:")) {
            parseSerialNumber(returnParam);
        } else if (buffer.startsWith("!read temps 0:")) {
            parseInternalTemperature(returnParam);
        } else if (buffer.startsWith("!read temps 1:")) {
            parseExternalTemperature(returnParam);
        } else if (buffer.startsWith("!seletek powok:")) {
            parsePowerOk(returnParam);
        } else if (buffer.startsWith("!step getpos")) {
            parseStepperPos(port, returnParam);
        } else if (buffer.startsWith("!step speed")) {
            parseStepperSpeed(port, returnParam);
        } else if (buffer.startsWith("!step stop ")) {
            parseStepperStop(port);
        }

        buffer = buffer.substring(endPos + 1);

        return parseSeletekMessages(buffer); // Recursively parse commands
    }

    /**
     * Parses the external temperature.
     * 
     * @param temp
     *            The external temperature
     */
    private void parseExternalTemperature(String temp) {
        int t = Integer.parseInt(temp);

        // TODO: document this "black magic"?
        double nt = ((t - 192) * 1.7 - 0) / 10;

        externalTemperatures[currentExternalTemperature] = nt;

        currentExternalTemperature++;

        if (currentExternalTemperature == SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS) {
            currentExternalTemperature = 0;

            double sum = 0;

            for (int i = 0; i < SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS; i++) {
                sum += externalTemperatures[i];
            }

            double averageTemp = sum / SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS;

            if (averageTemp > MINUS_15_DEGREES) {
                temperatureSensorsP.setState(PropertyStates.OK);
                externalTemperatureE.setValue("" + averageTemp);

                updateProperty(temperatureSensorsP);

            }
        }
    }

    /**
     * Parses the internal temperature.
     * 
     * @param temp
     *            The internal temperature
     */
    private void parseInternalTemperature(String temp) {
        int t = Integer.parseInt(temp);

        // TODO: document this "black magic"?
        double nt = ((t - 261) * 1.8 - 250) / 10;

        internalTemperatures[currentInternalTemperature] = nt;

        currentInternalTemperature++;

        if (currentInternalTemperature == SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS) {
            currentInternalTemperature = 0;

            double sum = 0;

            for (int i = 0; i < SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS; i++) {
                sum += internalTemperatures[i];
            }

            double averageTemp = sum / SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS;
            temperatureSensorsP.setState(PropertyStates.OK);
            internalTemperatureE.setValue("" + averageTemp);

            updateProperty(temperatureSensorsP);

        }
    }

    /**
     * Gets the subdriver by the port number.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     * @return The subdriver for the specified port
     */
    private INDIDriver getDriverForPort(int port) {
        if (port == 0) {
            return mainSubdriver;
        }

        if (port == 1) {
            return expSubdriver;
        }

        return thirdSubdriver;
    }

    /**
     * Parses the stop focuser message.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     */
    private void parseStepperStop(int port) {
        INDIDriver subdriver = getDriverForPort(port);

        if (subdriver instanceof SeletekFocuser) {
            ((SeletekFocuser) subdriver).stopFocuser();
        }
    }

    /**
     * Parses the current position of the focuser message.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     * @param stepperPos
     *            The current position of the focuser
     */
    private void parseStepperPos(int port, String stepperPos) {
        INDIDriver subdriver = getDriverForPort(port);

        if (subdriver instanceof SeletekFocuser) {
            ((SeletekFocuser) subdriver).showFocusPosition(Integer.parseInt(stepperPos));
        }
    }

    /**
     * Parses the speed of the focuser message.
     * 
     * @param port
     *            0 for Main port, 1 for Exp port, 2 for Third port
     * @param stepperSpeed
     *            The current speed of the focuser
     */
    private void parseStepperSpeed(int port, String stepperSpeed) {
        INDIDriver subdriver = getDriverForPort(port);

        if (subdriver instanceof SeletekFocuser) {
            ((SeletekFocuser) subdriver).setDesiredSpeed();
        }
    }

    /**
     * Parses the power status message of the Seletek.
     * 
     * @param powerOk
     *            The power status of the Seletek
     */
    private void parsePowerOk(String powerOk) {
        powerOkP.setState(PropertyStates.OK);

        if (powerOk.startsWith("1")) {
            powerOkE.setValue(LightStates.OK);
        } else {
            powerOkE.setValue(LightStates.ALERT);
        }

        updateProperty(powerOkP);

    }

    /**
     * Parses the serial number message of the Seletek.
     * 
     * @param serialNumber
     *            The serial number of the Seletek
     */
    private void parseSerialNumber(String serialNumber) {
        seletekInfoP.setState(PropertyStates.OK);
        seletekSerialNumberE.setValue(serialNumber);

        updateProperty(seletekInfoP);

    }

    /**
     * Parses the version message of the Seletek.
     * 
     * @param versionParam
     *            The version of the Seletek
     */
    private void parseVersion(String versionParam) {
        String version = "";
        if (versionParam.charAt(SELETEK_VERSION_TYPE_INDEX) == '1') {
            version += "Original Seletek";
        } else if (versionParam.charAt(0) == '2') {
            version += "Armadillo Seletek";
        } else if (versionParam.charAt(0) == '3') {
            version += "Platypus Seletek";

            this.addProperty(thirdDeviceP);
            if (thirdDeviceP.getSelectedValue().compareTo("Focuser") == 0) {
                thirdSubdriver = new SeletekFocuser(super.getINDIConnection(), 2, this);
                registerSubdriver(thirdSubdriver);
            }
        } else {
            version += "Unknown Seletek";
        }

        version += " - ";
        version += versionParam.charAt(SELETEK_VERSION_MAIN_VERSION_INDEX);
        version += ".";
        version += versionParam.charAt(SELETEK_VERSION_SUB_VERSION_INDEX);
        version += " (Build " + versionParam.charAt(SELETEK_VERSION_BUILT_INDEX);
        version += ")";

        seletekInfoP.setState(PropertyStates.OK);
        seletekVersionE.setValue(version);

        updateProperty(seletekInfoP);

    }

    @Override
    public void isBeingDestroyed() {
        LOG.info("Destroy Seletek Driver");

        destroyMainSubdriver();
        destroyExpSubdriver();

        super.isBeingDestroyed();
    }
}
