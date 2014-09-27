package laazotea.indi.driver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) 
@Target({ElementType.FIELD})     
public @interface INDIe {

    String name() default "";

    String label() default "";

    double valueD() default 0d;

    double minimumD() default 0d;

    double maximumD() default 0d;

    double stepD() default 0d;

    String numberFormat() default "";

}
