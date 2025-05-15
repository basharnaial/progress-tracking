package com.secure.course;

import com.secure.course.Course;
import com.secure.course.CourseRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.secure.model.StudentCourse;
import com.secure.model.StudentCourseRepository;
import com.secure.appuser.AppUser;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @GetMapping
    public String listCourses(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        model.addAttribute("activeTab", "courses");
        return "courses";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        model.addAttribute("activeTab", "courses");
        return "dashboard-tutor";
    }

    @RestController
    @RequestMapping("/api/courses")
    class CourseApiController {
        private final StudentCourseRepository studentCourseRepository;
        public CourseApiController(StudentCourseRepository studentCourseRepository) {
            this.studentCourseRepository = studentCourseRepository;
        }

        @GetMapping("/{courseId}/students")
        public List<Map<String, Object>> getStudentsForCourse(@PathVariable Long courseId) {
            List<StudentCourse> enrollments = studentCourseRepository.findByCourseId(courseId);
            return enrollments.stream().map(sc -> {
                AppUser s = sc.getStudent();
                Map<String, Object> map = new HashMap<>();
                map.put("id", s.getId());
                map.put("name", s.getFirstName() + " " + s.getLastName());
                return map;
            }).collect(Collectors.toList());
        }

        @PostMapping("/{courseId}/attendance")
        public void updateAttendance(@PathVariable Long courseId, @RequestBody List<Long> absentStudentIds) {
            List<StudentCourse> enrollments = studentCourseRepository.findByCourseId(courseId);
            for (StudentCourse sc : enrollments) {
                if (absentStudentIds.contains(sc.getStudent().getId())) {
                    double current = sc.getAttendance() == null ? 0 : sc.getAttendance();
                    sc.setAttendance(Math.max(0, current - 0.25));
                }
            }
            studentCourseRepository.saveAll(enrollments);
        }
    }

}