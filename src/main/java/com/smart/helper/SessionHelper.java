package com.smart.helper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SessionHelper {
    
    /**
     * Remove message from session
     */
    public boolean removeMessageFromSession() {
        try {
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
            session.removeAttribute("message");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Clear user-related session data
     */
    public boolean clearUserSession() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpSession session = attr.getRequest().getSession(false);
                if (session != null) {
                    session.removeAttribute("user");
                    session.removeAttribute("userDetails");
                    session.removeAttribute("SPRING_SECURITY_CONTEXT");
                }
                
                // Clear cookies
                HttpServletResponse response = attr.getResponse();
                if (response != null) {
                    Cookie cookie = new Cookie("JSESSIONID", "");
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    
                    // Also remove remember-me cookie if present
                    Cookie rememberMe = new Cookie("remember-me", "");
                    rememberMe.setMaxAge(0);
                    rememberMe.setPath("/");
                    response.addCookie(rememberMe);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Clear form-related session data
     */
    public boolean clearFormData() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpServletRequest request = attr.getRequest();
                HttpSession session = request.getSession(false);
                
                if (session != null) {
                    // Generic session cleanup for forms
                    session.removeAttribute("org.springframework.validation.BindingResult.user");
                    session.removeAttribute("org.springframework.web.servlet.support.SessionFlashMapManager.FLASH_MAPS");
                    session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
                }
                
                // Clear any form-related cookies
                HttpServletResponse response = attr.getResponse();
                if (response != null) {
                    Cookie[] cookies = request.getCookies();
                    if (cookies != null) {
                        for (Cookie cookie : cookies) {
                            if (cookie.getName().startsWith("form_")) {
                                cookie.setValue("");
                                cookie.setMaxAge(0);
                                cookie.setPath("/");
                                response.addCookie(cookie);
                            }
                        }
                    }
                    
                    // Set cache control headers to prevent form data caching
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 