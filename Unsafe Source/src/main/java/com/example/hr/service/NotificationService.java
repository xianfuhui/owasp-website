package com.example.hr.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.hr.entity.Employee;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String sendMail(String to, String subject, String text) {
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

    public String sendMailToMultiple(String[] emails, String subject, String text) {
        try {
            for (String email : emails) {
                email = email.trim();
                if (email.isEmpty() || email.contains("\n") || email.contains("\r")) continue;

                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo(email);
                mail.setSubject(subject.trim());
                mail.setText(text);
                mail.setFrom("noreply@example.com");

                mailSender.send(mail);
            }
            return "Sent to selected employees successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String sendMailToAll(Iterable<Employee> employees, String subject, String text) {
        try {
            for (Employee e : employees) {
                String email = e.getEmail();
                // Chặn header injection
                if (email == null || email.contains("\n") || email.contains("\r")) continue;

                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo(email);
                mail.setSubject(subject.trim());
                mail.setText(text);
                mail.setFrom("noreply@example.com");

                mailSender.send(mail);
            }
            return "Sent to all employees successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
