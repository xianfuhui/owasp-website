package com.example.hr.controller;

import com.example.hr.entity.Account;
import com.example.hr.entity.Employee;
import com.example.hr.repository.AccountRepository;
import com.example.hr.service.AccountService;
import com.example.hr.service.ActivityLogService;
import com.example.hr.service.EmployeeService;
import com.example.hr.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AccountController {

    private final AccountRepository repo;
    private final AccountService accountService;
    private final EmployeeService employeeService;
    private final ActivityLogService logService;

    public AccountController(AccountService accountService, AccountRepository repo,
                             EmployeeService employeeService, ActivityLogService logService) {
        this.accountService = accountService;
        this.repo = repo;
        this.employeeService = employeeService;
        this.logService = logService;
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
    // Login / Home / Logout
    // ============================
    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model) {
        Account acc = repo.findByUsername(SecurityUtil.getCurrentUsername());
        model.addAttribute("account", acc);
        return "home";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout=true";
    }

    // ============================
    // Danh sách tài khoản
    // ============================
    @GetMapping("/accounts/list")
    public String list(Model model) {
        model.addAttribute("accounts", accountService.getAll());
        return "accounts/list";
    }

    // ============================
    // Thêm mới
    // ============================
    @GetMapping("/accounts/add")
    public String addForm(Model model) {
        model.addAttribute("account", new Account());
        model.addAttribute("employees", employeeService.getAll());
        model.addAttribute("roles", List.of("USER", "HR", "ADMIN"));
        return "accounts/add";
    }

    @PostMapping("/accounts/add")
    public String add(@ModelAttribute Account acc,
                      @RequestParam String employeeId,
                      @RequestParam String role,
                      Model model) {

        acc.setEmployeeId(employeeId);
        acc.setRole(role);

        try {
            accountService.create(acc);
            logService.log(
                    "CREATE",
                    "Created account for employee: " + employeeId,
                    SecurityUtil.getCurrentUsername()
            );
            return "redirect:/accounts/list";
        } catch (RuntimeException e) {
            model.addAttribute("account", acc);
            model.addAttribute("employees", employeeService.getAll());
            model.addAttribute("roles", List.of("USER", "HR", "ADMIN"));
            model.addAttribute("errorMessage", e.getMessage());
            return "accounts/add";
        }
    }

    // ============================
    // Sửa
    // ============================
    @GetMapping("/accounts/edit/{id}")
    public String editForm(@PathVariable String id, Model model) {
        Account acc = accountService.getById(id);
        if (acc == null) return "redirect:/accounts/list";

        model.addAttribute("account", acc);
        model.addAttribute("employees", employeeService.getAll());
        model.addAttribute("roles", List.of("USER", "HR", "ADMIN"));
        return "accounts/edit";
    }

    @PostMapping("/accounts/edit/{id}")
    public String edit(@PathVariable String id,
                       @ModelAttribute Account acc,
                       @RequestParam String role) {

        Account oldAcc = accountService.getById(id);
        String changes = diff(oldAcc, acc);

        acc.setRole(role);
        accountService.update(id, acc);

        logService.log(
                "UPDATE",
                "Account " + id + " updated. Changes: " + changes,
                SecurityUtil.getCurrentUsername()
        );

        return "redirect:/accounts/list";
    }

    // ============================
    // Xóa
    // ============================
    @GetMapping("/accounts/delete/{id}")
    public String delete(@PathVariable String id) {
        accountService.delete(id);
        logService.log(
                "DELETE",
                "Deleted account: " + id,
                SecurityUtil.getCurrentUsername()
        );
        return "redirect:/accounts/list";
    }

    // ============================
    // Chi tiết
    // ============================
    @GetMapping("/accounts/detail/{id}")
    public String detail(@PathVariable String id, Model model) {
        Account acc = accountService.getById(id);
        model.addAttribute("account", acc);

        Employee emp = employeeService.getById(acc.getEmployeeId());
        model.addAttribute("employee", emp);

        return "accounts/detail";
    }

    @GetMapping("/accounts/change-password")
    public String changePasswordForm(Model model) {
        return "accounts/change-password";
    }

    @PostMapping("/accounts/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {

        String username = SecurityUtil.getCurrentUsername();

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu mới và xác nhận không khớp!");
            return "accounts/change-password";
        }

        String result = accountService.changePassword(username, oldPassword, newPassword);
        if (result.contains("thành công")) {
            model.addAttribute("successMessage", result);
        } else {
            model.addAttribute("errorMessage", result);
        }

        return "accounts/change-password";
    }
}
