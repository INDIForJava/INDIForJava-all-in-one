
package org.indilib.i4j.server;

/*
 * #%L
 * INDI for Java Server Library
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Ostermiller.util.CircularByteBuffer;

/**
 * A class that represent a Java Device (created with the INDI Driver library).
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.32, July 23, 2013
 *
 * @see org.indilib.i4j.driver
 */
public class INDIJavaDevice extends INDIDevice {

    private static final Logger LOG = LoggerFactory.getLogger(INDIJavaDevice.class);
  /**
   * The Driver
   */
  private INDIDriver driver;
  /**
   * An identifier of the Java Device. Can be the name of a JAR file or any
   * other String, but it must be UNIQUE.
   */
  private String identifier;
  /**
   * A buffer to send information to the Driver.
   */
  private CircularByteBuffer toDriver;
  /**
   * A buffer to get information from the Driver.
   */
  private CircularByteBuffer fromDriver;
  /**
   * The class of the Driver.
   */
  private Class driverClass;
  /**
   * The name of the device. May be null if it has not been discovered through a
   * <code>defXXXVector</code> message.
   */
//  private String name;
  /**
   * A list of names of the Device (it may be more than one)
   */
  private ArrayList<String> names;

  /**
   * Constructs a new Java Device and starts listening to its messages.
   *
   * @param server The server which listens to this Device.
   * @param driverClass The class of the Driver.
   * @param identifier The JAR file from where to load the Driver.
   * @throws INDIException if there is any problem instantiating the Driver.
   */
  protected INDIJavaDevice(AbstractINDIServer server, Class driverClass, String identifier) throws INDIException {
    super(server);

    //name = null;
    names = new ArrayList<String>();
    this.identifier = identifier;
    this.driverClass = driverClass;

    toDriver = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
    fromDriver = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);

    try {
      Constructor c = driverClass.getConstructor(InputStream.class, OutputStream.class);
      this.driver = (INDIDriver) c.newInstance(toDriver.getInputStream(), fromDriver.getOutputStream());
    } catch (InstantiationException ex) {
      LOG.error("Problem instantiating driver (not an INDI for Java Driver?)",ex);
      throw new INDIException("Problem instantiating driver (not an INDI for Java Driver?) - InstantiationException");
    } catch (IllegalAccessException ex) {
      LOG.error("Problem instantiating driver (not an INDI for Java Driver?)",ex);
      throw new INDIException("Problem instantiating driver (not an INDI for Java Driver?) - IllegalAccessException");
    } catch (NoSuchMethodException ex) {
      LOG.error("Problem instantiating driver (not an INDI for Java Driver?)",ex);
      throw new INDIException("Problem instantiating driver (not an INDI for Java Driver?) - NoSuchMethodException");
    } catch (InvocationTargetException ex) {
      LOG.error("Problem instantiating driver (not an INDI for Java Driver?)",ex);
      throw new INDIException("Problem instantiating driver (not an INDI for Java Driver?) - InvocationTargetException");
    } catch (ClassCastException ex) {
      LOG.error("Problem instantiating driver (not an INDI for Java Driver?)",ex);
      throw new INDIException("Problem instantiating driver (not an INDI for Java Driver?) - ClassCastException");
    }

    driver.startListening();
  }

  /**
   * Gets the identifier of the Device (probablythe name of the JAR file that
   * includes it).
   *
   * @return The identifier of the Device
   */
  protected String getIdentifier() {
    return identifier;
  }

  /**
   * Deals with a possible new Device name. If the Device already has a name,
   * the new name is discarded.
   *
   * @param possibleNewName The new possible new name.
   */
  /*  @Override
   protected void dealWithPossibleNewDeviceName(String possibleNewName) {
   if (name == null) {
   name = possibleNewName;
   }
   }*/
  /**
   * Deals with a possible new Device name, adding it if it is new.
   *
   * @param possibleNewName The new possible new name.
   */
  @Override
  protected void dealWithPossibleNewDeviceName(String possibleNewName) {
    if (!names.contains(possibleNewName)) {
      names.add(possibleNewName);
    }
  }

  /**
   * Checks if the Device has a particular name.
   *
   * @param name The name to check.
   * @return <code>true</code> if the Device respond to <code>name</code>.
   * <code>false</code> otherwise.
   */
  /*  @Override
   protected boolean hasName(String name) {
   if (this.name == null) {
   return false;
   }
    
   if (this.name.equals(name)) {
   return true;
   }
    
   if (driver.hasSubDriver(name)) {
   return true; 
   }
    
   return false;
   }*/
  /**
   * Checks if the Device has a particular name.
   *
   * @param name The name to check.
   * @return <code>true</code> if the Device respond to <code>name</code>.
   * <code>false</code> otherwise.
   */
  @Override
  protected boolean hasName(String name) {
    if (names.contains(name)) {
      return true;
    }

    return false;
  }

  @Override
  public void closeConnections() {
    try {
      toDriver.getInputStream().close();
    } catch (IOException e) {
    }
    try {
      toDriver.getOutputStream().close();
    } catch (IOException e) {
    }
    try {
      fromDriver.getInputStream().close();
    } catch (IOException e) {
    }
    try {
      fromDriver.getOutputStream().close();
    } catch (IOException e) {
    }
  }

  @Override
  public InputStream getInputStream() {
    return fromDriver.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() {
    return toDriver.getOutputStream();
  }

  @Override
  public String getDeviceIdentifier() {
    return identifier + "-+-" + driverClass.getName();
  }

  /**
   * Checks if the Device corresponds to a particular Device Identifier.
   *
   * @param deviceIdentifier The Device Identifier to check.
   * @return <code>true</code> if the Device corresponds to the Device
   * Identifier (that is, is in the jar file).
   */
  @Override
  public boolean isDevice(String deviceIdentifier) {
    return getDeviceIdentifier().startsWith(deviceIdentifier);
  }

  /*  @Override
   protected String[] getNames() {
   return new String[]{name};
   }*/
  @Override
  protected String[] getNames() {
    String[] ns = new String[names.size()];

    for (int i = 0 ; i < ns.length ; i++) {
      ns[i] = names.get(i);
    }

    return ns;
  }

  /**
   * Gets a String representation of the Device.
   *
   * @return A String representation of the Device.
   */
  @Override
  public String toString() {
    return "Java Device: " + identifier + " - " + driverClass.getName();
  }

  @Override
  public void isBeingDestroyed() {
    driver.isBeingDestroyed();
  }
}
