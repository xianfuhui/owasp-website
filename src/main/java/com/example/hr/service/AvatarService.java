package com.example.hr.service;

import com.example.hr.entity.Employee;
import com.example.hr.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AvatarService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public String uploadFromUrl(String employeeId, String imageUrl) {
        try {

            // Cố tình không kiểm tra domain → dễ bị SSRF
            URL url = new URL(imageUrl);

            // đọc dữ liệu từ URL
            InputStream in = url.openStream();

            Path folder = Path.of("uploads/avatars/");
            Files.createDirectories(folder);

            Path filePath = folder.resolve("avatar_" + employeeId + ".png");
            Files.copy(in, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // lưu vào DB
            Employee emp = employeeRepository.findById(employeeId).orElseThrow();
            emp.setAvatarPath(filePath.toString());
            employeeRepository.save(emp);

            return "Saved avatar to: " + filePath;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading avatar: " + e.getMessage());
        }
    }
}
