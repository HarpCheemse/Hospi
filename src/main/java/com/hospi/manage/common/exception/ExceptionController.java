package com.hospi.manage.common.exception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ExceptionController {
    @GetMapping("/401")
    public String unAuthorized() {
        System.out.println("HELLO");
        return "error/401";
    }
}
