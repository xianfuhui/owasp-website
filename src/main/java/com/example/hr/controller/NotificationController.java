package com.example.hr.controller;

import com.example.hr.service.NotificationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notify")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // Form gửi mail vulnerable
    @GetMapping("/send-vulnerable")
    public String sendVulnForm() {
        return "notifications/send-vulnerable";
    }

    // Form gửi mail secure
    @GetMapping("/send-safe")
    public String sendSafeForm() {
        return "notifications/send-safe";
    }


    // Vulnerable endpoint
    @PostMapping("/send-vulnerable")
    public String sendVulnerable(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String message,
            Model model) {

        String result = service.sendMailVulnerable(to, subject, message);

        model.addAttribute("status", result);
        return "notifications/result";
    }


    // Secure endpoint
    @PostMapping("/send-safe")
    public String sendSafe(
            @RequestParam @Email String to,
            @RequestParam @NotBlank String subject,
            @RequestParam @NotBlank String message,
            Model model) {

        String result = service.sendMailSafe(to, subject, message);

        model.addAttribute("status", result);
        return "notifications/result";
    }
}
