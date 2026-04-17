package org.maldeclabs.spider.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.maldeclabs.spider.application.services.exceptions.ResourceNotFoundException;
import org.maldeclabs.spider.domain.entities.Account;
import org.maldeclabs.spider.domain.entities.StripePlanDetails;
import org.maldeclabs.spider.domain.entities.StripeSubscription;
import org.maldeclabs.spider.domain.enums.AccountRole;
import org.maldeclabs.spider.domain.enums.SubscriptionType;
import org.maldeclabs.spider.domain.repositories.AccountRepository;
import org.maldeclabs.spider.domain.repositories.StripePlanDetailsRepository;
import org.maldeclabs.spider.domain.repositories.StripeSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class StripeService {
    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.promo.id}")
    private String promoId;

    @Autowired
    private StripeSubscriptionRepository stripeSubscriptionRepository;

    @Autowired
    private StripePlanDetailsRepository stripePlanDetailsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, StripePlanDetails> priceIdToStripePlanDetails = new HashMap<>();

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private JavaMailSender mailSender;

    public StripeService() {
        priceIdToStripePlanDetails.put("price_free_monthly", new  StripePlanDetails("FREE", SubscriptionType.MONTHLY));
        priceIdToStripePlanDetails.put("price_free_yearly", new  StripePlanDetails("FREE", SubscriptionType.YEARLY));
        priceIdToStripePlanDetails.put("price_individual_monthly", new  StripePlanDetails("INDIVIDUAL", SubscriptionType.MONTHLY));
        priceIdToStripePlanDetails.put("price_individual_yearly", new  StripePlanDetails("INDIVIDUAL", SubscriptionType.YEARLY));
        priceIdToStripePlanDetails.put("price_enterprise_monthly", new  StripePlanDetails("ENTERPRISE", SubscriptionType.MONTHLY));
        priceIdToStripePlanDetails.put("price_enterprise_yearly", new  StripePlanDetails("ENTERPRISE", SubscriptionType.YEARLY));
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public String createCustomer(Account account) throws StripeException {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(account.getEmail())
                .setName(account.getName())
                .build();

        Customer customer = Customer.create(params);
        return customer.getId();
    }

    public void sendEmail(String to, String name, String product, String verificationLink){
        String htmlContent = loadEmailTemplate("invoice.html");
        String content = htmlContent
                .replace("{{name}}", name)
                .replace("{{verificationLink}}", verificationLink);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        try {
            helper.setTo(to);
            helper.setSubject(product + " Subscription Receipt");
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

    public Map<String, Object> cancelSubscription(String id, Account account) throws StripeException {
        StripeSubscription stripeSubscription = stripeSubscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found in database"));

        Subscription subscription = Subscription.retrieve(stripeSubscription.getStripeSubscriptionId());

        // Verificar a última fatura associada a essa assinatura
        InvoiceCollection invoices = Invoice.list(
                InvoiceListParams.builder()
                        .setSubscription(subscription.getId())
                        .setLimit(1L) // Pegamos apenas a fatura mais recente
                        .build()
        );

        boolean hasPaidInvoice = false;
        Long currentPeriodEnd = subscription.getCurrentPeriodEnd(); // Data final do período pago

        // Verifica se há fatura paga
        if (invoices.getData().size() > 0) {
            Invoice latestInvoice = invoices.getData().get(0);
            if ("paid".equals(latestInvoice.getStatus())) {
                hasPaidInvoice = true;
            }
        }

        if (hasPaidInvoice) {
            subscription.update(SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build());

            stripeSubscription.setStatus("active_until_end");
        } else {
            account.setRole(AccountRole.FREE);
            accountService.update(account.getId(), account);
            subscription.cancel();
            stripeSubscription.setStatus("canceled");
        }

        // Salvar atualização no banco
        stripeSubscriptionRepository.save(stripeSubscription);

        // Retorno da resposta
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Subscription canceled successfully.");
        response.put("subscriptionId", stripeSubscription.getStripeSubscriptionId());
        response.put("status", subscription.getStatus());
        response.put("access_until", hasPaidInvoice ? Instant.ofEpochSecond(currentPeriodEnd).toString() : "Canceled Immediately");

        logger.info("Assinatura {} cancelada com sucesso. Acesso válido até: {}", stripeSubscription.getStripeSubscriptionId(), currentPeriodEnd);

        return response;
    }

    public Map<String, Object> getSubscription(String subscriptionId) throws StripeException {
        StripeSubscription existentSubscription = stripeSubscriptionRepository.findByStripeSubscriptionId(subscriptionId);
        if (existentSubscription == null) {
            throw new ResourceNotFoundException("Subscription not found in database");
        }

        Subscription subscription = Subscription.retrieve(subscriptionId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", subscription.getId());
        response.put("status", subscription.getStatus());
        response.put("customer", subscription.getCustomer());
        response.put("created", subscription.getCreated());
        response.put("current_period_end", subscription.getCurrentPeriodEnd());
        response.put("cancel_at_period_end", subscription.getCancelAtPeriodEnd());

        return response;
    }

    public Map<String, Object> updateSubscription(String subscriptionId, Map<String, Object> updates) throws StripeException {
        StripeSubscription existentSubscription = stripeSubscriptionRepository.findByStripeSubscriptionId(subscriptionId);
        if (existentSubscription == null) {
            throw new ResourceNotFoundException("Subscription not found in database");
        }

        Subscription subscription = Subscription.retrieve(subscriptionId);
        SubscriptionUpdateParams.Builder paramsBuilder = SubscriptionUpdateParams.builder();

        // Atualiza status, plano ou outras informações
        if (updates.containsKey("cancelAtPeriodEnd")) {
            boolean cancelAtPeriodEnd = (boolean) updates.get("cancelAtPeriodEnd");
            paramsBuilder.setCancelAtPeriodEnd(cancelAtPeriodEnd);
        }

        Subscription updatedSubscription = subscription.update(paramsBuilder.build());

        // Atualiza no banco de dados
        existentSubscription.setStatus(updatedSubscription.getStatus());
        stripeSubscriptionRepository.save(existentSubscription);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Subscription updated successfully");
        response.put("status", updatedSubscription.getStatus());

        return response;
    }

    public String createCheckoutSession(String priceId, String customerEmail, String successUrl, String cancelUrl) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build())
                .setCustomerEmail(customerEmail)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                //.setAllowPromotionCodes(false)
                .addDiscount(SessionCreateParams.Discount.builder()
                        .setCoupon(promoId)
                        .build())
                .build();

        Session session = Session.create(params);
        return session.getId();
    }

    public String getInvoicePdfUrl(String subscriptionId) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        String invoiceId = subscription.getLatestInvoice();
        Invoice invoice = Invoice.retrieve(invoiceId);

        return invoice.getInvoicePdf();
    }

    public void handleCheckoutSessionCompleted(Event event) throws JsonProcessingException {
        logger.info("Entrando em handleCheckoutSessionCompleted...");

        JsonNode node = objectMapper.readTree(event.getData().getObject().toJson());

        String accountEmail = node.get("customer_email").asText();
        Account account = accountRepository.findByEmail(accountEmail);
        String subscriptionId = node.get("subscription").asText();
        String customerId = node.get("customer").asText();
        String paymentStatus = node.get("payment_status").asText();

        // Verificar se o nó "created" não é nulo antes de tentar acessar seu valor
        long timestampSecondsStart = 0;
        if (node.has("created") && !node.get("created").isNull()) {
            timestampSecondsStart = node.get("created").asLong();
        }
        LocalDateTime startDate = Instant.ofEpochSecond(timestampSecondsStart)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Verificar se o nó "expires_at" não é nulo antes de tentar acessar seu valor
        long timestampSecondsEnd = 0;
        if (node.has("expires_at") && !node.get("expires_at").isNull()) {
            timestampSecondsEnd = node.get("expires_at").asLong();
        }
        LocalDateTime endDate = Instant.ofEpochSecond(timestampSecondsEnd)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        JsonNode paymentMethodTypesNode = node.get("payment_method_types");
        List<String> paymentMethodTypes = new ArrayList<>();

        if (paymentMethodTypesNode != null && paymentMethodTypesNode.isArray()) {
            paymentMethodTypes = objectMapper.convertValue(paymentMethodTypesNode, new TypeReference<List<String>>() {});
        }

        double amount_total = node.get("amount_total").asDouble();
        double price = amount_total / 100;

        // Verifica se já existe uma assinatura associada à conta
        StripeSubscription existingSubscription = account.getStripeSubscription();
        StripePlanDetails existingPlanDetails;

        String product = "";

        if (existingSubscription != null) {
            logger.info("Atualizando assinatura existente no banco: {}", existingSubscription.getStripeSubscriptionId());

            // Atualiza os dados do StripePlanDetails
            existingPlanDetails = existingSubscription.getStripePlanDetails();
            if (price == 109.99) {
                product = "Infinity PRO";
                account.setRole(AccountRole.BASIC);
                accountRepository.save(account);
                existingPlanDetails.setPlanType("INDIVIDUAL");
                existingPlanDetails.setSubscriptionType(SubscriptionType.MONTHLY);
            } else if (price == 1209.89) {
                product = "Infinity PRO";
                account.setRole(AccountRole.BASIC);
                accountRepository.save(account);
                existingPlanDetails.setPlanType("INDIVIDUAL");
                existingPlanDetails.setSubscriptionType(SubscriptionType.YEARLY);
            }
            stripePlanDetailsRepository.save(existingPlanDetails);

            // Atualiza os dados da StripeSubscription
            existingSubscription.setStripeSubscriptionId(subscriptionId);
            existingSubscription.setStripeCustomerId(customerId);
            existingSubscription.setStatus(paymentStatus);
            existingSubscription.setStartDate(LocalDateTime.now(ZoneId.systemDefault()));
            if (existingPlanDetails.getSubscriptionType() == SubscriptionType.MONTHLY){
                existingSubscription.setEndDate(LocalDateTime.now(ZoneId.systemDefault()).plusMonths(1));
            }else {
                existingSubscription.setEndDate(LocalDateTime.now(ZoneId.systemDefault()).plusYears(1));
            }
            existingSubscription.setPaymentMethodType(paymentMethodTypes);
            stripeSubscriptionRepository.save(existingSubscription);

            try {
                String invoiceURL = getInvoicePdfUrl(account.getStripeSubscription().getStripeSubscriptionId());
                sendEmail(account.getEmail(), account.getProfile(), product, invoiceURL);
            } catch (StripeException e) {
                throw new RuntimeException("error no envio de email - {}" + e.getMessage());
            }

            logger.info("Assinatura atualizada no banco com sucesso! ID: {}", subscriptionId);
        } else {
            logger.info("Criando nova assinatura no banco...");

            // Cria um novo StripePlanDetails
            StripePlanDetails newPlanDetails = new StripePlanDetails();
            if (price == 109.99) {
                product = "Infinity PRO";
                account.setRole(AccountRole.BASIC);
                newPlanDetails.setPlanType("INDIVIDUAL");
                newPlanDetails.setSubscriptionType(SubscriptionType.MONTHLY);
            } else if (price == 1209.89) {
                product = "Infinity PRO";
                account.setRole(AccountRole.BASIC);
                newPlanDetails.setPlanType("INDIVIDUAL");
                newPlanDetails.setSubscriptionType(SubscriptionType.YEARLY);
            }
            stripePlanDetailsRepository.save(newPlanDetails);

            // Cria uma nova StripeSubscription
            StripeSubscription subscription = new StripeSubscription();
            subscription.setStripeSubscriptionId(subscriptionId);
            subscription.setStripeCustomerId(customerId);
            subscription.setStatus(paymentStatus);
            subscription.setStartDate(LocalDateTime.now(ZoneId.systemDefault()));
            if (newPlanDetails.getSubscriptionType() == SubscriptionType.MONTHLY){
                subscription.setEndDate(LocalDateTime.now(ZoneId.systemDefault()).plusMonths(1));
            }else {
                subscription.setEndDate(LocalDateTime.now(ZoneId.systemDefault()).plusYears(1));
            }
            subscription.setPaymentMethodType(paymentMethodTypes);
            subscription.setStripePlanDetails(newPlanDetails);
            stripeSubscriptionRepository.save(subscription);

            // Associa a nova assinatura à conta
            account.setStripeSubscription(subscription);
            accountRepository.save(account);

            try {
                String invoiceURL = getInvoicePdfUrl(account.getStripeSubscription().getStripeSubscriptionId());
                sendEmail(account.getEmail(), account.getProfile(), product, invoiceURL);
            } catch (StripeException e) {
                throw new RuntimeException("error no envio de email - {}" + e.getMessage());
            }

            logger.info("Nova assinatura salva no banco com sucesso! ID: {}", subscriptionId);
        }
    }

    public void handleInvoicePaymentFailed(Event event) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(event.getData().getObject().toJson());

        String accountEmail = node.get("customer_email").asText();
        Account account = accountRepository.findByEmail(accountEmail);

        StripeSubscription existingSubscription = account.getStripeSubscription();
        StripePlanDetails existingPlanDetails;

        if (existingSubscription != null) {
            logger.info("(handleInvoicePaymentFailed) Atualizando assinatura existente no banco: {}", existingSubscription.getStripeSubscriptionId());
            existingSubscription.setStatus("payment_failed");
            account.setRole(AccountRole.FREE);
            accountRepository.save(account);
            stripeSubscriptionRepository.save(existingSubscription);
        }
    }
}
