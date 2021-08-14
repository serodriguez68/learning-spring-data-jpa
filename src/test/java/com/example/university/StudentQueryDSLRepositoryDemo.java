package com.example.university;

import com.example.university.domain.QStudent;
import com.example.university.repo.StudentQueryDSLRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.example.university.repo.StudentQueryDSLExpressionsFactory.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StudentQueryDSLRepositoryDemo {
    @Autowired
    StudentQueryDSLRepository studentQueryDSLRepository;

    /**
     * QueryDSL query demos.
     *
     * Students persisted to H2 in-Memory database at startup.
     * @see UniversityApplication
     * */
    @Test
    public void queryDSLQueryExamples() {
        /* Examples with the client of the repo freely building whatever predicate it likes through direct use of
        QStudent */
        QStudent student = QStudent.student;
        BooleanExpression hasLastNameSmith = student.attendee.lastName.equalsIgnoreCase("SMITH");
        BooleanExpression isFullTime = student.fullTime.eq(true);
        BooleanExpression isOlderThan15 = student.age.gt(15);
        BooleanExpression isOlderThan20 = student.age.gt(20);

        System.out.println("\nFind full time students with last name 'SMITH' older than 15");
        studentQueryDSLRepository.findAll(hasLastNameSmith.and(isFullTime).and(isOlderThan15)).forEach(System.out::println);

        System.out.println("\nFind full time students OR older than 20");
        studentQueryDSLRepository.findAll(isFullTime.or(isOlderThan20)).forEach(System.out::println);

        /* Examples using the DSLExpressionFactory helper class */
        System.out.println("\nFind full time students with last name 'SMITH' older than 15");
        studentQueryDSLRepository.findAll(hasLastName("Smith").and(isOlderThan(15))).forEach(System.out::println);

        System.out.println("\nFind full time students OR older than 20");
        studentQueryDSLRepository.findAll(isFullTime().or(isOlderThan(20))).forEach(System.out::println);
    }
}
