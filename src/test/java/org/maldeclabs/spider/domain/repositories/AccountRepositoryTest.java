package org.maldeclabs.spider.domain.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.maldeclabs.spider.domain.entities.Account;
import org.maldeclabs.spider.domain.entities.EmailConfirmation;
import org.maldeclabs.spider.application.services.AccountService;
import org.maldeclabs.spider.domain.enums.AccountRole;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Random;



@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("find by E-mail [case1] -> Must return the correct account with the email address provided")
    void findByEmailCase1() {
        String email = "jonhdoe@mail.com";
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );
        Account expectedAccount = new Account("john Doe","johnD",  email, "password", AccountRole.FREE, emailConfirmation);

        Mockito.when(repository.findByEmail(email)).thenReturn(expectedAccount);
        Account actualAccount = accountService.findByEmail(email);

        Assertions.assertEquals(expectedAccount, actualAccount);
    }

    @Test
    @DisplayName("find by E-mail [case2] -> Should return null if the email is not registered")
    void findByEmailCase2() {
        String nonExistingEmail = "nonexistent@example.com";

        Mockito.when(repository.findByEmail(nonExistingEmail)).thenReturn(null);
        Account actualAccount = accountService.findByEmail(nonExistingEmail);

        Assertions.assertNull(actualAccount);
    }
}