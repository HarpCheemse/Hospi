package com.hospi.manage.config;

import com.hospi.manage.core.security.session.AccountUserDetailsService;
import com.hospi.manage.core.security.session.RoleBasedAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final AccountUserDetailsService userDetailsService;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final RoleBasedAuthenticationSuccessHandler successHandler;

    @Value("${app.security.remember-me-key}")
    private String rememberMeKey;

    public SecurityConfig(AccountUserDetailsService userDetailsService, CustomAuthEntryPoint customAuthEntryPoint,
                          RoleBasedAuthenticationSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.successHandler = successHandler;
    }

    /**
     * Provide the application-wide password encoder (Argon2).
     *
     * @return an {@link Argon2PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    }

    /**
     * Provide a DAO authentication provider wired with the user details service and password encoder.
     *
     * @return a configured {@link DaoAuthenticationProvider}
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    /**
     * Provide the authentication manager backed by the DAO provider.
     *
     * @return a {@link ProviderManager}
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    /**
     * Build the security filter chain with URL-based authorization, form login, remember-me, and exception handling.
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if the filter chain cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth

                        .requestMatchers("/",
                                "/rooms",
                                "/rooms/**",
                                "/policies",
                                "/contact",
                                "/my-booking/**",
                                "/book",
                                "/book/**",
                                "/error/**",
                                "/hotel-picture/**",
                                "/room-type-picture/**",
                                "/login",
                                "/auth/password/**",
                                "/css/**",
                                "/js/**",
                                "/assets/**",
                                "/error/**").permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        .requestMatchers("/receptionist/**").hasAnyRole("RECEPTIONIST",
                                "ADMIN")
                        .requestMatchers("/leader/**").hasRole("LEADER")

                        .anyRequest().authenticated())

                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true"))

                .sessionManagement(session -> session.sessionFixation().migrateSession()).formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key(rememberMeKey)
                        //30 days
                        .tokenValiditySeconds(60 * 60 * 24 * 30)
                )

                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthEntryPoint));

        return http.build();
    }
}
