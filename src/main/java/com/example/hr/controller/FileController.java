package com.example.hr.controller;

import com.example.hr.entity.Account;
import com.example.hr.service.AccountService;
import com.example.hr.util.AccessControlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequestMapping("/uploads")
public class FileController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/avatars/{filename:.+}")
    public ResponseEntity<?> getAvatarFile(
            @PathVariable String filename,
            @AuthenticationPrincipal UserDetails user) {

        System.out.println("Request file: " + filename);

        try {
            // Tách employeeId từ filename
            // avatar_692f4af788b1b514901a9b00.png
            if (!filename.startsWith("avatar_") || !filename.endsWith(".png")) {
                return ResponseEntity.status(400).body("Invalid file");
            }

            String employeeId = filename.substring(7, filename.length() - 4);
            System.out.println("Extracted employeeId = " + employeeId);

            Account currentAccount = accountService.getByUsername(user.getUsername());

            // Kiểm tra quyền
            AccessControlUtil.checkViewOrDownload(currentAccount, employeeId);

            Path filePath = Path.of("uploads/avatars", filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] data = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(data);

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(data.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(403).body("Access denied");
        }
    }

    @GetMapping("/contracts/{employeeId}/{filename:.+}")
    public ResponseEntity<?> getContract(@PathVariable String employeeId,
                                         @PathVariable String filename,
                                         @AuthenticationPrincipal UserDetails user) {

        try {
            Account acc = accountService.getByUsername(user.getUsername());
            AccessControlUtil.checkViewOrDownload(acc, employeeId);

            Path filePath = Path.of("uploads/contracts", employeeId, filename);
            if (!Files.exists(filePath)) return ResponseEntity.notFound().build();

            byte[] data = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(data.length)
                    .body(new ByteArrayResource(data));

        } catch (Exception e) {
            return ResponseEntity.status(403).body("Access denied");
        }
    }
}
