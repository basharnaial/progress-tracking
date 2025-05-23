package com.secure.course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.secure.student.StudentCourse;
import com.secure.student.StudentCourseRepository;
import com.secure.appuser.AppUser;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import com.secure.appuser.AppUserRepository;
import com.secure.appuser.AppUserRole;
import org.springframework.http.ResponseEntity;

@Controller
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private AppUserRepository appUserRepository;

//    @GetMapping("/courses")
//    public String listCourses(Model model) {
//        List<Course> courses = courseRepository.findAll();
//        model.addAttribute("courses", courses);
//        model.addAttribute("activeTab", "courses");
//        return "courses";
//    }

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

        @GetMapping("/{courseId}/gpa")
        public List<Map<String, Object>> getCourseGpa(@PathVariable Long courseId) {
            List<StudentCourse> enrollments = studentCourseRepository.findByCourseId(courseId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (StudentCourse sc : enrollments) {
                if (!Boolean.TRUE.equals(sc.getFinished())) continue;
                double total =
                    (sc.getMidterm() != null ? sc.getMidterm() : 0) +
                    (sc.getFinalExam() != null ? sc.getFinalExam() : 0) +
                    (sc.getAssignments() != null ? sc.getAssignments() : 0) +
                    (sc.getQuizzes() != null ? sc.getQuizzes() : 0) +
                    (sc.getProject() != null ? sc.getProject() : 0) +
                    (sc.getAttendance() != null ? sc.getAttendance() : 0);
                double gpa = (total / 100.0) * 4.0;
                String gradeCategory;
                String color;
                if (gpa >= 3.7) { gradeCategory = "Excellent"; color = "#228B22"; }
                else if (gpa >= 3.3) { gradeCategory = "Very Good"; color = "#32CD32"; }
                else if (gpa >= 2.7) { gradeCategory = "Good"; color = "#1E90FF"; }
                else if (gpa >= 2.0) { gradeCategory = "Fair"; color = "#FFA500"; }
                else if (gpa >= 1.0) { gradeCategory = "Pass"; color = "#FFD700"; }
                else { gradeCategory = "Fail"; color = "#FF1744"; }
                Map<String, Object> row = new HashMap<>();
                row.put("studentId", sc.getStudent().getId());
                row.put("name", sc.getStudent().getFirstName() + " " + sc.getStudent().getLastName());
                row.put("gpa", gpa);
                row.put("percentage", total);
                row.put("gradeCategory", gradeCategory);
                row.put("color", color);
                result.add(row);
            }
            return result;
        }

        @GetMapping("/{courseId}/students-for-grading")
        public List<Map<String, Object>> getStudentsForGrading(@PathVariable Long courseId) {
            List<StudentCourse> enrollments = studentCourseRepository.findByCourseId(courseId);
            return enrollments.stream().map(sc -> {
                Map<String, Object> map = new HashMap<>();
                map.put("studentId", sc.getStudent().getId());
                map.put("name", sc.getStudent().getFirstName() + " " + sc.getStudent().getLastName());
                map.put("midterm", sc.getMidterm());
                map.put("finalExam", sc.getFinalExam());
                map.put("assignments", sc.getAssignments());
                map.put("quizzes", sc.getQuizzes());
                map.put("project", sc.getProject());
                map.put("attendance", sc.getAttendance());
                map.put("finished", sc.getFinished());
                return map;
            }).collect(Collectors.toList());
        }

        @PostMapping("/{courseId}/grades")
        public ResponseEntity<?> updateAllStudentGrades(
            @PathVariable Long courseId,
            @RequestBody List<Map<String, Object>> gradesList
        ) {
            try {
                List<StudentCourse> enrollments = studentCourseRepository.findByCourseId(courseId);
                Map<Long, StudentCourse> enrollmentMap = enrollments.stream()
                    .collect(Collectors.toMap(sc -> sc.getStudent().getId(), sc -> sc));

                for (Map<String, Object> entry : gradesList) {
                    Long studentId = ((Number) entry.get("studentId")).longValue();
                    Map<String, Object> grades = (Map<String, Object>) entry.get("grades");

                    StudentCourse enrollment = enrollmentMap.get(studentId);
                    if (enrollment == null) {
                        throw new RuntimeException("Enrollment not found for student: " + studentId);
                    }

                    updateGrades(enrollment, grades);
                    studentCourseRepository.save(enrollment);
                }
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        @PostMapping("/{courseId}/grades/{studentId}")
        public ResponseEntity<?> updateStudentGrades(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> grades
        ) {
            try {
                StudentCourse enrollment = studentCourseRepository.findByCourseId(courseId).stream()
                    .filter(sc -> sc.getStudent().getId().equals(studentId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));

                updateGrades(enrollment, grades);
                studentCourseRepository.save(enrollment);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        private void updateGrades(StudentCourse enrollment, Map<String, Object> grades) {
            if (grades.containsKey("midterm")) {
                Object value = grades.get("midterm");
                enrollment.setMidterm(value instanceof Number ? ((Number) value).intValue() : 0);
            }
            if (grades.containsKey("finalExam")) {
                Object value = grades.get("finalExam");
                enrollment.setFinalExam(value instanceof Number ? ((Number) value).intValue() : 0);
            }
            if (grades.containsKey("assignments")) {
                Object value = grades.get("assignments");
                enrollment.setAssignments(value instanceof Number ? ((Number) value).intValue() : 0);
            }
            if (grades.containsKey("quizzes")) {
                Object value = grades.get("quizzes");
                enrollment.setQuizzes(value instanceof Number ? ((Number) value).intValue() : 0);
            }
            if (grades.containsKey("project")) {
                Object value = grades.get("project");
                enrollment.setProject(value instanceof Number ? ((Number) value).intValue() : 0);
            }
            if (grades.containsKey("attendance")) {
                Object value = grades.get("attendance");
                enrollment.setAttendance(value instanceof Number ? ((Number) value).doubleValue() : 0.0);
            }
            if (grades.containsKey("finished")) {
                Object value = grades.get("finished");
                enrollment.setFinished(value instanceof Boolean ? (Boolean) value : false);
            }
        }
    }

    @RestController
    @RequestMapping("/api/students")
    class StudentGpaApiController {
        private final StudentCourseRepository studentCourseRepository;
        public StudentGpaApiController(StudentCourseRepository studentCourseRepository) {
            this.studentCourseRepository = studentCourseRepository;
        }
        @GetMapping("/gpa")
        public List<Map<String, Object>> getStudentGpas() {
            List<StudentCourse> finished = studentCourseRepository.findAll().stream()
                .filter(sc -> Boolean.TRUE.equals(sc.getFinished()))
                .collect(Collectors.toList());
            Map<Long, List<Map<String, Object>>> studentCourseStats = new HashMap<>();
            for (StudentCourse sc : finished) {
                double total =
                    (sc.getMidterm() != null ? sc.getMidterm() : 0) +
                    (sc.getFinalExam() != null ? sc.getFinalExam() : 0) +
                    (sc.getAssignments() != null ? sc.getAssignments() : 0) +
                    (sc.getQuizzes() != null ? sc.getQuizzes() : 0) +
                    (sc.getProject() != null ? sc.getProject() : 0) +
                    (sc.getAttendance() != null ? sc.getAttendance() : 0);
                double gpaForCourse = (total / 100.0) * 4.0;
                Map<String, Object> stat = new HashMap<>();
                stat.put("gpa", gpaForCourse);
                stat.put("percentage", total);
                stat.put("name", sc.getStudent().getFirstName() + " " + sc.getStudent().getLastName());
                studentCourseStats.computeIfAbsent(sc.getStudent().getId(), k -> new ArrayList<>()).add(stat);
            }
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<Long, List<Map<String, Object>>> entry : studentCourseStats.entrySet()) {
                double gpaSum = 0, percentSum = 0;
                int count = entry.getValue().size();
                String name = "";
                for (Map<String, Object> stat : entry.getValue()) {
                    gpaSum += (double) stat.get("gpa");
                    percentSum += (double) stat.get("percentage");
                    name = (String) stat.get("name");
                }
                double avgGpa = gpaSum / count;
                double avgPercent = percentSum / count;
                String gradeCategory;
                String color;
                if (avgGpa >= 3.7) { gradeCategory = "Excellent"; color = "#228B22"; }
                else if (avgGpa >= 3.3) { gradeCategory = "Very Good"; color = "#32CD32"; }
                else if (avgGpa >= 2.7) { gradeCategory = "Good"; color = "#1E90FF"; }
                else if (avgGpa >= 2.0) { gradeCategory = "Fair"; color = "#FFA500"; }
                else if (avgGpa >= 1.0) { gradeCategory = "Pass"; color = "#FFD700"; }
                else { gradeCategory = "Fail"; color = "#FF1744"; }
                Map<String, Object> row = new HashMap<>();
                row.put("studentId", entry.getKey());
                row.put("name", name);
                row.put("gpa", avgGpa);
                row.put("percentage", avgPercent);
                row.put("gradeCategory", gradeCategory);
                row.put("color", color);
                result.add(row);
            }
            return result;
        }
    }

    @RestController
    @RequestMapping("/api/dashboard")
    class DashboardOverviewApiController {
        private final AppUserRepository appUserRepository;
        private final CourseRepository courseRepository;
        private final StudentCourseRepository studentCourseRepository;
        public DashboardOverviewApiController(AppUserRepository appUserRepository, CourseRepository courseRepository, StudentCourseRepository studentCourseRepository) {
            this.appUserRepository = appUserRepository;
            this.courseRepository = courseRepository;
            this.studentCourseRepository = studentCourseRepository;
        }
        @GetMapping("/overview")
        public Map<String, Object> getOverview() {
            long totalStudents = appUserRepository.findAll().stream().filter(u -> u.getAppUserRole() == AppUserRole.STUDENT).count();
            long totalCourses = courseRepository.count();
            List<StudentCourse> finished = studentCourseRepository.findAll().stream().filter(sc -> Boolean.TRUE.equals(sc.getFinished())).collect(Collectors.toList());
            double total = 0;
            int count = 0;
            Map<Long, List<Double>> studentGpas = new HashMap<>();
            for (StudentCourse sc : finished) {
                double sum =
                    (sc.getMidterm() != null ? sc.getMidterm() : 0) +
                    (sc.getFinalExam() != null ? sc.getFinalExam() : 0) +
                    (sc.getAssignments() != null ? sc.getAssignments() : 0) +
                    (sc.getQuizzes() != null ? sc.getQuizzes() : 0) +
                    (sc.getProject() != null ? sc.getProject() : 0) +
                    (sc.getAttendance() != null ? sc.getAttendance() : 0);
                total += sum;
                count++;
                double gpaForCourse = (sum / 100.0) * 4.0;
                studentGpas.computeIfAbsent(sc.getStudent().getId(), k -> new ArrayList<>()).add(gpaForCourse);
            }
            double averageGradePercent = count > 0 ? (total / count) : 0;
            // Top GPA and Pass Rate
            List<Double> avgGpas = studentGpas.values().stream()
                .map(list -> list.stream().mapToDouble(Double::doubleValue).average().orElse(0)).collect(Collectors.toList());
            double topGpa = avgGpas.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double passRate = avgGpas.isEmpty() ? 0 : (100.0 * avgGpas.stream().filter(gpa -> gpa >= 2.0).count() / avgGpas.size());
            Map<String, Object> map = new HashMap<>();
            map.put("totalStudents", totalStudents);
            map.put("totalCourses", totalCourses);
            map.put("averageGradePercent", averageGradePercent);
            map.put("topGpa", topGpa);
            map.put("passRate", passRate);
            return map;
        }
    }

}
