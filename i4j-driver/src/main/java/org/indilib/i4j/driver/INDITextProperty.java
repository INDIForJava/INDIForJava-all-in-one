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
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.INDIDateFormat;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.util.INDIPropertyBuilder;

/**
 * A class representing a INDI Text Property.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 7, 2013
 */
public class INDITextProperty extends INDIProperty<INDITextElement> {

    /**
     * servial version id.
     */
    private static final long serialVersionUID = -1568675891716860995L;

    public INDITextProperty(INDIPropertyBuilder<INDITextProperty> builder) {
        super(builder);
    }

    /**
     * Loads a Text Property from a file.
     * 
     * @param driver
     *            The Driver to which this property is associated
     * @param name
     *            The name of the property
     * @return The loaded text property or <code>null</code> if it could not be
     *         loaded.
     */
    private static INDITextProperty loadTextProperty(INDIDriver driver, String name) {
        INDIProperty<?> prop;

        try {
            prop = INDIProperty.loadFromFile(driver, name);
        } catch (INDIException e) { // Was not correctly loaded
            return null;
        }

        if (!(prop instanceof INDITextProperty)) {
            return null;
        }

        INDITextProperty tp = (INDITextProperty) prop;
        tp.setSaveable(true);
        return tp;
    }

    /**
     * Constructs an instance of <code>INDITextProperty</code> with a particular
     * <code>driver</code>, <code>name</code>, <code>label</code>,
     * <code>group</code>, <code>state</code>, <code>permission</code> and a 0
     * timeout.
     * 
     * @param driver
     *            The Driver to which this property is associated.
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
     * @see INDIProperty
     */
    public INDITextProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission) {
        super(driver, name, label, group, state, permission, 0);
    }

    @Override
    public INDITextElement getElement(String name) {
        return (INDITextElement) super.getElement(name);
    }

    @Override
    protected String getXMLPropertyDefinitionInit() {
        String xml =
                "<defTextVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\""
                        + Constants.getPropertyStateAsString(getState()) + "\" perm=\"" + Constants.getPropertyPermissionAsString(getPermission()) + "\" timeout=\""
                        + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertyDefinitionInit(String message) {
        String xml =
                "<defTextVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\""
                        + Constants.getPropertyStateAsString(getState()) + "\" perm=\"" + Constants.getPropertyPermissionAsString(getPermission()) + "\" timeout=\""
                        + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertyDefinitionEnd() {
        String xml = "</defTextVector>";

        return xml;
    }

    @Override
    protected String getXMLPropertySetInit() {
        String xml =
                "<setTextVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState())
                        + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertySetInit(String message) {
        String xml =
                "<setTextVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState())
                        + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertySetEnd() {
        String xml = "</setTextVector>";

        return xml;
    }

    @Override
    protected Class<INDITextElement> elementClass() {
        return INDITextElement.class;
    }
}
