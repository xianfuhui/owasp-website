package com.example.hr.controller;

import com.example.hr.entity.Employee;
import com.example.hr.service.EmployeeService;
import com.example.hr.service.ActivityLogService;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/employees/import")
public class EmployeeImportController {

    private final EmployeeService employeeService;
    private final ActivityLogService logService;

    public EmployeeImportController(EmployeeService employeeService,
                                    ActivityLogService logService) {
        this.employeeService = employeeService;
        this.logService = logService;
    }

    // Form upload file
    @GetMapping
    public String importPage() {
        return "employees/import";
    }

    // Xử lý file Excel
    @PostMapping
    public String importExcel(@RequestParam("file") MultipartFile file, Model model) {
        List<Employee> imported = new ArrayList<>();

        try {
            InputStream input = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(input);
            Sheet sheet = workbook.getSheetAt(0);

            // Dòng đầu là header, bắt đầu từ row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Employee emp = new Employee();

                emp.setFullName(getString(row, 0));
                emp.setEmail(getString(row, 1));
                emp.setPhone(getString(row, 2));
                emp.setPosition(getString(row, 3));
                emp.setSalary(getDouble(row, 4));
                emp.setAvatarPath(null);

                employeeService.create(emp);
                imported.add(emp);
            }

            workbook.close();

            logService.log(
                "IMPORT",
                "Imported " + imported.size() + " employees from Excel",
                "system"
            );

            model.addAttribute("imported", imported);
            return "employees/list";

        } catch (Exception e) {
            model.addAttribute("error", "Import failed: " + e.getMessage());
            return "employees/list";
        }
    }

    // =============================
    // Helper functions
    // =============================
    private String getString(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? null : cell.toString().trim();
    }

    private double getDouble(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return 0;
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            try {
                return Double.parseDouble(cell.toString());
            } catch (Exception ex) {
                return 0;
            }
        }
    }
}
