package com.example.hr.controller;

import com.example.hr.service.AvatarService;
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

    @GetMapping("/upload-avatar")
    public String showUploadForm() {
        return "employees/avatars/upload-avatar";
    }

    @PostMapping("/upload-avatar")
    public String uploadByUrl(@RequestParam String employeeId,
                              @RequestParam String imageUrl,
                              Model model) {

        String username = SecurityUtil.getCurrentUsername();

        logger.info("User {} requested avatar upload for employeeId={} with URL={}",
                username, employeeId, imageUrl);

        try {
            String result = avatarService.uploadFromUrl(employeeId, imageUrl);

            logger.info("Avatar upload SUCCESS for employeeId={} by {}", employeeId, username);

            model.addAttribute("successMessage", result);

        } catch (Exception e) {

            logger.error("Avatar upload FAILED for employeeId={} by {}. Error={}", 
                    employeeId, username, e.getMessage());

            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "employees/avatars/upload-avatar";
    }
}
