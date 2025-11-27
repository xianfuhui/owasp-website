package com.example.hr.service;

import com.example.hr.entity.ContractFile;
import com.example.hr.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ContractSecureService {

    @Autowired
    private ContractRepository repo;

    private final String BASE_FOLDER = "/var/hr/contracts/";

    public byte[] downloadSecure(String contractId, String currentUserId, String role) {
        try {
            ContractFile file = repo.findById(contractId).orElseThrow();

            // 1. Kiểm tra quyền truy cập
            if (!file.getEmployeeId().equals(currentUserId) && !"ADMIN".equals(role)) {
                throw new SecurityException("Access denied");
            }

            // 2. Chặn Path Traversal
            Path safeBase = Path.of(BASE_FOLDER).normalize();
            Path requested = Path.of(file.getFilePath()).normalize();

            if (!requested.startsWith(safeBase)) {
                throw new SecurityException("Invalid file path!");
            }

            // 3. Đọc file an toàn
            return Files.readAllBytes(requested);

        } catch (Exception e) {
            throw new RuntimeException("Secure download error: " + e.getMessage());
        }
    }
}
