package org.tuvarna.smartdeliveryplatform.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = OrderPlacementValidator.class)
@Documented
public @interface ValidOrderPlacement {
    String message() default "Delivery address is incomplete.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
