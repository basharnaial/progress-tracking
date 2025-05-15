package com.secure.registration;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard-tutor"; // dashboard-tutor.html
    }


    @GetMapping("/attendance")
    public String attendance() {
        return "attendance"; // attendance.html
    }

    @GetMapping("/users")
    public String users() {
        return "users"; // dashboard-tutor.html
    }

}
