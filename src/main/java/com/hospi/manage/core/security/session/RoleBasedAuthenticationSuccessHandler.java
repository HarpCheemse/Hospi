package com.hospi.manage.core.security.session;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .orElseThrow()
                .getAuthority();

        System.out.println(authentication.isAuthenticated());
        System.out.println(request.getSession(false).getId());

        switch (role) {
            case "ROLE_ADMIN" -> response.sendRedirect("/admin");

            case "ROLE_MANAGER" -> response.sendRedirect("/manager");

            case "ROLE_RECEPTIONIST" -> response.sendRedirect("/receptionist");

            case "ROLE_LEADER" -> response.sendRedirect("/leader");

            default -> response.sendRedirect("/error/401");
        }
    }
}
