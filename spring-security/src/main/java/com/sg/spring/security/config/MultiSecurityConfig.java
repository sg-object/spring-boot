package com.sg.spring.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Profile("multi")
@EnableWebSecurity(debug = true)
@Configuration
public class MultiSecurityConfig {

  @Order(1)
  @Bean
  SecurityFilterChain firstChain(final HttpSecurity http) throws Exception {
    http.securityMatchers(auth -> auth.requestMatchers("/test/**"));

    http.authorizeHttpRequests(req -> req.requestMatchers("/test/pass").permitAll());

    return http.build();
  }

  @Order(2)
  @Bean
  SecurityFilterChain secondChain(final HttpSecurity http) throws Exception {
    http.securityMatchers(auth -> auth.requestMatchers("/admin/**"));

    /*
     * 기본적으로 CSRF Filter가 설정되어 있음
     * CSRF를 비활성하면 Filter Chain에서 CsrfFilter가 제거됨
     * */
    http.csrf(AbstractHttpConfigurer::disable);

    http.authorizeHttpRequests(req -> req.requestMatchers("/admin/pass").permitAll());

    return http.build();
  }
}
