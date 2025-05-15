package com.personalblog.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Map<Integer, String> ERROR_MESSAGES = new HashMap<>();
    
    static {
        ERROR_MESSAGES.put(400, "Bad request. Your browser sent a request that this server could not understand.");
        ERROR_MESSAGES.put(401, "You need to log in to access this resource.");
        ERROR_MESSAGES.put(403, "You don't have permission to access this resource.");
        ERROR_MESSAGES.put(404, "We couldn't find the content you were looking for. The post may have been removed, renamed, or never existed in the first place.");
        ERROR_MESSAGES.put(500, "Something went wrong on our end. We're working to fix the issue.");
        ERROR_MESSAGES.put(503, "The service is temporarily unavailable. Please try again later.");
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error status
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        int statusCode = 500; // Default to internal server error
        
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }
        
        // Get error message
        String errorMessage = ERROR_MESSAGES.getOrDefault(
            statusCode, 
            "An unexpected error occurred. Please try again later."
        );
        
        // Add error information to the model
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMessage);
        
        // Add request URI if available
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (path != null) {
            model.addAttribute("path", path.toString());
        }
        
        // Add exception details if available (for 500 errors in development)
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (exception != null && statusCode == 500) {
            model.addAttribute("exception", exception.toString());
        }
        
        // Determine which error template to use
        switch (statusCode) {
            case 404:
                return "error/404";
            case 403:
                return "error/403";
            case 401:
                return "error/401";
            default:
                return "error/generic";
        }
    }
} 