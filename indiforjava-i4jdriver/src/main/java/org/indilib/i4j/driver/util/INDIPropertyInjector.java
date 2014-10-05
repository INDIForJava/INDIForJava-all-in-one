/*
 *  This file is part of INDI for Java Driver.
 * 
 *  INDI for Java Driver is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Driver is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package org.indilib.i4j.driver.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.indilib.i4j.driver.INDIBLOBElement;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDIElement;
import org.indilib.i4j.driver.INDILightElement;
import org.indilib.i4j.driver.INDILightProperty;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDIProperty;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectExtention;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.annotation.Rename;

/**
 * This is the INDI field injector it is responsible for interpreting the
 * specified annotations on a field in a driver or in a driver extensions.
 * depending on the type of the field and the specified annotations it will
 * decide which property element or extension to instantiate.<br/>
 * <br/>
 * The injection is done field by field and top down. So first the superclass
 * fields are injected and then the subclass fields. The fields are injected in
 * the order defined in the class. The order is relevant because elements are
 * injected in the first preceding property
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIPropertyInjector {

    /**
     * During the injection process it is importent to keep the context, so that
     * when we are in a extention and a property of a driver is referenced wi
     * can find where it is. This is done by storeing the context in a
     * threadlocal var that will be emptied as soon as de the driver is
     * instanciated.
     */
    private static ThreadLocal<INDIPropertyInjector> current = new ThreadLocal<INDIPropertyInjector>();

    /**
     * current map of properties already injected in the driver or its
     * extensions.
     */
    private Map<String, INDIProperty<?>> properties = new HashMap<String, INDIProperty<?>>();

    /**
     * Logger to log errors to.
     */
    private static Logger LOG = Logger.getLogger(INDIPropertyInjector.class.getName());;

    /**
     * the current driver that is being injected.
     */
    private final INDIDriver driver;

    /**
     * the last created property, used for all following elements.
     */
    private INDIProperty<?> lastProperty = null;

    /**
     * the current group if this is set all empty groups following will be set
     * to this group.
     */
    private String currentGroup;

    /**
     * the current prefix, this prefix will be added for every property or
     * element name.
     */
    private String currentPrefix;

    /**
     * the currently active renaming this is used to rename special fields
     * inside an extension.
     */
    private Rename[] currentRenamings;

    /**
     * Inject all property/element and extension fields in the object
     * recursively.
     * 
     * @param driver
     *            the current driver instance
     * @param object
     *            the current injection object -> can be a driver or an
     *            extension
     */
    public static void initialize(INDIDriver driver, Object object) {
        INDIPropertyInjector original = current.get();
        try {
            INDIPropertyInjector running = original;
            if (running == null) {
                running = new INDIPropertyInjector(driver);
                current.set(running);
            }
            running.initializeAnnotatedProperties(object);
        } finally {
            if (original == null) {
                current.remove();
            }
        }
    }

    /**
     * Constructor with driver. this is private because it should only used
     * Internally.
     * 
     * @param driver
     *            the inidriver
     */
    private INDIPropertyInjector(INDIDriver driver) {
        this.driver = driver;
    }

    private INDIProperty<?> findNamedProperty(String name, INDIProperty<?> lastProperty) {
        if (!name.isEmpty()) {
            INDIProperty<?> property = properties.get(name);
            if (property != null) {
                return property;
            }
        }
        return lastProperty;
    }

    /**
     * get the value of a field by reflection.
     * 
     * @param object
     *            the object to get the field from
     * @param field
     *            the field
     * @return the value of the field in the specified object
     */
    private Object getFieldValue(Object object, Field field) {
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not set indi element", e);
        }
    }

    /**
     * Now we process the hirachie top to bottom (that's why the recursion is
     * first and than the processing.
     * 
     * @param instance
     *            the instance to inject
     * @param clazz
     *            the current class.
     */
    private void initializeAnnotatedClass(Object instance, Class<?> clazz) {
        if (clazz != null) {
            initializeAnnotatedClass(instance, clazz.getSuperclass());
            for (Field field : clazz.getDeclaredFields()) {
                initializeDriverExtention(instance, field);
                initializeAnnotatedProperty(instance, field);
                initializeAnnotatedElement(instance, field);
            }
        }
    }

    /**
     * inject the field value of the instance, if it is annotated with an
     * InjectElement annotation. select the appropriate value from the type and
     * the annotations.
     * 
     * @param instance
     *            the instance to fill
     * @param field
     *            the current field.
     */
    private void initializeAnnotatedElement(Object instance, Field field) {
        InjectElement elem = field.getAnnotation(InjectElement.class);
        if (elem != null) {
            INDIProperty<?> lastProperty = this.lastProperty;
            lastProperty = findNamedProperty(elem.property(), lastProperty);
            if (lastProperty != null) {
                INDIElement lastElement = null;
                if (INDINumberElement.class.isAssignableFrom(field.getType())) {
                    lastElement =
                            new INDINumberElement((INDINumberProperty) lastProperty, elem.name(), elem.label(), elem.numberValue(), elem.minimum(), elem.maximum(), elem.step(),
                                    elem.numberFormat());
                } else if (INDITextElement.class.isAssignableFrom(field.getType())) {
                    lastElement = new INDITextElement((INDITextProperty) lastProperty, elem.name(), elem.label(), elem.textValue());
                } else if (INDISwitchElement.class.isAssignableFrom(field.getType())) {
                    lastElement = new INDISwitchElement((INDISwitchProperty) lastProperty, elem.name(), elem.label(), elem.switchValue());
                } else if (INDIBLOBElement.class.isAssignableFrom(field.getType())) {
                    lastElement = new INDIBLOBElement((INDIBLOBProperty) lastProperty, elem.name(), elem.label());
                } else if (INDILightElement.class.isAssignableFrom(field.getType())) {
                    lastElement = new INDILightElement((INDILightProperty) lastProperty, elem.name(), elem.label(), elem.state());
                } else {
                    LOG.log(Level.SEVERE, "Unknown property type" + elem.property() + " for element " + field);
                }
                setFieldValue(instance, field, lastElement);
            } else {
                LOG.log(Level.SEVERE, "could not find property " + elem.property() + " for element " + field);
            }
        }
    }

    /**
     * start method for the recursion, fill the object instance top down.
     * 
     * @param instance
     *            the instance to fill.
     */
    private void initializeAnnotatedProperties(Object instance) {
        initializeAnnotatedClass(instance, instance.getClass());
    }

    /**
     * inject the field value of the instance, if it is annotated with an
     * InjectProperty annotation. select the appropriate value from the type and
     * the annotations.
     * 
     * @param instance
     *            the instance to fill
     * @param field
     *            the current field.
     */
    private void initializeAnnotatedProperty(Object instance, Field field) {
        InjectProperty prop = field.getAnnotation(InjectProperty.class);
        if (prop != null) {
            String group = prop.group();
            if (group.isEmpty() && currentGroup != null) {
                group = currentGroup;
            }
            String name = rename(prop.name());
            if (INDINumberProperty.class.isAssignableFrom(field.getType())) {
                lastProperty = new INDINumberProperty(driver, name, prop.label(), group, prop.state(), prop.permission(), prop.timeout());
            } else if (INDITextProperty.class.isAssignableFrom(field.getType())) {
                lastProperty = new INDITextProperty(driver, name, prop.label(), group, prop.state(), prop.permission(), prop.timeout());
            } else if (INDISwitchProperty.class.isAssignableFrom(field.getType())) {
                lastProperty = new INDISwitchProperty(driver, name, prop.label(), group, prop.state(), prop.permission(), prop.timeout(), prop.switchRule());
            } else if (INDIBLOBProperty.class.isAssignableFrom(field.getType())) {
                lastProperty = new INDIBLOBProperty(driver, name, prop.label(), group, prop.state(), prop.permission(), prop.timeout());
            } else if (INDILightProperty.class.isAssignableFrom(field.getType())) {
                lastProperty = new INDILightProperty(driver, name, prop.label(), group, prop.state());
            } else {
                LOG.log(Level.SEVERE, "Unknown property type for element " + field);
            }
            if (prop.saveable()) {
                lastProperty.setSaveable(true);
            }
            properties.put(lastProperty.getName(), lastProperty);
            setFieldValue(instance, field, lastProperty);
        }
    }

    /**
     * Apply any defined renaming to the name.
     * 
     * @param name
     *            the name to start with
     * @return the name changed with prefix and renamings
     */
    private String rename(String name) {
        if (currentRenamings != null) {
            for (Rename rename : currentRenamings) {
                if (rename.name().equals(name)) {
                    return rename.to();
                }
            }
        }
        if (currentPrefix != null) {
            return currentPrefix + name;
        }
        return name;
    }

    /**
     * If the field is a driver extension, the context is set for the injection
     * of the extension, after construction the context is reset.
     * 
     * @param instance
     *            the instance in which the extension will be injected
     * @param field
     *            the field that specifies the extension.
     */
    private void initializeDriverExtention(Object instance, Field field) {
        if (INDIDriverExtension.class.isAssignableFrom(field.getType())) {
            InjectExtention extentionAnnot = field.getAnnotation(InjectExtention.class);
            String oldValue = currentGroup;
            String oldPrefix = currentGroup;
            Rename[] oldRenamings = currentRenamings;
            try {
                if (extentionAnnot != null) {
                    if (!extentionAnnot.group().isEmpty()) {
                        currentGroup = extentionAnnot.group();
                    }
                    if (!extentionAnnot.prefix().isEmpty()) {
                        currentPrefix = extentionAnnot.prefix();
                    }
                    if (extentionAnnot.rename().length > 0) {
                        currentRenamings = extentionAnnot.rename();
                    }
                }
                INDIDriverExtension<?> driverExtention = instanciateDriverExtention(instance, field);
                setFieldValue(instance, field, driverExtention);
            } finally {
                currentGroup = oldValue;
                currentPrefix = oldPrefix;
                currentRenamings = oldRenamings;
            }
        }
    }

    /**
     * search the constructor that has a driver as a parameter and call it.
     * 
     * @param instance
     *            the instance in which the extension will be injected
     * @param field
     *            the field that specifies the extension.
     * @return the newly instantiated extension or the existing one if it was
     *         already set
     */
    private INDIDriverExtension<?> instanciateDriverExtention(Object instance, Field field) {
        INDIDriverExtension<?> driverExtention = null;
        try {
            driverExtention = (INDIDriverExtension<?>) getFieldValue(instance, field);
            if (driverExtention == null) {
                for (Constructor<?> constructor : field.getType().getConstructors()) {
                    if (constructor.getParameterTypes().length == 1 && INDIDriver.class.isAssignableFrom(constructor.getParameterTypes()[0])) {
                        driverExtention = (INDIDriverExtension<?>) constructor.newInstance(driver);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not instanciate Driver extention", e);
        }
        return driverExtention;
    }

    /**
     * set the value of a field by reflection.
     * 
     * @param object
     *            the object defining the field
     * @param field
     *            the field to set
     * @param fieldValue
     *            the value to set the field to
     */
    private void setFieldValue(Object object, Field field, Object fieldValue) {
        field.setAccessible(true);
        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not set indi element", e);
        }
    }

}
