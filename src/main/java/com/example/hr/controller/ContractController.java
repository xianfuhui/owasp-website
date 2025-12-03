package com.example.hr.controller;

import com.example.hr.entity.Contract;
import com.example.hr.repository.ContractRepository;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    private final ContractRepository repo;

    public ContractController(ContractRepository repo) {
        this.repo = repo;
    }

    // 1) List hợp đồng theo employeeId
    @GetMapping("/employee/{employeeId}")
    public String listByEmployee(@PathVariable String employeeId, Model model) {

        List<Contract> files = repo.findByEmployeeId(employeeId);

        model.addAttribute("contracts", files);
        model.addAttribute("employeeId", employeeId);

        return "contracts/list";
    }

    // 2) Upload file
    @PostMapping("/upload/{employeeId}")
    public String uploadContract(@PathVariable String employeeId,
                                 @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return "redirect:/contracts/employee/" + employeeId + "?error=empty";
        }

        String uploadDir = "uploads/contracts/" + employeeId;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = Paths.get(uploadDir, file.getOriginalFilename()).toAbsolutePath().toString();
        file.transferTo(new File(filePath));

        Contract contract = new Contract();
        contract.setEmployeeId(employeeId);
        contract.setFileName(file.getOriginalFilename());
        contract.setFilePath(filePath);
        repo.save(contract);

        return "redirect:/contracts/employee/" + employeeId + "?success=uploaded";
    }

    // 3) Download file
    @GetMapping("/download/{id}")
    public ResponseEntity<FileSystemResource> downloadContract(@PathVariable String id) throws IOException {
        Contract contract = repo.findById(id).orElseThrow(() -> new RuntimeException("Contract not found"));
        File file = new File(contract.getFilePath());

        if (!file.exists()) {
            throw new RuntimeException("File not found: " + contract.getFilePath());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + contract.getFileName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }

    // 4) Delete file
    @GetMapping("/delete/{id}")
    public String deleteContract(@PathVariable String id) {
        Contract contract = repo.findById(id).orElseThrow(() -> new RuntimeException("Contract not found"));
        File file = new File(contract.getFilePath());

        if (file.exists()) {
            boolean deleted = file.delete();
            System.out.println("Deleted file: " + file.getAbsolutePath() + " -> " + deleted);
        }

        repo.delete(contract);
        return "redirect:/contracts/employee/" + contract.getEmployeeId() + "?success=deleted";
    }

    // 5) View file trực tiếp trên web
    @GetMapping("/view/{id}")
    public ResponseEntity<FileSystemResource> viewContract(@PathVariable String id) throws IOException {
        Contract contract = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        File file = new File(contract.getFilePath());

        if (!file.exists()) {
            throw new RuntimeException("File not found: " + contract.getFilePath());
        }

        // Xác định type dựa trên extension (ở đây ví dụ PDF, hình ảnh)
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + contract.getFileName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType(contentType))
                .body(new FileSystemResource(file));
    }
}
