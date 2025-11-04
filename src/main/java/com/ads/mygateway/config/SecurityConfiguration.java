//package com.ads.mygateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfiguration {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .cors() // ✅ Enable CORS
//                .and()
//                .csrf().disable() // Disable CSRF if you use APIs
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/**").permitAll() // adjust as needed
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
//}
