package com.example.hr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {

        http.csrf().disable()
            // .authorizeHttpRequests(auth -> auth
            //         .requestMatchers("/login", "/css/**").permitAll()
            //         .anyRequest().authenticated()
            // )
            // .formLogin(login -> login
            //         .loginPage("/login")
            //         .defaultSuccessUrl("/dashboard", true)
            // )
            // .logout(logout -> logout
            //         .logoutUrl("/logout")
            //         .logoutSuccessUrl("/login?logout")
            // );
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()   // cho phép truy cập tất cả
            )
            .formLogin().disable()              // tắt login
            .logout().disable();                // tắt logout

        return http.build();
    }
}
