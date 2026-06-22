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
@Constraint(validatedBy = RegistrationAddressValidator.class)
@Documented
public @interface ValidRegistrationAddress {
    String message() default "Address is incomplete.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
