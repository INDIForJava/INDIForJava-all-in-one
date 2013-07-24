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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import laazotea.indi.Constants;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIException;

/**
 * A class representing a Filter Wheel Driver in the INDI Protocol. INDI Filter
 * Wheel Drivers should extend this class. It is in charge of handling the
 * standard properties for Filter Wheels:
 * <ul>
 * <li>N_FILTERS -> N_FILTERS (number)</li>
 * <li>FILTER_NAMES -> FILTER_NAME_1, FILTER_NAME_2, ..., FILTER_NAME_N
 * (text)</li>
 * <li>CURRENT_FILTER -> F_1, F_2, ..., F_N (Switch)</li>
 * </ul>
 *
 * It is <strong>VERY IMPORTANT</strong> that any subclasses implement a
 * <code>super.processNewTextValue(property, timestamp, elementsAndValues);</code>
 * and
 * <code>super.processNewSwitchValue(property, timestamp, elementsAndValues);</code>
 * at the beginning of
 * <code>processNewTextValue</code> and
 * <code>processNewSwitchValue</code> to handle the generic filter wheel
 * properties correctly.
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.32, July 25, 2013
 */
public abstract class INDIFilterWheelDriver extends INDIDriver {

  /**
   * The N_FILTERS property
   */
  private INDINumberProperty nFiltersP;
  /**
   * The N_FILTERS element
   */
  private INDINumberElement nFiltersE;
  /**
   * The FILTER_NAMES property
   */
  private INDITextProperty filterNamesP;
  /**
   * The CURRENT_FILTER property
   */
  private INDISwitchProperty currentFilterP;
  /**
   * The file to which the FILTER_NAMES property will be stored
   */
  private String filterNamesFileName;

  /**
   * Indicates how many filters does the filter wheel manage.
   *
   * @return The number of filters that the filter wheel manages.
   */
  public abstract int getNumberOfFilters();

  /**
   * Constructs a INDIFilterWheelDriver with a particular
   * <code>inputStream<code> from which to read the incoming messages (from clients) and a
   * <code>outputStream</code> to write the messages to the clients.
   *
   * @param inputStream The stream from which to read messages
   * @param outputStream The stream to which to write the messages
   */
  public INDIFilterWheelDriver(InputStream inputStream, OutputStream outputStream) {
    super(inputStream, outputStream);

    nFiltersP = new INDINumberProperty(this, "N_FILTERS", "Filters", "Control", Constants.PropertyStates.OK, Constants.PropertyPermissions.RO, 0);
    nFiltersE = new INDINumberElement(nFiltersP, "N_FILTERS", "Number of Filters", getNumberOfFilters(), getNumberOfFilters(), getNumberOfFilters(), 1, "%1.0f");

    filterNamesFileName = getName() + ".filterNames";

    try {
      filterNamesP = (INDITextProperty) INDIProperty.loadFromFile(this, filterNamesFileName);
    } catch (INDIException ex) {
      System.out.println(ex.getMessage());
    }

    if (filterNamesP == null) {
      filterNamesP = new INDITextProperty(this, "FILTER_NAMES", "Filter Names", "Configuration", Constants.PropertyStates.OK, Constants.PropertyPermissions.RW, 0);

      for (int i = 0 ; i < getNumberOfFilters() ; i++) {
        INDITextElement te = new INDITextElement(filterNamesP, "FILTER_NAME_" + i, "Filter " + (i + 1), "Filter " + (i + 1));
      }

      saveFilterNames();
    }

    currentFilterP = new INDISwitchProperty(this, "CURRENT_FILTER", "Current Filter", "Control", Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW, 0, Constants.SwitchRules.ONE_OF_MANY);
    ArrayList<INDIElement> elements = filterNamesP.getElementsAsList();

    for (int i = 0 ; i < elements.size() ; i++) {
      SwitchStatus sws = Constants.SwitchStatus.OFF;

      if (i == 0) {
        sws = Constants.SwitchStatus.ON;
      }

      INDISwitchElement se = new INDISwitchElement(currentFilterP, "F_" + (i + 1), elements.get(i).getValue() + "", sws);
    }

    addProperty(nFiltersP);
    addProperty(filterNamesP);
  }

  @Override
  public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
    if (property == filterNamesP) {
      for (int i = 0 ; i < elementsAndValues.length ; i++) {
        INDITextElement el = elementsAndValues[i].getElement();
        String val = elementsAndValues[i].getValue();
        el.setValue(val);
      }

      filterNamesP.setState(Constants.PropertyStates.OK);

      try {
        updateProperty(filterNamesP);
      } catch (INDIException e) {
        e.printStackTrace();
      }

      saveFilterNames();
    }
  }

  @Override
  public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
    if (property == currentFilterP) {
      boolean changed = false;
      int filterNumber = -1;

      for (int i = 0 ; i < elementsAndValues.length ; i++) {
        if (elementsAndValues[i].getValue() == Constants.SwitchStatus.ON) {
          changed = currentFilterP.setOnlyOneSwitchOn(elementsAndValues[i].getElement());
          String name = elementsAndValues[i].getElement().getName();
          filterNumber = Integer.parseInt(name.substring(name.lastIndexOf("_") + 1));
        }
      }

      if (changed) {
        currentFilterP.setState(Constants.PropertyStates.BUSY);

        changeFilter(filterNumber);
      } else {
        currentFilterP.setState(Constants.PropertyStates.OK);
      }

      try {
        updateProperty(currentFilterP);
      } catch (INDIException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Saves the filter names into a particular file.
   */
  private void saveFilterNames() {
    try {
      INDIProperty.saveToFile(filterNamesP, filterNamesFileName);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Implements the actual changing of the filter on the wheel.
   *
   * @param filterNumber The filter that must be setted on the filer wheel
   */
  protected abstract void changeFilter(int filterNumber);

  /**
   * Notifies that the wheel has finished changing the filter. Should be called
   * by subclases when approppiate.
   */
  public void filterHasBeenChanged() {
    currentFilterP.setState(Constants.PropertyStates.OK);

    try {
      updateProperty(currentFilterP);
    } catch (INDIException e) {
      e.printStackTrace();
    }
  }

  /**
   * Shows the CURRENT_FILTER property. Usually called when the driver connects
   * to the wheel.
   */
  protected void showCurrentFilterProperty() {
    addProperty(currentFilterP);
  }

  /**
   * Hidess the CURRENT_FILTER property. Usually called when the driver
   * disconnects from the wheel.
   */
  protected void hideCurrentFilterProperty() {
    removeProperty(currentFilterP);
  }
}
