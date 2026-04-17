package org.maldeclabs.spider.gateways.rest.controllers;

import com.stripe.exception.StripeException;
import org.maldeclabs.spider.application.services.AccountService;
import org.maldeclabs.spider.application.services.StripeService;
import org.maldeclabs.spider.application.services.exceptions.ResourceNotFoundException;
import org.maldeclabs.spider.domain.entities.Account;
import org.maldeclabs.spider.domain.enums.AccountRole;
import org.maldeclabs.spider.gateways.rest.dto.CheckoutSessionDTO;
import org.maldeclabs.spider.gateways.rest.responses.StandardCheckoutSessionResponse;
import org.maldeclabs.spider.gateways.rest.responses.StandardResponse;
import org.maldeclabs.spider.infra.security.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {
    private static final Logger logger = LoggerFactory.getLogger(StripeController.class);

    @Autowired
    private StripeService stripeService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/invoice/pdf")
    public ResponseEntity<Map<String, String>> getInvoicePdf(@RequestHeader("Authorization") String bearerToken) {
        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                logger.error("(getInvoicePdf) token provided presents an error in its format or content --> token provided ({})", bearerToken);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "invalid token provided"));
            }

            String token = bearerToken.substring(7);
            String email = tokenService.extractEmail(token);
            Account account = accountService.findByEmail(email);

            if(account == null){
                logger.error("(getInvoicePdf) account not find, check the implementation of findByEmail in AccountService");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Account information not found or missing"));
            }

            String pdfUrl = stripeService.getInvoicePdfUrl(account.getStripeSubscription().getStripeSubscriptionId());

            Map<String, String> response = new HashMap<>();
            response.put("invoicePdfUrl", pdfUrl);

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error retrieving invoice: " + e.getMessage()));
        }
    }

    @DeleteMapping(value = "/delete/subscription")
    public ResponseEntity<?> cancelSubscription(@RequestHeader("Authorization") String bearerToken) {
        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                logger.error("(cancelSubscription) token provided presents an error in its format or content --> token provided ({})", bearerToken);
                return buildResponse(HttpStatus.UNAUTHORIZED, "invalid token provided");
            }

            String token = bearerToken.substring(7);
            String email = tokenService.extractEmail(token);
            Account account = accountService.findByEmail(email);

            if(account == null){
                logger.error("(cancelSubscription) account not find, check the implementation of findByEmail in AccountService");
                return buildResponse(HttpStatus.NOT_FOUND, "Account information not found or missing");
            }

            Map<String, Object> response = stripeService.cancelSubscription(account.getStripeSubscription().getId(), account);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Erro ao cancelar a assinatura: " + e.getMessage()));
        }
    }

    @GetMapping("/get/subscription")
    public ResponseEntity<?> getSubscription(@RequestHeader("Authorization") String bearerToken) {
        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                logger.error("(getSubscription) token provided presents an error in its format or content --> token provided ({})", bearerToken);
                return buildResponse(HttpStatus.UNAUTHORIZED, "invalid token provided");
            }

            String token = bearerToken.substring(7);
            String email = tokenService.extractEmail(token);
            Account account = accountService.findByEmail(email);

            if(account == null){
                logger.error("(getSubscription) account not find, check the implementation of findByEmail in AccountService");
                return buildResponse(HttpStatus.NOT_FOUND, "Account information not found or missing");
            }

            Map<String, Object> subscription = stripeService.getSubscription(account.getStripeSubscription().getStripeSubscriptionId());
            return ResponseEntity.ok(subscription);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Error retrieving subscription: " + e.getMessage()));
        }
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestHeader("Authorization") String bearerToken, @RequestBody CheckoutSessionDTO data){
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return buildResponse(HttpStatus.UNAUTHORIZED, "invalid token provided");
        }

        try {
            String token = bearerToken.substring(7);
            String email = tokenService.extractEmail(token);
            Account account = accountService.findByEmail(email);

            if(account == null){
                return buildResponse(HttpStatus.NOT_FOUND, "Account information not found or missing");
            }

            String sessionId = stripeService.createCheckoutSession(data.priceId(), email, data.successUrl(), data.cancelUrl());
            HttpStatus status = HttpStatus.OK;
            StandardCheckoutSessionResponse response = new StandardCheckoutSessionResponse(Instant.now(), 200, "success in creating session checkout", sessionId);
            return ResponseEntity.status(status).body(response);
        }catch (StripeException e){
            return buildResponse(HttpStatus.CONFLICT, "Stripe Error :" + e.getMessage());
        }
    }

    private ResponseEntity<StandardResponse> buildResponse(HttpStatus status, String message) {
        StandardResponse response = new StandardResponse(Instant.now(), status.value(), message);
        return ResponseEntity.status(status).body(response);
    }
}
