
package org.indilib.i4j.driver;

/*
 * #%L
 * INDI for Java Driver Library
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

import org.indilib.i4j.Constants;
import org.indilib.i4j.INDIException;

/**
 * A class representing a the standard INDI PORT Property.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 7, 2013
 */
public class INDIPortProperty extends INDITextProperty {

  /**
   * The PORT element.
   */
  private INDITextElement portE;

  /**
   * Constructs an instance of a PORTS property, with its PORT element. If the
   * default value is null, "/dev/ttyUSB0" is assumed.
   *
   * @param driver The Driver to which this property is associated
   * @param defaultValue The default value for the port
   */
  public INDIPortProperty(INDIDriver driver, String defaultValue) {
    super(driver, "DEVICE_PORT", "Ports", "Main Control", Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW);

    if (defaultValue == null) {
      defaultValue = "/dev/ttyUSB0";
    }

    portE = new INDITextElement(this, "PORT", "Port", defaultValue);
  }

  /**
   * Loads an instance of
   * <code>INDIPortProperty</code> from a file or, if it cannot be loaded,
   * constructs it with a particular
   * <code>driver</code> and
   * <code>default value</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param defaultValue The default value for the port
   * @return The loaded port property or a new constructed one if cannot be
   * loaded.
   */
  public static INDIPortProperty createSaveablePortProperty(INDIDriver driver, String defaultValue) {
    INDIPortProperty pp = loadPortProperty(driver, "DEVICE_PORT");

    if (pp == null) {
      pp = new INDIPortProperty(driver, defaultValue);
      pp.setSaveable(true);
    }

    return pp;
  }

  /**
   * Loads a Port Property from a file.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the property
   * @return The loaded port property or <code>null</code> if it could not be
   * loaded.
   */
  private static INDIPortProperty loadPortProperty(INDIDriver driver, String name) {
    INDIProperty prop;

    try {
      prop = INDIProperty.loadFromFile(driver, name);
    } catch (INDIException e) {  // Was not correctly loaded
      return null;
    }

    if (!(prop instanceof INDIPortProperty)) {
      return null;
    }

    INDIPortProperty sp = (INDIPortProperty)prop;
    sp.setSaveable(true);
    return sp;
  }

  /**
   * Gets the PORT element value.
   *
   * @return The PORT element value
   */
  public String getPort() {
    return portE.getValue();
  }

  /**
   * Sets the PORT element value.
   *
   * @param port The new value for the PORT element
   */
  public void setPort(String port) {
    portE.setValue(port);
    this.setState(Constants.PropertyStates.OK);

    getDriver().updateProperty(this);
  }

  /**
   * Sets the PORT element value if the
   * <code>property</code> corresponds to this object. This method is a
   * convenience one that can be placed in
   * <code>INDIDriver.processNewTextValue</code> safely.
   *
   * @param property If this property corresponds to this PORTS property, the
   * property will be updated
   * @param elementsAndValues An array of pairs of Text Elements and its
   * requested values to be parsed and updated if <code>property</code>
   * corresponds to this PORTS property
   *
   * @see INDIDriver#processNewTextValue
   */
  public void processTextValue(INDITextProperty property, INDITextElementAndValue[] elementsAndValues) {
    if (property == this) {
      String port = elementsAndValues[0].getValue();

      setPort(port);
    }
  }
}
