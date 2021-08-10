package com.example.university;

import com.example.university.domain.Course;
import com.example.university.domain.Department;
import com.example.university.domain.Person;
import com.example.university.domain.Staff;
import com.example.university.repo.CourseQueryRepository;
import com.example.university.view.CourseView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

/**
 * Created by maryellenbowman.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class DebuggingDemos {

    @Autowired
    private CourseQueryRepository courseRepository;

    /**
     * Common Mistakes, Uncomment to debug
     *
     * Courses persisted to H2 in-Memory database at startup.
     * @see UniversityApplication
     */
    @Test
    public void runtimeErrors() {

        Course course = courseRepository.findByDepartmentName("Sciences");

        //Various ways to leverage the Optional
        CourseView view = courseRepository.getCourseViewByName("English 101").get();
        view = courseRepository.getCourseViewByName("English 101").orElseThrow();
        view = courseRepository.getCourseViewByName("English 100").orElse(
                new CourseView("dummyCourse",
                        "Bad Instructor",
                        "No Department"));

        // Using optional on a native SQL query
        Optional<Course> maybeCourse2 = courseRepository.nativeFindByDepartmentName("Fake Sciences");
        Course course2 = maybeCourse2.orElse(
                new Course(
                        "Default Course",
                        0,
                        new Staff(new Person("Default", "Person")),
                        new Department(
                                "Default Department",
                                new Staff(new Person("Default", "Chair")))
                )
        );
    }
}
