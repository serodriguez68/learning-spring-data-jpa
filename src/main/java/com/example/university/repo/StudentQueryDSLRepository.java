package com.example.university.repo;

import com.example.university.domain.Student;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * This repo can be used to query the Student collection using QueryDSL predicates.
 * The StudentQueryDSLExpressionsFactory is as a convenience class that callers of the repo can use
 * to form the predicates.
 *
 * @see StudentQueryDSLExpressionsFactory
 * */
public interface StudentQueryDSLRepository extends CrudRepository<Student, Integer>,
        QuerydslPredicateExecutor<Student> {
    /* *
     * Available methods
     * Student findOne(Predicate predicate);
     * Iterable<Student> findAll(Predicate predicate);
     * Iterable<Student> findAll(Predicate predicate, Sort sort);
     * Page<Student> findAll(Predicate predicate, Pageable pageable);
     * long count(Predicate predicate);
     * boolean exists(Predicate predicate);
     *
     * A predicate can be one or more BooleanExpressions chained together
     * */
}

