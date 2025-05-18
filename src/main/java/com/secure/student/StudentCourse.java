package com.secure.student;

import com.secure.appuser.AppUser;
import com.secure.course.Course;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
public class StudentCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private LocalDate enrollmentDate;

    // Grades
    @Min(0) @Max(20)
    private Integer midterm;

    @Min(0) @Max(10)
    private Integer quizzes;

    @Min(0) @Max(10)
    private Integer assignments;

    @Min(0) @Max(20)
    private Integer project;

    @Min(0) @Max(35)
    private Integer finalExam;

    @Min(0) @Max(5)
    private Double attendance;

    private Boolean finished = false;
}
