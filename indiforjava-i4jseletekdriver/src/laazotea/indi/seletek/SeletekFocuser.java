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
package laazotea.indi.seletek;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIException;
import laazotea.indi.driver.*;

/**
 * A class that acts as a INDI for Java Focuser Driver for a focuser connected
 * to a Seletek.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 8, 2013
 */
public class SeletekFocuser extends INDIFocuserDriver implements INDINotLoadableDriver, Runnable {

  /**
   * A name suffix for the driver (Main or Exp).
   */
  private String nameSuffix;
  /**
   * The Seletek port number to which the focuser is attached.
   */
  private int seletekPort;
  /**
   * The Seletek Driver.
   */
  private I4JSeletekDriver driver;
  /**
   * To finish the thread that reads the positions of the focuser.
   */
  private boolean stopPositionReaderThread;
  /**
   * Marks if the reader thread should ask for the position of the focser (only
   * when it is moving).
   */
  private boolean updatePosition;
  /**
   * The Wire Mode Property
   */
  private INDISwitchOneOfManyProperty wireModeP;
  /**
   * The Model Property
   */
  private INDISwitchOneOfManyProperty modelP;
  /**
   * The Half Step Property
   */
  private INDISwitchOneOrNoneProperty halfStepP;

  /**
   * Constructs an instance of a
   * <code>SeletekFocuser</code> with a particular
   * <code>inputStream<code> from which to read the incoming messages (from clients) and a
   * <code>outputStream</code> to write the messages to the clients.
   *
   * @param inputStream The stream from which to read messages
   * @param outputStream The stream to which to write the messages
   * @param seletekPort 0 - Main port, 1 - Exp port
   * @param driver The Seletek Driver
   */
  public SeletekFocuser(InputStream inputStream, OutputStream outputStream, int seletekPort, I4JSeletekDriver driver) {
    super(inputStream, outputStream);

    this.seletekPort = seletekPort;
    this.driver = driver;

    if (seletekPort == 1) {
      nameSuffix = "Exp";
    } else {
      nameSuffix = "Main";
    }

    initializeStandardProperties();
    showSpeedProperty();
    speedHasBeenChanged(); // To set the possibly saved speed

    showStopFocusingProperty();

    driver.stopStepper(seletekPort);

    stopPositionReaderThread = false;
    updatePosition = false;

    wireModeP = INDISwitchOneOfManyProperty.createSaveableSwitchOneOfManyProperty(this, "wireMode", "Wire Mode", "Configuration", PropertyStates.IDLE, PropertyPermissions.RW, new String[]{"Lunático", "Lunático Inverted", "RF/Moonlite", "RF/Moonlite Inverted"});
    addProperty(wireModeP);
    int mode = wireModeP.getSelectedIndex();
    driver.setStepperWireMode(seletekPort, mode);

    modelP = INDISwitchOneOfManyProperty.createSaveableSwitchOneOfManyProperty(this, "model", "Model", "Configuration", PropertyStates.IDLE, PropertyPermissions.RW, new String[]{"Unipolar", "Bipolar", "Step and Dir"});
    addProperty(modelP);
    int model = modelP.getSelectedIndex();
    driver.setStepperModel(seletekPort, model);

    halfStepP = INDISwitchOneOrNoneProperty.createSaveableSwitchOneOrNoneProperty(this, "halfStep", "Half Step", "Configuration", PropertyStates.IDLE, PropertyPermissions.RW, "Half Step", SwitchStatus.OFF);
    addProperty(halfStepP);
    boolean half = (halfStepP.getStatus() == SwitchStatus.ON);
    driver.setStepperHalfStep(seletekPort, half);

    Thread readerThread = new Thread(this);
    readerThread.start();
  }

  @Override
  public String getName() {
    return "Seletek Focuser (" + nameSuffix + ")";
  }

  @Override
  public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
//    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
    super.processNewSwitchValue(property, timestamp, elementsAndValues);

    if (property == wireModeP) {
      int mode = wireModeP.getSelectedIndex(elementsAndValues);
      driver.setStepperWireMode(seletekPort, mode);
      wireModeP.setSelectedIndex(elementsAndValues);
      wireModeP.setState(PropertyStates.OK);

      try {
        updateProperty(wireModeP);
      } catch (INDIException e) {
      }
    }

    if (property == modelP) {
      int model = modelP.getSelectedIndex(elementsAndValues);
      driver.setStepperModel(seletekPort, model);
      modelP.setSelectedIndex(elementsAndValues);
      modelP.setState(PropertyStates.OK);

      try {
        updateProperty(modelP);
      } catch (INDIException e) {
      }
    }

    if (property == halfStepP) {
      boolean half = (halfStepP.getStatus(elementsAndValues) == SwitchStatus.ON);
      driver.setStepperHalfStep(seletekPort, half);
      halfStepP.setStatus(elementsAndValues);
      halfStepP.setState(PropertyStates.OK);

      try {
        updateProperty(halfStepP);
      } catch (INDIException e) {
      }
    }
  }

  @Override
  public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
    super.processNewNumberValue(property, timestamp, elementsAndValues);
  }

  @Override
  public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
//    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getMaximumSpeed() {
    return 10;
  }

  @Override
  public final void speedHasBeenChanged() {
    driver.setStepperSpeed(seletekPort, getCurrentSpeed());
  }

  /**
   * The Seletek signals that the speed has been set.
   */
  void setDesiredSpeed() {
    super.desiredSpeedSet();
  }

  @Override
  public final int getMaximumAbsPos() {
    return 100000;
  }

  @Override
  public final int getMinimumAbsPos() {
    return 0;
  }

  @Override
  public final int getInitialAbsPos() {
    return 50000;
  }

  @Override
  public void absolutePositionHasBeenChanged() {
    driver.stepperGotoAbs(seletekPort, getDesiredAbsPosition());
    updatePosition = true;
  }

  @Override
  public void stopHasBeenRequested() {
    driver.stopStepper(seletekPort);
  }

  /**
   * The Seletek informs that the focuser has stopped.
   */
  void stopFocuser() {
    stopped();

    finalPositionReached();
    updatePosition = false;
    driver.getStepperPos(seletekPort);
  }

  /**
   * The Seletek informs about the focuser position
   *
   * @param position The position of the focuser
   */
  void showFocusPosition(int position) {
    positionChanged(position);

    if (position == getDesiredAbsPosition()) {
      finalPositionReached();
      updatePosition = false;
    }
  }

  @Override
  public void isBeingDestroyed() {
    stopPositionReaderThread = true;

    sleep(200);

    super.isBeingDestroyed();
  }

  /**
   * The thread will ask for the position of the focuser every 200 miliseconds
   * when it is moving.
   */
  @Override
  public void run() {
    while (!stopPositionReaderThread) {
      if (updatePosition) {
        driver.getStepperPos(seletekPort);
      }

      sleep(200);
    }
  }

  /**
   * Sleep for some time.
   *
   * @param milis The number of miliseconds to sleep
   */
  private void sleep(int milis) {
    try {
      Thread.sleep(milis);
    } catch (InterruptedException e) {
    }
  }
}
