package com.secure.registration;

import com.secure.course.CourseRepository;
import com.secure.course.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class LoginController {
    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        model.addAttribute("activeTab", "courses");
        return "dashboard-tutor";
    }

    @GetMapping("/attendance")
    public String attendance(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        model.addAttribute("activeTab", "courses");
        return "attendance";
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        model.addAttribute("activeTab", "courses");
        return "users";
    }
}
