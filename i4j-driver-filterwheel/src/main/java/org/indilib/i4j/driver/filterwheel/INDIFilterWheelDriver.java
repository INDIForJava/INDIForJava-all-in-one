
package org.indilib.i4j.driver.filterwheel;

/*
 * #%L
 * INDI for Java Abstract Filter Wheel Driver
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.indilib.i4j.Constants;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDIProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class representing a Filter Wheel Driver in the INDI Protocol. INDI Filter
 * Wheel Drivers should extend this class. It is in charge of handling the
 * standard properties for Filter Wheels:
 * <ul>
 * <li>filter_names -> filter_name_1, filter_name_2, ..., filter_name_N
 * (text)</li>
 * <li>FILTER_SLOT -> FILTER_SLOT_VALUE (number)</li>
 * <li>FILTER_NAME -> FILTER_NAME_VALUE (text)</li>
 * </ul>
 *
 * It is <strong>VERY IMPORTANT</strong> that any subclasses use
 * <code>super.processNewTextValue(property, timestamp, elementsAndValues);</code>
 * and
 * <code>super.processNewNumberValue(property, timestamp, elementsAndValues);</code>
 * at the beginning of
 * <code>processNewTextValue</code> and
 * <code>processNewNumberValue</code> to handle the generic filter wheel
 * properties correctly.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 7, 2013
 */
public abstract class INDIFilterWheelDriver extends INDIDriver {
    private static Logger LOG = LoggerFactory.getLogger(INDIFilterWheelDriver.class);

  /**
   * The filter_names property
   */
  private INDITextProperty filterNamesP;
  /**
   * The FILTER_SLOT property
   */
  private INDINumberProperty filterSlotP;
  /**
   * The FILTER_SLOT_VALUE element
   */
  private INDINumberElement filterSlotValueE;
  /**
   * The FILTER_NAME property
   */
  private INDITextProperty filterNameP;
  /**
   * The FILTER_NAME_VALUE element property
   */
  private INDITextElement filterNameValueE;

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
  }

  /**
   * Initializes the standard properties. MUST BE CALLED BY SUBDRIVERS.
   */
  protected void initializeStandardProperties() {
    try {
      filterNamesP = (INDITextProperty)INDIProperty.loadFromFile(this, "filter_names");
    } catch (INDIException ex) {
        LOG.error("INDIException",ex);
    }

    if (filterNamesP == null) {
      filterNamesP = INDITextProperty.createSaveableTextProperty(this, "filter_names", "Filter Names", "Configuration", Constants.PropertyStates.OK, Constants.PropertyPermissions.RW, 0);

      for (int i = 0 ; i < getNumberOfFilters() ; i++) {
        INDITextElement te = new INDITextElement(filterNamesP, "filter_name_" + (i + 1), "Filter " + (i + 1), "Filter " + (i + 1));
      }
    }

    filterSlotP = new INDINumberProperty(this, "FILTER_SLOT", "Filter Slot", "Control", Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RW, 0);
    filterSlotValueE = new INDINumberElement(filterSlotP, "FILTER_SLOT_VALUE", "Filter Slot Value", 1, 1, getNumberOfFilters(), 1, "%1.0f");

    filterNameP = new INDITextProperty(this, "FILTER_NAME", "Filter Name", "Control", Constants.PropertyStates.IDLE, Constants.PropertyPermissions.RO, 0);
    String firstFilterName = filterNamesP.getElement("filter_name_1").getValue();
    filterNameValueE = new INDITextElement(filterNameP, "FILTER_NAME_VALUE", "Filter Name Value", firstFilterName);

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

      updateProperty(filterNamesP);
    }
  }

  @Override
  public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
    if (property == filterSlotP) {
      int newFilterNumber = elementsAndValues[0].getValue().intValue();

      if ((newFilterNumber > 0) && (newFilterNumber <= getNumberOfFilters())) {
        filterSlotP.setState(Constants.PropertyStates.BUSY);
        filterNameP.setState(Constants.PropertyStates.BUSY);

        changeFilter(newFilterNumber);
      } else {
        filterSlotP.setState(Constants.PropertyStates.OK);
        filterNameP.setState(Constants.PropertyStates.OK);
      }

      updateProperty(filterSlotP);
      updateProperty(filterNameP);
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
   *
   * @param filterSlot The Filter Slot that is currently on.
   */
  protected void filterHasBeenChanged(int filterSlot) {
    LOG.info("Filter has been changed " + filterSlot);

    filterSlotP.setState(Constants.PropertyStates.OK);
    filterNameP.setState(Constants.PropertyStates.OK);


      filterSlotValueE.setValue("" + filterSlot);

      filterNameValueE.setValue(filterNamesP.getElement("filter_name_" + filterSlot).getValue());

      updateProperty(filterNameP);
      updateProperty(filterSlotP);
  }

  protected void setBusy() {
    filterSlotP.setState(Constants.PropertyStates.BUSY);
    filterNameP.setState(Constants.PropertyStates.BUSY);


      updateProperty(filterNameP);
      updateProperty(filterSlotP);

  }

  /**
   * Shows the FILTER_SLOT and FILTER_NAME properties. Usually called when the
   * driver connects to the wheel.
   */
  protected void showFilterSlotAndNameProperties() {
    addProperty(filterSlotP);
    addProperty(filterNameP);
  }

  /**
   * Hides the FILTER_SLOT and FILTER_NAME properties. Usually called when the
   * driver disconnects from the wheel.
   */
  protected void hideFilterSlotAndNameProperties() {
    removeProperty(filterSlotP);
    removeProperty(filterNameP);
  }
}
