package com.secure.registration;

import com.secure.course.CourseRepository;
import com.secure.course.Course;
import com.secure.model.StudentCourseRepository;
import com.secure.model.StudentCourse;
import com.secure.appuser.AppUser;
import com.secure.appuser.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class LoginController {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private AppUserRepository appUserRepository;

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

    @GetMapping("/dashboard-student")
    public String dashboardStudent(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser user = (AppUser) authentication.getPrincipal();
        List<StudentCourse> enrollments = studentCourseRepository.findByStudent(user);
        model.addAttribute("enrollments", enrollments);
        // حساب المعدل التراكمي GPA للطالب
        double gpaSum = 0;
        int gpaCount = 0;
        for (StudentCourse sc : enrollments) {
            if (Boolean.TRUE.equals(sc.getFinished())) {
                double total =
                        (sc.getMidterm() != null ? sc.getMidterm() : 0) +
                        (sc.getFinalExam() != null ? sc.getFinalExam() : 0) +
                        (sc.getAssignments() != null ? sc.getAssignments() : 0) +
                        (sc.getQuizzes() != null ? sc.getQuizzes() : 0) +
                        (sc.getProject() != null ? sc.getProject() : 0) +
                        (sc.getAttendance() != null ? sc.getAttendance() : 0);
                double gpa = (total / 100.0) * 4.0;
                gpaSum += gpa;
                gpaCount++;
            }
        }
        double studentGpa = gpaCount > 0 ? gpaSum / gpaCount : 0;
        String studentGpaLabel;
        String studentGpaColor;
        if (studentGpa >= 3.7) { studentGpaLabel = "Excellent"; studentGpaColor = "#228B22"; }
        else if (studentGpa >= 3.3) { studentGpaLabel = "Very Good"; studentGpaColor = "#32CD32"; }
        else if (studentGpa >= 2.7) { studentGpaLabel = "Good"; studentGpaColor = "#1E90FF"; }
        else if (studentGpa >= 2.0) { studentGpaLabel = "Fair"; studentGpaColor = "#FFA500"; }
        else if (studentGpa >= 1.0) { studentGpaLabel = "Pass"; studentGpaColor = "#FFD700"; }
        else { studentGpaLabel = "Fail"; studentGpaColor = "#FF1744"; }
        model.addAttribute("studentGpa", studentGpa);
        model.addAttribute("studentGpaLabel", studentGpaLabel);
        model.addAttribute("studentGpaColor", studentGpaColor);
        return "dashboard-student";
    }
}
