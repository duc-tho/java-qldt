package com.nhom11.qldt.config;

import com.nhom11.qldt.feature.auth.model.dto.LoginForm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                auth.requestMatchers("/login", "/assets/**", "/images/**")
                    .permitAll()
                    // --------
                    .requestMatchers("/users/**")
                    .hasRole("ADMIN")
                    // --------
                    .requestMatchers("/registration-periods/**", "/announcements/**", "/departments/**")
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
                    .defaultSuccessUrl("/", false)
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
