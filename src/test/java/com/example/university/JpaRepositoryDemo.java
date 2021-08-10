package com.example.university;

import com.example.university.domain.Department;
import com.example.university.domain.Person;
import com.example.university.domain.Staff;
import com.example.university.repo.CourseRepository;
import com.example.university.repo.DepartmentRepository;
import com.example.university.repo.StaffRepository;
import com.example.university.repo.StudentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * Demonstrate JPA Repository methods with DepartmentRepository
 * <p>
 * Created by maryellenbowman
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaRepositoryDemo {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StaffRepository staffRepository;

    /**
     * I (Sergio) added this as a patch since this app is not configured to start with a fresh DB for every test
     */
    @Before
    public void cleanDatabase() {
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        departmentRepository.deleteAll();
        staffRepository.deleteAll();
    }

    /**
     * Exercise JPA Repository methods.
     */
    @Test
    public void runJpaRepositoryMethods() {
        /* Setup of staff */
        Staff deanJones = staffRepository.save(new Staff(new Person("John","Jones")));
        Staff deanMartin = staffRepository.save(new Staff(new Person("Matthew","Martin")));
        Staff profBrown =   staffRepository.save(new Staff(new Person ("James", "Brown")));

        departmentRepository.save(new Department("Humanities", deanJones));
        departmentRepository.flush();

        departmentRepository.saveAndFlush(new Department("Fine Arts", deanMartin));

        departmentRepository.save(new Department("Social Science", profBrown));

        System.out.println("\n*************3 Departments*************");
        departmentRepository.findAll().forEach(System.out::println);

        departmentRepository.deleteInBatch(
                departmentRepository.findAll().subList(0,1));

        System.out.println("\n*************1 Less Departments*************");
        departmentRepository.findAll().forEach(System.out::println);
        departmentRepository.deleteAllInBatch();
        System.out.println("\n*************Zero Departments*************");
        departmentRepository.findAll().forEach(System.out::println);

    }
    @Before
    @After
    public void banner() {
        System.out.println("\n\n-------------------------------------------------" +
                "-------------------------------------\n");
    }
}
