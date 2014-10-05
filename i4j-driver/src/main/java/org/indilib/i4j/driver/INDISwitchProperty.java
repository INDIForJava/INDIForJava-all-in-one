
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

import java.util.List;

import org.indilib.i4j.Constants;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIDateFormat;
import org.indilib.i4j.INDIException;

/**
 * A class representing a INDI Switch Property.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 7, 2013
 */
public class INDISwitchProperty extends INDIProperty<INDISwitchElement> {

  /**
   * The current Rule for this Switch Property.
   */
  private SwitchRules rule;

  /**
   * Constructs an instance of
   * <code>INDISwitchProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code> and
   * <code>rule</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param rule The rule of the Switch Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDISwitchProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, SwitchRules rule) throws IllegalArgumentException {
    super(driver, name, label, group, state, permission, timeout);

    if (permission == PropertyPermissions.WO) {
      throw new IllegalArgumentException("Switch Properties cannot be Write Only");
    }

    this.rule = rule;
  }

  /**
   * Loads an instance of
   * <code>INDISwitchProperty</code> from a file or, if it cannot be loaded,
   * constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code> and
   * <code>rule</code>. The property will autosave its status to a file every
   * time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param rule The rule of the Switch Property
   * @throws IllegalArgumentException
   * @return The loaded switch property or a new constructed one if cannot be
   * loaded.
   * @see INDIProperty
   */
  public static INDISwitchProperty createSaveableSwitchProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, SwitchRules rule) throws IllegalArgumentException {
    INDISwitchProperty sp = loadSwitchProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchProperty(driver, name, label, group, state, permission, timeout, rule);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Loads a Switch Property from a file.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the property
   * @return The loaded switch property or <code>null</code> if it could not be
   * loaded.
   */
  private static INDISwitchProperty loadSwitchProperty(INDIDriver driver, String name) {
    INDIProperty prop;

    try {
      prop = INDIProperty.loadFromFile(driver, name);
    } catch (INDIException e) {  // Was not correctly loaded
      return null;
    }

    if (!(prop instanceof INDISwitchProperty)) {
      return null;
    }

    INDISwitchProperty sp = (INDISwitchProperty)prop;
    sp.setSaveable(true);
    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>rule</code>, and a 0 timeout.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param rule The rule of the Switch Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDISwitchProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, SwitchRules rule) throws IllegalArgumentException {
    super(driver, name, label, group, state, permission, 0);

    if (permission == PropertyPermissions.WO) {
      throw new IllegalArgumentException("Switch Properties cannot be Write Only");
    }

    this.rule = rule;
  }

  /**
   * Loads an instance of
   * <code>INDISwitchProperty</code> from a file or, if it cannot be loaded,
   * constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>rule</code>. The property will autosave its status to a file every
   * time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param rule The rule of the Switch Property
   * @throws IllegalArgumentException
   * @return The loaded switch property or a new constructed one if cannot be
   * loaded.
   * @see INDIProperty
   */
  public static INDISwitchProperty createSaveableSwitchProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, SwitchRules rule) throws IllegalArgumentException {
    INDISwitchProperty sp = loadSwitchProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchProperty(driver, name, label, group, state, permission, rule);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>rule</code>, and a 0 timeout and default group.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param rule The rule of the Switch Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDISwitchProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, SwitchRules rule) throws IllegalArgumentException {
    super(driver, name, label, null, state, permission, 0);

    if (permission == PropertyPermissions.WO) {
      throw new IllegalArgumentException("Switch Properties cannot be Write Only");
    }

    this.rule = rule;
  }

  /**
   * Loads an instance of
   * <code>INDISwitchProperty</code> from a file or, if it cannot be loaded,
   * constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>rule</code>. The property will autosave its status to a file every
   * time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param rule The rule of the Switch Property
   * @throws IllegalArgumentException
   * @return The loaded switch property or a new constructed one if cannot be
   * loaded.
   * @see INDIProperty
   */
  public static INDISwitchProperty createSaveableSwitchProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, SwitchRules rule) throws IllegalArgumentException {
    INDISwitchProperty sp = loadSwitchProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchProperty(driver, name, label, state, permission, rule);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>rule</code>, and a 0 timeout, a default group and a label equal to
   * its
   * <code>name</code>
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param rule The rule of the Switch Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDISwitchProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, SwitchRules rule) throws IllegalArgumentException {
    super(driver, name, null, null, state, permission, 0);

    if (permission == PropertyPermissions.WO) {
      throw new IllegalArgumentException("Switch Properties cannot be Write Only");
    }

    this.rule = rule;
  }

  /**
   * Loads an instance of
   * <code>INDISwitchProperty</code> from a file or, if it cannot be loaded,
   * constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>rule</code>. The property will autosave its status to a file every
   * time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param rule The rule of the Switch Property
   * @throws IllegalArgumentException
   * @return The loaded switch property or a new constructed one if cannot be
   * loaded.
   * @see INDIProperty
   */
  public static INDISwitchProperty createSaveableSwitchProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, SwitchRules rule) throws IllegalArgumentException {
    INDISwitchProperty sp = loadSwitchProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchProperty(driver, name, state, permission, rule);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Resets to OFF al switch elements. Should not usually be called by drivers.
   * Used by
   * <code>INDISwitchElement.setValue()</code> to ensure that if the Switch rule
   * allows only one ON simultaneous value the condition is hold.
   */
  public void resetAllSwitches() {
    List<INDISwitchElement> list = getElementsAsList();

    for (int i = 0 ; i < list.size() ; i++) {
        INDISwitchElement e = list.get(i);

      e.setValue(SwitchStatus.OFF);
    }
  }

  /**
   * Resets to OFF al switch elements but one which is set to ON. Very useful
   * when dealing with
   * <code>SwitchRules.ONE_OF_MANY</code> tipe of Switches.
   *
   * @param element The element that is turned ON.
   *
   * @return <code>true</code> if the selected element has been changed.
   * <code>false</code> otherwise.
   */
  public boolean setOnlyOneSwitchOn(INDISwitchElement element) {
    boolean changed = false;

    if (element.getValue() == Constants.SwitchStatus.OFF) {
      changed = true;
    }

    resetAllSwitches();

    element.setValue(Constants.SwitchStatus.ON);

    return changed;
  }

  /**
   * Gets the current Rule for this Switch Property
   *
   * @return the current Rule for this Switch Property
   */
  public SwitchRules getRule() {
    return rule;
  }

  /**
   * Checks if the Rule of this Switch property holds.
   *
   * @return <code>true</code> if the values of the Elements of this Property
   * comply with the Rule. <code>false</code> otherwise.
   */
  protected boolean checkCorrectValues() {
    if (getState() == PropertyStates.OK) {

      int selectedCount = getSelectedCount();

      if ((rule == SwitchRules.ONE_OF_MANY) && (selectedCount != 1)) {
        return false;
      }

      if ((rule == SwitchRules.AT_MOST_ONE) && (selectedCount > 1)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Gets the number of selected Switch Elements.
   *
   * @return the number of selected Elements.
   */
  private int getSelectedCount() {
    int selectedCount = 0;

    List<INDISwitchElement> list = getElementsAsList();

    for (int i = 0 ; i < list.size() ; i++) {
      INDISwitchElement el = list.get(i);
      if (el.getValue() == SwitchStatus.ON) {
        selectedCount++;
      }
    }

    return selectedCount;
  }

  @Override
  public INDISwitchElement getElement(String name) {
    return (INDISwitchElement)super.getElement(name);
  }

  @Override
  protected String getXMLPropertyDefinitionInit() {
    String xml = "<defSwitchVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" perm=\"" + Constants.getPropertyPermissionAsString(getPermission()) + "\" rule=\"" + Constants.getSwitchRuleAsString(getRule()) + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertyDefinitionInit(String message) {
    String xml = "<defSwitchVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" perm=\"" + Constants.getPropertyPermissionAsString(getPermission()) + "\" rule=\"" + Constants.getSwitchRuleAsString(getRule()) + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertyDefinitionEnd() {
    String xml = "</defSwitchVector>";

    return xml;
  }

  @Override
  protected String getXMLPropertySetInit() {
    String xml = "<setSwitchVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertySetInit(String message) {
    String xml = "<setSwitchVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertySetEnd() {
    String xml = "</setSwitchVector>";

    return xml;
  }

    /**
     * get the selected Element when the rule is ONE_OF_MANY
     * 
     * @return
     */
    public INDISwitchElement getOnElement() {
        if (rule == SwitchRules.ONE_OF_MANY) {
            throw new UnsupportedOperationException("getOnElement() is only valid with SwitchRules.ONE_OF_MANY");
        } else {
            for (INDIElement element : getElementsAsList()) {
                if (element.getValue() == SwitchStatus.ON) {
                    return (INDISwitchElement) element;
                }
            }
            return null;
        }
    }

    /**
     * reset all elements to off
     * @return
     */
    public void reset() {
        for (INDIElement element : getElementsAsList()) {
            if (element.getValue() == SwitchStatus.ON) {
                ((INDISwitchElement) element).setValue(SwitchStatus.OFF);
            }
        }
    }
}