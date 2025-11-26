package com.example.hr.controller;

import com.example.hr.service.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AvatarController {

    @Autowired
    private AvatarService avatarService;

    @GetMapping("/upload-avatar")
    public String showUploadForm() {
        return "upload-avatar";
    }

    // Upload avatar FROM URL (gây SSHR nếu không kiểm tra)
    @PostMapping("/upload-by-url")
    public String uploadByUrl(@RequestParam String employeeId,
                            @RequestParam String imageUrl,
                            Model model) {

        String result = avatarService.uploadFromUrl(employeeId, imageUrl);
        model.addAttribute("message", result);
        return "upload-avatar";
    }
}
