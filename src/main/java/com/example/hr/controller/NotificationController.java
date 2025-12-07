package com.example.hr.controller;

import com.example.hr.entity.Employee;
import com.example.hr.service.NotificationService;
import com.example.hr.service.EmployeeService;
import com.example.hr.util.SecurityUtil;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService service;
    private final EmployeeService employeeService;

    public NotificationController(NotificationService service, EmployeeService employeeService) {
        this.service = service;
        this.employeeService = employeeService;
    }

    @GetMapping("/send-mail")
    public String sendMailForm() {
        return "notifications/send-mail";
    }

    @PostMapping("/send-mail")
    public String sendMail(
            @RequestParam(required = false) String to,
            @RequestParam String subject,
            @RequestParam String message,
            @RequestParam String sendOption,
            Model model) {

        String username = SecurityUtil.getCurrentUsername();

        if (subject == null || subject.isBlank() || message == null || message.isBlank()) {
            log.warn("[MAIL] '{}' failed: missing subject or message", username);
            model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ subject và message");
            return "notifications/send-mail";
        }

        try {
            String result;

            switch (sendOption) {
                case "all":
                    List<Employee> employees = employeeService.getAll();
                    log.info("[MAIL] '{}' sending to ALL employees. Total recipients={}", username, employees.size());
                    result = service.sendMailToAll(employees, subject, message);
                    break;

                case "some":
                    if (to == null || to.isEmpty()) {
                        log.warn("[MAIL] '{}' failed: option=some but no recipients provided", username);
                        model.addAttribute("errorMessage", "Vui lòng nhập email của các nhân viên");
                        return "notifications/send-mail";
                    }
                    String[] emails = to.split(",");
                    log.info("[MAIL] '{}' sending to selected employees: {}", username, (Object) emails);
                    result = service.sendMailToMultiple(emails, subject, message);
                    break;

                case "single":
                default:
                    log.info("[MAIL] '{}' sending to single: {}", username, to);
                    result = service.sendMail(to, subject, message);
                    break;
            }

            if (result.toLowerCase().contains("error")) {
                model.addAttribute("errorMessage", result);
            } else {
                model.addAttribute("successMessage", result);
            }

            log.info("[MAIL] '{}' mail action result: {}", username, result);

        } catch (Exception e) {
            log.error("[MAIL] '{}' ERROR during sending mail: {}", username, e.getMessage(), e);
            model.addAttribute("errorMessage", "Lỗi khi gửi mail: " + e.getMessage());
        }

        return "notifications/send-mail";
    }
}
