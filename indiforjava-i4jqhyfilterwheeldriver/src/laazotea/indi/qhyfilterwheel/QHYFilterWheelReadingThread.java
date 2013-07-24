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

import java.io.FileInputStream;
import java.io.IOException;

/**
 * A Thread that reads the input stream connecting to the Filter Wheel and
 * notifies the driver about its changes.
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.32, July 25, 2013
 */
public class QHYFilterWheelReadingThread extends Thread {

  /**
   * The input stream from the filer wheel.
   */
  FileInputStream fwInput;
  /**
   * The driver to which communicate messages from the filter wheel.
   */
  I4JQHYFilterWheelDriver driver;
  /**
   * Used to signal when the thread must end.
   */
  private boolean end;
  /**
   * Used to know when the thread has ended.
   */
  private boolean ended;

  /**
   * Constructs an instance of a
   * <code>QHYFilterWheelReadingThread</code> that reads messages from the
   * filter wheel and notifies the driver about it.
   *
   * @param driver The driver to notify about messages of the wheel.
   * @param fwInput The input stream from which to read.
   */
  public QHYFilterWheelReadingThread(I4JQHYFilterWheelDriver driver, FileInputStream fwInput) {
    this.fwInput = fwInput;
    this.driver = driver;
  }

  /**
   * Used to cleanly stop the reading thread.
   */
  protected synchronized void end() {
    end = true;
  }

  /**
   * To know if the thread has ended.
   *
   * @return <code>true</code> if the thread has ended. <code>false</code>
   * otherwise.
   */
  protected boolean hasEnded() {
    return ended;
  }

  @Override
  public void run() {
    ended = false;
    String buffer = "";

    while (!end) {
//      System.out.print("+");
      try {
        if (fwInput.available() > 0) {
          byte[] readed = new byte[512];

          int br = fwInput.read(readed);
//System.out.println("MEC " + new String(readed, 0, br));
          if (br > 0) {
            buffer += new String(readed, 0, br);
            if (buffer.startsWith("-")) {
              driver.filterHasBeenChanged();
              buffer = "";
            }
          }
        }
      } catch (IOException e) {
        end = true;
      }

      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
      }
    }

    System.out.println("QHY Filter Wheel Reader Thread Ending");

    ended = true;
  }
}
