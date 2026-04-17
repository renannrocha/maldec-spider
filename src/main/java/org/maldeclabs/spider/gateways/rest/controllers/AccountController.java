package org.maldeclabs.spider.gateways.rest.controllers;

import jakarta.validation.Valid;
import org.maldeclabs.spider.application.services.AccountService;
import org.maldeclabs.spider.application.services.EmailConfirmationService;
import org.maldeclabs.spider.domain.entities.Account;
import org.maldeclabs.spider.domain.entities.EmailConfirmation;
import org.maldeclabs.spider.gateways.rest.dto.AccountDataResponseDTO;
import org.maldeclabs.spider.gateways.rest.dto.UpdateAccountDTO;
import org.maldeclabs.spider.gateways.rest.dto.UpdatePasswordDTO;
import org.maldeclabs.spider.gateways.rest.responses.StandardGetResponse;
import org.maldeclabs.spider.gateways.rest.responses.StandardResponse;
import org.maldeclabs.spider.infra.security.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@RestController
@RequestMapping(value = "/api/account")
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AccountService service;

    @Autowired
    private EmailConfirmationService emailConfirmationService;

    @GetMapping()
    public ResponseEntity<StandardResponse> getAccountInfo(@RequestHeader("Authorization") String bearerToken) {
        logger.info("(getAccountInfo) request made by this token ({}) [called at {}]", bearerToken, System.currentTimeMillis());
        try{
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                logger.error("(getAccountInfo) token provided presents an error in its format or content --> token provided ({})", bearerToken);
                return buildResponse(HttpStatus.UNAUTHORIZED, "invalid token provided");
            }

            String token = bearerToken.substring(7);
            String email = tokenService.extractEmail(token);
            Account obj = service.findByEmail(email);

            if(obj == null){
                logger.error("(getAccountInfo) account not find, check the implementation of findByEmail in AccountService");
                return buildResponse(HttpStatus.NOT_FOUND, "Account information not found or missing");
            }

            EmailConfirmation ec = obj.getEmailConfirmation();

            logger.info("(getAccountInfo) success in returning Account information");
            HttpStatus status = HttpStatus.OK;
            StandardGetResponse<AccountDataResponseDTO> response = new StandardGetResponse<>(
                    Instant.now(),
                    status.value(),
                    "success in returning Account information",
                    new AccountDataResponseDTO(obj.getName(), obj.getProfile(), obj.getEmail(), ec.getEnabled(), obj.getRole(), obj.getStripeSubscription())
            );

            return ResponseEntity.status(status).body(response);
        } catch (RuntimeException e){
            logger.error("(getAccountInfo) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<StandardResponse> delete(@RequestHeader("Authorization") String bearerToken) {
        logger.info("(delete) request made by this token ({}) [called at {}]", bearerToken, System.currentTimeMillis());
        try{
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                logger.error("(delete) token provided presents an error in its format or content --> token provided ({})", bearerToken);
                return buildResponse(HttpStatus.UNAUTHORIZED, "invalid token provided");
            }

            String token = bearerToken.substring(7);
            String email = tokenService.extractEmail(token);
            Account obj = service.findByEmail(email);

            if(obj == null){
                logger.error("(delete) account not find, check the implementation of findByEmail in AccountService");
                return buildResponse(HttpStatus.NOT_FOUND, "Account information not found or missing");
            }

            service.delete(obj.getId());
            emailConfirmationService.delete(obj.getEmailConfirmation());

            String info = "success in deleting the user: " + obj.getName();
            logger.info("(delete) {}", info);

            return buildResponse(HttpStatus.NO_CONTENT, info);
        } catch(RuntimeException e) {
            logger.error("(delete) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    @PutMapping(value = "/update")
    public ResponseEntity<StandardResponse> update(@RequestHeader("Authorization") String bearerToken, @RequestBody @Valid UpdateAccountDTO data) {
        logger.info("(update) request made by this token ({}) [called at {}]", bearerToken, System.currentTimeMillis());
        try{
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                logger.error("(update) token provided presents an error in its format or content --> token provided ({})", bearerToken);
                return buildResponse(HttpStatus.UNAUTHORIZED, "invalid token provided");
            } else if(Objects.equals(data.name(), "") || Objects.equals(data.profile(), "") || Objects.equals(data.email(), "")){
                // IMPORTANTE - validação de campos temporaria ate conseguir validar os cados com as annotations

                return buildResponse(HttpStatus.BAD_REQUEST, "body information is incorrect or incomplete");
            } else if(data.name() == null || data.profile() == null || data.email() == null){
                // IMPORTANTE - validação de campos temporaria ate conseguir validar os cados com as annotations

                return buildResponse(HttpStatus.BAD_REQUEST, "body information is incorrect or incomplete");
            }

            String token = bearerToken.substring(7);
            String email = tokenService.extractEmail(token);
            Account account = service.findByEmail(email);

            if(account == null){
                logger.error("(update) account not find, check the implementation of findByEmail in AccountService");
                return buildResponse(HttpStatus.NOT_FOUND, "Account information not found or missing");
            }

            EmailConfirmation oldEmailConfirmation = account.getEmailConfirmation();
            EmailConfirmation newEmailConfirmation = new EmailConfirmation(
                    false,
                    String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                    LocalDateTime.now().plusMinutes(5)
            );

            logger.info("(update) performing the update on the name from {} to {} performed by {}", account.getName(), data.name(), email);
            account.setName(data.name());

            if (!data.profile().equals(account.getProfile()) && service.existsByProfile(data.profile())) {
                logger.error("(update) the update request made by {} failed because the profile entered is already in use by another Account", email);
                return buildResponse(HttpStatus.BAD_REQUEST, "the profile name is already in use");
            }

            logger.info("(update) performing the update on the profile name from {} to {} performed by {}", account.getProfile(), data.profile(), email);
            account.setProfile(data.profile());

            if(!Objects.equals(data.email(), account.getEmail())){
                logger.info("(update) performing the update on the email from {} to {} performed by {}", account.getEmail(), data.email(), email);
                account.setEmail(data.email());
                account.setEmailConfirmation(newEmailConfirmation);
                this.emailConfirmationService.insert(newEmailConfirmation);
                this.emailConfirmationService.delete(oldEmailConfirmation);
                this.emailConfirmationService.sendVerificationEmail(account.getEmail(), newEmailConfirmation.getCode());
                logger.info("(update) The verification email was sent to {}", account.getEmail());
            }

            service.update(account.getId(), account);
            logger.info("(update) success in updating user data");

            return buildResponse(HttpStatus.OK, "success in updating user data");
        } catch (RuntimeException e){
            logger.error("(update) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    @PutMapping(value = "/update-role")
    public ResponseEntity<StandardResponse> updateRole(@RequestHeader("Authorization") String bearerToken, @RequestBody Map<String, Object> requestBody){
        logger.info("(updateToFree) request made by this token ({}) [called at {}]", bearerToken, System.currentTimeMillis());
        try{
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                logger.error("(updateRole) token provided presents an error in its format or content --> token provided ({})", bearerToken);
                return buildResponse(HttpStatus.UNAUTHORIZED, "invalid token provided");
            }

            String token = bearerToken.substring(7);
            String email = tokenService.extractEmail(token);
            Account existingAccount = service.findByEmail(email);

            if(existingAccount  == null){
                logger.error("(updateRole) account not find, check the implementation of findByEmail in AccountService");
                return buildResponse(HttpStatus.NOT_FOUND, "Account information not found or missing");
            }

            Integer newRole = (Integer) requestBody.get("role");

            service.updateRole(existingAccount.getId() , newRole);
            logger.info("(updateRole) success in updating the access authority to free related to this account: {}", email);

            return buildResponse(HttpStatus.OK, "success in updating the access authority to free");
        } catch (RuntimeException e){
            logger.error("(updateRole) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<StandardResponse> updatePassword(@RequestHeader("Authorization") String bearerToken, @RequestBody UpdatePasswordDTO data){
        logger.info("(updatePassword) request made by this token ({}) [called at {}]", bearerToken, System.currentTimeMillis());
        try{
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                logger.error("(updatePassword) token provided presents an error in its format or content --> token provided ({})", bearerToken);
                return buildResponse(HttpStatus.UNAUTHORIZED, "invalid token provided");
            }

            String token = bearerToken.substring(7);
            String email = tokenService.extractEmail(token);
            Account account = service.findByEmail(email);

            if(account == null){
                logger.error("(updatePassword) account not find, check the implementation of findByEmail in AccountService");
                return buildResponse(HttpStatus.NOT_FOUND, "Account information not found or missing");
            }

            service.updatePassword(account.getId(), data.oldPassword(), data.newPassword());
            logger.info("(updatePassword) success in updating the password for this account: {}", email);

            return buildResponse(HttpStatus.OK, "success in updating the password");
        } catch (RuntimeException e){
            logger.error("(updatePassword) unexpected error related to {} ", e.getMessage());
            throw e;
        }
    }

    private ResponseEntity<StandardResponse> buildResponse(HttpStatus status, String message) {
        StandardResponse response = new StandardResponse(Instant.now(), status.value(), message);
        return ResponseEntity.status(status).body(response);
    }
}
