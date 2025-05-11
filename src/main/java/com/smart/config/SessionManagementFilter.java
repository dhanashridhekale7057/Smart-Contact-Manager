package com.smart.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

/**
 * Filter to manage session cleanup and prevent form data persistence
 */
@Component
public class SessionManagementFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization required
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
        
        // Set cache control headers to prevent browsers from caching pages with form data
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Add security headers
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        
        // Check if this is a page with a form (login, signup)
        String uri = request.getRequestURI();
        if ((uri.contains("/signin") || uri.contains("/signup")) && "GET".equals(request.getMethod())) {
            if (session != null) {
                // Remove form data from session when viewing login/signup pages
                session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
            }
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup required
    }
} 