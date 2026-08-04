package org.example.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class GmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:#{null}}")
    private String from;

    @Value("${spring.mail.password:#{null}}")
    private String password;

    public GmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean isConfigured() {
        return from != null && !from.isEmpty()
            && password != null && !password.isEmpty();
    }

    public boolean sendEmail(String to, String subject, String htmlContent) {
        if (!isConfigured()) {
            System.err.println("Gmail no configurado: falta mail.config con gmail.username y gmail.password");
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("Email enviado a " + to + " correctamente");
            return true;
        } catch (Exception e) {
            System.err.println("Error enviando email a " + to + ": " + e.getMessage());
            return false;
        }
    }
}
