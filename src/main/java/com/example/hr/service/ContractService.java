package com.example.hr.service;

import com.example.hr.entity.ContractFile;
import com.example.hr.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ContractService {

    @Autowired
    private ContractRepository repo;

    // Vulnerable
    public byte[] downloadVulnerable(String contractId) {
        try {
            ContractFile file = repo.findById(contractId).orElseThrow();

            // Không kiểm tra quyền
            // Không kiểm tra đường dẫn
            // Không mã hóa file → đọc từ file trực tiếp
            Path p = Path.of(file.getFilePath());

            return Files.readAllBytes(p);

        } catch (Exception e) {
            throw new RuntimeException("Download error: " + e.getMessage());
        }
    }
}
