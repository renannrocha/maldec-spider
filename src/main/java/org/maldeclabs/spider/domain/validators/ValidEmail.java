package org.maldeclabs.spider.domain.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.maldeclabs.spider.domain.validators.constraintValidation.EmailFormatValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = EmailFormatValidator.class)
public @interface ValidEmail {

    String message() default "The email address provided is invalid.";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
