package com.example.university.repo;

import com.example.university.domain.QStudent;
import com.querydsl.core.types.dsl.BooleanExpression;

/**
* A convenience helper class that callers of the StudentQueryDSLRepository can use
* to form predicates.
*
* @see StudentQueryDSLRepository
* */
public class StudentQueryDSLExpressionsFactory {
    public static BooleanExpression hasLastName(String lastName) {
        return QStudent.student.attendee.lastName.eq(lastName);
    }

    public static BooleanExpression isFullTime() {
        return QStudent.student.fullTime.eq(true);
    }

    public static BooleanExpression isOlderThan(int age) {
        return QStudent.student.age.gt(age);
    }
}
