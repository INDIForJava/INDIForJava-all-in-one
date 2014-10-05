/*
 *  This file is part of INDI for Java Raspberry PI GPIO Driver.
 * 
 *  INDI for Java Raspberry PI GPIO Driver is free software: you can 
 *  redistribute it and/or modify it under the terms of the GNU General Public 
 *  License as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Raspberry PI GPIO Driver is distributed in the hope that it
 *  will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Raspberry PI GPIO Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package org.indilib.i4j.driver.raspberrypi;

/**
 * A Thread that asks the Raspberry Pi GPIO Driver to update the sensor
 * Properties periodically.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.36, November 23, 2013
 */
public class RaspberryPiSensorReaderThread extends Thread {

  /**
   * To stop the Thread.
   */
  private boolean stopReading;
  /**
   * The Raspberry Pi GPIO Driver
   */
  private I4JRaspberryPiGPIODriver driver;

  /**
   * Constructs an instance of the Thread.
   *
   * @param driver The Raspberry Pi GPIO Driver
   */
  protected RaspberryPiSensorReaderThread(I4JRaspberryPiGPIODriver driver) {
    stopReading = false;
    this.driver = driver;
  }

  /**
   * Asks the thread to stop.
   */
  protected void stopReading() {
    stopReading = true;
  }

  @Override
  public void run() {
    while (!stopReading) {
      driver.setSensors();
      sleep(10000);
    }
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
