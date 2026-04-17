package org.maldeclabs.spider.domain.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.maldeclabs.spider.domain.validators.constraintValidation.ProfileFormatValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = ProfileFormatValidator.class)
public @interface ValidProfile {
    String message() default "The profile provided is invalid";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
