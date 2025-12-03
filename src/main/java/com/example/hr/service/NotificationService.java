package com.example.hr.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ===============================
    // 1) Vulnerable version
    // ===============================
    public String sendMailVulnerable(String to, String subject, String msg) {
        try {
            // THƯ VIỆN CŨ -> chứa nhiều CVE
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.unsafe-legacy.com");

            Session session = Session.getInstance(props);
            MimeMessage message = new MimeMessage(session);

            // Không validate input -> CRLF Injection
            message.setFrom("noreply@example.com");
            message.addRecipients(jakarta.mail.Message.RecipientType.TO, to);
            message.setSubject(subject);
            message.setText(msg);

            jakarta.mail.Transport.send(message);
            return "Sent using vulnerable method";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // ===============================
    // 2) Secure version
    // ===============================
    public String sendMailSafe(String to, String subject, String text) {
        try {

            // Chặn header injection
            if (to.contains("\n") || to.contains("\r")) {
                return "Invalid email address";
            }

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject.trim());
            mail.setText(text);
            mail.setFrom("noreply@example.com");

            mailSender.send(mail);

            return "Sent securely";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
