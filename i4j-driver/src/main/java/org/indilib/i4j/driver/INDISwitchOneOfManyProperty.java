package org.indilib.i4j.driver;

/*
 * #%L INDI for Java Driver Library %% Copyright (C) 2013 - 2014 indiforjava %%
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Lesser Public License for more details. You should have received a copy of
 * the GNU General Lesser Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>. #L%
 */

import java.util.List;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIException;

/**
 * A class representing a INDI One Of Many Switch Property. It simplifies
 * dealing with Switch elements and so on.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 8, 2013
 */
public class INDISwitchOneOfManyProperty extends INDISwitchProperty {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -7669211544834222712L;

    /**
     * Loads a Switch One of Many Property from a file.
     * 
     * @param driver
     *            The Driver to which this property is associated
     * @param name
     *            The name of the property
     * @return The loaded switch one of many property or <code>null</code> if it
     *         could not be loaded.
     */
    private static INDISwitchOneOfManyProperty loadSwitchOneOfManyProperty(INDIDriver driver, String name) {
        INDIProperty<?> prop;

        try {
            prop = INDIProperty.loadFromFile(driver, name);
        } catch (INDIException e) { // Was not correctly loaded
            return null;
        }

        if (!(prop instanceof INDISwitchOneOfManyProperty)) {
            return null;
        }

        INDISwitchOneOfManyProperty sp = (INDISwitchOneOfManyProperty) prop;
        sp.setSaveable(true);
        return sp;
    }

    /**
     * Constructs an instance of <code>INDISwitchOneOfManyProperty</code> with a
     * particular <code>driver</code>, <code>name</code>, <code>label</code>,
     * <code>group</code>, <code>state</code>, <code>permission</code>,
     * <code>elements</code> and <code>selectedElement</code>.
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
     * @param elements
     *            The name of the option
     * @param selectedElement
     *            The initial status of the option
     * @see INDISwitchProperty
     */
    public INDISwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, String[] elements,
            int selectedElement) {
        super(driver, name, label, group, state, permission, 0, SwitchRules.ONE_OF_MANY);

        createElements(elements, selectedElement);
    }

    /**
     * Constructs an instance of <code>INDISwitchOneOfManyProperty</code> with a
     * particular <code>driver</code>, <code>name</code>, <code>label</code>,
     * <code>group</code>, <code>state</code>, <code>permission</code> and
     * <code>elements</code>.
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
     * @param elements
     *            The name of the option
     * @see INDISwitchProperty
     */
    public INDISwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, String[] elements) {
        super(driver, name, label, group, state, permission, 0, SwitchRules.ONE_OF_MANY);

        createElements(elements, 0);
    }

    /**
     * Loads an instance of <code>INDISwitchOneOfManyProperty</code> from a file
     * or, if it cannot be loaded, constructs it with a particular
     * <code>driver</code>, <code>name</code>, <code>label</code>,
     * <code>group</code>, <code>state</code>, <code>permission</code> and
     * <code>elements</code>. The property will autosave its status to a file
     * every time that it is changed.
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
     * @param elements
     *            The name of the option
     * @return The loaded switch one of many property or a new constructed one
     *         if cannot be loaded.
     * @see INDISwitchProperty
     */
    public static INDISwitchOneOfManyProperty createSaveableSwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state,
            PropertyPermissions permission, String[] elements) {
        INDISwitchOneOfManyProperty sp = loadSwitchOneOfManyProperty(driver, name);

        if (sp == null) {
            sp = new INDISwitchOneOfManyProperty(driver, name, label, group, state, permission, elements);
            sp.setSaveable(true);
        }

        return sp;
    }

    /**
     * Creates de Switch Elements of the property. Each Element name and label
     * will be the same. The <code>defaultOption</code> element will be
     * selected.
     * 
     * @param options
     *            The names of the Switch Elements
     * @param defaultOption
     *            The number of the selected element
     */
    private void createElements(String[] options, int defaultOption) {
        if (defaultOption >= options.length) {
            defaultOption = 0;
        }
        for (int i = 0; i < options.length; i++) {
            SwitchStatus ss = SwitchStatus.OFF;

            if (i == defaultOption) {
                ss = SwitchStatus.ON;
            }
            new INDISwitchElement(this, options[i], ss);
        }
    }

    /**
     * Gets the name of the selected element.
     * 
     * @return The name of the selected eleent
     */
    public String getSelectedValue() {
        INDISwitchElement e = getSelectedElement();

        return e.getName();
    }

    /**
     * Gets the selected element.
     * 
     * @return The selected element
     */
    private INDISwitchElement getSelectedElement() {
        List<INDISwitchElement> list = getElementsAsList();

        for (int i = 0; i < list.size(); i++) {
            INDISwitchElement e = list.get(i);

            if (e.getValue() == SwitchStatus.ON) {
                return e;
            }
        }

        return null; // Should never happen
    }

    /**
     * Gets the index of the selected element.
     * 
     * @return The index of the selected element
     */
    public int getSelectedIndex() {
        List<INDISwitchElement> list = getElementsAsList();

        for (int i = 0; i < list.size(); i++) {
            INDISwitchElement e = list.get(i);

            if (e.getValue() == SwitchStatus.ON) {
                return i;
            }
        }

        return -1; // Should never happen
    }

    /**
     * Sets the selected Element to the one with a particular <code>index</code>
     * .
     * 
     * @param index
     *            The index of the Element that is being selected
     */
    public void setSelectedIndex(int index) {
        if ((index < 0) || (index >= this.getElementCount())) {
            return;
        }

        List<INDISwitchElement> list = getElementsAsList();

        INDISwitchElement e = (INDISwitchElement) list.get(index);

        setOnlyOneSwitchOn(e);
    }

    /**
     * Gets the index of the element that should be selected according to some
     * Elements and Values pairs. This method DOES NOT change the selected index
     * nor returns the really selected element index.
     * 
     * @param ev
     *            The pairs of elements and values
     * @return The index of the element that would be selected according to the
     *         pairs of elements and values.
     */
    public int getSelectedIndex(INDISwitchElementAndValue[] ev) {
        for (int i = 0; i < ev.length; i++) {
            if (ev[i].getValue() == SwitchStatus.ON) {
                List<INDISwitchElement> list = getElementsAsList();

                for (int h = 0; h < list.size(); h++) {
                    if (list.get(h) == ev[i].getElement()) {
                        return h;
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Gets the element that should be selected according to some Elements and
     * Values pairs. This method DOES NOT change the selected index nor returns
     * the really selected element.
     * 
     * @param ev
     *            The pairs of elements and values
     * @return The element that would be selected according to the pairs of
     *         elements and values.
     */
    public String getSelectedValue(INDISwitchElementAndValue[] ev) {
        for (int i = 0; i < ev.length; i++) {
            if (ev[i].getValue() == SwitchStatus.ON) {
                List<INDISwitchElement> list = getElementsAsList();

                for (int h = 0; h < list.size(); h++) {
                    if (list.get(h) == ev[i].getElement()) {
                        return list.get(h).getName();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Sets the selected Element to the one specified in an array of elements
     * and values.
     * 
     * @param ev
     *            The pairs of elements and values
     */
    public void setSelectedIndex(INDISwitchElementAndValue[] ev) {
        int selected = getSelectedIndex(ev);

        setSelectedIndex(selected);
    }
}
