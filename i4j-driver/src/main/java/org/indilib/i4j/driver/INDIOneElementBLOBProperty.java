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

import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.util.INDIPropertyBuilder;

/**
 * A class representing a INDI BLOB Property with only one BLOB Element (with
 * the same name and label of the Property).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.36, November 23, 2013
 */
public class INDIOneElementBLOBProperty extends INDIBLOBProperty {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 2274089274345591530L;

    /**
     * The BLOB Element.
     */
    private INDIBLOBElement element;

    public INDIOneElementBLOBProperty(INDIPropertyBuilder<INDIBLOBProperty> builder) {
        super(builder);
    }

    /**
     * Loads a One Element BLOB Property from a file.
     * 
     * @param driver
     *            The Driver to which this property is associated
     * @param name
     *            The name of the property
     * @return The loaded BLOB property or <code>null</code> if it could not be
     *         loaded.
     */
    private static INDIOneElementBLOBProperty loadOneElementBLOBProperty(INDIDriver driver, String name) {
        INDIProperty<?> prop;

        try {
            prop = INDIProperty.loadFromFile(driver, name);
        } catch (INDIException e) { // Was not correctly loaded
            return null;
        }

        if (!(prop instanceof INDIOneElementBLOBProperty)) {
            return null;
        }

        INDIOneElementBLOBProperty tp = (INDIOneElementBLOBProperty) prop;
        tp.setSaveable(true);
        return tp;
    }

    /**
     * Gets the value of the Element.
     * 
     * @return The Value of the Element.
     * @see INDIBLOBElement#getValue()
     */
    public INDIBLOBValue getValue() {
        return element.getValue();
    }

    /**
     * Sets the value of the Element.
     * 
     * @param newValue
     *            The new value for the Element
     * @see INDIBLOBElement#setValue(Object newValue)
     */
    public void setValue(Object newValue) {
        element.setValue(newValue);
    }
}
