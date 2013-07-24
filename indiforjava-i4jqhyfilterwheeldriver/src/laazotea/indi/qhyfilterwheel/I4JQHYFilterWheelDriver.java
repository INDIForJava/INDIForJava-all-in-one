/*
 *  This file is part of INDI for Java Driver.
 * 
 *  INDI for Java Driver is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Driver is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Driver.  If not, see 
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
import laazotea.indi.INDIException;
import laazotea.indi.driver.INDIBLOBElementAndValue;
import laazotea.indi.driver.INDIBLOBProperty;
import laazotea.indi.driver.INDIConnectionHandler;
import laazotea.indi.driver.INDIFilterWheelDriver;
import laazotea.indi.driver.INDINumberElementAndValue;
import laazotea.indi.driver.INDINumberProperty;
import laazotea.indi.driver.INDIPortProperty;
import laazotea.indi.driver.INDISwitchElementAndValue;
import laazotea.indi.driver.INDISwitchProperty;
import laazotea.indi.driver.INDITextElementAndValue;
import laazotea.indi.driver.INDITextProperty;

/**
 * A class that acts as a INDI for Java Driver for the QHY Filter Wheel.
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.32, July 25, 2013
 */
public class I4JQHYFilterWheelDriver extends INDIFilterWheelDriver implements INDIConnectionHandler {
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
  private QHYFilterWheelReadingThread readingThread;

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

    portP = new INDIPortProperty(this, "/dev/ttyUSB0");

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

    this.addProperty(portP);
  }

  @Override
  public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
    super.processNewSwitchValue(property, timestamp, elementsAndValues);
  }

  @Override
  public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
    //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

      readingThread = new QHYFilterWheelReadingThread(this, fwInput);
      readingThread.start();

    } catch (IOException e) {
      throw new INDIException("Connection to the QHY Filter Wheel failed. Check port permissions");
    }

    showCurrentFilterProperty();

    changeFilter(1);
  }

  @Override
  public void driverDisconnect(Date timestamp) throws INDIException {
    System.out.println("Disconnecting from QHY Filter Wheel");
    System.out.flush();

    try {
      if (readingThread != null) {
        readingThread.end();
      }

      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
      }

      if (fwInput != null) {
        fwInput.close();
        fwOutput.close();
      }

      fwInput = null;
      fwOutput = null;
    } catch (IOException e) {
    }

    hideCurrentFilterProperty();
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
      try {
        fwOutput.write(("" + (filterNumber - 1)).getBytes());
        fwOutput.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
