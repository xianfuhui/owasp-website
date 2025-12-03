package com.example.hr.controller;

import com.example.hr.entity.Account;
import com.example.hr.repository.AccountRepository;
import com.example.hr.service.AccountService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AccountController {
    private final AccountRepository repo;
    private final AccountService accountService;

    public AccountController(AccountService accountService, AccountRepository repo) {
        this.accountService = accountService;
        this.repo = repo;
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";  
    }

    @GetMapping("/register")
    public String showRegisterForm() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        String msg = accountService.register(username, password);
        model.addAttribute("message", msg);
        return "register";
    }

    @GetMapping("/home")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Account acc = repo.findByUsername(username);

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
}
