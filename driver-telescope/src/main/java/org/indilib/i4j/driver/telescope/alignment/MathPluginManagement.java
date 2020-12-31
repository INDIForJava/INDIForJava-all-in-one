package org.indilib.i4j.driver.telescope.alignment;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
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
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.driver.*;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.driver.event.TextEvent;
import org.indilib.i4j.driver.telescope.INDITelescope;

import java.util.*;

import static org.indilib.i4j.Constants.PropertyStates.OK;

/**
 * This driver extension is used to configure and select a math plugin to use
 * for the telescope alignment calculations.
 * 
 * @author Richard van Nieuwenhoven
 */
public class MathPluginManagement extends INDIDriverExtension<INDITelescope> {

    /**
     * tab to use for the alignment fields.
     */
    private static final String ALIGNMENT_TAB = "Alignment";

    /**
     * All defined plugins in the current classloader. key is the id of the
     * plugin.
     */
    private Map<String, IMathPlugin> plugins;

    /**
     * The switch property with all the availabe math plugins.
     */
    @InjectProperty(name = "ALIGNMENT_SUBSYSTEM_MATH_PLUGINS", label = "Math Plugins", group = ALIGNMENT_TAB)
    private INDISwitchProperty alignmentSubsystemMathPlugins;

    /**
     * The predefined entry for the default build in meth plugin.
     */
    @InjectElement(name = BuiltInMathPlugin.INBUILT_MATH_PLUGIN_NAME, label = BuiltInMathPlugin.INBUILT_MATH_PLUGIN_LABEL, switchValue = SwitchStatus.ON)
    private INDISwitchElement builtInMathPluginElement;

    /**
     * Reinitialisation switch property. initialize the math plugin.
     */
    @InjectProperty(name = "ALIGNMENT_SUBSYSTEM_MATH_PLUGIN_INITIALISE", label = "(Re)Initialise Plugin", group = ALIGNMENT_TAB, switchRule = SwitchRules.AT_MOST_ONE)
    private INDISwitchProperty alignmentSubsystemMathPluginInitialise;

    /**
     * Reinitialisation switch element. initialize the math plugin.
     */
    @InjectElement(name = "ALIGNMENT_SUBSYSTEM_MATH_PLUGIN_INITIALISE", label = "OK")
    private INDISwitchElement alignmentSubsystemMathPluginInitialiseElement;

    /**
     * Activation of the alignment system switch property.
     */
    @InjectProperty(name = "ALIGNMENT_SUBSYSTEM_ACTIVE", label = "Activate alignment subsystem", group = ALIGNMENT_TAB, switchRule = SwitchRules.AT_MOST_ONE)
    private INDISwitchProperty alignmentSubsystemActive;

    /**
     * Activation of the alignment system switch element..
     */
    @InjectElement(name = "ALIGNMENT SUBSYSTEM ACTIVE", label = "Alignment Subsystem Active")
    private INDISwitchElement alignmentSubsystemActiveElement;

    /**
     * The following property is used for configuration purposes only and is not
     * exposed to the client.The currenty selected math plugin property.
     */
    @InjectProperty(name = "ALIGNMENT_SUBSYSTEM_CURRENT_MATH_PLUGIN", label = "Current Math Plugin", group = ALIGNMENT_TAB, saveable = true)
    private INDITextProperty alignmentSubsystemCurrentMathPlugin;

    /**
     * The currenty selected math plugin element.
     */
    @InjectElement(name = "ALIGNMENT ALIGNMENT_SUBSYSTEM_CURRENT_MATH_PLUGIN ACTIVE", label = "Current Math Plugin", textValue = BuiltInMathPlugin.INBUILT_MATH_PLUGIN_NAME)
    private INDITextElement alignmentSubsystemCurrentMathPluginElement;

    /**
     * The active math plugin.
     */
    private IMathPlugin plugin;

    /**
     * The in memmory sync point database to use.
     */
    private final InMemoryDatabase inMemoryDatabase;

    /**
     * Constructor for the math plugin extention. Only available for telescope
     * drivers.
     * 
     * @param driver
     *            the telescope driver.
     */
    public MathPluginManagement(INDITelescope driver) {
        super(driver);
        inMemoryDatabase = new InMemoryDatabase();
        enumeratePlugins();
        for (IMathPlugin mathPlugin : plugins.values()) {
            alignmentSubsystemMathPlugins.newElement().name(mathPlugin.id()).label(mathPlugin.name()).create();
        }
        alignmentSubsystemMathPlugins.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newAlignmentSubsystemMathPluginsValue(elementsAndValues);
            }
        });
        alignmentSubsystemMathPluginInitialise.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newAlignmentSubsystemMathPluginInitialiseValue();
            }
        });
        alignmentSubsystemActive.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newAlignmentSubsystemActiveValue(elementsAndValues);
            }
        });
        alignmentSubsystemCurrentMathPlugin.setEventHandler(new TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                newAlignmentSubsystemCurrentMathPluginValue(elementsAndValues);
            }
        });
    }

    /**
     * Scann the class path after defined math plugins and store them in the
     * map.
     */
    private void enumeratePlugins() {
        plugins = new HashMap<>();
        ServiceLoader<IMathPlugin> loader = ServiceLoader.load(IMathPlugin.class, Thread.currentThread().getContextClassLoader());
        for (IMathPlugin iMathPlugin : loader) {
            plugins.put(iMathPlugin.id(), iMathPlugin);
        }
    }

    /**
     * activation/deactivations switch of the alignement system pressed.
     * 
     * @param elementsAndValues
     *            the new values for the button.
     */
    private void newAlignmentSubsystemActiveValue(INDISwitchElementAndValue[] elementsAndValues) {
        alignmentSubsystemActive.setState(OK);
        alignmentSubsystemActive.setValues(elementsAndValues);

        // Update client
        driver.updateProperty(alignmentSubsystemActive);
    }

    /**
     * The selection of the current math plugin was changed. (But maybe it did
     * not really change)
     * 
     * @param elementsAndValues
     *            the new values for the selected plugin.
     */
    private void newAlignmentSubsystemCurrentMathPluginValue(INDITextElementAndValue[] elementsAndValues) {
        INDISwitchElement currentPlugin = alignmentSubsystemMathPlugins.getOnElement();
        alignmentSubsystemMathPlugins.setState(OK);
        alignmentSubsystemCurrentMathPlugin.setValues(elementsAndValues);
        // in java no difference between buildin and other
        // Unload old plugin if required
        resetPluginIfChanged(currentPlugin);
    }

    /**
     * (re)initialize the current math plugin.
     */
    private void newAlignmentSubsystemMathPluginInitialiseValue() {
        alignmentSubsystemMathPluginInitialise.setState(OK);
        alignmentSubsystemMathPluginInitialise.resetAllSwitches();
        // Update client
        driver.updateProperty(alignmentSubsystemMathPluginInitialise);
        // Initialise or reinitialise the current math plugin
        initialise();
    }

    /**
     * The selection of the current math plugin was changed by the client. (But
     * maybe it did not really change)
     * 
     * @param elementsAndValues
     *            the new values for the selected plugin.
     */
    private void newAlignmentSubsystemMathPluginsValue(INDISwitchElementAndValue[] elementsAndValues) {
        INDISwitchElement currentPlugin = alignmentSubsystemMathPlugins.getOnElement();
        alignmentSubsystemMathPlugins.setValues(elementsAndValues);
        alignmentSubsystemMathPlugins.setState(OK);
        resetPluginIfChanged(currentPlugin);
    }

    /**
     * @return the current math plugin. get it from the map when nessesary.
     */
    private IMathPlugin plugin() {
        if (plugin == null) {
            String message = null;
            plugin = plugins.get(alignmentSubsystemCurrentMathPluginElement.getValue());
            if (plugin != null) {
                plugin.create();
                alignmentSubsystemMathPlugins.setOnlyOneSwitchOn((INDISwitchElement) alignmentSubsystemMathPlugins.getElement(plugin.id()));
            } else {
                message = "MathPluginManagement - cannot load plugin " + alignmentSubsystemCurrentMathPluginElement.getValue() + "\n";
            }
            // Update client
            driver.updateProperty(alignmentSubsystemMathPlugins, message);
        }
        return plugin;
    }

    /**
     * ok, the plugin changed destroy the old one.
     * 
     * @param currentPlugin
     *            the current plugin.
     */
    private void resetPluginIfChanged(INDISwitchElement currentPlugin) {
        INDISwitchElement newPlugin = alignmentSubsystemMathPlugins.getOnElement();

        if (newPlugin != currentPlugin) {
            // New plugin requested
            // Unload old plugin if required
            if (plugin != null) {
                plugin.destroy();
                plugin = null;
            }
            plugin();
        }
    }

    @Override
    public void connect() {
        addProperty(alignmentSubsystemMathPlugins);
        addProperty(alignmentSubsystemMathPluginInitialise);
        addProperty(alignmentSubsystemActive);
    }

    @Override
    public void disconnect() {
        removeProperty(alignmentSubsystemMathPlugins);
        removeProperty(alignmentSubsystemMathPluginInitialise);
        removeProperty(alignmentSubsystemActive);
    }

    /**
     * @return the current mount alignment from the active math plugin.
     */
    public MountAlignment getApproximateAlignment() {
        return plugin().getApproximateAlignment();
    }

    /**
     * initialize the current math plugin.
     * 
     * @return true if successful.
     */
    public boolean initialise() {
        inMemoryDatabase.loadDatabase(driver.getName());
        return plugin().initialise(inMemoryDatabase);
    }

    /**
     * set the mount alignment of the current math plugin.
     * 
     * @param approximateAlignment
     *            the alignment to set.
     */
    public void setApproximateAlignment(MountAlignment approximateAlignment) {
        plugin().setApproximateAlignment(approximateAlignment);
    }

    /**
     * Get the alignment corrected telescope pointing direction for the supplied
     * celestial coordinates.
     * 
     * @param rightAscension
     *            Right Ascension (Decimal Hours).
     * @param declination
     *            Declination (Decimal Degrees).
     * @param julianOffset
     *            to be applied to the current julian date.
     * @param apparentTelescopeDirectionVector
     *            Parameter to receive the corrected telescope direction
     * @return True if successful
     */
    public boolean transformCelestialToTelescope(double rightAscension, double declination, double julianOffset, TelescopeDirectionVector apparentTelescopeDirectionVector) {
        if (alignmentSubsystemActiveElement.isOn()) {
            return plugin().transformCelestialToTelescope(rightAscension, declination, julianOffset, apparentTelescopeDirectionVector);
        } else {
            return false;
        }
    }

    /**
     * Get the true celestial coordinates for the supplied telescope pointing
     * direction.
     * 
     * @param apparentTelescopeDirectionVector
     *            the telescope direction
     * @param julianOffset
     *            to be applied to the current julian date.
     * @param rightAscension
     *            Parameter to receive the Right Ascension (Decimal Hours).
     * @param declination
     *            Parameter to receive the Declination (Decimal Degrees).
     * @return True if successful
     */
    public boolean transformTelescopeToCelestial(TelescopeDirectionVector apparentTelescopeDirectionVector, double julianOffset, DoubleRef rightAscension,
            DoubleRef declination) {
        if (alignmentSubsystemActiveElement.isOn()) {
            return plugin().transformTelescopeToCelestial(apparentTelescopeDirectionVector, julianOffset, rightAscension, declination);
        } else {
            return false;
        }
    }

    /**
     * set the location coordinates in the database.
     * 
     * @param lat
     *            the latitude.
     * @param lng
     *            the longtitude.
     */
    public void setDatabaseReferencePosition(double lat, double lng) {
        inMemoryDatabase.setDatabaseReferencePosition(lat, lng);
    }

    /**
     * force the alignment system active. use this method in the constructor of
     * your driver if your telescope driver depends on the alignment system to
     * be active.
     */
    public void forceActive() {
        alignmentSubsystemActiveElement.setOn();
        alignmentSubsystemActive.setPermission(PropertyPermissions.RO);
        alignmentSubsystemActive.setState(OK);
        driver.updateProperty(alignmentSubsystemActive);
    }

    /**
     * add the database entry to the database and reinitialize.
     * 
     * @param entry
     *            the entry to add to the database
     */
    public void add(AlignmentDatabaseEntry entry) {
        inMemoryDatabase.getAlignmentDatabase().add(entry);
        plugin.initialise(inMemoryDatabase);
    }

}
