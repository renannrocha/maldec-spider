package org.maldeclabs.spider.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.maldeclabs.spider.application.services.EmailConfirmationService;
import org.maldeclabs.spider.domain.entities.EmailConfirmation;
import org.maldeclabs.spider.domain.repositories.EmailConfirmationRepository;
import org.maldeclabs.spider.application.services.exceptions.DatabaseException;
import org.maldeclabs.spider.application.services.exceptions.ResourceNotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Random;


@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class EmailConfirmationServiceTest {

    @InjectMocks
    private EmailConfirmationService emailConfirmationService;

    private MimeMessage mimeMessage;

    @Mock
    private EmailConfirmationRepository repository;

    @Mock
    private JavaMailSender mailSender;


    @Test
    @DisplayName("insert [case1] -> must enter the email confirmation information successfully into the database")
    void insertCase1() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                false,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );
        Mockito.when(repository.save(emailConfirmation)).thenReturn(emailConfirmation);
        EmailConfirmation result = emailConfirmationService.insert(emailConfirmation);

        Assertions.assertEquals(result, emailConfirmation);
    }

    @Test
    @DisplayName("insert [case2] -> Should throw an exception when you try to save email receipts information with incorrect or missing data")
    void insertCase2() {
        EmailConfirmation obj = new EmailConfirmation();
        Mockito.when(repository.save(obj)).thenThrow(DatabaseException.class);

        Assertions.assertThrows(DatabaseException.class, () -> emailConfirmationService.insert(obj));
    }

    @Test
    @DisplayName("Get by code [case1] -> should return the information from an email confirmation by code")
    void getByCodeCase1() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Mockito.when(repository.findByCode(emailConfirmation.getCode())).thenReturn(emailConfirmation);
        EmailConfirmation result = emailConfirmationService.findByCode(emailConfirmation.getCode());

        Assertions.assertEquals(result, emailConfirmation);
    }

    @Test
    @DisplayName("Get by code [case2] -> should return an exception when the code of the e-mail information does not exist")
    void getByCodeCase2() {
        Mockito.when(repository.findByCode("non_existent_id")).thenReturn(null);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> emailConfirmationService.findByCode("non_existent_id"));
    }

    @Test
    @DisplayName("Send verification email [case1] -> should send email successfully")
    void sendVerificationEmailCase1() throws MessagingException {
        String email = "test@test.com";
        String code = "123456";

        mimeMessage = Mockito.mock(MimeMessage.class);
        Mockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        Assertions.assertDoesNotThrow(() -> emailConfirmationService.sendVerificationEmail(email, code));

        Mockito.verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send verification email [case2] -> should throw RuntimeException when sending fails")
    void sendVerificationEmailCase2() throws MessagingException {
        String email = "test@test.com";
        String code = "123456";

        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        Mockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Mockito.doThrow(new RuntimeException("Email sending failed"))
                .when(mailSender).send(Mockito.any(MimeMessage.class));

        Assertions.assertThrows(RuntimeException.class, () -> {
            emailConfirmationService.sendVerificationEmail(email, code);
        });

        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(MimeMessage.class));
    }

}