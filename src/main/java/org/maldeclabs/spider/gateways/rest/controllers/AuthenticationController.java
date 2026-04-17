package org.maldeclabs.spider.gateways.rest.controllers;

import jakarta.validation.Valid;
import org.maldeclabs.spider.domain.entities.Account;
import org.maldeclabs.spider.domain.entities.EmailConfirmation;
import org.maldeclabs.spider.domain.entities.EmailForgotPassword;
import org.maldeclabs.spider.domain.enums.AccountRole;
import org.maldeclabs.spider.gateways.rest.dto.*;
import org.maldeclabs.spider.gateways.rest.responses.StandardLoginResponse;
import org.maldeclabs.spider.gateways.rest.responses.StandardResponse;
import org.maldeclabs.spider.infra.security.TokenService;
import org.maldeclabs.spider.application.services.AccountService;
import org.maldeclabs.spider.application.services.EmailConfirmationService;
import org.maldeclabs.spider.application.services.EmailForgotPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EmailConfirmationService emailConfirmationService;

    @Autowired
    private EmailForgotPasswordService emailForgotPasswordService;

    @Value("${frontBaseUrl}")
    private String frontBaseUrl;

    @PostMapping("/login")
    public ResponseEntity<StandardLoginResponse> login(@RequestBody @Valid AuthenticationDTO data){
        logger.info("[website-api] INFO - request (login) made by this account: {} { called at {} }", data.email(), System.currentTimeMillis());
        try{
            logger.info("[website-api] INFO - starting login process for account: {}", data.email());
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());

            var auth = this.authenticationManager.authenticate(usernamePassword);
            logger.info("[website-api] INFO - Authentication successful for: {}", data.email());

            var token = this.tokenService.generateToken((Account) auth.getPrincipal());
            logger.info("[website-api] INFO - Success to generate a new jwt token to {}", data.email());

            HttpStatus status = HttpStatus.OK;
            StandardLoginResponse response = new StandardLoginResponse(Instant.now(), status.value(), "Authentication successful", token);
            return ResponseEntity.status(status).body(response);
        } catch (RuntimeException e){
            logger.error("[website-api] ERROR - (login) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/validate-jwt")
    public ResponseEntity<StandardResponse> validateToken(@RequestBody @Valid ValidateTokenDTO data){
        logger.info("[website-api] INFO - request (validateToken), called at {} ", System.currentTimeMillis());
        String subject = tokenService.validateToken(data.token());

        if (subject.isEmpty()){
            logger.info("[website-api] ERROR - token not valid: {}", data.token());
            return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        return buildResponse(HttpStatus.OK, "valid token");
    }

    @PostMapping("/register")
    public ResponseEntity<StandardResponse> register(@RequestBody @Valid RegisterDTO data){
        logger.info("(register) request made by this account: {} [called at {}]", data.email(), System.currentTimeMillis());
        logger.info("(register) starting registration process for this account: {}", data.email());

        try{
            String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
            EmailConfirmation emailConfirmation = new EmailConfirmation(
                    false,
                    String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                    LocalDateTime.now().plusMinutes(5)
            );

            Account newUser = new Account(data.name(), data.profile(), data.email(), encryptedPassword, AccountRole.FREE, emailConfirmation);

            this.emailConfirmationService.insert(emailConfirmation);
            this.accountService.insert(newUser);
            this.emailConfirmationService.sendVerificationEmail(newUser.getEmail(), emailConfirmation.getCode());

            logger.info("(register) verification code sent to {}", data.email());
            logger.info("(register) successful registration for the account");

            return buildResponse(HttpStatus.OK, "register successful");
        }catch (RuntimeException e){
            logger.error("(register) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/resend-email")
    public ResponseEntity<StandardResponse> resendEmail(@RequestBody @Valid ResendEmailDTO data){
        logger.info("(resendEmail) request made by this account: {} [called at {}]", data.email(), System.currentTimeMillis());

        if(!accountService.existsByEmail(data.email())){
            return buildResponse(HttpStatus.BAD_REQUEST, "Email Not Found");
        }

        try{
            Account account = this.accountService.findByEmail(data.email());
            EmailConfirmation emailConfirmation = account.getEmailConfirmation();

            if(emailConfirmation.getId() != null){
                if(emailConfirmation.getEnabled()){
                    logger.error("(resendEmail) the email is already confirmed");
                    return buildResponse(HttpStatus.CONFLICT, "Email is already confirmed.");
                }

                emailConfirmation.setExpiresAt(LocalDateTime.now().plusMinutes(5));
                emailConfirmation.setCode(String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000));

                this.emailConfirmationService.insert(emailConfirmation);
                this.emailConfirmationService.sendVerificationEmail(data.email(), emailConfirmation.getCode());

                String info = "resent the confirmation email to " + data.email();
                logger.info("(resendEmail) {}", info);
                return buildResponse(HttpStatus.OK, info);
            }else{
                logger.error("(resendEmail) email provided by the user is null");
                return buildResponse(HttpStatus.BAD_REQUEST, "Failed to resend confirmation email.");
            }
        } catch (RuntimeException e){
            logger.error("(resendEmail) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<StandardResponse> confirmEmail(@RequestBody @Valid ConfirmEmailDTO data){
        logger.info("(confirmEmail) request (confirmEmail) called at {} ", System.currentTimeMillis());
        try{
            EmailConfirmation emailConfirmation = emailConfirmationService.findByCode(data.code());

            if (emailConfirmation == null) {
                logger.error("(confirmEmail) Invalid confirmation code");
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid confirmation code");
            } else if (emailConfirmation.getEnabled()) {
                logger.error("(confirmEmail) Email is already confirmed");
                return buildResponse(HttpStatus.CONFLICT, "Email is already confirmed");
            } else if (emailConfirmation.getExpiresAt().isBefore(LocalDateTime.now())) {
                logger.error("(confirmEmail) Confirmation code is expired");
                return buildResponse(HttpStatus.BAD_REQUEST, "Confirmation code is expired");
            } else if (!emailConfirmation.getCode().equals(data.code())) {
                logger.error("(confirmEmail) The code you entered is incorrect");
                return buildResponse(HttpStatus.BAD_REQUEST, "The code you entered is incorrect");
            } else {
                emailConfirmation.setEnabled(true);
                this.emailConfirmationService.insert(emailConfirmation);
                logger.info("(confirmEmail) confirmed email for account");
                return buildResponse(HttpStatus.OK, "confirmed email for account");
            }
        } catch (RuntimeException e){
            logger.error("(confirmEmail) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/request-forgot-password")
    public ResponseEntity<StandardResponse> requestForgotPassword(@RequestBody @Valid ResendEmailDTO data){
        logger.info("(requestForgotPassword) request made by this account: {} { called at {} }", data.email(), System.currentTimeMillis());
        try{
            Account account = accountService.findByEmail(data.email());

            if(account == null){
                logger.error("(requestForgotPassword) account provided is null");
                return buildResponse(HttpStatus.BAD_REQUEST, "Account not found");
            }

            EmailForgotPassword emailForgotPassword = new EmailForgotPassword(UUID.randomUUID().toString(), LocalDateTime.now().plusHours(2));
            emailForgotPassword.setAccount(account);
            this.emailForgotPasswordService.insert(emailForgotPassword);
            logger.info("(requestForgotPassword) saved the forgotten password record for the user: {}", data.email());

            account.getEmailForgotPasswords().add(emailForgotPassword);
            this.accountService.insert(account);
            logger.info("(requestForgotPassword) made the account relationship with the request password request");
            // template html tem que ser implementado aqui
            String linkFormulario = this.frontBaseUrl + "/recover-password" + "?token=" + emailForgotPassword.getToken();

            emailForgotPasswordService.sendEmail(data.email(), account.getName(), linkFormulario);

            String info = "password recovery email sent to this email: " + data.email();
            logger.info("(requestForgotPassword) {}", info);
            return buildResponse(HttpStatus.OK, info);
        } catch (RuntimeException e){
            logger.error("(requestForgotPassword) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<StandardResponse> forgotPassword(@RequestBody @Valid ForgotPasswordBodyDTO bodyData, @RequestParam String token){
        logger.info("(forgotPassword) request called at {}", System.currentTimeMillis());

        try{
            Account account = this.accountService.findByEFPToken(token);

            if (account == null) {
                logger.error("(forgotPassword) Invalid token for {}", token);
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid token");
            } else if (!bodyData.password().equals(bodyData.confirmPassword())) {
                logger.error("(forgotPassword) Passwords do not match");
                return buildResponse(HttpStatus.CONFLICT, "Passwords do not match");
            }

            EmailForgotPassword emailForgotPassword = account.getEmailForgotPasswordByToken(token);

            if(emailForgotPassword.getExpiresAt().isBefore(LocalDateTime.now())){
                logger.error("(forgotPassword) The token provided by the user is null");

                HttpStatus status = HttpStatus.BAD_REQUEST;
                StandardResponse response = new StandardResponse(Instant.now(), status.value(), "Token is expired");
                return ResponseEntity.status(status).body(response);
            }

            emailForgotPassword.setOldPassword(account.getPassword());
            emailForgotPasswordService.insert(emailForgotPassword);

            String encryptedPassword = new BCryptPasswordEncoder().encode(bodyData.password());
            account.setPassword(encryptedPassword);
            accountService.insert(account);

            logger.info("(forgotPassword) Password changed successfully.");
            return buildResponse(HttpStatus.OK, "password changed successfully");
        }catch (RuntimeException e){
            logger.error("(forgotPassword) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    private ResponseEntity<StandardResponse> buildResponse(HttpStatus status, String message) {
        StandardResponse response = new StandardResponse(Instant.now(), status.value(), message);
        return ResponseEntity.status(status).body(response);
    }
}
