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
 * A class representing a INDI BLOB Property.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 7, 2013
 */
public class INDIBLOBProperty extends INDIProperty<INDIBLOBElement> {

    /**
     * Constructs an instance of <code>INDIBLOBProperty</code> with the
     * properties collected by the builder.
     * 
     * @param builder
     *            the builder containing the properties.
     */
    public INDIBLOBProperty(INDIPropertyBuilder<INDIBLOBProperty> builder) {
        super(builder);
    }

    /**
     * Constructs an instance of <code>INDIBLOBProperty</code> with a particular
     * <code>driver</code>, <code>name</code>, <code>label</code>,
     * <code>group</code>, <code>state</code>, <code>permission</code> and
     * <code>timeout</code>.
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
     * @param timeout
     *            The timeout of the Property
     * @see INDIProperty
     */
    public INDIBLOBProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout) {
        super(driver, name, label, group, state, permission, timeout);
    }

    /**
     * Loads an instance of <code>INDIBLOBProperty</code> from a file or, if it
     * cannot be loaded, constructs it with a particular <code>driver</code>,
     * <code>name</code>, <code>label</code>, <code>group</code>,
     * <code>state</code>, <code>permission</code> and <code>timeout</code>. The
     * property will autosave its status to a file every time that it is
     * changed.
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
     * @param timeout
     *            The timeout of the Property
     * @return The loaded BLOB property or a new constructed one if cannot be
     *         loaded.
     * @see INDIProperty
     */
    public static INDIBLOBProperty createSaveableBLOBProperty(INDIDriver driver, String name, String label, String group, PropertyStates state,
            PropertyPermissions permission, int timeout) {
        INDIBLOBProperty bp = loadBLOBProperty(driver, name);

        if (bp == null) {
            bp = new INDIBLOBProperty(driver, name, label, group, state, permission, timeout);
            bp.setSaveable(true);
        }

        return bp;
    }

    /**
     * Loads a BLOB Property from a file.
     * 
     * @param driver
     *            The Driver to which this property is associated
     * @param name
     *            The name of the property
     * @return The loaded blob property or <code>null</code> if it could not be
     *         loaded.
     */
    private static INDIBLOBProperty loadBLOBProperty(INDIDriver driver, String name) {
        INDIProperty prop;

        try {
            prop = INDIProperty.loadFromFile(driver, name);
        } catch (INDIException e) { // Was not correctly loaded
            return null;
        }

        if (!(prop instanceof INDIBLOBProperty)) {
            return null;
        }

        INDIBLOBProperty bp = (INDIBLOBProperty) prop;
        bp.setSaveable(true);
        return bp;
    }

    /**
     * Constructs an instance of <code>INDIBLOBProperty</code> with a particular
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
    public INDIBLOBProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission) {
        super(driver, name, label, group, state, permission, 0);
    }

    /**
     * Loads an instance of <code>INDIBLOBProperty</code> from a file or, if it
     * cannot be loaded, constructs it with a particular <code>driver</code>,
     * <code>name</code>, <code>label</code>, <code>group</code>,
     * <code>state</code> and <code>permission</code>. The property will
     * autosave its status to a file every time that it is changed.
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
     * @return The loaded BLOB property or a new constructed one if cannot be
     *         loaded.
     * @see INDIProperty
     */
    public static INDIBLOBProperty createSaveableBLOBProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission) {
        INDIBLOBProperty bp = loadBLOBProperty(driver, name);

        if (bp == null) {
            bp = new INDIBLOBProperty(driver, name, label, group, state, permission);
            bp.setSaveable(true);
        }

        return bp;
    }

    /**
     * Constructs an instance of <code>INDIBLOBProperty</code> with a particular
     * <code>driver</code>, <code>name</code>, <code>label</code>,
     * <code>state</code>, <code>permission</code> and a 0 timeout and default
     * group.
     * 
     * @param driver
     *            The Driver to which this property is associated.
     * @param name
     *            The name of the Property
     * @param label
     *            The label of the Property
     * @param state
     *            The initial state of the Property
     * @param permission
     *            The permission of the Property
     * @see INDIProperty
     */
    public INDIBLOBProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission) {
        super(driver, name, label, null, state, permission, 0);
    }

    /**
     * Loads an instance of <code>INDIBLOBProperty</code> from a file or, if it
     * cannot be loaded, constructs it with a particular <code>driver</code>,
     * <code>name</code>, <code>label</code>, <code>state</code> and
     * <code>permission</code>. The property will autosave its status to a file
     * every time that it is changed.
     * 
     * @param driver
     *            The Driver to which this property is associated.
     * @param name
     *            The name of the Property
     * @param label
     *            The label of the Property
     * @param state
     *            The initial state of the Property
     * @param permission
     *            The permission of the Property
     * @return The loaded BLOB property or a new constructed one if cannot be
     *         loaded.
     * @see INDIProperty
     */
    public static INDIBLOBProperty createSaveableBLOBProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission) {
        INDIBLOBProperty bp = loadBLOBProperty(driver, name);

        if (bp == null) {
            bp = new INDIBLOBProperty(driver, name, label, state, permission);
            bp.setSaveable(true);
        }

        return bp;
    }

    /**
     * Constructs an instance of <code>INDIBLOBProperty</code> with a particular
     * <code>driver</code>, <code>name</code>, <code>state</code>,
     * <code>permission</code> and a 0 timeout, a default group and a label
     * equal to its <code>name</code>
     * 
     * @param driver
     *            The Driver to which this property is associated.
     * @param name
     *            The name of the Property
     * @param state
     *            The initial state of the Property
     * @param permission
     *            The permission of the Property
     * @see INDIProperty
     */
    public INDIBLOBProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission) {
        super(driver, name, null, null, state, permission, 0);
    }

    /**
     * Loads an instance of <code>INDIBLOBProperty</code> from a file or, if it
     * cannot be loaded, constructs it with a particular <code>driver</code>,
     * <code>name</code>, <code>state</code> and <code>permission</code>. The
     * property will autosave its status to a file every time that it is
     * changed.
     * 
     * @param driver
     *            The Driver to which this property is associated.
     * @param name
     *            The name of the Property
     * @param state
     *            The initial state of the Property
     * @param permission
     *            The permission of the Property
     * @return The loaded BLOB property or a new constructed one if cannot be
     *         loaded.
     * @see INDIProperty
     */
    public static INDIBLOBProperty createSaveableBLOBProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission) {
        INDIBLOBProperty bp = loadBLOBProperty(driver, name);

        if (bp == null) {
            bp = new INDIBLOBProperty(driver, name, state, permission);
            bp.setSaveable(true);
        }

        return bp;
    }

    @Override
    public INDIBLOBElement getElement(String name) {
        return (INDIBLOBElement) super.getElement(name);
    }

    @Override
    protected String getXMLPropertyDefinitionInit() {
        String xml =
                "<defBLOBVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\""
                        + Constants.getPropertyStateAsString(getState()) + "\" perm=\"" + Constants.getPropertyPermissionAsString(getPermission()) + "\" timeout=\""
                        + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertyDefinitionInit(String message) {
        String xml =
                "<defBLOBVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\""
                        + Constants.getPropertyStateAsString(getState()) + "\" perm=\"" + Constants.getPropertyPermissionAsString(getPermission()) + "\" timeout=\""
                        + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertyDefinitionEnd() {
        String xml = "</defBLOBVector>";

        return xml;
    }

    @Override
    protected String getXMLPropertySetInit() {
        String xml =
                "<setBLOBVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState())
                        + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertySetInit(String message) {
        String xml =
                "<setBLOBVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState())
                        + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertySetEnd() {
        String xml = "</setBLOBVector>";

        return xml;
    }

    @Override
    protected Class<INDIBLOBElement> elementClass() {
        return INDIBLOBElement.class;
    }
}
