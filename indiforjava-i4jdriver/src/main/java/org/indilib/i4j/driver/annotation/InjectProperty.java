package org.indilib.i4j.driver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;

@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.FIELD
})
public @interface InjectProperty {

    PropertyPermissions permission() default PropertyPermissions.RW;

    int timeout() default 60;

    String name() ;

    String label() ;

    PropertyStates state() default PropertyStates.IDLE;

    String group() default "";

    boolean saveable() default false;

    SwitchRules switchRule() default SwitchRules.ONE_OF_MANY; 

}
