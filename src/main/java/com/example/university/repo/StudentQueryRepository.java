package com.example.university.repo;

import com.example.university.domain.Person;
import com.example.university.domain.Student;

import java.util.List;

/**
 * Declaring a StudentQueryRepository which can only query the Database
 */
public interface StudentQueryRepository extends ReadOnlyRepository<Student, Integer>{
    //Simple Query Methods
    List<Student> findByFullTime(boolean fullTime);
    List<Student> findByAge(Integer age);
    List<Student> findByAttendeeLastName(String last);

    //Query Methods with Clauses and Expressions

    /* The below two methods give the same result since Person holds firstName and lastName only */
    Student findByAttendeeFirstNameAndAttendeeLastName(String firstName, String lastName);
    Student findByAttendee(Person person);

    List<Student> findByAgeGreaterThan(int minimumAge);
    List<Student> findByAgeLessThan(int maximumAge);
    List<Student> findByAttendeeLastNameIgnoreCase(String lastName);
    /**
     * Like is a keyword
     * You will need to pass the wildcard % in the parameters:
     * studentRepository.findByAttendeeLastNameLike("%i%")
     * */
    List<Student> findByAttendeeLastNameLike(String likeString);
    Student findFirstByOrderByAttendeeLastNameAsc(); //Finds the highest student in the alphabet
    Student findTopByOrderByAgeDesc(); // find oldest student
    List<Student> findTop3ByOrderByAgeDesc(); //find 3 oldest students
}
