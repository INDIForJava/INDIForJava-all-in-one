package laazotea.indi.driver.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import laazotea.indi.driver.INDIDriver;
import laazotea.indi.driver.INDIDriverExtention;
import laazotea.indi.driver.INDIElement;
import laazotea.indi.driver.INDINumberElement;
import laazotea.indi.driver.INDINumberProperty;
import laazotea.indi.driver.INDIProperty;
import laazotea.indi.driver.INDISwitchElement;
import laazotea.indi.driver.INDISwitchProperty;
import laazotea.indi.driver.INDITextElement;
import laazotea.indi.driver.INDITextProperty;
import laazotea.indi.driver.annotation.INDIe;
import laazotea.indi.driver.annotation.INDIp;

public class AnnotatedFieldInitializer {

    private static ThreadLocal<AnnotatedFieldInitializer> current = new ThreadLocal<AnnotatedFieldInitializer>();

    private Map<String, INDIProperty<?>> properties = new HashMap<String, INDIProperty<?>>();

    private static Logger LOG = Logger.getLogger(AnnotatedFieldInitializer.class.getName());;

    private final INDIDriver driver;

    private INDIProperty<?> lastProperty = null;

    private INDIElement lastElement = null;

    public static void initialize(INDIDriver driver, Object object) {
        AnnotatedFieldInitializer original = current.get();
        try {
            AnnotatedFieldInitializer running = original;
            if (running == null) {
                running = new AnnotatedFieldInitializer(driver);
                current.set(running);
            }
            running.initializeAnnotatedProperties(object);
        } finally {
            if (original == null) {
                current.remove();
            }
        }
    }
    
    private AnnotatedFieldInitializer(INDIDriver driver) {
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
        INDIe elem = field.getAnnotation(INDIe.class);
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
        INDIp prop = field.getAnnotation(INDIp.class);
        if (prop != null) {
            if (INDINumberProperty.class.isAssignableFrom(field.getType())) {
                lastProperty = new INDINumberProperty(driver, prop.name(), prop.label(), prop.group(), prop.state(), prop.permission(), prop.timeout());
            } else if (INDITextProperty.class.isAssignableFrom(field.getType())) {
                lastProperty = new INDITextProperty(driver, prop.name(), prop.label(), prop.group(), prop.state(), prop.permission(), prop.timeout());
            } else if (INDISwitchProperty.class.isAssignableFrom(field.getType())) {
                lastProperty = new INDISwitchProperty(driver, prop.name(), prop.label(), prop.group(), prop.state(), prop.permission(), prop.timeout(), prop.switchRule());
            }
            if (prop.saveable()) {
                lastProperty.setSaveable(true);
            }
            properties.put(lastProperty.getName(), lastProperty);
            setFieldValue(instance, field, lastProperty);
        }
    }

    private void initializeDriverExtention(Object instance, Field field) {
        if (INDIDriverExtention.class.isAssignableFrom(field.getType())) {
            INDIDriverExtention<?> driverExtention = instanciateDriverExtention(instance, field);
            setFieldValue(instance, field, driverExtention);
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
