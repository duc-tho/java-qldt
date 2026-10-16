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
                    .requestMatchers(
                        "/dot/tao", "/dot/new", "/dot/*/sua", "/dot/*/edit")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY")
                    .requestMatchers(HttpMethod.POST, "/dot", "/dot/*", "/dot/*/xoa")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY")
                    .requestMatchers("/dot/*/hoi-dong", "/dot/*/hoi-dong/**")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY")
                    .requestMatchers("/dot/*/phan-cong", "/dot/*/phan-cong/**")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY")
                    .requestMatchers(HttpMethod.POST, "/dot/*/ket-qua/**")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY")
                    .requestMatchers("/dot/*/danh-gia", "/dot/*/danh-gia/**")
                    .hasRole("LECTURER")
                    .requestMatchers(HttpMethod.GET, "/dot/*/ket-qua")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY", "LECTURER", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/dot/*/nhom/*/bao-cao/tep/*")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY", "LECTURER", "STUDENT")
                    .requestMatchers(HttpMethod.POST, "/dot/*/nhom/*/bao-cao")
                    .hasRole("STUDENT")
                    .requestMatchers(
                        HttpMethod.POST,
                        "/dot/*/de-tai/xoa",
                        "/dot/*/de-tai/*/duyet",
                        "/dot/*/de-tai/*/tu-choi")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/dot/*/de-tai/tao",
                        "/dot/*/de-tai/giang-vien",
                        "/dot/*/de-tai/*/sua")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY", "LECTURER")
                    .requestMatchers(
                        HttpMethod.POST,
                        "/dot/*/de-tai",
                        "/dot/*/de-tai/*",
                        "/dot/*/de-tai/*/xoa")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY", "LECTURER")
                    .requestMatchers(HttpMethod.GET, "/dot", "/dot/*")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY", "LECTURER", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/dot/*/de-tai", "/dot/*/de-tai/*")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY", "LECTURER", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/dot/*/nhom")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY", "LECTURER", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/dot/*/nhom/tao")
                    .hasRole("STUDENT")
                    .requestMatchers(HttpMethod.GET, "/dot/*/nhom/*")
                    .hasAnyRole("ADMIN", "HEAD_OF_FACULTY", "LECTURER", "STUDENT")
                    .requestMatchers(
                        HttpMethod.POST,
                        "/dot/*/nhom/*/duyet-de-tai",
                        "/dot/*/nhom/*/tu-choi-de-tai")
                    .hasRole("LECTURER")
                    .requestMatchers(
                        HttpMethod.POST,
                        "/dot/*/nhom",
                        "/dot/*/nhom/*/thanh-vien",
                        "/dot/*/nhom/*/thanh-vien/*/xoa",
                        "/dot/*/nhom/*/truong-nhom",
                        "/dot/*/nhom/*/gui-duyet",
                        "/dot/*/nhom/*/de-tai")
                    .hasRole("STUDENT")
                    // --------
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/student-group/*/reports/files/*")
                    .hasAnyRole("STUDENT", "LECTURER", "HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/student-group/*")
                    .hasAnyRole("STUDENT", "LECTURER", "HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/student-group/*/topic")
                    .hasAnyRole("STUDENT", "LECTURER", "HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/student-group/**")
                    .hasRole("STUDENT")
                    .requestMatchers(HttpMethod.GET, "/student-groups/**")
                    .hasRole("STUDENT")
                    .requestMatchers(HttpMethod.GET, "/topic-registrations/**")
                    .hasRole("LECTURER")
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/councils")
                    .hasAnyRole("HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/councils/**")
                    .hasAnyRole("HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/council-workspace/**")
                    .hasRole("LECTURER")
                    .requestMatchers(HttpMethod.GET, "/topic-proposals/**")
                    .hasRole("LECTURER")
                    .requestMatchers(HttpMethod.GET, "/registration-periods/{periodId}/topics/propose")
                    .hasRole("LECTURER")
                    .requestMatchers(HttpMethod.GET, "/registration-periods/*/topics/new")
                    .hasAnyRole("HEAD_OF_FACULTY", "ADMIN")
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
                        HttpMethod.GET, "/registration-periods/**")
                    .hasAnyRole("HEAD_OF_FACULTY", "ADMIN")
                    .requestMatchers("/announcements/**", "/departments/**")
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
