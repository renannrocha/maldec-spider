package org.maldeclabs.spider.service;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.maldeclabs.spider.application.services.EmailForgotPasswordService;

import static org.junit.jupiter.api.Assertions.*;

class EmailForgotPasswordServiceTest {

    @Test
    @DisplayName("Load email template -> should load the template successfully")
    void loadEmailTemplate() throws Exception {
        EmailForgotPasswordService service = new EmailForgotPasswordService();

        Method method = EmailForgotPasswordService.class.getDeclaredMethod("loadEmailTemplate", String.class);
        method.setAccessible(true);

        String templateContent = (String) method.invoke(service, "forgot-password.html");

        assertNotNull(templateContent, "O conteúdo do template não deve ser nulo");
        assertFalse(templateContent.isEmpty(), "O conteúdo do template não deve estar vazio");
    }
}
