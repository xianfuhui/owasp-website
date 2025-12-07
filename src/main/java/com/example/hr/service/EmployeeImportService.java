package com.example.hr.service;

import com.example.hr.entity.Employee;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeImportService {

    private final EmployeeService employeeService;

    public EmployeeImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Import danh sách Employee từ Excel
     * @param input Excel input stream
     * @return danh sách Employee đã import
     */
    public List<Employee> importFromExcel(InputStream input) throws Exception {
        List<Employee> imported = new ArrayList<>();

        Workbook workbook = new XSSFWorkbook(input);
        Sheet sheet = workbook.getSheetAt(0);

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
        return imported;
    }

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
