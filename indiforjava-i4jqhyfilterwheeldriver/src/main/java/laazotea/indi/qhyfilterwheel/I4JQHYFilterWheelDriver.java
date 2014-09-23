/*
 *  This file is part of INDI for Java QHY Filter Wheel Driver.
 * 
 *  INDI for Java QHY Filter Wheel Driver is free software: you can 
 *  redistribute it and/or modify it under the terms of the GNU General Public 
 *  License as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java QHY Filter Wheel Driver is distributed in the hope that it
 *  will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java QHY Filter Wheel Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package laazotea.indi.qhyfilterwheel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIException;
import laazotea.indi.driver.INDIBLOBElementAndValue;
import laazotea.indi.driver.INDIBLOBProperty;
import laazotea.indi.driver.INDIConnectionHandler;
import laazotea.indi.driver.INDIFilterWheelDriver;
import laazotea.indi.driver.INDINumberElement;
import laazotea.indi.driver.INDINumberElementAndValue;
import laazotea.indi.driver.INDINumberProperty;
import laazotea.indi.driver.INDIPortProperty;
import laazotea.indi.driver.INDISwitchElement;
import laazotea.indi.driver.INDISwitchElementAndValue;
import laazotea.indi.driver.INDISwitchProperty;
import laazotea.indi.driver.INDITextElementAndValue;
import laazotea.indi.driver.INDITextProperty;

/**
 * A class that acts as a INDI for Java Driver for the QHY Filter Wheel.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.35, November 11, 2013
 */
public class I4JQHYFilterWheelDriver extends INDIFilterWheelDriver implements INDIConnectionHandler, Runnable {

  /**
   * The PORTS property.
   */
  INDIPortProperty portP;
  /**
   * The serial input stream to communicate with the filter wheel.
   */
  private FileInputStream fwInput;
  /**
   * The serial output stream to communicate with the filter wheel.
   */
  private FileOutputStream fwOutput;
  /**
   * A thread that listens to the answers of the filter wheel.
   */
  private Thread readingThread;
  /**
   * The filter positions (to configure the wheel)
   */
  private INDINumberProperty filterPositionsP;
  /**
   * The filter positions (to configure the wheel)
   */
  private INDINumberElement[] filterPositionsE;
  /**
   * To return to factory settings
   */
  private INDISwitchProperty factorySettingsP;
  /**
   * To return to factory settings
   */
  private INDISwitchElement factorySettingsE;

  /**
   * Constructs an instance of a
   * <code>I4JQHYFilterWheelDriver</code> with a particular
   * <code>inputStream<code> from which to read the incoming messages (from clients) and a
   * <code>outputStream</code> to write the messages to the clients.
   *
   * @param inputStream The stream from which to read messages.
   * @param outputStream The stream to which to write the messages.
   */
  public I4JQHYFilterWheelDriver(InputStream inputStream, OutputStream outputStream) {
    super(inputStream, outputStream);

    initializeStandardProperties();

    portP = INDIPortProperty.createSaveablePortProperty(this, "/dev/ttyUSB0");

    filterPositionsP = new INDINumberProperty(this, "filter_positions", "Filter Positions", "Expert Configuration", PropertyStates.IDLE, PropertyPermissions.RW, 0);

    filterPositionsE = new INDINumberElement[5];

    filterPositionsE[0] = new INDINumberElement(filterPositionsP, "filter_1_position", "Filter 1 Position", 0, 0, 1000, 1, "%1.0f");
    filterPositionsE[1] = new INDINumberElement(filterPositionsP, "filter_2_position", "Filter 2 Position", 0, 0, 1000, 1, "%1.0f");
    filterPositionsE[2] = new INDINumberElement(filterPositionsP, "filter_3_position", "Filter 3 Position", 0, 0, 1000, 1, "%1.0f");
    filterPositionsE[3] = new INDINumberElement(filterPositionsP, "filter_4_position", "Filter 4 Position", 0, 0, 1000, 1, "%1.0f");
    filterPositionsE[4] = new INDINumberElement(filterPositionsP, "filter_5_position", "Filter 5 Position", 0, 0, 1000, 1, "%1.0f");

    factorySettingsP = new INDISwitchProperty(this, "factory_settings", "Factory Settings", "Expert Configuration", PropertyStates.IDLE, PropertyPermissions.RW, 0, SwitchRules.AT_MOST_ONE);
    factorySettingsE = new INDISwitchElement(factorySettingsP, "factory_setting", "Factory Settings", SwitchStatus.OFF);

    addProperty(portP);
  }

  @Override
  public String getName() {
    return "QHY Filter Wheel (RS232)";
  }

  @Override
  public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
    super.processNewTextValue(property, timestamp, elementsAndValues);

    portP.processTextValue(property, elementsAndValues);
  }

  @Override
  public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
    if (property == factorySettingsP) {
      sendMessageToFilterWheel("SEF");

      sleep(10);

      getSetupPositions();

      factorySettingsP.setState(PropertyStates.OK);
      try {
        updateProperty(factorySettingsP);
      } catch (INDIException e) {
      }
    }
  }

  @Override
  public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
    super.processNewNumberValue(property, timestamp, elementsAndValues);

    if (property == filterPositionsP) {
      int[] positions = new int[5];
      for (int i = 0 ; i < filterPositionsE.length ; i++) {
        positions[i] = filterPositionsE[i].getValue().intValue();
      }

      for (int i = 0 ; i < elementsAndValues.length ; i++) {
        for (int h = 0 ; h < filterPositionsE.length ; h++) {
          if (filterPositionsE[h] == elementsAndValues[i].getElement()) {
            positions[h] = elementsAndValues[i].getValue().intValue();
          }
        }
      }

      sendSetupPositions(positions);

      sleep(10);

      getSetupPositions();
    }
  }

  @Override
  public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
    //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void driverConnect(Date timestamp) throws INDIException {
    System.out.println("Connecting to QHY Filter Wheel");

    File port = new File(portP.getPort());
    if (!port.exists()) {
      throw new INDIException("Connection to the QHY Filter Wheel failed: port file does not exist.");
    }

    try {
      fwInput = new FileInputStream(portP.getPort());
      fwOutput = new FileOutputStream(portP.getPort());

      readingThread = new Thread(this);
      readingThread.start();
    } catch (IOException e) {
      throw new INDIException("Connection to the QHY Filter Wheel failed. Check port permissions");
    }

    showFilterSlotAndNameProperties();

    addProperty(filterPositionsP);
    addProperty(factorySettingsP);

    getSetupPositions();

    changeFilter(1);
  }

  @Override
  public void driverDisconnect(Date timestamp) throws INDIException {
    System.out.println("Disconnecting from QHY Filter Wheel");
    System.out.flush();

    try {
      if (readingThread != null) {
        readerEnd = true;
        readingThread = null;
      }

      sleep(200);

      if (fwInput != null) {
        fwInput.close();
        fwOutput.close();
      }

      fwInput = null;
      fwOutput = null;
    } catch (IOException e) {
    }

    hideFilterSlotAndNameProperties();

    removeProperty(filterPositionsP);
    removeProperty(factorySettingsP);

    System.out.println("Disconnected from QHY Filter Wheel");
    System.out.flush();
  }

  @Override
  public int getNumberOfFilters() {
    return 5;
  }

  @Override
  protected void changeFilter(int filterNumber) {
    if ((filterNumber > 0) && (filterNumber <= getNumberOfFilters())) {
      setBusy();
      if (readingThread != null) {
        lastAskedFilter = filterNumber;
      }

      sendMessageToFilterWheel("" + (filterNumber - 1));
    }
  }

  /**
   * Sets the positions for each filter slot.
   *
   * @param newPositions The new positions.
   */
  private void setSetupPositions(int[] newPositions) {
    for (int i = 0 ; i < 5 ; i++) {
      if ((newPositions[i] < 0) || (newPositions[i] > 1000)) {
        newPositions[i] = 0;
      }
      filterPositionsE[i].setValue("" + newPositions[i]);
    }

    filterPositionsP.setState(PropertyStates.OK);

    try {
      updateProperty(filterPositionsP);
    } catch (INDIException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sends the message to retrieve the positions of each slot.
   */
  private void getSetupPositions() {
    sendMessageToFilterWheel("SEG");
  }

  /**
   * Sets the position for each of the 5 filter slots.
   *
   * @param positions The positions for each slot.
   */
  private void sendSetupPositions(int[] positions) {
    byte[] message = new byte[20];
    message[0] = 'S';
    message[1] = 'E';
    message[2] = 'W';
    message[3] = 0;
    message[4] = (byte)(positions[0] >>> 8);
    message[5] = (byte)(positions[0]);
    message[6] = (byte)(positions[1] >>> 8);
    message[7] = (byte)(positions[1]);
    message[8] = (byte)(positions[2] >>> 8);
    message[9] = (byte)(positions[2]);
    message[10] = (byte)(positions[3] >>> 8);
    message[11] = (byte)(positions[3]);
    message[12] = (byte)(positions[4] >>> 8);
    message[13] = (byte)(positions[4]);
    message[14] = (byte)(600 >>> 8);
    message[15] = (byte)(600);
    message[16] = (byte)(700 >>> 8);
    message[17] = (byte)(700);
    message[18] = (byte)(800 >>> 8);
    message[19] = (byte)(800);

    /* for (int i = 0 ; i < message.length ; i++) {
     System.out.println(String.format("0x%02X", message[i]));
     }*/

    sendMessageToFilterWheel(message);
  }

  /**
   * Sends a message to the filter wheel.
   *
   * @param message The message to be sent
   */
  private void sendMessageToFilterWheel(String message) {
    sendMessageToFilterWheel(message.getBytes());
  }

  /**
   * Sends a message to the filter wheel.
   *
   * @param message The message to be sent
   */
  private void sendMessageToFilterWheel(byte[] message) {
    try {
      for (int i = 0 ; i < message.length ; i++) {
        fwOutput.write(message[i]);
        sleep(10);
      }
      fwOutput.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  /**
   * Used to signal when the thread must end.
   */
  private boolean readerEnd;
  /**
   * Used to know when the thread has ended.
   */
  private boolean readerEnded;
  /**
   * Used to know which was the last asked filter
   */
  private int lastAskedFilter;

  /**
   * The reader thread.
   */
  @Override
  public void run() {
    readerEnded = false;

    while (!readerEnd) {
      try {
        if (fwInput.available() > 0) {
          byte[] readed = new byte[512];

          int br = fwInput.read(readed);
//          System.out.println(br);
//System.out.println("MEC " + new String(readed, 0, br));
          if (br == 1) {
            if (readed[0] == '-') {
              filterHasBeenChanged(lastAskedFilter);
            }
          } else if (br == 17) {
            int[] filterPositions = new int[5];

            /* for (int i = 0 ; i < 17 ; i++) {
             System.out.println(String.format("0x%02X", readed[i]));
             }*/

            filterPositions[0] = (readed[1] << 8) & 0xff00 | (readed[2]) & 0xff;
            filterPositions[1] = (readed[3] << 8) & 0xff00 | (readed[4]) & 0xff;
            filterPositions[2] = (readed[5] << 8) & 0xff00 | (readed[6]) & 0xff;
            filterPositions[3] = (readed[7] << 8) & 0xff00 | (readed[8]) & 0xff;
            filterPositions[4] = (readed[9] << 8) & 0xff00 | (readed[10]) & 0xff;

            setSetupPositions(filterPositions);
          }
        }
      } catch (IOException e) {
        readerEnd = true;
      }

      sleep(200);
    }

    System.out.println("QHY Filter Wheel Reader Thread Ending");

    readerEnded = true;
  }

  /**
   * Sleeps for a certain amount of time.
   *
   * @param milis The number of milliseconds to sleep
   */
  private void sleep(int milis) {
    try {
      Thread.sleep(milis);
    } catch (InterruptedException e) {
    }
  }
}
