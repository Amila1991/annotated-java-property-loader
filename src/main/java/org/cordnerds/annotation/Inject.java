package org.cordnerds.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * @author Amila Karunathilaka
 */

@Retention(RetentionPolicy.SOURCE)
@Target(value = {FIELD, METHOD, CONSTRUCTOR})
public @interface Inject {
}
