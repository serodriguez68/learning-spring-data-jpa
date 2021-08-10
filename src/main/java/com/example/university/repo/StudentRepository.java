package com.example.university.repo;

import com.example.university.domain.Person;
import com.example.university.domain.Student;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * DataSource Management for the Students at the University.
 *
 * Created by maryellenbowman
 */
/*- Integer is the type of the ID for the Student entity
 * - By extending CrudRepository, we gain all CRUD methods.
 * - With Spring Data we don't have to implement the methods. At boot time, Spring Data scans all the interfaces that
 * extend `CrudRepository` and implements the code for the CrudRepository methods.
 * - Go to CrudRepositoryDemo to see how to exercise the repo.
 *
 * */
public interface StudentRepository extends CrudRepository<Student, Integer>{
}
