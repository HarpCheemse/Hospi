package com.hospi.manage.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Authentication entry point that redirects unauthenticated requests to the 401 error page. */
@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    /**
     * Redirect the client to the 401 error page on authentication failure.
     *
     * @param request       the HTTP request
     * @param response      the HTTP response
     * @param authException the authentication exception that triggered the entry point
     * @throws IOException      if an I/O error occurs during the redirect
     * @throws ServletException if the redirect fails
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        request.setAttribute(
                RequestDispatcher.ERROR_STATUS_CODE,
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.sendRedirect("/error/401");
    }
}