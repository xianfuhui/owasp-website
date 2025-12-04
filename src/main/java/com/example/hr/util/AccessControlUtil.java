package com.example.hr.util;

import com.example.hr.entity.Account;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AccessControlUtil {

    private AccessControlUtil() {} 

    public static void checkViewOrDownload(Account currentAccount, String targetEmployeeId) {
        String role = currentAccount.getRole();

        // ADMIN hoặc HR được phép xem tất cả
        if ("ADMIN".equals(role) || "HR".equals(role)) {
            return;
        }

        // USER chỉ được xem/tải file của chính mình
        if (!targetEmployeeId.equals(currentAccount.getEmployeeId())) {
            System.out.println("Access denied for user: " + currentAccount.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không được phép truy cập");
        }

        System.out.println("Access granted for user: " + currentAccount.getUsername());
    }
}