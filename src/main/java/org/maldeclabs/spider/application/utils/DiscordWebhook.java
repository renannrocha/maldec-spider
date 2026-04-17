package org.maldeclabs.spider.application.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class DiscordWebhook {
    private static final Logger logger = LoggerFactory.getLogger(DiscordWebhook.class);

    //@Value("${discord.webhook.channel.stripe.events}")
    private static final String DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/1344104082226548800/vsjECopTsfhAnXGSvcv_wIBaHxErYnmtqNoIvquPzRuWt5O7ak1kNm5JUwddSC3QnTnw";

    //@Value("${discord.webhook.channel.request.build.infinity.pro}")
    private static final String DISCORD_WEBHOOK_REQUEST_BUILD_URL = "https://discord.com/api/webhooks/1351382934330015765/HMrFYJ2cb2cqNxIP42MIKSEFa1eC-fsuMGDwNefAbHk5Crax-Fkdbfvjgl7xHz2Cjq-c";

    public static void sendMessage(String message) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(DISCORD_WEBHOOK_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("User-Agent", "curl/7.64.1");

            String jsonPayload = String.format("{\"content\": \"%s\"}", message);
            httpPost.setEntity(new StringEntity(jsonPayload));

            httpClient.execute(httpPost);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public static void sendEmbed(String title, String description, Color color, String footer, List<Field> fields) {
        if (title == null || title.isEmpty()) {
            logger.error("Título não pode ser nulo ou vazio.");
            return;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(DISCORD_WEBHOOK_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("User-Agent", "curl/7.64.1");

            int colorDecimal = color != null ? color.getRGB() & 0xFFFFFF : 5814783; // Cor padrão (roxo)

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode embed = objectMapper.createObjectNode();
            embed.put("title", title);
            embed.put("description", description);
            embed.put("color", colorDecimal);

            if (fields != null && !fields.isEmpty()) {
                ArrayNode fieldsArray = objectMapper.createArrayNode();
                for (Field field : fields) {
                    ObjectNode fieldObj = objectMapper.createObjectNode();
                    fieldObj.put("name", field.getName());
                    fieldObj.put("value", field.getValue());
                    fieldObj.put("inline", field.isInline());
                    fieldsArray.add(fieldObj);
                }
                embed.set("fields", fieldsArray);
            }

            ObjectNode footerObj = objectMapper.createObjectNode();
            footerObj.put("text", footer);
            embed.set("footer", footerObj);

            ArrayNode embedsArray = objectMapper.createArrayNode();
            embedsArray.add(embed);

            ObjectNode payload = objectMapper.createObjectNode();
            payload.set("embeds", embedsArray);

            String jsonPayload = payload.toPrettyString();
            logger.info("Payload JSON enviado para o Discord: {}", jsonPayload);

            httpPost.setEntity(new StringEntity(jsonPayload));

            HttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() != 204) {
                logger.error("Erro ao enviar embed para o Discord. Status code: {}", response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            logger.error("Erro de I/O ao enviar embed para o Discord: {}", e.getMessage());
        }
    }

    public static void sendRequestBuildEmbed(String title, String description, Color color, String footer, List<Field> fields) {
        if (title == null || title.isEmpty()) {
            logger.error("(sendRequestBuildEmbed) Título não pode ser nulo ou vazio.");
            return;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(DISCORD_WEBHOOK_REQUEST_BUILD_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("User-Agent", "curl/7.64.1");

            int colorDecimal = color != null ? color.getRGB() & 0xFFFFFF : 5814783; // Cor padrão (roxo)

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode embed = objectMapper.createObjectNode();
            embed.put("title", title);
            embed.put("description", description);
            embed.put("color", colorDecimal);

            if (fields != null && !fields.isEmpty()) {
                ArrayNode fieldsArray = objectMapper.createArrayNode();
                for (Field field : fields) {
                    ObjectNode fieldObj = objectMapper.createObjectNode();
                    fieldObj.put("name", field.getName());
                    fieldObj.put("value", field.getValue());
                    fieldObj.put("inline", field.isInline());
                    fieldsArray.add(fieldObj);
                }
                embed.set("fields", fieldsArray);
            }

            ObjectNode footerObj = objectMapper.createObjectNode();
            footerObj.put("text", footer);
            embed.set("footer", footerObj);

            ArrayNode embedsArray = objectMapper.createArrayNode();
            embedsArray.add(embed);

            ObjectNode payload = objectMapper.createObjectNode();
            payload.set("embeds", embedsArray);

            String jsonPayload = payload.toPrettyString();
            logger.info("(sendRequestBuildEmbed) Payload JSON enviado para o Discord: {}", jsonPayload);

            httpPost.setEntity(new StringEntity(jsonPayload));

            HttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() != 204) {
                logger.error("(sendRequestBuildEmbed) Erro ao enviar embed para o Discord. Status code: {}", response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            logger.error("(sendRequestBuildEmbed) Erro de I/O ao enviar embed para o Discord: {}", e.getMessage());
        }
    }
}
