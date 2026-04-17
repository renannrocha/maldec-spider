package org.maldeclabs.spider.gateways.rest.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.maldeclabs.spider.application.services.StripeService;
import org.maldeclabs.spider.application.utils.DiscordWebhook;
import org.maldeclabs.spider.application.utils.Field;
import org.maldeclabs.spider.gateways.rest.responses.StandardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private StripeService stripeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/webhooks")
    public ResponseEntity<StandardResponse> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        logger.info("Payload received by Stripe: {}", payload);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            logger.info("Event details received from Stripe : {}", event.getType());
        } catch (SignatureVerificationException e) {
            logger.error("Error verifying webhook signature : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new StandardResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Assinatura do webhook inválida"));
        }

        try {
            JsonNode node = objectMapper.readTree(event.getData().getObject().toJson());
            List<Field> fields = new ArrayList<>();
            String eventId = node.get("id").asText();

            switch (event.getType()) {
                case "checkout.session.completed":
                    fields.add(new Field("Event ID", "`" + eventId + "`", true));

                    DiscordWebhook.sendEmbed(
                            "Checkout Session Completed",
                            "A checkout session has been completed",
                            Color.GREEN,
                            "For more information, check out the Stripe dashboard",
                            fields
                    );

                    stripeService.handleCheckoutSessionCompleted(event);
                    break;

                case "customer.subscription.created":
                    fields.add(new Field("Event ID", "`" + eventId + "`", true));

                    DiscordWebhook.sendEmbed(
                            "A new subscription has been created",
                            "A new signature has been created in the system",
                            Color.GREEN,
                            "For more information, check out the Stripe dashboard",
                            fields
                    );
                    break;

                case "customer.subscription.deleted":
                    fields.add(new Field("Event ID", "`" + eventId + "`", true));

                    DiscordWebhook.sendEmbed(
                            "A subscription has been canceled",
                            "The system has picked up a cancellation of a subscription",
                            Color.ORANGE,
                            "For more information, check out the Stripe dashboard",
                            fields
                    );
                    break;

                case "invoice.payment_succeeded":
                    fields.add(new Field("Event ID", "`" + eventId + "`", true));

                    DiscordWebhook.sendEmbed(
                            "A new invoice payment has been made",
                            "A new invoice payment has been successfully processed by the system",
                            Color.GREEN,
                            "For more information, check out the Stripe dashboard",
                            fields
                    );
                    break;

                case "invoice.payment_failed":
                    fields.add(new Field("Event ID", "`" + eventId + "`", true));

                    DiscordWebhook.sendEmbed(
                            "Payment Failure",
                            "Hears a payment failure involving the subscription service ",
                            Color.RED,
                            "For more information, check out the Stripe dashboard",
                            fields
                    );
                    stripeService.handleInvoicePaymentFailed(event);
                    break;

                case "payment_intent.succeeded":
                    fields.add(new Field("Event ID", "`" + eventId + "`", true));

                    DiscordWebhook.sendEmbed(
                            "Payment Intent Completed Successfully",
                            "An intention to pay has been successfully processed by the system",
                            Color.GREEN,
                            "For more information, check out the Stripe dashboard",
                            fields
                    );
                    break;

                default:
                    logger.info("Unhandled webhook event : {}", event.getType());
                    DiscordWebhook.sendMessage("Event received : " + event.getType());
                    break;
            }
        } catch (Exception e) {
            logger.error("Unexpected error processing webhook : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StandardResponse(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error"));
        }
        return ResponseEntity.ok(new StandardResponse(Instant.now(), HttpStatus.OK.value(), "Webhook Received Successfully"));
    }
}
