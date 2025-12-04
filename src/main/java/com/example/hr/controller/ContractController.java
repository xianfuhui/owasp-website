package com.example.hr.controller;

import com.example.hr.entity.Account;
import com.example.hr.entity.Contract;
import com.example.hr.repository.ContractRepository;
import com.example.hr.service.AccountService;
import com.example.hr.util.AccessControlUtil;
import com.example.hr.util.SecurityUtil;

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
import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    private final ContractRepository repo;
    private final AccountService accountService;

    public ContractController(ContractRepository repo, AccountService accountService) {
        this.repo = repo;
        this.accountService = accountService;
    }

    // =========================
    // 1. List contract by employee
    // =========================
    @GetMapping("/employee/{employeeId}")
    public String listByEmployee(@PathVariable String employeeId, Model model) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        Account currentAccount = accountService.getByUsername(currentUsername);

        // Kiểm tra quyền: ADMIN/HR xem tất cả, USER chỉ xem chính mình
        AccessControlUtil.checkViewOrDownload(currentAccount, employeeId);

        List<Contract> files = repo.findByEmployeeId(employeeId);

        model.addAttribute("contracts", files);
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("currentAccount", currentAccount);

        return "contracts/list";
    }

    // =========================
    // 2. Upload file (100% relative path)
    // =========================
    @PostMapping("/upload/{employeeId}")
    public String uploadContract(@PathVariable String employeeId,
                                 @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return "redirect:/contracts/employee/" + employeeId + "?error=empty";
        }

        String relativeDir = "uploads/contracts/" + employeeId;
        String relativeFile = relativeDir + "/" + file.getOriginalFilename();

        Path dirPath = Paths.get(relativeDir);
        Files.createDirectories(dirPath);

        Path filePath = Paths.get(relativeFile);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Contract contract = new Contract();
        contract.setEmployeeId(employeeId);
        contract.setFileName(file.getOriginalFilename());
        contract.setFilePath(relativeFile); // DB only stores relative path

        repo.save(contract);

        return "redirect:/contracts/employee/" + employeeId + "?success=uploaded";
    }

    // =========================
    // 3. Download file (relative only)
    // =========================
    @GetMapping("/download/{id}")
    public ResponseEntity<FileSystemResource> downloadContract(@PathVariable String id) throws IOException {

        Contract contract = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        String currentUsername = SecurityUtil.getCurrentUsername();
        Account currentAccount = accountService.getByUsername(currentUsername);

        // Kiểm tra quyền: ADMIN/HR xem tất cả, USER chỉ xem chính mình
        AccessControlUtil.checkViewOrDownload(currentAccount, contract.getEmployeeId());

        Path path = Paths.get(contract.getFilePath());
        File file = path.toFile();

        if (!file.exists()) {
            throw new RuntimeException("File not found: " + contract.getFilePath());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + contract.getFileName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }

    // =========================
    // 4. Delete file (relative)
    // =========================
    @GetMapping("/delete/{id}")
    public String deleteContract(@PathVariable String id) {

        Contract contract = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        Path path = Paths.get(contract.getFilePath());
        File file = path.toFile();

        if (file.exists()) {
            file.delete();
        }

        repo.delete(contract);

        return "redirect:/contracts/employee/" + contract.getEmployeeId() + "?success=deleted";
    }

    // =========================
    // 5. View file inline (relative)
    // =========================
    @GetMapping("/view/{id}")
    public ResponseEntity<FileSystemResource> viewContract(@PathVariable String id) throws IOException {

        Contract contract = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));


        String currentUsername = SecurityUtil.getCurrentUsername();
        Account currentAccount = accountService.getByUsername(currentUsername);

        // Kiểm tra quyền: ADMIN/HR xem tất cả, USER chỉ xem chính mình
        AccessControlUtil.checkViewOrDownload(currentAccount, contract.getEmployeeId());

        Path path = Paths.get(contract.getFilePath());
        File file = path.toFile();

        if (!file.exists()) {
            throw new RuntimeException("File not found: " + contract.getFilePath());
        }

        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + contract.getFileName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType(contentType))
                .body(new FileSystemResource(file));
    }
}
