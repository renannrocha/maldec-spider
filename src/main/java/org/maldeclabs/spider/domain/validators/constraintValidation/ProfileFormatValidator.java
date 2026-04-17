package org.maldeclabs.spider.domain.validators.constraintValidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.maldeclabs.spider.domain.repositories.AccountRepository;
import org.maldeclabs.spider.domain.validators.ValidProfile;
import org.springframework.beans.factory.annotation.Autowired;

public class ProfileFormatValidator implements ConstraintValidator<ValidProfile, String> {
    private static final String PROFILE_REGEX = "^[a-zA-Z0-9]*$";

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public void initialize(ValidProfile constraintAnnotation) {
    }

    @Override
    public boolean isValid(String profile, ConstraintValidatorContext context) {
        if (profile == null || profile.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The profile field can't be empty")
                    .addConstraintViolation();
            return false;
        }

        if(!profile.matches(PROFILE_REGEX)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The profile provided is not supported")
                    .addConstraintViolation();
            return false;
        }

        if(accountRepository.existsByProfile(profile)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The profile provided is already in use")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
