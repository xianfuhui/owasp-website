package com.example.hr.service;

import com.example.hr.entity.Employee;
import com.example.hr.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Service
public class AvatarService {

    @Autowired
    private EmployeeRepository employeeRepository;

    // Danh sách domain được phép
    private static final List<String> ALLOWED_DOMAINS = List.of("img-s-msn-com.akamaized.net"); 
    // private static final List<String> ALLOWED_DOMAINS = List.of("example.com", "cdn.example.com");

    // Kiểm tra IP private
    private boolean isPrivateIp(InetAddress addr) {
        return addr.isAnyLocalAddress()
                || addr.isLoopbackAddress()
                || addr.isLinkLocalAddress()
                || addr.isSiteLocalAddress();
    }

    public String uploadFromUrl(String employeeId, String imageUrl) {
        try {
            URL url = new URL(imageUrl);

            // Kiểm tra domain
            String host = url.getHost();
            if (ALLOWED_DOMAINS.stream().noneMatch(host::endsWith)) {
                throw new RuntimeException("Domain not allowed: " + host);
            }

            // Giải resolve IP và kiểm tra private
            InetAddress inetAddress = InetAddress.getByName(host);
            if (isPrivateIp(inetAddress)) {
                throw new RuntimeException("Access to private IP is forbidden: " + inetAddress);
            }

            // Ngăn redirect
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            String contentType = conn.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("URL is not an image. Content-Type: " + contentType);
            }

            try (InputStream in = conn.getInputStream()) {
                // Optional: kiểm tra ảnh có thực sự đọc được
                BufferedImage img = ImageIO.read(in);
                if (img == null) {
                    throw new RuntimeException("Failed to read image from URL");
                }

                Path folder = Path.of("uploads/avatars/");
                Files.createDirectories(folder);

                Path filePath = folder.resolve("avatar_" + employeeId + ".png");
                ImageIO.write(img, "png", filePath.toFile());

                // Lưu vào DB
                Employee emp = employeeRepository.findById(employeeId).orElseThrow();
                emp.setAvatarPath(filePath.toString());
                employeeRepository.save(emp);

                return "Saved avatar to: " + filePath;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error uploading avatar: " + e.getMessage(), e);
        }
    }

    //-------------
    //A10 - SSRF
    //-------------
    public String uploadFromUrlvulnerable(String employeeId, String imageUrl) {
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
