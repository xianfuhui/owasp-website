package com.example.hr.controller;

import com.example.hr.entity.Employee;
import com.example.hr.service.EmployeeImportService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/employees/import")
public class EmployeeImportController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeImportController.class);

    private final EmployeeImportService importService;

    public EmployeeImportController(EmployeeImportService importService) {
        this.importService = importService;
    }

    @GetMapping
    public String importPage() {
        return "employees/import";
    }

    @PostMapping
    public String importExcel(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "File rỗng, vui lòng chọn file Excel.");
            log.warn("[ERROR=IMPORT_EMPLOYEES] empty file uploaded");
            return "employees/import";
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            model.addAttribute("errorMessage", "Định dạng file không hợp lệ. Chỉ nhận .xlsx");
            log.warn("[ERROR=IMPORT_EMPLOYEES] invalid format file={}", file.getOriginalFilename());
            return "employees/import";
        }

        try {
            log.info("[ACTION=IMPORT_EMPLOYEES] start file={}", file.getOriginalFilename());

            List<Employee> imported = importService.importFromExcel(file.getInputStream());

            log.info("[RESULT=IMPORT_EMPLOYEES] imported={}", imported.size());

            model.addAttribute("successMessage", "Import thành công " + imported.size() + " nhân viên!");
            model.addAttribute("imported", imported);

        } catch (Exception e) {
            log.error("[ERROR=IMPORT_EMPLOYEES] import failed: {}", e.getMessage());
            model.addAttribute("errorMessage", "Import thất bại: " + e.getMessage());
        }

        return "employees/import";
    }
}
