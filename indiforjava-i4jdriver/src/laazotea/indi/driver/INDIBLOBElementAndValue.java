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

import laazotea.indi.INDIBLOBValue;

/**
 * A class representing a pair of a <code>INDIBLOBElement</code> and a <code>INDIBLOBValue</code>.
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.10, March 19, 2012
 */
public class INDIBLOBElementAndValue implements INDIElementAndValue {
  private final INDIBLOBElement element;
  private final INDIBLOBValue value;

  /**
   * Constructs an instance of a <code>INDIBLOBElementAndValue</code>. This class should not usually be instantiated by specific Drivers.
   * @param element The BLOB Element
   * @param value The BLOB Value
   */
  protected INDIBLOBElementAndValue(INDIBLOBElement element, INDIBLOBValue value) {
    this.element = element;
    this.value = value;
  }

  @Override
  public INDIBLOBElement getElement() {
    return element;
  }

  @Override
  public INDIBLOBValue getValue() {
    return value;
  }
}
