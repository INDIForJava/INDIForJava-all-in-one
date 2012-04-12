/*
 *  This file is part of INDI for Java Server.
 * 
 *  INDI for Java Server is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Server is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Server.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package laazotea.indi.server;

import com.Ostermiller.util.CircularByteBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import laazotea.indi.INDIException;
import laazotea.indi.driver.INDIDriver;

/**
 * A class that represent a Java Device (created with the INDI Driver library).
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.21, April 4, 2012
 *
 * @see laazotea.indi.driver
 */
public class INDIJavaDevice extends INDIDevice {

  /**
   * The Driver
   */
  private INDIDriver driver;
  /**
   * An identifier of the Java Device. Can be the name of a JAR file or any other String, but it must be UNIQUE.
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
  private String name;

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
    
    name = null;
    this.identifier = identifier;
    this.driverClass = driverClass;
    
    toDriver = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
    fromDriver = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
    
    try {
      Constructor c = driverClass.getConstructor(InputStream.class, OutputStream.class);
      this.driver = (INDIDriver) c.newInstance(toDriver.getInputStream(), fromDriver.getOutputStream());
    } catch (InstantiationException ex) {
      throw new INDIException("Problem instantiating driver (not an INDIfor Java Driver?)");
    } catch (IllegalAccessException ex) {
      throw new INDIException("Problem instantiating driver (not an INDIfor Java Driver?)");
    } catch (NoSuchMethodException ex) {
      throw new INDIException("Problem instantiating driver (not an INDIfor Java Driver?)");
    } catch (InvocationTargetException ex) {
      throw new INDIException("Problem instantiating driver (not an INDIfor Java Driver?)");
    } catch (ClassCastException ex) {
      throw new INDIException("Problem instantiating driver (not an INDIfor Java Driver?)"); 
    }
    
    driver.startListening();
  }

  /**
   * Gets the identifier of the Device (probablythe name of the JAR file that includes it).
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
  @Override
  protected void dealWithPossibleNewDeviceName(String possibleNewName) {
    if (name == null) {
      name = possibleNewName;
    }
  }

  /**
   * Checks if the Device has a particular name.
   *
   * @param name The name to check.
   * @return
   * <code>true</code> if the Device respond to
   * <code>name</code>.
   * <code>false</code> otherwise.
   */
  @Override
  protected boolean hasName(String name) {
    if (this.name == null) {
      return false;
    }
    
    if (this.name.equals(name)) {
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
   * @return
   * <code>true</code> if the Device corresponds to the Device Identifier (that
   * is, is in the jar file).
   */
  @Override
  public boolean isDevice(String deviceIdentifier) {
    return getDeviceIdentifier().startsWith(deviceIdentifier);
  }
  
  @Override
  protected String[] getNames() {
    return new String[]{name};
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
}
