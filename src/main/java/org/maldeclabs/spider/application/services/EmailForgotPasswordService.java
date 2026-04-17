package org.maldeclabs.spider.application.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.maldeclabs.spider.domain.entities.EmailForgotPassword;
import org.maldeclabs.spider.domain.repositories.EmailForgotPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

@Service
public class EmailForgotPasswordService {
    @Autowired
    private EmailForgotPasswordRepository repository;

    @Autowired
    private JavaMailSender mailSender;

    public EmailForgotPassword insert(EmailForgotPassword obj){
        repository.save(obj);
        return obj;
    }

    public void sendEmail(String to, String name, String resetLink){
        String htmlContent = loadEmailTemplate("forgot-password.html");
        String content = htmlContent
                .replace("{{name}}", name)
                .replace("{{resetLink}}", resetLink);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        try {
            helper.setTo(to);
            helper.setSubject("request forgot password");
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
}
