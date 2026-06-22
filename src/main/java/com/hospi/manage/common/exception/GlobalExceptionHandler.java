package com.hospi.manage.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.hospi.manage.common.constant.Attributes.ERROR;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("resource",
                ex.getResource());
        return "error/404";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, RedirectAttributes redirect) {
        log.warn("Illegal state: {}", ex.getMessage());
        redirect.addFlashAttribute(ERROR,
                ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, RedirectAttributes redirect) {
        log.warn("Illegal argument: {}", ex.getMessage());
        redirect.addFlashAttribute(ERROR,
                ex.getMessage());
        return "redirect:/";
    }
}
