package org.maldeclabs.spider.gateways.rest.controllers;

import org.maldeclabs.spider.application.services.AccountService;
import org.maldeclabs.spider.application.utils.DiscordWebhook;
import org.maldeclabs.spider.application.utils.Field;
import org.maldeclabs.spider.domain.entities.Account;
import org.maldeclabs.spider.gateways.rest.responses.StandardResponse;
import org.maldeclabs.spider.infra.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/download")
public class FileDownloadController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ResourceLoader resourceLoader;

    private static final String BASE_PRO_DIRECTORY = "/build/infinity/pro/";
    //private static final String BASE_PRO_DIRECTORY = System.getProperty("user.dir") + "/src/main/resources/infinity/pro/";

    @GetMapping(value = "/infinity/pro/generate-build")
    public ResponseEntity<?> generateUserBuild(@RequestHeader("Authorization") String bearerToken){
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return buildStandardResponse(HttpStatus.BAD_REQUEST, "token provided presents an error in its format or content");
        }

        String token = bearerToken.substring(7);
        String email = tokenService.extractEmail(token);
        Account account = accountService.findByEmail(email);

        if(account == null){
            return buildStandardResponse(HttpStatus.CONFLICT, "Account information not found or missing");
        }

        Path userDirPath = Paths.get(BASE_PRO_DIRECTORY + account.getProfile());

        try {
            if (!Files.exists(userDirPath)) {
                Files.createDirectories(userDirPath);
            } else {
                return buildStandardResponse(HttpStatus.CONFLICT, "Build request already made for this user: " + account.getProfile());
            }
        } catch (IOException e) {
            return buildStandardResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating the build request for the user: " + account.getProfile());
        }

        List<Field> fields = new ArrayList<>();
        fields.add(new Field("nome", "`"+ account.getName() +"`", true));
        fields.add(new Field("profile", "`"+ account.getProfile() +"`", true));
        fields.add(new Field("email", "`"+ account.getEmail() +"`", true));
        fields.add(new Field("subscription id", "`"+ account.getStripeSubscription().getStripeSubscriptionId() +"`", true));
        fields.add(new Field("plan type", "`"+ account.getStripeSubscription().getStripePlanDetails().getPlanType() +"`", true));
        fields.add(new Field("subscription type", "`"+ account.getStripeSubscription().getStripePlanDetails().getSubscriptionType() +"`", true));
        fields.add(new Field("start date", "`"+ account.getStripeSubscription().getStartDate() +"`", true));
        fields.add(new Field("end date", "`"+ account.getStripeSubscription().getEndDate() +"`", true));
        fields.add(new Field("payment status", "`"+ account.getStripeSubscription().getStatus() +"`", true));

        DiscordWebhook.sendRequestBuildEmbed("Build generation request", "a user made the build generation request", Color.BLUE, "For more information, visit Stripe's Dashboard", fields);

        return buildStandardResponse(HttpStatus.OK, "Build request created for this user: " + userDirPath.toString());
    }

    @GetMapping(value = "/deb/infinity/pro")
    public ResponseEntity<?> downloadInfinityProDeb(@RequestHeader("Authorization") String bearerToken){
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return buildStandardResponse(HttpStatus.BAD_REQUEST, "token provided presents an error in its format or content");
        }

        String token = bearerToken.substring(7);
        String email = tokenService.extractEmail(token);
        Account account = accountService.findByEmail(email);

        if(account == null){
            return buildStandardResponse(HttpStatus.CONFLICT, "Account information not found or missing");
        }

        String userPlanType = account.getStripeSubscription().getStripePlanDetails().getPlanType();
        String userPaymentStatus = account.getStripeSubscription().getStatus();

        // verifica se o usuário esta no plano "INDIVIDUAL", verifica o status do pagamento da assinatura,
        if(!Objects.equals(userPlanType, "INDIVIDUAL") || Objects.equals(userPaymentStatus, "paid")){
            return buildStandardResponse(HttpStatus.BAD_REQUEST, "Unauthorized user: pending payment or inactive subscription");
        }

        String resourcePath = "file:/build/infinity/pro/" + account.getProfile() + "/infinity-pro-1.1-amd64.deb";
        Resource resource = resourceLoader.getResource(resourcePath);

        // Verificação se o arquivo existe
        if (!resource.exists()) {
            return buildStandardResponse(HttpStatus.NOT_FOUND,
                    "No build available right now. It usually takes up to 24 hours. Need help after that? Just get in touch with support!");
        }

        return buildResponse(resource, "infinity-pro-1.1-amd64.deb");
    }

    @GetMapping(value = "/gz/infinity/pro")
    public ResponseEntity<?> downloadInfinityProGz(@RequestHeader("Authorization") String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return buildStandardResponse(HttpStatus.BAD_REQUEST, "token provided presents an error in its format or content");
        }

        String token = bearerToken.substring(7);
        String email = tokenService.extractEmail(token);
        Account account = accountService.findByEmail(email);

        if(account == null){
            return buildStandardResponse(HttpStatus.CONFLICT, "Account information not found or missing");
        }

        String userPlanType = account.getStripeSubscription().getStripePlanDetails().getPlanType();
        String userPaymentStatus = account.getStripeSubscription().getStatus();

        // verifica se o usuário esta no plano "INDIVIDUAL", verifica o status do pagamento da assinatura,
        if(!Objects.equals(userPlanType, "INDIVIDUAL") || Objects.equals(userPaymentStatus, "paid")){
            return buildStandardResponse(HttpStatus.BAD_REQUEST, "Unauthorized user: pending payment or inactive subscription");
        }

        String resourcePath = "file:/build/infinity/pro/" + account.getProfile() + "/infinity-pro-1.1.tar.gz";
        Resource resource = resourceLoader.getResource(resourcePath);

        // Verificação se o arquivo existe
        if (!resource.exists()) {
            return buildStandardResponse(HttpStatus.NOT_FOUND,
                    "No build available right now. It usually takes up to 24 hours. Need help after that? Just get in touch with support!");
        }

        return buildResponse(resource, "infinity-pro-1.1.tar.gz");
    }

    @GetMapping(value = "/deb/infinity/demo")
    public ResponseEntity<?> downloadInfinityDemoDeb() {
        Resource resource = resourceLoader.getResource("file:/build/infinity/demo/infinity-demo-1.1-amd64.deb");
        return buildResponse(resource, "infinity-demo-1.1-amd64.deb");
    }

    @GetMapping(value = "/gz/infinity/demo")
    public ResponseEntity<?> downloadInfinityDemoGz() {
        Resource resource = resourceLoader.getResource("file:/build/infinity/demo/infinity-demo-1.1.tar.gz");
        return buildResponse(resource, "infinity-demo-1.1.tar.gz");
    }


    private ResponseEntity<?> buildResponse(Resource resource, String filename){
        if (!resource.exists()) {
            return buildStandardResponse(HttpStatus.CONFLICT, "Arquivo não encontrado: " + filename);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    private ResponseEntity<StandardResponse> buildStandardResponse(HttpStatus status, String message) {
        StandardResponse response = new StandardResponse(Instant.now(), status.value(), message);
        return ResponseEntity.status(status).body(response);
    }
}
