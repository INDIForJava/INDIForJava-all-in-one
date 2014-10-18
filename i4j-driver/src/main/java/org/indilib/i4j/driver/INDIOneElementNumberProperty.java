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

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.INDIException;

/**
 * A class representing a INDI Number Property with only one Number Element
 * (with the same name and label of the Property).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.37, January 11, 2014
 */
public class INDIOneElementNumberProperty extends INDINumberProperty {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -4210491722297116674L;

    /**
     * the single element of this propery.
     */
    private INDINumberElement element;

    /**
     * Loads a One Element Number Property from a file.
     * 
     * @param driver
     *            The Driver to which this property is associated
     * @param name
     *            The name of the property
     * @return The loaded number property or <code>null</code> if it could not
     *         be loaded.
     */
    private static INDIOneElementNumberProperty loadOneElementNumberProperty(INDIDriver driver, String name) {
        INDIProperty<?> prop;

        try {
            prop = INDIProperty.loadFromFile(driver, name);
        } catch (INDIException e) { // Was not correctly loaded
            return null;
        }

        if (!(prop instanceof INDIOneElementNumberProperty)) {
            return null;
        }

        INDIOneElementNumberProperty tp = (INDIOneElementNumberProperty) prop;
        tp.setSaveable(true);
        return tp;
    }

    /**
     * Constructs an instance of <code>INDIOneElementNumberProperty</code> with
     * a particular <code>driver</code>, <code>name</code>, <code>label</code>,
     * <code>group</code>, <code>state</code>, <code>permission</code>,
     * <code>minimum</code>, <code>maximum</code>, <code>step</code>,
     * <code>format</code> and a initial <code>value</code> for the Element.
     * 
     * @param driver
     *            The Driver to which this property is associated
     * @param name
     *            The name of the Property
     * @param label
     *            The label of the Property
     * @param group
     *            The group of the Property
     * @param state
     *            The initial state of the Property
     * @param permission
     *            The permission of the Property
     * @param minimum
     *            The minimum value for the Element
     * @param maximum
     *            The maximum value for the Element
     * @param step
     *            The step value for the Element
     * @param format
     *            the number format for the Element
     * @param value
     *            Initial value for the Element
     * @see INDINumberProperty
     */
    public INDIOneElementNumberProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, double minimum,
            double maximum, double step, String format, double value) {
        super(driver, name, label, group, state, permission);

        element = new INDINumberElement(this, name, label, value, minimum, maximum, step, format);
    }

    /**
     * Gets the value of the Element.
     * 
     * @return The Value of the Element.
     * @see INDINumberElement#getValue()
     */
    public Double getValue() {
        return element.getValue();
    }

    /**
     * Sets the value of the Element.
     * 
     * @param newValue
     *            The new value for the Element
     * @see INDINumberElement#setValue(Object newValue)
     */
    public void setValue(Object newValue) {
        element.setValue(newValue);
    }
}
