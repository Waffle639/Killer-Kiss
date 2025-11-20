package org.example.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Servicio para enviar emails usando SendGrid API (HTTP)
 * Solución para Render.com que bloquea puerto SMTP 587
 */
@Service
public class SendGridApiService {

    @Value("${sendgrid.api.key:}")
    private String apiKey;

    /**
     * Envía email usando SendGrid API HTTP (no SMTP)
     */
    public boolean sendEmail(String from, String to, String subject, String textContent) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("✗ SendGrid API Key no configurada");
            return false;
        }

        try {
            Email fromEmail = new Email(from);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", textContent);
            Mail mail = new Mail(fromEmail, subject, toEmail, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✓ Email enviado via SendGrid API [HTTP " + response.getStatusCode() + "]");
                return true;
            } else {
                System.err.println("✗ SendGrid API error: HTTP " + response.getStatusCode());
                System.err.println("  Body: " + response.getBody());
                return false;
            }
            
        } catch (IOException e) {
            System.err.println("✗ Error SendGrid API: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
