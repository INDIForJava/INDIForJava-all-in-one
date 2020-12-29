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

import java.util.Date;

import org.indilib.i4j.Constants;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.indilib.i4j.properties.INDIStandardProperty.FILTER_NAME;
import static org.indilib.i4j.properties.INDIStandardProperty.FILTER_SLOT;

/**
 * A class representing a Filter Wheel Driver in the INDI Protocol. INDI Filter
 * Wheel Drivers should extend this class. It is in charge of handling the
 * standard properties for Filter Wheels:
 * <ul>
 * <li>filter_names - filter_name_1, filter_name_2, ..., filter_name_N (text)</li>
 * <li>FILTER_SLOT - FILTER_SLOT_VALUE (number)</li>
 * <li>FILTER_NAME - FILTER_NAME_VALUE (text)</li>
 * </ul>
 * It is <strong>VERY IMPORTANT</strong> that any subclasses use
 * <code>super.processNewTextValue(property, timestamp, elementsAndValues);</code>
 * and
 * <code>super.processNewNumberValue(property, timestamp, elementsAndValues);</code>
 * at the beginning of <code>processNewTextValue</code> and
 * <code>processNewNumberValue</code> to handle the generic filter wheel
 * properties correctly.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public abstract class INDIFilterWheelDriver extends INDIDriver {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIFilterWheelDriver.class);

    /**
     * The filter_names property.
     */
    private INDITextProperty filterNamesP;

    /**
     * The FILTER_SLOT property.
     */
    private INDINumberProperty filterSlotP;

    /**
     * The FILTER_SLOT_VALUE element.
     */
    private INDINumberElement filterSlotValueE;

    /**
     * The FILTER_NAME property.
     */
    private INDITextProperty filterNameP;

    /**
     * The FILTER_NAME_VALUE element property.
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
     * <code>inputStream</code> from which to read the incoming messages (from
     * clients) and a <code>outputStream</code> to write the messages to the
     * clients.
     * 
     * @param connection
     *            the indi connection to the server.
     */
    public INDIFilterWheelDriver(INDIConnection connection) {
        super(connection);
    }

    /**
     * Initializes the standard properties. MUST BE CALLED BY SUBDRIVERS.
     */
    protected void initializeStandardProperties() {

        filterNamesP = newTextProperty().name("filter_names").label("Filter Names").group("Configuration")//
                .state(PropertyStates.OK).permission(PropertyPermissions.RW).create();
        for (int i = 0; i < getNumberOfFilters(); i++) {
            filterNamesP.newElement().name("filter_name_" + (i + 1)).label("Filter " + (i + 1)).textValue("Filter " + (i + 1)).create();
        }

        filterSlotP = newNumberProperty().name(FILTER_SLOT).label("Filter Slot").group("Control").create();
        filterSlotValueE = filterSlotP.newElement().name("FILTER_SLOT_VALUE").label("Filter Slot Value")//
                .numberValue(1).minimum(1).maximum(getNumberOfFilters()).step(1).numberFormat("%1.0f").create();

        filterNameP = newTextProperty().name(FILTER_NAME).label("Filter Name").group("Control").permission(PropertyPermissions.RO).create();

        String firstFilterName = filterNamesP.getElement("filter_name_1").getValue();
        filterNameValueE = filterNameP.newElement().name("FILTER_NAME_VALUE").label("Filter Name Value").textValue(firstFilterName).create();

        addProperty(filterNamesP);
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
        if (property == filterNamesP) {
            for (INDITextElementAndValue elementsAndValue : elementsAndValues) {
                INDITextElement el = elementsAndValue.getElement();
                String val = elementsAndValue.getValue();
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

            if (newFilterNumber > 0 && newFilterNumber <= getNumberOfFilters()) {
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
     * @param filterNumber
     *            The filter that must be setted on the filer wheel
     */
    protected abstract void changeFilter(int filterNumber);

    /**
     * Notifies that the wheel has finished changing the filter. Should be
     * called by subclases when approppiate.
     * 
     * @param filterSlot
     *            The Filter Slot that is currently on.
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

    /**
     * set the filterslot and name to busy.
     */
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
