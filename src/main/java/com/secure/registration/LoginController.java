package com.secure.registration;

import com.secure.course.CourseRepository;
import com.secure.course.Course;

import com.secure.appuser.AppUser;
import com.secure.appuser.AppUserRepository;
import com.secure.email.EmailSender;
import com.secure.registration.token.OtpTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class LoginController {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private OtpTokenService otpTokenService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/verify-otp")
    public String showOtpPage() {
        return "otp-verification";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AppUser user = (AppUser) auth.getPrincipal();
            
            otpTokenService.getOtp(otp);
            otpTokenService.setConfirmedAt(otp);

            // Redirect based on user role
            if (user.getAppUserRole().name().equals("STUDENT")) {
                return "redirect:/dashboard-student";
            } else {
                return "redirect:/dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Invalid or expired OTP");
            return "otp-verification";
        }
    }

//    @GetMapping("/dashboard")
//    public String dashboard(Model model) {
//        List<Course> courses = courseRepository.findAll();
//        model.addAttribute("courses", courses);
//        model.addAttribute("activeTab", "courses");
//        return "dashboard-tutor";
//    }

//    @GetMapping("/attendance")
//    public String attendance(Model model) {
//        List<Course> courses = courseRepository.findAll();
//        model.addAttribute("courses", courses);
//        model.addAttribute("activeTab", "courses");
//        return "attendance";
//    }

    @GetMapping("/users")
    public String users(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        model.addAttribute("activeTab", "users");
        List<AppUser> users = appUserRepository.findAll();
        model.addAttribute("users", users);
        return "users";
    }

}
