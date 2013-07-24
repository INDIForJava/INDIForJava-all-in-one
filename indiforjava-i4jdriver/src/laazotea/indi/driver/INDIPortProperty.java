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
import laazotea.indi.INDIException;

/**
 * A class representing a the standard INDI PORT Property.
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.32, July 23, 2013
 */
public class INDIPortProperty extends INDITextProperty {

  /**
   * The PORT element.
   */
  private INDITextElement portE;

  /**
   * Constructs an instance of a PORTS property, with its PORT element.
   *
   * @param driver The Driver to which this property is associated
   * @param defaultValue The default value for the port
   */
  public INDIPortProperty(INDIDriver driver, String defaultValue) {
    super(driver, "DEVICE_PORT", "Ports", "Main Control", Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW);
    portE = new INDITextElement(this, "PORT", "Port", "/dev/ttyUSB0");
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

    try {
      getDriver().updateProperty(this);
    } catch (INDIException e) {
      e.printStackTrace();
    }
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
      String port = elementsAndValues[0].getElement().getValue();

      setPort(port);
    }
  }
}
