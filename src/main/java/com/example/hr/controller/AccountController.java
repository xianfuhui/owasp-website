package com.example.hr.controller;

import com.example.hr.entity.Account;
import com.example.hr.entity.Employee;
import com.example.hr.service.AccountService;
import com.example.hr.service.EmployeeService;
import com.example.hr.util.SecurityUtil;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final EmployeeService employeeService;

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    public AccountController(AccountService accountService, EmployeeService employeeService) {
        this.accountService = accountService;
        this.employeeService = employeeService;
    }

    // ============================
    // Utility để diff dữ liệu update
    // ============================
    private String diff(Account oldAcc, Account newAcc) {
        StringBuilder sb = new StringBuilder();
        if (!oldAcc.getUsername().equals(newAcc.getUsername())) {
            sb.append("Username: ").append(oldAcc.getUsername())
              .append(" -> ").append(newAcc.getUsername()).append("; ");
        }
        if (newAcc.getPassword() != null && !newAcc.getPassword().isEmpty()) {
            sb.append("Password changed; ");
        }
        if (!oldAcc.getRole().equals(newAcc.getRole())) {
            sb.append("Role: ").append(oldAcc.getRole())
              .append(" -> ").append(newAcc.getRole()).append("; ");
        }
        return sb.toString();
    }

    // ============================
    // Danh sách tài khoản
    // ============================
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("accounts", accountService.getAll());
        return "accounts/list";
    }

    // ============================
    // Thêm mới
    // ============================
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("account", new Account());
        model.addAttribute("employees", employeeService.getAll());
        model.addAttribute("roles", List.of("USER", "HR", "ADMIN"));
        return "accounts/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Account acc,
                      @RequestParam String employeeId,
                      @RequestParam String role,
                      Model model) {

        acc.setEmployeeId(employeeId);
        acc.setRole(role);
        String username = SecurityUtil.getCurrentUsername();

        model.addAttribute("account", acc);
        model.addAttribute("employees", employeeService.getAll());
        model.addAttribute("roles", List.of("USER", "HR", "ADMIN"));

        try {
            accountService.create(acc);
            logger.info("CREATE | Created account for employee: {} by {}", employeeId, username);
            model.addAttribute("successMessage", "Tạo tài khoản thành công cho nhân viên: " + employeeId);
        } catch (RuntimeException e) {
            logger.error("CREATE FAILED | Failed to create account for employee: {} by {}. Error={}",
                    employeeId, username, e.getMessage());
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "accounts/add";
    }

    // ============================
    // Sửa
    // ============================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable String id, Model model) {
        Account acc = accountService.getById(id);
        if (acc == null) {
            model.addAttribute("errorMessage", "Tài khoản không tồn tại!");
            model.addAttribute("accounts", accountService.getAll());
            return "accounts/list";
        }
        model.addAttribute("account", acc);
        model.addAttribute("employees", employeeService.getAll());
        model.addAttribute("roles", List.of("USER", "HR", "ADMIN"));
        return "accounts/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable String id,
                       @ModelAttribute Account acc,
                       @RequestParam String role,
                       Model model) {

        String username = SecurityUtil.getCurrentUsername();

        model.addAttribute("account", acc);
        model.addAttribute("employees", employeeService.getAll());
        model.addAttribute("roles", List.of("USER", "HR", "ADMIN"));

        try {
            Account oldAcc = accountService.getById(id);
            if (oldAcc == null) throw new RuntimeException("Tài khoản không tồn tại!");

            String changes = diff(oldAcc, acc);
            acc.setRole(role);
            accountService.update(id, acc);

            logger.info("UPDATE | Account {} updated by {}. Changes: {}", id, username, changes);
            model.addAttribute("successMessage", "Cập nhật tài khoản thành công. Thay đổi: " + changes);
        } catch (RuntimeException e) {
            logger.error("UPDATE FAILED | Account {} update by {} failed. Error={}", id, username, e.getMessage());
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "accounts/edit";
    }

    // ============================
    // Xóa
    // ============================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id, Model model) {
        String username = SecurityUtil.getCurrentUsername();
        try {
            accountService.delete(id);
            logger.info("DELETE | Deleted account {} by {}", id, username);
            model.addAttribute("successMessage", "Xóa tài khoản thành công!");
        } catch (RuntimeException e) {
            logger.error("DELETE FAILED | Account {} deletion by {} failed. Error={}", id, username, e.getMessage());
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        model.addAttribute("accounts", accountService.getAll());
        return "redirect:/accounts/list";
    }

    // ============================
    // Chi tiết
    // ============================
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable String id, Model model) {
        Account acc = accountService.getById(id);
        if (acc == null) {
            model.addAttribute("errorMessage", "Tài khoản không tồn tại!");
            model.addAttribute("accounts", accountService.getAll());
            return "accounts/list";
        }
        model.addAttribute("account", acc);
        Employee emp = employeeService.getById(acc.getEmployeeId());
        model.addAttribute("employee", emp);
        return "accounts/detail";
    }

    // ============================
    // Đổi mật khẩu
    // ============================
    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        return "accounts/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {

        String username = SecurityUtil.getCurrentUsername();

        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("Mật khẩu mới và xác nhận không khớp!");
            }

            String result = accountService.changePassword(username, oldPassword, newPassword);
            if (result.contains("thành công")) {
                logger.info("PASSWORD | {} changed password successfully", username);
                model.addAttribute("successMessage", result);
            } else {
                throw new RuntimeException(result);
            }

        } catch (RuntimeException e) {
            logger.error("PASSWORD FAILED | {} failed to change password. Error={}", username, e.getMessage());
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "accounts/change-password";
    }
}
