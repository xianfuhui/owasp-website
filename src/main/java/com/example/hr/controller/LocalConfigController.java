package com.example.hr.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.nio.file.Files;

@Controller
public class LocalConfigController {

    private static final String L4 = "127.0.0.1";
    private static final String L6 = "0:0:0:0:0:0:0:1";
    private static final String LAN = "10.52.191.152"; 

    @GetMapping("/internal/config")
    public ResponseEntity<?> readConfig(HttpServletRequest req) throws Exception {

        String ip = req.getRemoteAddr();

        if (!ip.equals(L4) && !ip.equals(L6) && !ip.equals(LAN)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        File file = new File("configs/cauhinh.txt");
        byte[] content = Files.readAllBytes(file.toPath());

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(content);
    }
}
