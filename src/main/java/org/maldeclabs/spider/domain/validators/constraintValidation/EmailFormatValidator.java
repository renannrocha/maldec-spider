package org.maldeclabs.spider.domain.validators.constraintValidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.maldeclabs.spider.domain.repositories.AccountRepository;
import org.maldeclabs.spider.domain.validators.ValidEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailFormatValidator implements ConstraintValidator<ValidEmail, String> {

    // Definindo a expressão regular para o formato geral do e-mail
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    // Definindo os domínios permitidos
    private static final String DOMAIN_REGEX = "^(gmail.com|hotmail.com|yahoo.com|outlook.com|protonmail.com|proton.me)$";

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {

        if (email == null || email.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The email field can't be empty")
                    .addConstraintViolation();
            return false;
        }

        if (!email.matches(EMAIL_REGEX)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The email address is not supported")
                    .addConstraintViolation();
            return false;
        }

        String domain = email.substring(email.lastIndexOf('@') + 1);
        if (!domain.matches(DOMAIN_REGEX)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The email domain is not supported")
                    .addConstraintViolation();
            return false;
        }

        if(accountRepository.existsByEmail(email)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The email provided is already in use")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}