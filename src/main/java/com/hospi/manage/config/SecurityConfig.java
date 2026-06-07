package com.hospi.manage.config;

import com.hospi.manage.core.security.session.AccountUserDetailsService;
import com.hospi.manage.core.security.session.RoleBasedAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.Provider;

@Configuration
public class SecurityConfig {

    private final AccountUserDetailsService userDetailsService;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final RoleBasedAuthenticationSuccessHandler successHandler;

    public SecurityConfig(AccountUserDetailsService userDetailsService,
                          CustomAuthEntryPoint customAuthEntryPoint,
                          RoleBasedAuthenticationSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/rooms", "/policies", "/contact", "/my-booking", "/login", "/css/**", "/js/**",
                                "/error/**", "/hotel-picture/**", "/room-type-picture/**").permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        .requestMatchers("/receptionist/**").hasAnyRole("RECEPTIONIST", "ADMIN")

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                )

                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint)
                );

        return http.build();
    }
}
