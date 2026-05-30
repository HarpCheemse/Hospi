package com.hospi.manage.common.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(
            ResourceNotFoundException ex, Model model
    ) {
        model.addAttribute("messege", ex.getMessage());
        return "error/404";
    }
}
