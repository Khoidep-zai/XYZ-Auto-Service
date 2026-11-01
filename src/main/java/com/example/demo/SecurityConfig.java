package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean UserDetailsService userDetailsService(AccountRepository accounts) {
        return phone -> accounts.findByPhone(phone)
                .map(a -> User.withUsername(a.phone).password(a.passwordHash).roles(a.role.name()).build())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));
    }

    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/app.js", "/workshop.css", "/vendor/**", "/api/csrf", "/api/state", "/api/register", "/error").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpStatus.UNAUTHORIZED.value()))
                .accessDeniedHandler((req, res, ex) -> res.sendError(HttpStatus.FORBIDDEN.value())))
            .formLogin(form -> form.loginProcessingUrl("/api/login")
                .usernameParameter("phone").passwordParameter("password")
                .successHandler((req, res, auth) -> res.setStatus(HttpStatus.NO_CONTENT.value()))
                .failureHandler((req, res, ex) -> res.sendError(HttpStatus.UNAUTHORIZED.value())))
            .logout(logout -> logout.logoutUrl("/api/logout")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpStatus.NO_CONTENT.value())));
        return http.build();
    }
}
