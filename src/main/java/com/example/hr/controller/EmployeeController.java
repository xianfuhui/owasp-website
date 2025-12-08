package com.example.hr.controller;

import com.example.hr.entity.Account;
import com.example.hr.entity.Employee;
import com.example.hr.service.AccountService;
import com.example.hr.service.EmployeeService;
import com.example.hr.util.AccessControlUtil;
import com.example.hr.util.SecurityUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService service;
    private final AccountService accountService;

    public EmployeeController(EmployeeService service,
                              AccountService accountService) {
        this.service = service;
        this.accountService = accountService;
    }

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

    @GetMapping("/list")
    public String list(Model model) {
        String user = SecurityUtil.getCurrentUsername();
        logger.info("User {} requested employee list", user);

        try {
            model.addAttribute("employees", service.getAll());
        } catch (RuntimeException e) {
            logger.error("Error listing employees by {}: {}", user, e.getMessage());
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "employees/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "employees/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Employee emp, Model model) {
        String user = SecurityUtil.getCurrentUsername();
        try {
            Employee created = service.create(emp);
            logger.info("Employee {} created by {}", created.getId(), user);
            model.addAttribute("successMessage", "Tạo nhân viên thành công!");
        } catch (RuntimeException e) {
            logger.error("Error creating employee by {}: {}", user, e.getMessage());
            model.addAttribute("employee", emp);
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "employees/add";
        }

        return "redirect:/employees/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable String id, Model model) {
        String user = SecurityUtil.getCurrentUsername();
        try {
            Employee emp = service.getById(id);
            if (emp == null) throw new RuntimeException("Nhân viên không tồn tại");

            model.addAttribute("employee", emp);
            model.addAttribute("roles", List.of("EMPLOYEE", "HR", "ADMIN"));
        } catch (RuntimeException e) {
            logger.error("Error opening edit form for employee {} by {}: {}", id, user, e.getMessage());
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/employees/list";
        }

        return "employees/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable String id,
                       @ModelAttribute Employee emp,
                       Model model) {

        String user = SecurityUtil.getCurrentUsername();
        try {
            Employee oldEmp = service.getById(id);
            if (oldEmp == null) throw new RuntimeException("Nhân viên không tồn tại");

            String changes = diff(oldEmp, emp);
            service.update(id, emp);

            logger.info("Employee {} updated by {}. Changes: {}", id, user, changes);
            model.addAttribute("successMessage", "Cập nhật nhân viên thành công!");
        } catch (RuntimeException e) {
            logger.error("Error updating employee {} by {}: {}", id, user, e.getMessage());
            model.addAttribute("employee", emp);
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "employees/edit";
        }

        return "employees/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id, Model model) {
        String user = SecurityUtil.getCurrentUsername();
        try {
            boolean deleted = service.delete(id);
            if (!deleted) throw new RuntimeException("Nhân viên không tồn tại");

            logger.info("Employee {} deleted by {}", id, user);
            model.addAttribute("successMessage", "Xóa nhân viên thành công!");
        } catch (RuntimeException e) {
            logger.error("Error deleting employee {} by {}: {}", id, user, e.getMessage());
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/employees/list";
    }

    @GetMapping("/detail/{id}")
    public String getEmployeeDetail(@PathVariable String id, Model model) {
        String username = SecurityUtil.getCurrentUsername();
        try {
            Account currentAccount = accountService.getByUsername(username);
            //-------------
            //A01 – Broken Access Control 
            // AccessControlUtil.checkViewOrDownload(currentAccount, id);
            // -------------

            Employee emp = service.getById(id);
            if (emp == null) throw new RuntimeException("Nhân viên không tồn tại");

            model.addAttribute("employee", emp);
        } catch (RuntimeException e) {
            logger.error("Error viewing employee {} by {}: {}", id, username, e.getMessage());
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/employees/list";
        }

        return "employees/detail";
    }

    @GetMapping("/")
    public String getCurrentEmployeeDetail(@AuthenticationPrincipal UserDetails user, Model model) {
        try {
            Account currentAccount = accountService.getByUsername(user.getUsername());
            if (currentAccount == null || currentAccount.getEmployeeId() == null) {
                throw new RuntimeException("Không tìm thấy thông tin nhân viên cho tài khoản hiện tại");
            }

            Employee emp = service.getById(currentAccount.getEmployeeId());
            if (emp == null) {
                throw new RuntimeException("Nhân viên không tồn tại");
            }

            model.addAttribute("employee", emp);

        } catch (RuntimeException e) {
            logger.error("Error viewing current employee info for {}: {}", user.getUsername(), e.getMessage());
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/employees/list";
        }

        return "employees/detail";
    }

    @GetMapping("/accounts/{employeeId}")
    public String accountsOfEmployee(@PathVariable String employeeId, Model model) {
        String user = SecurityUtil.getCurrentUsername();
        try {
            Employee emp = service.getById(employeeId);
            if (emp == null) throw new RuntimeException("Nhân viên không tồn tại");

            List<Account> accs = accountService.getListByEmployeeId(employeeId);

            model.addAttribute("employee", emp);
            model.addAttribute("accounts", accs);
            model.addAttribute("currentAccount", accountService.getByUsername(SecurityUtil.getCurrentUsername()));
        } catch (RuntimeException e) {
            logger.error("Error fetching accounts for employee {} by {}: {}", employeeId, user, e.getMessage());
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/employees/list";
        }

        return "employees/accounts";
    }

    @GetMapping("/search")
    public String showSearch() {
        return "employees/search";
    }

    @GetMapping("/search-vulnerable")
    public String searchVuln(@RequestParam(value = "q", required = false) String q, Model model) {
        try {
            List<Employee> list = service.searchVulnerable(q);
            model.addAttribute("employees", list);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "employees/search";
    }

    @GetMapping("/search-safe")
    public String searchSafe(@RequestParam(value = "q", required = false) String q, Model model) {
        try {
            List<Employee> list = service.searchSafe(q);
            model.addAttribute("employees", list);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "employees/search";
    }
}
