package laazotea.indi.driver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import laazotea.indi.Constants.LightStates;
import laazotea.indi.Constants.SwitchStatus;

@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.FIELD
})
public @interface InjectElement {

    String name();

    String label();

    double valueD() default 0d;

    String valueT() default "";

    double minimumD() default 0d;

    double maximumD() default 0d;

    double stepD() default 0d;

    String numberFormat() default "%g";

    SwitchStatus switchValue() default SwitchStatus.OFF;

    String property() default "";

    LightStates state() default LightStates.IDLE;
}
