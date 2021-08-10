package com.example.university.repo;

import com.example.university.domain.Course;
import com.example.university.view.CourseView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Declaring a CourseQueryRepository which can only query the Database
 */
public interface CourseQueryRepository extends ReadOnlyRepository<Course, Integer> {
    Optional<Course> findByName(String name);

    List<Course> findByDepartmentChairMemberLastName(String chair);

    /* Named parameters like :chair used with the paired @Param("chair") annotation are equivalent
    * to using the ?1 syntax below. */
    @Query("Select c from Course c where c.department.chair.member.lastName=:chair")
    List<Course> findByChairLastName(@Param("chair")String chairLastName);

    @Query("Select c from Course c join c.prerequisites p where p.id = ?1")
    List<Course> findCourseByPrerequisite(int id);

    @Query("Select new com.example.university.view.CourseView" +
            "(c.name, c.instructor.member.lastName, c.department.name) from Course c where c.id=?1")
    CourseView getCourseView(int courseId) ;

    List<Course> findByCredits(@Param("credits") int credits);

    Page<Course> findByCredits(@Param("credits") int credits, Pageable pageable);

    Course findByDepartmentName(String deptName);


    /* Returns an object that is not a JPA entity, but some other data class we give JPQL to hold the data */
    @Query("Select new com.example.university.view.CourseView" +
            "(c.name, c.instructor.member.lastName, c.department.name) from Course c where c.name=?1")
    Optional<CourseView> getCourseViewByName(String name);

    /**
     * For a query this simple you would use Spring Data query expressions. This is just an example of how a native
     * query can be used.
    */
    @Query(value = "SELECT * FROM COURSE c WHERE name LIKE %:searchName%", nativeQuery = true)
    List<Course> findByFuzzyName(@Param("searchName")String searchName);

    /**
     * An example to illustrate optionals with native queries
    */
    @Query(value =
            "SELECT * FROM COURSE c JOIN DEPARTMENT d ON c.department_id = d.id WHERE d.name = :deptName LIMIT 1" ,
            nativeQuery = true)
    Optional<Course> nativeFindByDepartmentName(@Param("deptName") String deptName);
}