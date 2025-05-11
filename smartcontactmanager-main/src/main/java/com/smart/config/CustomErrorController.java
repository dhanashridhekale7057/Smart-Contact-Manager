package com.smart.config;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error status
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = "An error occurred. Please try again.";
        String errorTitle = "Error";
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                // 404 error
                errorMessage = "The page you are looking for does not exist.";
                errorTitle = "Page Not Found";
            } else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                // 500 error
                errorMessage = "Internal server error. Please try again later.";
                errorTitle = "Server Error";
            } else if(statusCode == HttpStatus.FORBIDDEN.value()) {
                // 403 error
                errorMessage = "You don't have permission to access this resource.";
                errorTitle = "Access Denied";
            }
        }
        
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("title", "Error - Smart Contact Manager");
        
        return "error";
    }
    
    public String getErrorPath() {
        return "/error";
    }
} 