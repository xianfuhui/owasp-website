package com.example.hr.controller;

import com.example.hr.entity.Account;
import com.example.hr.entity.Employee;
import com.example.hr.service.AccountService;
import com.example.hr.service.EmployeeService;
import com.example.hr.service.ActivityLogService;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;
    private final AccountService accountService;
    private final ActivityLogService logService;

    public EmployeeController(EmployeeService service,
                              AccountService accountService,
                              ActivityLogService logService) {
        this.service = service;
        this.accountService = accountService;
        this.logService = logService;
    }

    // ============================
    // Utility để diff dữ liệu update
    // ============================
    private String diff(Employee oldEmp, Employee newEmp) {
        StringBuilder sb = new StringBuilder();
        compare(sb, "fullName", oldEmp.getFullName(), newEmp.getFullName());
        compare(sb, "email", oldEmp.getEmail(), newEmp.getEmail());
        compare(sb, "phone", oldEmp.getPhone(), newEmp.getPhone());
        compare(sb, "position", oldEmp.getPosition(), newEmp.getPosition());
        compare(sb, "salary", oldEmp.getSalary(), newEmp.getSalary());
        compare(sb, "avatarPath", oldEmp.getAvatarPath(), newEmp.getAvatarPath());
        return sb.length() == 0 ? "No changes" : sb.toString();
    }

    private void compare(StringBuilder sb, String field, Object oldVal, Object newVal) {
        if (oldVal == null && newVal == null) return;
        if (oldVal == null || !oldVal.equals(newVal)) {
            sb.append(field)
              .append(": '").append(oldVal)
              .append("' → '").append(newVal)
              .append("'; ");
        }
    }

    // ============================
    // Controller
    // ============================

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("employees", service.getAll());
        return "employees/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "employees/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Employee emp) {
        service.create(emp);

        logService.log(
            "CREATE",
            "Created employee: " + emp.getFullName(),
            "system"
        );

        return "redirect:/employees/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable String id, Model model) {

        Employee emp = service.getById(id);
        if (emp == null) return "redirect:/employees";

        Account acc = null;
        if (emp.getId() != null) {
            acc = accountService.getByEmployeeId(emp.getId());
        }

        model.addAttribute("employee", emp);
        model.addAttribute("account", acc);
        model.addAttribute("roles", List.of("EMPLOYEE", "HR", "ADMIN"));

        return "employees/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable String id,
                       @ModelAttribute Employee emp,
                       @RequestParam("role") String role) {

        Employee oldEmp = service.getById(id);
        String changes = diff(oldEmp, emp);

        service.update(id, emp, role);

        logService.log(
            "UPDATE",
            "Employee " + id + " updated. Changes: " + changes,
            "system"
        );

        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id) {
        service.delete(id);

        logService.log(
            "DELETE",
            "Deleted employee: " + id,
            "system"
        );

        return "redirect:/employees";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable String id, Model model) {
        Employee emp = service.getById(id);
        model.addAttribute("employee", emp);
        return "employees/detail";
    }

    @GetMapping("/accounts/{employeeId}")
    public String accountsOfEmployee(@PathVariable String employeeId, Model model) {

        // Lấy employee
        Employee emp = service.getById(employeeId);
        if (emp == null) {
            return "redirect:/employees/list";
        }

        // Lấy danh sách account của employee
        Account acc = accountService.getByEmployeeId(employeeId);

        model.addAttribute("employee", emp);
        model.addAttribute("account", acc);

        return "employees/accounts"; // view mới
    }

    @GetMapping("/search")
    public String showSearch() {
        return "employees/search"; 
    }

    @GetMapping("/search-vulnerable")
    public String searchVuln(@RequestParam(value = "q", required = false) String q, Model model) {

        logService.log(
            "SEARCH",
            "Search vulnerable with query: " + q,
            "system"
        );

        List<Employee> list = service.searchVulnerable(q);
        model.addAttribute("employees", list);
        return "employees/search";
    }

    @GetMapping("/search-safe")
    public String searchSafe(@RequestParam(value = "q", required = false) String q, Model model) {

        logService.log(
            "SEARCH",
            "Search safe with query: " + q,
            "system"
        );

        List<Employee> list = service.searchSafe(q);
        model.addAttribute("employees", list);
        return "employees/search";
    }
}
