package com.secure.registration;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * Display the registration form (only teachers).
     */
    @GetMapping("/register")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest(null, null, null, null, "STUDENT"));
        return "register";
    }

    /**
     * Handle form submission to register a new user.
     */
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('TEACHER')")
    public String registerUser(@ModelAttribute RegistrationRequest registrationRequest,
                               Model model) {
        try {
            registrationService.register(registrationRequest);
            model.addAttribute("message", "Registration successful. Confirmation email sent.");
            return "dashboard-tutor";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    /**
     * Handle email confirmation link.
     */
    @GetMapping("/register/confirm")
    public String confirmToken(@RequestParam("token") String token,
                               Model model) {
        String result = registrationService.confirmToken(token);
        model.addAttribute("message", result.equals("confirmed") ? "Email confirmed!" : result);
        return "confirm";  // Create a Thymeleaf template for confirmation
    }
}