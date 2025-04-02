package com.bu.dong.fuseki.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()  // 开放接口
                        .requestMatchers("/admin/**").hasRole("ADMIN")  // 角色控制
                        .anyRequest().authenticated()  // 其他需认证
                )
                .formLogin(form -> form
                        .loginPage("/custom-login")  // 自定义登录页
                        .defaultSuccessUrl("/dashboard")
                )
                .logout(logout -> logout
                        .logoutUrl("/custom-logout")
                        .deleteCookies("JSESSIONID")
                );  // 开发环境禁用CSRF
        return http.build();
    }
}