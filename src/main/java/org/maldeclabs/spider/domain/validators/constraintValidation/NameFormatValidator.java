package org.maldeclabs.spider.domain.validators.constraintValidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.maldeclabs.spider.domain.validators.ValidName;
import org.springframework.stereotype.Component;

@Component
public class NameFormatValidator implements ConstraintValidator<ValidName, String> {
    private static final String NAME_REGEX = "^[a-zA-Z ]*$";

    @Override
    public void initialize(ValidName constraintAnnotation) {
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The name field cannot be empty")
                    .addConstraintViolation();
            return false;
        }

        if(!name.matches(NAME_REGEX)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The name provided is not supported")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
