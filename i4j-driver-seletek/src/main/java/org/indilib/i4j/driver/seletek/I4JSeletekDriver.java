/*
 *  This file is part of INDI for Java Seletek Driver.
 * 
 *  INDI for Java Seletek Driver is free software: you can 
 *  redistribute it and/or modify it under the terms of the GNU General Public 
 *  License as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Seletek Driver is distributed in the hope that it
 *  will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Seletek Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.indilib.i4j.Constants.LightStates;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIBLOBElementAndValue;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIConnectionHandler;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDILightElement;
import org.indilib.i4j.driver.INDILightProperty;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDIPortProperty;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchOneOfManyProperty;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that acts as a INDI for Java Driver for the Seletek (by Lunatico
 * [http://lunatico.es]).
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.35, November 11, 2013
 */
public class I4JSeletekDriver extends INDIDriver implements INDIConnectionHandler {
    private static Logger LOG = LoggerFactory.getLogger(I4JSeletekDriver.class);
  /**
   * The Port Property
   */
  private INDIPortProperty portP;
  /**
   * The Information Property
   */
  private INDITextProperty seletekInfoP;
  /**
   * The Seletek Version Element
   */
  private INDITextElement seletekVersionE;
  /**
   * The Seletek Serial Number Element
   */
  private INDITextElement seletekSerialNumberE;
  /**
   * The Main Device Property
   */
  private INDISwitchOneOfManyProperty mainDeviceP;
  /**
   * The Exp Device Property
   */
  private INDISwitchOneOfManyProperty expDeviceP;
  /**
   * The Third Device Property
   */
  private INDISwitchOneOfManyProperty thirdDeviceP;
  /**
   * The Temperature Sensors Property
   */
  private INDINumberProperty temperatureSensorsP;
  /**
   * The internal temperature Element
   */
  private INDINumberElement internalTemperatureE;
  /**
   * The external temperature Eleent
   */
  private INDINumberElement externalTemperatureE;
  /**
   * The Power OK Property
   */
  private INDILightProperty powerOkP;
  /**
   * The Power Ok Element
   */
  private INDILightElement powerOkE;
  /**
   * The input stream from which to read from the Seletek
   */
  private FileInputStream seletekInput;
  /**
   * The output stream to write to the Seletek
   */
  private FileOutputStream seletekOutput;
  /**
   * The thread that reads the messages from the Seletek
   */
  private SeletekReadingThread readingThread;
  /**
   * The thread that asks for the temperatures and sernsors of the Seletek
   */
  private SeletekSensorStatusRequesterThread sensorStatusThread;
  /**
   * Readed internal temperatures
   */
  private double[] internalTemperatures;
  /**
   * The current internal temperature
   */
  int currentInternalTemperature;
  /**
   * Readed external temperatures
   */
  private double[] externalTemperatures;
  /**
   * The current external temerature
   */
  int currentExternalTemperature;
  /**
   * The main subdriver
   */
  private INDIDriver mainSubdriver;
  /**
   * The exp subdriver
   */
  private INDIDriver expSubdriver;
  /**
   * The third subdriver
   */
  private INDIDriver thirdSubdriver;

  /**
   * Constructs an instance of a
   * <code>I4JSeletekDriver</code> with a particular
   * <code>inputStream<code> from which to read the incoming messages (from clients) and a
   * <code>outputStream</code> to write the messages to the clients.
   *
   * @param inputStream The stream from which to read messages
   * @param outputStream The stream to which to write the messages
   */
  public I4JSeletekDriver(InputStream inputStream, OutputStream outputStream) {
    super(inputStream, outputStream);

    internalTemperatures = new double[SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS];
    externalTemperatures = new double[SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS];
    currentInternalTemperature = 0;
    currentExternalTemperature = 0;

    portP = INDIPortProperty.createSaveablePortProperty(this, "/dev/ttyUSB0");

    seletekInfoP = new INDITextProperty(this, "seletekinfo", "Seletek Info", "Main Control", PropertyStates.OK, PropertyPermissions.RO);
    seletekVersionE = new INDITextElement(seletekInfoP, "version", "Version", "?");
    seletekSerialNumberE = new INDITextElement(seletekInfoP, "serialnumber", "Serial Number", "N/A");

    temperatureSensorsP = new INDINumberProperty(this, "temperatures", "Temperatures", "Device Sensors", PropertyStates.IDLE, PropertyPermissions.RO);
    internalTemperatureE = new INDINumberElement(temperatureSensorsP, "internalTemperature", "Internal", "0.0", "-50.0", "100.0", "0.1", "%1.1f");
    externalTemperatureE = new INDINumberElement(temperatureSensorsP, "externalTemperature", "External", "0.0", "-50.0", "100.0", "0.1", "%1.1f");

    powerOkP = new INDILightProperty(this, "power", "Power", "Device Sensors", PropertyStates.IDLE);
    powerOkE = new INDILightElement(powerOkP, "power", "Power", LightStates.IDLE);

    mainDeviceP = INDISwitchOneOfManyProperty.createSaveableSwitchOneOfManyProperty(this, "mainDevice", "Main Device", "Main Control", PropertyStates.IDLE, PropertyPermissions.RW, new String[]{"None", "Focuser"});

    expDeviceP = INDISwitchOneOfManyProperty.createSaveableSwitchOneOfManyProperty(this, "expDevice", "Exp. Device", "Main Control", PropertyStates.IDLE, PropertyPermissions.RW, new String[]{"None", "Focuser"});

    thirdDeviceP = INDISwitchOneOfManyProperty.createSaveableSwitchOneOfManyProperty(this, "thirdDevice", "Third Device", "Main Control", PropertyStates.IDLE, PropertyPermissions.RW, new String[]{"None", "Focuser"});

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
          mainSubdriver = new SeletekFocuser(super.getInputStream(), super.getOutputStream(), 0, this);
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
          expSubdriver = new SeletekFocuser(super.getInputStream(), super.getOutputStream(), 1, this);
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
          thirdSubdriver = new SeletekFocuser(super.getInputStream(), super.getOutputStream(), 2, this);
          registerSubdriver(thirdSubdriver);
        }
      }
      thirdDeviceP.setState(PropertyStates.OK);


        updateProperty(thirdDeviceP);

    }
  }

  /**
   * Destroys the Main subdriver
   */
  private void destroyMainSubdriver() {
    if (mainSubdriver != null) {
      unregisterSubdriver(mainSubdriver);
      mainSubdriver.isBeingDestroyed();
      mainSubdriver = null;
    }
  }

  /**
   * Destroys the Exp subdriver
   */
  private void destroyExpSubdriver() {
    if (expSubdriver != null) {
      unregisterSubdriver(expSubdriver);
      expSubdriver.isBeingDestroyed();
      expSubdriver = null;
    }
  }

  /**
   * Destroys the Third subdriver
   */
  private void destroyThirdSubdriver() {
    if (thirdSubdriver != null) {
      unregisterSubdriver(thirdSubdriver);
      thirdSubdriver.isBeingDestroyed();
      thirdSubdriver = null;
    }
  }

  @Override
  public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
  }

  @Override
  public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
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
      mainSubdriver = new SeletekFocuser(super.getInputStream(), super.getOutputStream(), 0, this);
      registerSubdriver(mainSubdriver);
    }

    if (expDeviceP.getSelectedValue().compareTo("Focuser") == 0) {
      expSubdriver = new SeletekFocuser(super.getInputStream(), super.getOutputStream(), 1, this);
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

      Utils.sleep(200);

      if (seletekInput != null) {
        seletekInput.close();
        seletekOutput.close();
      }

      seletekInput = null;
      seletekOutput = null;
    } catch (IOException e) {
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
  void askForInternalTemperature() {
    sendCommandToSeletek("!read temps 0#");
  }

  /**
   * Sends the command to the Seletek to get the external termperature sensor
   * reading.
   */
  void askForExternalTemperature() {
    sendCommandToSeletek("!read temps 1#");
  }

  /**
   * Sends the command to the Seletek to get information about its power status.
   */
  void askForPowerOk() {
    sendCommandToSeletek("!seletek powok#");
  }

  /**
   * Sends the command to the Seletek to get the position of a focuser.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   */
  void getStepperPos(int port) {
    sendCommandToSeletek("!step getpos " + port + "#");
  }

  /**
   * Sends the command to the Seletek to set the focuser speed.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   * @param speed The new speed for the focuser
   */
  void setStepperSpeed(int port, int speed) {
    if (speed < 0) {
      speed = 0;
    }

    int newSpeed = 21 - speed * 2 + 5;

    sendCommandToSeletek("!step speed " + port + " " + newSpeed + "#");
  }

  /**
   * Sends the command to the Seletek to ask the focuser to go to a particular
   * position.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   * @param position The desired position for the focuser
   */
  void stepperGotoAbs(int port, int position) {
    sendCommandToSeletek("!step goto " + port + " " + position + "#");
  }

  /**
   * Sends the command to the Seletek to stop the focuser.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   */
  void stopStepper(int port) {
    sendCommandToSeletek("!step stop " + port + "#");
  }

  /**
   * Sends the command to the Seletek to set the wire mode of the focuser.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   * @param wireMode 0 - Lunático, 1 - Lunático Inverted, 2 - RF/Moonlite, 3 -
   * RF/Moonlite Inverted
   */
  void setStepperWireMode(int port, int wireMode) {
    sendCommandToSeletek("!step wiremode " + port + " " + wireMode + "#");
  }

  /**
   * Sends the command to the Seletek to set the model of the focuser.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   * @param model 0 - Unipolar, 1 - Bipolar, 2 - DC, 3 - Step and Dir
   */
  void setStepperModel(int port, int model) {
    sendCommandToSeletek("!step model " + port + " " + model + "#");
  }

  /**
   * Sends the command to the Seletek to set the focuser to half step.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   * @param halfStep <code>true</code> if the ficuser must be set at half step
   * mode.
   */
  void setStepperHalfStep(int port, boolean halfStep) {
    int h = 0;
    if (halfStep) {
      h = 1;
    }

    sendCommandToSeletek("!step halfstep " + port + " " + h + "#");
  }

  /**
   * Sends the command to the Seletek to set the moving power for the focuser.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   * @param speed The new power for the focuser when moving
   */
  void setStepperMovePower(int port, int power) {
    if (power < 0) {
      power = 0;
    }

    if (power > 1023) {
      power = 1023;
    }

    sendCommandToSeletek("!step movepow " + port + " " + power + "#");
  }

  /**
   * Sends the command to the Seletek to set the stopped power for the focuser.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   * @param speed The new power for the focuser when stopped
   */
  void setStepperStopPower(int port, int power) {
    if (power < 0) {
      power = 0;
    }

    if (power > 1023) {
      power = 1023;
    }

    sendCommandToSeletek("!step stoppow " + port + " " + power + "#");
  }

  /**
   * Sends a command to the Seletek.
   *
   * @param command The command to be set to the Seletek
   */
  private synchronized void sendCommandToSeletek(String command) {
    try {
      seletekOutput.write(command.getBytes());
      seletekOutput.flush();
    } catch (IOException e) {
        LOG.error("io exception",e);
    }
  }

  /**
   * Parses the messages from the Seletek.
   *
   * @param buffer The messages from the Seletek
   * @return The messages that have still to be parsed (this function is called
   * recursively)
   */
  protected String parseSeletekMessages(String buffer) {
    int initPos = buffer.indexOf("!");

    if (initPos == -1) {  // Discard message
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

    return parseSeletekMessages(buffer);  // Recursively parse commands
  }

  /**
   * Parses the external temperature.
   *
   * @param temp The external temperature
   */
  private void parseExternalTemperature(String temp) {
    int t = Integer.parseInt(temp);

    double nt = (((t - 192) * 1.7) - 0) / 10;

    externalTemperatures[currentExternalTemperature] = nt;

    currentExternalTemperature++;

    if (currentExternalTemperature == SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS) {
      currentExternalTemperature = 0;

      double sum = 0;

      for (int i = 0 ; i < SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS ; i++) {
        sum += externalTemperatures[i];
      }

      double averageTemp = sum / SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS;

      if (averageTemp > -15) {
        temperatureSensorsP.setState(PropertyStates.OK);
        externalTemperatureE.setValue("" + averageTemp);


          updateProperty(temperatureSensorsP);

      }
    }
  }

  /**
   * Parses the internal temperature.
   *
   * @param temp The internal temperature
   */
  private void parseInternalTemperature(String temp) {
    int t = Integer.parseInt(temp);

    double nt = (((t - 261) * 1.8) - 250) / 10;

    internalTemperatures[currentInternalTemperature] = nt;

    currentInternalTemperature++;

    if (currentInternalTemperature == SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS) {
      currentInternalTemperature = 0;

      double sum = 0;

      for (int i = 0 ; i < SeletekSensorStatusRequesterThread.TEMPERATURE_READINGS ; i++) {
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
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
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
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   */
  private void parseStepperStop(int port) {
    INDIDriver subdriver = getDriverForPort(port);

    if (subdriver instanceof SeletekFocuser) {
      ((SeletekFocuser)subdriver).stopFocuser();
    }
  }

  /**
   * Parses the current position of the focuser message.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   * @param stepperPos The current position of the focuser
   */
  private void parseStepperPos(int port, String stepperPos) {
    INDIDriver subdriver = getDriverForPort(port);

    if (subdriver instanceof SeletekFocuser) {
      ((SeletekFocuser)subdriver).showFocusPosition(Integer.parseInt(stepperPos));
    }
  }

  /**
   * Parses the speed of the focuser message.
   *
   * @param port 0 for Main port, 1 for Exp port, 2 for Third port
   * @param stepperSpeed The current speed of the focuser
   */
  private void parseStepperSpeed(int port, String stepperSpeed) {
    INDIDriver subdriver = getDriverForPort(port);

    if (subdriver instanceof SeletekFocuser) {
      ((SeletekFocuser)subdriver).setDesiredSpeed();
    }
  }

  /**
   * Parses the power status message of the Seletek.
   *
   * @param powerOk The power status of the Seletek
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
   * @param serialNumber The serial number of the Seletek
   */
  private void parseSerialNumber(String serialNumber) {
    seletekInfoP.setState(PropertyStates.OK);
    seletekSerialNumberE.setValue(serialNumber);

      updateProperty(seletekInfoP);

  }

  /**
   * Parses the version message of the Seletek.
   *
   * @param versionParam The version of the Seletek
   */
  private void parseVersion(String versionParam) {
    String version = "";
    if (versionParam.charAt(0) == '1') {
      version += "Original Seletek";
    } else if (versionParam.charAt(0) == '2') {
      version += "Armadillo Seletek";
    } else if (versionParam.charAt(0) == '3') {
      version += "Platypus Seletek";

      this.addProperty(thirdDeviceP);
      if (thirdDeviceP.getSelectedValue().compareTo("Focuser") == 0) {
        thirdSubdriver = new SeletekFocuser(super.getInputStream(), super.getOutputStream(), 2, this);
        registerSubdriver(thirdSubdriver);
      }
    } else {
      version += "Unknown Seletek";
    }

    version += " - ";
    version += versionParam.charAt(1);
    version += ".";
    version += versionParam.charAt(2);
    version += " (Build " + versionParam.charAt(3);
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
