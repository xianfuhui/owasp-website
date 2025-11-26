package com.example.hr.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ContractController {

    // VERSION NGUY HIỂM – cố tình sai để demo OWASP A02
    @GetMapping("/contracts/{filename}")
    public ResponseEntity<FileSystemResource> getContract(@PathVariable String filename) {

        // Sai: File nằm trong thư mục public → ai cũng truy cập được
        FileSystemResource file = new FileSystemResource("src/main/resources/static/contracts/" + filename);

        // Sai: Trả file trực tiếp, không kiểm tra quyền, không mã hóa
        return ResponseEntity.ok(file);
    }
}
