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
import org.indilib.i4j.driver.INDIDriverExtention;
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

public class INDIPropertyInjector {

    private static ThreadLocal<INDIPropertyInjector> current = new ThreadLocal<INDIPropertyInjector>();

    private Map<String, INDIProperty<?>> properties = new HashMap<String, INDIProperty<?>>();

    private static Logger LOG = Logger.getLogger(INDIPropertyInjector.class.getName());;

    private final INDIDriver driver;

    private INDIProperty<?> lastProperty = null;

    private INDIElement lastElement = null;

    private String currentGroup;

    private String currentPrefix;

    private Rename[] currentRenamings;

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

    private Object getFieldValue(Object object, Field field) {
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not set indi element", e);
        }
    }

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

    private void initializeAnnotatedElement(Object instance, Field field) {
        InjectElement elem = field.getAnnotation(InjectElement.class);
        if (elem != null) {
            INDIProperty<?> lastProperty = this.lastProperty;
            lastProperty = findNamedProperty(elem.property(), lastProperty);
            if (lastProperty != null) {
                if (INDINumberElement.class.isAssignableFrom(field.getType())) {
                    lastElement =
                            new INDINumberElement((INDINumberProperty) lastProperty, elem.name(), elem.label(), elem.valueD(), elem.minimumD(), elem.maximumD(), elem.stepD(),
                                    elem.numberFormat());
                } else if (INDITextElement.class.isAssignableFrom(field.getType())) {
                    lastElement = new INDITextElement((INDITextProperty) lastProperty, elem.name(), elem.label(), elem.valueT());
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

    private void initializeAnnotatedProperties(Object instance) {
        initializeAnnotatedClass(instance, instance.getClass());
    }

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

    private void initializeDriverExtention(Object instance, Field field) {
        if (INDIDriverExtention.class.isAssignableFrom(field.getType())) {
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
                INDIDriverExtention<?> driverExtention = instanciateDriverExtention(instance, field);
                setFieldValue(instance, field, driverExtention);
            } finally {
                currentGroup = oldValue;
                currentPrefix = oldPrefix;
                currentRenamings = oldRenamings;
            }
        }
    }

    private INDIDriverExtention<?> instanciateDriverExtention(Object instance, Field field) {
        INDIDriverExtention<?> driverExtention = null;
        try {
            driverExtention = (INDIDriverExtention<?>) getFieldValue(instance, field);
            if (driverExtention == null) {
                for (Constructor<?> constructor : field.getType().getConstructors()) {
                    if (constructor.getParameterTypes().length == 1 && INDIDriver.class.isAssignableFrom(constructor.getParameterTypes()[0])) {
                        driverExtention = (INDIDriverExtention<?>) constructor.newInstance(driver);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not instanciate Driver extention", e);
        }
        return driverExtention;
    }

    private void setFieldValue(Object object, Field field, Object fieldValue) {
        field.setAccessible(true);
        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not set indi element", e);
        }
    }

}
