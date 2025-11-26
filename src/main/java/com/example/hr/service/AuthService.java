package com.example.hr.service;

import com.example.hr.entity.Account;
import com.example.hr.repository.AccountRepository;
import com.example.hr.util.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AccountRepository accountRepo;

    public AuthService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    public boolean login(String username, String password) {

        Account acc = accountRepo.findByUsername(username)
                .orElse(null);

        if (acc == null) return false;

        if (acc.isLocked()) return false;

        if (!PasswordUtil.match(password, acc.getPassword())) {
            acc.setFailedAttempts(acc.getFailedAttempts() + 1);

            if (acc.getFailedAttempts() >= 5) {
                acc.setLocked(true);
            }

            accountRepo.save(acc);
            return false;
        }

        acc.setFailedAttempts(0);
        accountRepo.save(acc);
        return true;
    }
}
