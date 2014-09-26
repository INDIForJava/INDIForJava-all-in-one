package laazotea.indi.driver.telescope.alignment;

import static laazotea.indi.Constants.PropertyPermissions.RW;
import static laazotea.indi.Constants.PropertyStates.IDLE;
import static laazotea.indi.Constants.PropertyStates.OK;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIException;
import laazotea.indi.driver.INDISwitchElement;
import laazotea.indi.driver.INDISwitchElementAndValue;
import laazotea.indi.driver.INDISwitchProperty;
import laazotea.indi.driver.INDITextElement;
import laazotea.indi.driver.INDITextElementAndValue;
import laazotea.indi.driver.INDITextProperty;
import laazotea.indi.driver.telescope.INDITelescope;

public class MathPluginManagement {

    private final static String ALIGNMENT_TAB = "Alignment";

    private final INDITelescope driver;

    private Map<String, IMathPlugin> plugins;

    private BuiltInMathPlugin builtInMathPlugin = new BuiltInMathPlugin();

    private INDISwitchProperty alignmentSubsystemMathPlugins;

    private INDISwitchProperty alignmentSubsystemMathPluginInitialise;

    private INDISwitchProperty alignmentSubsystemActive;

    private INDITextProperty alignmentSubsystemCurrentMathPlugin;

    private INDITextElement alignmentSubsystemCurrentMathPluginElement;

    private IMathPlugin plugin;

    private INDISwitchElement alignmentSubsystemMathPluginInitialiseElement;

    private InMemoryDatabase inMemoryDatabase;

    public MathPluginManagement(INDITelescope driver) {
        this.driver = driver;
        enumeratePlugins();
        this.alignmentSubsystemMathPlugins =
                new INDISwitchProperty(driver, "ALIGNMENT_SUBSYSTEM_MATH_PLUGINS", "Math Plugins", ALIGNMENT_TAB, IDLE, RW, 60,
                        laazotea.indi.Constants.SwitchRules.ONE_OF_MANY);
        new INDISwitchElement(this.alignmentSubsystemMathPlugins, builtInMathPlugin.id(), builtInMathPlugin.name(), SwitchStatus.ON);
        for (IMathPlugin mathPlugin : plugins.values()) {
            new INDISwitchElement(this.alignmentSubsystemMathPlugins, mathPlugin.id(), mathPlugin.name(), SwitchStatus.OFF);
        }
        driver.addProperty(this.alignmentSubsystemMathPlugins);

        this.alignmentSubsystemMathPluginInitialise =
                new INDISwitchProperty(driver, "ALIGNMENT_SUBSYSTEM_MATH_PLUGIN_INITIALISE", "(Re)Initialise Plugin", ALIGNMENT_TAB, IDLE, RW, 60,
                        laazotea.indi.Constants.SwitchRules.AT_MOST_ONE);
        this.alignmentSubsystemMathPluginInitialiseElement =
                new INDISwitchElement(this.alignmentSubsystemMathPluginInitialise, "ALIGNMENT_SUBSYSTEM_MATH_PLUGIN_INITIALISE", "OK", SwitchStatus.OFF);
        driver.addProperty(this.alignmentSubsystemMathPluginInitialise);

        this.alignmentSubsystemActive =
                new INDISwitchProperty(driver, "ALIGNMENT_SUBSYSTEM_ACTIVE", "Activate alignment subsystem", ALIGNMENT_TAB, IDLE, RW, 60,
                        laazotea.indi.Constants.SwitchRules.AT_MOST_ONE);
        new INDISwitchElement(this.alignmentSubsystemActive, "ALIGNMENT SUBSYSTEM ACTIVE", "Alignment Subsystem Active", SwitchStatus.OFF);
        driver.addProperty(this.alignmentSubsystemActive);

        // The following property is used for configuration purposes only and is
        // not exposed to the client.
        this.alignmentSubsystemCurrentMathPlugin =
                new INDITextProperty(driver, "ALIGNMENT_SUBSYSTEM_CURRENT_MATH_PLUGIN", "Current Math Plugin", ALIGNMENT_TAB, IDLE, PropertyPermissions.RO, 60);
        this.alignmentSubsystemCurrentMathPluginElement =
                new INDITextElement(this.alignmentSubsystemCurrentMathPlugin, "ALIGNMENT_SUBSYSTEM_CURRENT_MATH_PLUGIN", "Current Math Plugin", builtInMathPlugin.id());

    }

    private void enumeratePlugins() {
        plugins = new HashMap<>();
        ServiceLoader<IMathPlugin> loader = ServiceLoader.load(IMathPlugin.class, Thread.currentThread().getContextClassLoader());
        Iterator<IMathPlugin> iter = loader.iterator();
        while (iter.hasNext()) {
            IMathPlugin iMathPlugin = (IMathPlugin) iter.next();
            plugins.put(iMathPlugin.id(), iMathPlugin);
        }
    }

    public void processNewTextValue(INDITextProperty property, Date date, INDITextElementAndValue[] elementsAndValues) {
        if (alignmentSubsystemCurrentMathPlugin == property) {
            alignmentSubsystemMathPlugins.setState(OK);
            property.setValues(elementsAndValues);
            // in java no difference between buildin and other
            // Unload old plugin if required
            if (plugin != null) {
                plugin.destroy();
                plugin = null;
            }
            // It is not the built in so try to load it
            String message = null;
            if ((plugin = plugins.get(alignmentSubsystemCurrentMathPluginElement.getValue())) != null) {
                plugin.create();
                alignmentSubsystemMathPlugins.setOnlyOneSwitchOn(alignmentSubsystemMathPlugins.getElement(plugin.id()));
            } else {
                message = "MathPluginManagement - cannot load plugin " + alignmentSubsystemCurrentMathPluginElement.getValue() + "\n";
            }
            try {
                driver.updateProperty(alignmentSubsystemMathPlugins, message);
            } catch (INDIException e) {
            }
        }
    }

    public void processNewSwitchValue(INDISwitchProperty property, Date date, INDISwitchElementAndValue[] elementsAndValues) {
        if (alignmentSubsystemMathPlugins == property) {
            INDISwitchElement currentPlugin = alignmentSubsystemMathPlugins.getOnElement();
            property.setValues(elementsAndValues);
            alignmentSubsystemMathPlugins.setState(OK);
            INDISwitchElement newPlugin = alignmentSubsystemMathPlugins.getOnElement();

            if (newPlugin != currentPlugin) {
                // New plugin requested
                // Unload old plugin if required
                if (plugin != null) {
                    plugin.destroy();
                    plugin = null;
                }
                // It is not the built in so try to load it
                String message = null;
                if ((plugin = plugins.get(alignmentSubsystemCurrentMathPluginElement.getValue())) != null) {
                    plugin.create();
                    alignmentSubsystemMathPlugins.setOnlyOneSwitchOn(alignmentSubsystemMathPlugins.getElement(plugin.id()));
                } else {
                    message = "MathPluginManagement - cannot load plugin " + alignmentSubsystemCurrentMathPluginElement.getValue() + "\n";
                }
                // Update client
                try {
                    driver.updateProperty(alignmentSubsystemMathPlugins, message);
                } catch (INDIException e) {
                }
            }
        } else if (alignmentSubsystemMathPluginInitialise == property) {
            alignmentSubsystemMathPluginInitialise.setState(OK);
            alignmentSubsystemMathPluginInitialise.reset();
            // Update client
            try {
                driver.updateProperty(property);
            } catch (INDIException e) {
            }

            // Initialise or reinitialise the current math plugin
            initialise();
        } else if (alignmentSubsystemActive == property) {
            alignmentSubsystemActive.setState(OK);
            property.setValues(elementsAndValues);

            // Update client
            try {
                driver.updateProperty(property);
            } catch (INDIException e) {
            }
        }
    }

    // These must match the function signatures in MathPlugin

    public MountAlignment getApproximateMountAlignment() {
        return plugin.getApproximateMountAlignment();
    }

    public boolean initialise() {
        return plugin.initialise(this.inMemoryDatabase);
    }

    public void setApproximateMountAlignment(MountAlignment approximateAlignment) {
        plugin.setApproximateMountAlignment(this.inMemoryDatabase);
    }

    private double rightAscension;

    private double declination;

    public boolean transformCelestialToTelescope(double rightAscension, double declination, double julianOffset, TelescopeDirectionVector apparentTelescopeDirectionVector) {
        if (alignmentSubsystemActive.firstElement().getValue() == SwitchStatus.ON)
            return plugin.transformCelestialToTelescope(this.rightAscension = rightAscension, this.declination = declination, julianOffset, apparentTelescopeDirectionVector);
        else
            return false;
    }

    private TelescopeDirectionVector apparentTelescopeDirectionVector;

    public boolean transformTelescopeToCelestial(TelescopeDirectionVector ApparentTelescopeDirectionVector, DoubleRef rightAscension, DoubleRef declination) {
        if (alignmentSubsystemActive.firstElement().getValue() == SwitchStatus.ON)
            return plugin.transformTelescopeToCelestial(this.apparentTelescopeDirectionVector = apparentTelescopeDirectionVector, rightAscension, declination);
        else
            return false;
    }

}
