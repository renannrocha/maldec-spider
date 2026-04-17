package org.maldeclabs.spider.domain.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.maldeclabs.spider.domain.entities.EmailConfirmation;
import org.maldeclabs.spider.application.services.EmailConfirmationService;
import org.maldeclabs.spider.application.services.exceptions.ResourceNotFoundException;
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
class EmailConfirmationRepositoryTest {

    @Mock
    private EmailConfirmationRepository repository;

    @InjectMocks
    private EmailConfirmationService emailConfirmationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("find by code [case1] -> Should return the correct email confirmation with the code provided")
    void findByCodeCase1() {
        String code = "1234";
        EmailConfirmation expectedConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );
        expectedConfirmation.setCode(code);

        Mockito.when(repository.findByCode(code)).thenReturn(expectedConfirmation);
        EmailConfirmation actualConfirmation = emailConfirmationService.findByCode(code);

        Assertions.assertEquals(expectedConfirmation, actualConfirmation);
    }

    @Test
    @DisplayName("find by code [case2] -> Must throw ResourceNotFoundException if the code is not registered")
    void findByCodeCase2() {
        String nonExistingCode = "9876";

        Mockito.when(repository.findByCode(nonExistingCode)).thenReturn(null);

        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> emailConfirmationService.findByCode(nonExistingCode)
        );
    }
}