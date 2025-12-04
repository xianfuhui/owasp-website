package com.example.hr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.hr.service.AccountService;

@Configuration
public class SecurityConfig {
        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, AccountService accountService) throws Exception {

                http.csrf().disable();

                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/login", "/register").permitAll()
                                .requestMatchers("/css/**", "/js/**").permitAll()
                                .requestMatchers("/employees/detail/**").hasAnyRole("ADMIN", "HR", "USER")
                                .requestMatchers("/contracts/employee/**", "/contracts/view/**", "/contracts/download/**", "/accounts/change-password").hasAnyRole("ADMIN", "HR", "USER")
                                .requestMatchers("/employees/**", "/contracts/**").hasAnyRole("ADMIN", "HR")
                                .requestMatchers("/accounts/**").hasRole("ADMIN")
                                .anyRequest().authenticated());

                http.formLogin(form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/doLogin")
                                .failureHandler((req, res, ex) -> {
                                        String username = req.getParameter("username");

                                        if (ex.getCause() instanceof LockedException) {
                                                res.sendRedirect("/login?locked=true");
                                                return;
                                        }

                                        if (ex instanceof BadCredentialsException) {
                                                accountService.recordFailedAttempt(username);
                                                res.sendRedirect("/login?badpass=true");
                                                return;
                                        }

                                        res.sendRedirect("/login?error=true");
                                })
                                .successHandler((req, res, auth) -> {
                                        accountService.resetAttempts(auth.getName());
                                        res.sendRedirect("/home");
                                })
                                .permitAll());

                http.logout(logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout=true")
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .permitAll());

                return http.build();
        }
}
