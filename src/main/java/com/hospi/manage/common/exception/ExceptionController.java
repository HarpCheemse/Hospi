package com.hospi.manage.common.exception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Controller that renders custom error pages. */
@Controller
@RequestMapping("/error")
public class ExceptionController {
    /**
     * Show the 401 Unauthorized page.
     *
     * @return the 401 error template view name
     */
    @GetMapping("/401")
    public String unAuthorized() {
        return "error/401";
    }
}
