package com.example.hr.controller;

import com.example.hr.service.AvatarService;
import com.example.hr.service.EmployeeService;
import com.example.hr.util.SecurityUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees/avatars")
public class AvatarController {

    private static final Logger logger = LoggerFactory.getLogger(AvatarController.class);

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/upload-avatar")
    public String showUploadForm(Model model) {
        model.addAttribute("employees", employeeService.getAll());
        return "employees/avatars/upload-avatar";
    }

    @PostMapping("/upload-avatar")
    public String uploadByUrl(@RequestParam String employeeId,
                              @RequestParam String imageUrl,
                              Model model) {

        String username = SecurityUtil.getCurrentUsername();

        logger.info("[ACTION=UPLOAD_AVATAR] user={} employeeId={} url={}",
                username, employeeId, imageUrl);

        try {
            String result = avatarService.uploadFromUrlvulnerable(employeeId, imageUrl);

            logger.info("[RESULT=UPLOAD_AVATAR] employeeId={} user={}", employeeId, username);

            model.addAttribute("successMessage", result);

        } catch (Exception e) {

            logger.error("[ERROR=UPLOAD_AVATAR] employeeId={} user={} Error={}", 
                    employeeId, username, e.getMessage());

            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "employees/avatars/upload-avatar";
    }
}
