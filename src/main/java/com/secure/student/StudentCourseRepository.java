package com.secure.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.secure.appuser.AppUser;
 
@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    List<StudentCourse> findByCourseId(Long courseId);
    List<StudentCourse> findByStudent(AppUser student);
} 