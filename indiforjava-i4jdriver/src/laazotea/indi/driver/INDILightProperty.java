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
package laazotea.indi.driver;

import laazotea.indi.Constants;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIDateFormat;

/**
 * A class representing a INDI Light Property.
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.11, March 19, 2012
 */
public class INDILightProperty extends INDIProperty {
  /**
   * Constructs an instance of <code>INDILightProperty</code> with a particular <code>Driver</code>, <code>name</code>, <code>label</code>, <code>group</code> and <code>state</code>.
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @throws IllegalArgumentException 
   * @see INDIProperty
   */
  public INDILightProperty(INDIDriver driver, String name, String label, String group, PropertyStates state) throws IllegalArgumentException {
    super(driver, name, label, group, state, PropertyPermissions.RO, 0);
  }

  /**
   * Constructs an instance of <code>INDILightProperty</code> with a particular <code>Driver</code>, <code>name</code>, <code>label</code> and <code>state</code>. The group will be the default one.
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @throws IllegalArgumentException 
   * @see INDIProperty
   */
  public INDILightProperty(INDIDriver driver, String name, String label, PropertyStates state) throws IllegalArgumentException {
    super(driver, name, label, null, state, PropertyPermissions.RO, 0);
  }

  /**
   * Constructs an instance of <code>INDILightProperty</code> with a particular <code>Driver</code>, <code>name</code>, <code>label</code> and <code>state</code>. The group will be the default one and the label equal to its <code>name</code>.
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @throws IllegalArgumentException 
   * @see INDIProperty
   */
  public INDILightProperty(INDIDriver driver, String name, PropertyStates state) throws IllegalArgumentException {
    super(driver, name, null, null, state, PropertyPermissions.RO, 0);
  }

    @Override
  public INDILightElement getElement(String name) {
    return (INDILightElement)super.getElement(name); 
  }
    
  @Override
  protected String getXMLPropertyDefinitionInit() {
    String xml = "<defLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertyDefinitionInit(String message) {
    String xml = "<defLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertyDefinitionEnd() {
    String xml = "</defLightVector>";

    return xml;
  }

  @Override
  protected String getXMLPropertySetInit() {
    String xml = "<setLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertySetInit(String message) {
    String xml = "<setLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertySetEnd() {
    String xml = "</setLightVector>";

    return xml;
  }
}
