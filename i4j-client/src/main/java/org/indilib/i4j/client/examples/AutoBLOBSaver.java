
package org.indilib.i4j.client.examples;

/*
 * #%L
 * INDI for Java Client Library
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
import java.io.IOException;
import java.util.Date;

import org.indilib.i4j.Constants.BLOBEnables;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.client.INDIBLOBElement;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIElementListener;
import org.indilib.i4j.client.INDIServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A INDI Client that listens to a particular BLOB Element and saves it to a
 * file whenever it is updated.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.31, April 11, 2012
 */
public class AutoBLOBSaver implements INDIElementListener {
    private static Logger LOG = LoggerFactory.getLogger(AutoBLOBSaver.class);
  private INDIServerConnection connection;
  private String deviceName;
  private String propertyName;
  private String elementName;

  public AutoBLOBSaver(String host, int port) {
    connection = new INDIServerConnection(host, port);
  }

  public void listenAndSaveBLOB(String deviceName, String propertyName, String elementName) {
    this.deviceName = deviceName;
    this.propertyName = propertyName;
    this.elementName = elementName;

    try {
      connection.connect();
      connection.askForDevices();  // Ask for all the devices.
    } catch (IOException e) {
      LOG.error("Problem with the connection: " + connection.getHost() + ":" + connection.getPort(),e); 
    }

    INDIBLOBElement el = null;

    while (el == null) {  // Wait until the Server has a Device with a property and element with the required name
      try {
        el = (INDIBLOBElement) connection.getElement(deviceName, propertyName, elementName);
        try {
          Thread.sleep(500); // Wait 0.5 seconds.
        } catch (InterruptedException e) {
        }
      } catch (ClassCastException e) {
        LOG.info("The Element is not a BLOB one.");
        System.exit(-1);
      }
    }

    try {
      el.getProperty().getDevice().BLOBsEnable(BLOBEnables.ALSO); // Enable receiving BLOBs from this Device
    } catch (IOException e) {
    }

    el.addINDIElementListener(this);  // We add ourselves as the listener for the Element.
  }

  @Override
  public void elementChanged(INDIElement element) {
    INDIBLOBElement be = (INDIBLOBElement) element;

    INDIBLOBValue v = be.getValue();

    if (v != null) {
      String fileName = deviceName + "_" + propertyName + "_" + elementName + "_" + new Date() + v.getFormat();
      
      File f = new File(fileName);
      
      try {
          LOG.info("Saving " + f);
        be.getValue().saveBLOBData(f);  // Saves the data to file
      } catch(IOException e) {
          LOG.error("could not save "+f,e); 
      }
    }
  }

  /**
   * Parses the arguments and creates the Client if they are correct.
   *
   * @param args
   */
  public static void main(String[] args) {
    if ((args.length < 4) || (args.length > 5)) {
      printErrorMessageAndExit();
    }

    String deviceName = args[0];
    String propertyName = args[1];
    String elementName = args[2];
    String host = args[3];
    int port = 7624;

    if (args.length > 4) {
      try {
        port = Integer.parseInt(args[4]);
      } catch (NumberFormatException e) {
        printErrorMessageAndExit();
      }
    }

    AutoBLOBSaver abs = new AutoBLOBSaver(host, port);

    abs.listenAndSaveBLOB(deviceName, propertyName, elementName);
  }

  private static void printErrorMessageAndExit() {
    System.out.println("The program must be called in the following way:");

    System.out.println("> java AutoBLOBSaver device property element host [port]\n  where");
    System.out.println("    device - is the INDI Device name");
    System.out.println("    property - is the BLOB Property name");
    System.out.println("    element - is the BLOB Element name");
    System.out.println("    host - is the INDI Server to connect to");
    System.out.println("    port - is the INDI Server port. If not present the default port (7624) will be used.\n");

    System.exit(-1);
  }
}
