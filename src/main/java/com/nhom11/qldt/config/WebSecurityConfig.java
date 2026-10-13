package com.nhom11.qldt.config;

import com.nhom11.qldt.feature.auth.model.dto.LoginForm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/**")
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/login",
                        "/assets/**",
                        "/images/**",
                        "/favicon.svg",
                        "/favicon-96x96.png",
                        "/favicon.ico",
                        "/apple-touch-icon.png",
                        "/web-app-manifest-192x192.png",
                        "/web-app-manifest-512x512.png",
                        "/site.webmanifest")
                    .permitAll()
                    // --------
                    .requestMatchers("/users/**")
                    .hasRole("ADMIN")
                    // --------
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/student-group/*/reports/files/*")
                    .hasAnyRole("STUDENT", "LECTURER", "HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/student-group/*")
                    .hasAnyRole("STUDENT", "LECTURER", "HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/student-group/*/topic")
                    .hasAnyRole("STUDENT", "LECTURER", "HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers("/registration-periods/*/student-group/**")
                    .hasRole("STUDENT")
                    .requestMatchers("/student-groups/**")
                    .hasRole("STUDENT")
                    .requestMatchers("/topic-registrations/**")
                    .hasRole("LECTURER")
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/councils")
                    .hasAnyRole("HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers("/registration-periods/*/councils/**")
                    .hasAnyRole("HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers("/council-workspace/**")
                    .hasRole("LECTURER")
                    .requestMatchers("/council-evaluations/**")
                    .hasRole("LECTURER")
                    .requestMatchers("/topic-proposals/**")
                    .hasRole("LECTURER")
                    .requestMatchers("/registration-periods/{periodId}/topics/propose")
                    .hasRole("LECTURER")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/registration-periods",
                        "/registration-periods/*",
                        "/registration-periods/*/groups",
                        "/registration-periods/*/topics",
                        "/registration-periods/*/topics/*")
                    .hasAnyRole("HEAD_OF_FACULTY", "ADMIN", "LECTURER")
                    // --------
                    .requestMatchers(
                        "/registration-periods/**", "/announcements/**", "/departments/**")
                    .hasAnyRole("HEAD_OF_FACULTY", "ADMIN")
                    // --------
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/", true)
                    .failureHandler(
                        (request, response, exception) -> {
                          request
                              .getSession()
                              .setAttribute(
                                  LoginForm.SESSION_EMAIL_ATTRIBUTE, request.getParameter("email"));
                          response.sendRedirect(request.getContextPath() + "/login?error");
                        })
                    .permitAll())
        .logout(
            logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true));

    return http.build();
  }
}
