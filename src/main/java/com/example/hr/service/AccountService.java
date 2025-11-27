package com.example.hr.service;

import com.example.hr.entity.Account;
import com.example.hr.repository.AccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AccountService implements UserDetailsService {

    private final AccountRepository repo;
    private final BCryptPasswordEncoder encoder;

    // Brute-force control
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 5 * 60 * 1000; // 5 phút

    // Password regex: ít nhất 8 ký tự, chữ hoa, chữ thường, số, ký tự đặc biệt
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    public AccountService(AccountRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account acc = repo.findByUsername(username);
        if (acc == null) throw new UsernameNotFoundException("User not found");

        return User.builder()
                .username(acc.getUsername())
                .password(acc.getPassword())
                .accountLocked(acc.isLocked())
                .roles(acc.getRole())
                .build();
    }

    // ================= Đăng nhập kiểm soát brute-force =================
    public String login(String username, String rawPassword) {
        Account acc = repo.findByUsername(username);
        if (acc == null) return "Sai tài khoản hoặc mật khẩu!";

        long now = System.currentTimeMillis();

        // Kiểm tra khóa
        if (acc.isLocked() && now < acc.getLockUntil()) {
            return "Tài khoản bị khóa tạm thời. Thử lại sau!";
        }

        if (acc.isLocked() && now >= acc.getLockUntil()) {
            acc.setLocked(false);
            acc.setFailedAttempts(0);
        }

        // Kiểm tra mật khẩu
        if (!encoder.matches(rawPassword, acc.getPassword())) {
            acc.setFailedAttempts(acc.getFailedAttempts() + 1);
            if (acc.getFailedAttempts() >= MAX_ATTEMPTS) {
                acc.setLocked(true);
                acc.setLockUntil(now + LOCK_DURATION);
            }
            repo.save(acc);
            return "Sai mật khẩu!";
        }

        // Đăng nhập thành công
        acc.setFailedAttempts(0);
        acc.setLocked(false);
        repo.save(acc);

        return "Đăng nhập thành công! Role: " + acc.getRole();
    }

    // ================= Validate password khi đăng ký =================
    public boolean isPasswordValid(String rawPassword) {
        return PASSWORD_PATTERN.matcher(rawPassword).matches();
    }

    // ================= Tạo tài khoản mới =================
    public String register(String username, String rawPassword) {
        if (repo.findByUsername(username) != null) {
            return "Username đã tồn tại!";
        }

        if (!isPasswordValid(rawPassword)) {
            return "Password không hợp lệ! Phải ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.";
        }

        Account acc = new Account();
        acc.setUsername(username);
        acc.setPassword(encoder.encode(rawPassword));
        acc.setRole("USER");
        repo.save(acc);

        return "Đăng ký thành công!";
    }
}
