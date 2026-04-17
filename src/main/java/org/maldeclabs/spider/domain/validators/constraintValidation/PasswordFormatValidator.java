package org.maldeclabs.spider.domain.validators.constraintValidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.maldeclabs.spider.domain.validators.ValidPassword;
import org.springframework.stereotype.Component;

@Component
public class PasswordFormatValidator implements ConstraintValidator<ValidPassword, String> {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String PASSWORD_REGEX = "^[a-zA-Z0-9._@#-]*$";
    private static final String PASSWORD_FORMAT_REGEX = "^(?=.*[A-Z])(?=.*[._@#-]).*$";


    @Override
    public void initialize(ValidPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext context) {
        if (s == null || s.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The password field cannot be empty")
                    .addConstraintViolation();
            return false;
        }

        if (s.length() < MIN_PASSWORD_LENGTH) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The password must be at least " + MIN_PASSWORD_LENGTH + " characters long")
                    .addConstraintViolation();
            return false;
        }

        if (containsSequentialNumbers(s)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The password cannot contain sequential numbers (e.g., 123, 456).")
                    .addConstraintViolation();
            return false;
        }

        if(!s.matches(PASSWORD_FORMAT_REGEX)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The password must contain at least one uppercase letter and one special character")
                    .addConstraintViolation();
            return false;
        }

        if(!s.matches(PASSWORD_REGEX)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The password must contain only letters, numbers, and symbols. _ @ # -")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean containsSequentialNumbers(String s) {
        int sequentialCount = 1;

        for (int i = 1; i < s.length(); i++) {
            char prevChar = s.charAt(i - 1);
            char currChar = s.charAt(i);

            if (Character.isDigit(prevChar) && Character.isDigit(currChar)) {
                if (currChar - prevChar == 1) {
                    sequentialCount++;
                    if (sequentialCount >= 3) {
                        return true;
                    }
                } else {
                    sequentialCount = 1;
                }
            } else {
                sequentialCount = 1;
            }
        }
        return false;
    }
}
