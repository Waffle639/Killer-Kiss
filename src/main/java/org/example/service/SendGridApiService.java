package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Servicio para enviar emails usando Resend API (HTTP)
 * Gratis: hasta 3000 emails/mes, 100/día
 * https://resend.com
 */
@Service
public class SendGridApiService {

    @Value("${resend.api.key:}")
    private String apiKey;

    /**
     * Envía email usando Resend API HTTP
     */
    public boolean sendEmail(String from, String to, String subject, String htmlContent) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("✗ Resend API Key no configurada");
            return false;
        }

        try {
            String safeHtml = htmlContent
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
            String safeSubject = subject.replace("\\", "\\\\").replace("\"", "\\\"");

            String body = "{\"from\":\"" + from + "\","
                + "\"to\":[\"" + to + "\"],"
                + "\"subject\":\"" + safeSubject + "\","
                + "\"html\":\"" + safeHtml + "\"}";

            URL url = new URL("https://api.resend.com/emails");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                System.out.println("✓ Email enviado via Resend API [HTTP " + status + "]");
                return true;
            } else {
                String errorBody = new String(
                    (status >= 400 ? conn.getErrorStream() : conn.getInputStream()).readAllBytes(),
                    StandardCharsets.UTF_8
                );
                System.err.println("✗ Resend API error: HTTP " + status + " → " + errorBody);
                return false;
            }

        } catch (Exception e) {
            System.err.println("✗ Error Resend API: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
