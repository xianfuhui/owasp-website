package com.example.hr.controller;

import com.example.hr.service.ContractService;
import com.example.hr.entity.ContractFile;
import com.example.hr.repository.ContractRepository;
import com.example.hr.service.ContractSecureService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService vulnerable;

    @Autowired
    private ContractSecureService secure;

    @Autowired
    private ContractRepository repo;

    // VULNERABLE
    @GetMapping("/download-vuln/{id}")
    public void downloadVuln(@PathVariable String id, HttpServletResponse response) throws Exception {
        byte[] data = vulnerable.downloadVulnerable(id);

        response.setContentType("application/pdf");
        response.getOutputStream().write(data);
    }

    // SECURE
    @GetMapping("/download/{id}")
    public void downloadSecure(@PathVariable String id,
                               @RequestParam String userId,
                               @RequestParam String role,
                               HttpServletResponse response) throws Exception {

        byte[] data = secure.downloadSecure(id, userId, role);

        response.setContentType("application/pdf");
        response.getOutputStream().write(data);
    }

    // ========================
    // VULNERABLE UPLOAD (A08)
    // ========================
    @PostMapping("/upload-vuln")
    public String uploadVulnerable(@RequestParam("file") MultipartFile file,
                                   @RequestParam("employeeId") String employeeId,
                                   Model model) {
        try {
            // Không kiểm tra loại file
            // Không giới hạn folder
            // Không xác thực ai đang upload

            Path folder = Path.of("uploads/contracts/");
            Files.createDirectories(folder);

            Path savePath = folder.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), savePath);

            repo.save(new ContractFile(
                    UUID.randomUUID().toString(),
                    employeeId,
                    file.getOriginalFilename(),
                    savePath.toString()
            ));

            model.addAttribute("msg", "Vulnerable file uploaded");
            return "contract/upload";

        } catch (Exception e) {
            throw new RuntimeException("Upload error: " + e.getMessage());
        }
    }


    // ========================
    // SECURE UPLOAD
    // ========================
    @PostMapping("/upload")
    public String uploadSecure(@RequestParam("file") MultipartFile file,
                               @RequestParam("employeeId") String employeeId,
                               HttpSession session,
                               Model model) {
        try {
            // 1) Kiểm tra đăng nhập
            String role = (String) session.getAttribute("role");
            if (role == null) {
                throw new SecurityException("Not logged in");
            }

            // 2) Chỉ admin mới được upload hợp đồng
            if (!role.equals("ADMIN")) {
                throw new SecurityException("Access denied");
            }

            // 3) Kiểm tra MIME-Type
            if (!file.getContentType().equals("application/pdf")) {
                throw new SecurityException("Only PDF allowed");
            }

            // 4) Chỉ lưu vào thư mục hợp lệ
            Path folder = Path.of("secure/contracts/");
            Files.createDirectories(folder);

            Path savePath = folder.resolve(UUID.randomUUID().toString() + ".pdf");
            Files.copy(file.getInputStream(), savePath);

            repo.save(new ContractFile(
                    UUID.randomUUID().toString(),
                    employeeId,
                    file.getOriginalFilename(),
                    savePath.toString()
            ));

            model.addAttribute("msg", "Secure upload OK");
            return "contract/upload";

        } catch (Exception e) {
            model.addAttribute("msg", "Error: " + e.getMessage());
            return "contract/upload";
        }
    }

    @GetMapping
    public String list(Model model, HttpSession session) {

        // Chỉ admin xem tất cả hợp đồng
        String role = (String) session.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            model.addAttribute("contracts", repo.findAll()); // cho demo, không secure
        } else {
            model.addAttribute("contracts", repo.findAll());
        }

        return "contract/list";
    }
}
