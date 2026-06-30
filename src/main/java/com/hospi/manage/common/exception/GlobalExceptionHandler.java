package com.hospi.manage.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.hospi.manage.common.constant.Attributes.ERROR;

/** Global exception handler mapping exceptions to error views or redirects with flash messages. */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handle a {@link ResourceNotFoundException} by rendering the 404 error page.
     *
     * @param ex    the exception containing the missing resource name
     * @param model the model to pass the resource name to the view
     * @return the 404 error template view name
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("resource",
                ex.getResource());
        return "error/404";
    }

    /**
     * Handle an {@link IllegalStateException} by redirecting home with the exception message as an error flash.
     *
     * @param ex       the exception containing the user-facing message
     * @param redirect redirect attributes to carry the flash message
     * @return redirect URL
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, RedirectAttributes redirect) {
        log.warn("Illegal state: {}", ex.getMessage());
        redirect.addFlashAttribute(ERROR,
                ex.getMessage());
        return "redirect:/";
    }

    /**
     * Handle an {@link IllegalArgumentException} by redirecting home with the exception message as an error flash.
     *
     * @param ex       the exception containing the user-facing message
     * @param redirect redirect attributes to carry the flash message
     * @return redirect URL
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, RedirectAttributes redirect) {
        log.warn("Illegal argument: {}", ex.getMessage());
        redirect.addFlashAttribute(ERROR,
                ex.getMessage());
        return "redirect:/";
    }
}
