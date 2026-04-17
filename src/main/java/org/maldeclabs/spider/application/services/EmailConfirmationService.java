package org.maldeclabs.spider.application.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.maldeclabs.spider.application.services.exceptions.DatabaseException;
import org.maldeclabs.spider.application.services.exceptions.ResourceNotFoundException;
import org.maldeclabs.spider.domain.entities.EmailConfirmation;
import org.maldeclabs.spider.domain.repositories.EmailConfirmationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

@Service
public class EmailConfirmationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailConfirmationService.class);

    @Autowired
    private EmailConfirmationRepository repository;

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String verificationCode){
        String htmlContent = loadEmailTemplate("verification-email.html");
        String content = htmlContent.replace("${verificationCode}", verificationCode);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        try {
            helper.setTo(to);
            helper.setSubject("Seu Código de Verificação");
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String loadEmailTemplate(String fileName) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("templates/" + fileName))
                ))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public EmailConfirmation insert(EmailConfirmation obj) {
        this.repository.save(obj);
        return obj;
    }

    public EmailConfirmation findByCode(String code) {
        EmailConfirmation obj = repository.findByCode(code);
        if(obj == null){
            throw new ResourceNotFoundException("Unable to find this user for verification");
        }else{
            return obj;
        }
    }

    public void delete(EmailConfirmation obj){
        try {
            repository.delete(obj);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
