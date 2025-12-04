package com.example.hr.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.hr.entity.Account;
import com.example.hr.service.AccountService;

public class SecurityUtil {

    private static AccountService accountService;

    private SecurityUtil() {} // private constructor để không thể khởi tạo

    // Phương thức để Spring inject AccountService tĩnh
    public static void setAccountService(AccountService service) {
        accountService = service;
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }
}
