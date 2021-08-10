# Spring Data 2 Notes

This repo contains some notes of taken from the LinkedIn Learning course
[Spring: Spring Data 2](https://www.linkedin.com/learning/spring-spring-data-2/welcome?contextUrn=urn%3Ali%3AlyndaLearningPath%3A5b101b04498e06fb6e2d8785&u=2094516)
.

# Chapter 1 - The Spring Data Umbrella Project

- The Spring Data project aims to provide a familiar and consistent data
  access API while still retaining the special traits of the underlying
  data store.
- Spring Data is an umbrella of multiple project.
  - Some address the common features of all data access code.
    - `Spring Data Commons` is the core project for all other Spring
      Data projects. It is data source agnostic.
  - Some are focused on the special traits of underlying data stores.

## Spring Data Commons

Basic operations:
- Convert Java business entities <-> Persistent target datastore records
- Lookup records
- Update records
- Delete records

The repository pattern is the core pattern for all Spring Data projects.
- The most basic abstraction is the `CRUDRepository`.
- Every DB has a DB specific repository interface (e.g. `JpaRepository`,
  `MongoRepository`) that extends from the Spring Data Commons
  repositories.


# Chapter 2 - Understanding JPA for Object-Relational Mapping

## Logical vs Physical models

- ORM frameworks solve the problem of how to map from the physical model
  (i.e. how data is stored, like tables or documents) to the logical
  model (i.e. meaningful programming language objects).
- With ORM frameworks we don't do the mapping by hand. In the case of
  Spring data, developers provide metadata in the form of XML or
  annotations to tell the framework libraries how to do the mapping.

## Brief history of the Java Persistence API

- JPA is the most popular ORM specification.
- JPA is actually a specification, it is not a framework per se
  (although people often refer to it as a framework).
- Hibernate, TopLink among others implement the specification.
- Java apps built using JPA strictly speaking do not use JPA, they use
  some other library that implements the JPA specification.
- These are the most important bits of the JPA spec:
  - The spec specifies the metadata required (XML / annotations) to map
    java entities to tables and java attributes to columns.
  - An Entity Manager does the CRUD for entities (aka domain objects).
    The entity's state is mapped backed to the tables.

## Mapping a Single Database Table to a Java Class

This is how a JPA entity looks like using annotations to specify the
metadata.

```java
@Entity
@Table(name = "STUDENT")
public class Student {
    // Entities must have a unique identifying value labelled with @Id
    @Id
    // If desired, we can tell JPA to generate the ID when persisted.
    // If we do this, we don't need to include the ID in the constructor
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="student_id") 
    private Integer studentId;

    @Column(name="student_name")
    private String name;
    
    @Column(name="student_fulltime")
    private boolean fullTime;

    @Column(name="student_age")
    private Integer age;
    
    public Student(String name, Boolean fullTime, Integer age) {
        this.name = name;
        this.fullTime = fullTime;
        this.age = age;
    }
    // Getters and setters ...
}
```

## Mapping table relationships to Java Classes

Continuing the university example:
- One department offers multiple courses
- A students can be enrolled in many courses
- A course has many students through enrollments

### One-to-many and Many-to-One

```java
@Entity
@Table(name="course")
public class Course {
    @Id
    @GeneratedValue
    @Column(name="course_id")
    private Integer id;
    
    @Column(name="course_name")
    private String name;
    
    @ManyToOne // Many courses to one Department (shows cardinality)
    // Many to one relationships are eager loaded by default (ie.
    // the department will get loaded when a course is loaded)
    @JoinColumn(name="course_dept_id") // Foreign key in courses table
    private Department department; // Full rich object
    
    // No need to pass id since it is auto-generated
    public Course(String name, Department department) {
        this.name = name;
        this.department = department;
    }
    
    // ... toString... other setters and getters
}
```

```java
@Entity
@Table(name="Department")
public class Department {
    @Id
    @GeneratedValue
    @Column(name="dept_id")
    private Integer id;
    
    @Column(name="dept_name")
    private String name;
    
    // Other side of the relationship
    @OneToMany(
            // Refers back to the department parameter in the Course entity 
            mappedBy="department",
            // By default one to many relationships are not automatically
            // fetched from the DB (they are lazy loaded). 
            // We can override that to  populate the associated
            // courses by default when a department is loaded
            fetch=FetchType.EAGER,
            // Control what happens with dependents (Courses) when the
            // parent (Department) changes
            cascade = CascadeType.ALL
            )
    private List<Course> courses = new ArrayList<>();
    
    public Department(String name) {
        this.name = name;
    }
    
    // ... toString... other methods
}
```

### Many-to-Many via join tables

When we associate many to many records via a join table (a table that
only holds foreign keys and is not an entity itself), we declare the
mapping in the entities at both ends of the relationship (e.g. Student,
and Course). In this case, there is no need for an `Enrollment` entity
as it is only a join table.

```java
@Entity
@Table(name = "STUDENT")
public class Student {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="student_id") 
    private Integer studentId;

    //... other attributes
    
    // One student has many enrollments
    @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    @JoinTable(
            // The table name in the DB
            name="Enrollment",
            // The column names of the intermediate join table
            joinColumns = {@JoinColumn(name="student_id")},
            inverseJoinColumns ={@JoinColumn(name="course_id")}
            )
     private List<Course> courses = new ArrayList<>();
    
    // Constructor, toString, Getters and setters ...
}
```

## The Java Persistence Query Language (JPQL)

- We use it to interact with entities and their persistent state.
- The queries are portable to any DBMS.
- The syntax is similar to SQL but references entities and attributes
  instead of tables and columns.

Let's look at an example where we want to get all data from Jane (a
student). By all data we mean: Jane's enrolled courses and those courses
departments.

In SQL, this would require a 4 way JOIN of the `STUDENT`, `ENROLLMENT`,
`COURSE` and `DEPARTMENT` table. In `JPQL` with the `FetchType.EAGER`
configurations shown above, the query would look like this:

```java
class Foo { 
    @PersistenceContext
    // EntityManager: An object that manages the CRUD operations of entities.
    private EntityManager entityManager;
    
    public void printJane() {
        // This is the JPQL query
        // Note that in Spring Data JPA writing this type of queries is
        // abstracted from us (more below)
        Student jane = 
        // Finds all results that comply with the condition
        entityManager.createQuery(
                "Select s from Student s where s.name = 'jane'", 
                Student.class) // The entity class to populate data with
                .getSingleResult();
        System.out.println(jane);
    }   
}
```

### A common exception to lookout for: Lazy Initialization Exception

The exception looks roughly like this: `LazyInitializationException:
failed to lazily initialize a collection of role:
....Student.courses...`.

This exception happens when we forget to declare
`fetch=FetchType.EAGER`, but our code expects to use the associated
records.

# Chapter 3 - Introduction to Spring Data JPA

## JPA without Spring Data

Vanilla JPA implemented by a framework like Hibernate is different from
Spring Data JPA. Vanilla JPA still requires a bunch of boilerplate code
that Spring Data JPA takes care of.

Here is an example of some common operations using vanilla JPA and JPQL
without Spring Data JPA.

```java
public class StudentRepo {
    @PersistenceContext
    private EntityManager entityManager;
    
    Student create(String name, boolean isFullTime, int age) {
        // Most of this is boilerplate
        entityManager.getTransaction().begin;
        Student newStudent = new Student(name, isFullTime, age);
        entityManager.persist(newStudent);
        entityManager.getTransaction().commit();
        entityManager.close();
        return newStudent;
    }
    
    List<Student> findByName(String nameLike) {
        // This is a lot of code for a simple SQL
        Query query = entityManager.createQuery(
                "select s from Student s where s.name LIKE :someName", Student.class);
        query.setParameter("someName", "%" + nameLike + "%");
        List<Student> result= query.getResultList();
        entityManager.close();
        return result;
    }
}
```

## Spring Data repository interfaces

Spring Data JPA solves the boilerplate problem shown above by following
some conventions and using the Repository pattern.

### The most basic repository

The `Repository` interface is just a marker without any methods, but it
is the basis of all Spring data repositories.

At boot time, Spring Data scans all classes that extend directly or
indirectly from the `Repository` interface and provides some
functionality to them (see example below).

```java
public interface Repository<T, ID>{}
// T is the Domain type (aka entity class name) the repository manages
// ID the type of the unique ID of the entity.
```

## The Crud Repository

Provides implemented methods to create, read, update and delete the
given entities. This is packaged in
`com.springframework.data.repository`

```java
public interface CrudRepository<T, ID> extends Repository<T, ID>{}
```

- To create or update, invoke the `save` or `saveAll` methods.
  - `T save(T entity)`
  - `Iterable<T> saveAll(Iterable<T> entityList);`
- To delete entities you can use 4 methods:
  - `void deleteById(ID id)`
  - `void deleteAll(Iterable< ? Extends T>`
  - `void delete(T entity)`
  - `void deleteAll()`

- To Read data we have 5 methods.
  - `Optional<T> findById(ID id)`
  - `Iterable<T> findAllById(Iterable<ID> ids)`
  - `Iterable<T> findAll()`
  - `long count()`
  - `boolean existsById(ID id)`

To declare a repository for your entity:

```java
public interface StudentRepository extends CrudRepository<Student, Integer>{}
// The id of the Student entity is an Integer
```

By doing this, we get access to all the methods above for on the
`StudentRepository`. We don't need to implement any of these methods in
our repo. At boot time, Spring Data scans all the interfaces that extend
`CrudRepository` and implements the code for the given Entity and ID
type.

Go to `StudentRepository.java` and `CrudRepositoryDemo` to view a CRUD
repository in action.

## Database specific repositories

The CRUD repository is a database (aka data source) agnostic. However,
spring data also provides some database-specific interfaces and modules
that allow us to leverage specific traits of the DB technology we are
using.

### The JPA Repository

The `JPARepository` is an example of a Spring Data module that allow us
to use methods that are specific to DB technologies that support
transactions and batch deleting.
- [More information here](https://www.baeldung.com/spring-data-repositories).
- The `JPARepository` extends `CRUDRepository` and
  `PagingAndSortingRepository` so we get all those methods as well.
- Additionally, we get methods like:
  - `void flush()` - Flushing is forcing Spring data do execute the
    commands in the DB in the open transaction. When we flush, before
    commit, the changes can only be viewed within our transaction. [More
    info here.](https://stackoverflow.com/a/14322619)
  - `<T> saveAndFlush(T entity)`
  - `void deleteInBatch(Iterable<T> iterable)` - delete several entities
    in one atomic method more efficiently than how CRUD repository will
    do it.
  - `void deleteAllInBatch()`
- See `DepartmentRepository` and `JpaRepositoryDemo` to see this in
  action.


## Other DB specific repositories

**Why do we need DB specific repositories?**
- Most times there is no need for them.
- However, if we really need to use a specific DB feature in a repo,
  then we will need to extend from one DB specific Repo.

Examples:
- `MongoRepository`
- `SolrCrudRepository`: Apache Solr
- `GemfireRepository`: Pivotal GemFire


# Chapter 4 - Querying with Spring Data

## Property expression query methods

This chapter shows how to create custom queries based on entity
properties. The examples in the sample project are given within
repositories that extend from `ReadOnlyRepository`. However, this
capability is there for any repository that extends from any Spring Data
Commons repository.

Custom queries based on entity properties can be created within
repositories by following a method naming convention. SpringDataJPA
takes care of the implementation of those custom queries as long as we
have followed these naming rules:
1. Start by declaring the return type
2. The method name must start with `findBy`
3. Follow the name with the entity attribute name you want to query on
   in camel case (e.g. `FullTime`)
   1. For querying based on embedded sub-attributes, chain the name of
      the attribute and sub-attribute like this:
      `findByAttendeeLastName`
4. Add query parameters (i.e. method parameters) whose type matches the
   property type in the entity.

Examples of this type of queries can be found in the
`StudentQueryRepository` and in `QueryDemos#simpleQueryExamples`

Syntax errors on the naming of Spring Data query methods **are found at
app startup, not at runtime** (e.g. findByAtttendeeeeeLastName(String
name) will fail at startup with a `PropertyReferenceException`. This is
good because it reduces the likelihood of a bug making it to production.


## Query methods with clauses and expressions
The next level up from property based queries. We are going to show how
to create queries like `find by full time or age less than`.

The naming conventions are similar, just include some keywords in the
method name. Here are some examples:
- `Student findByAttendeeFirstNameAndAttendeeLastName(String firstName,
  String lastName)`
- `Student findByAttendee(Person person)`
- `List<Student> findByAgeGreaterThan(int minimumAge)`
- `List<Student> findByAgeLessThan(int maximumAge)`
- `List<Student> findByAttendeeLastNameIgnoreCase(String lastName)` -
  Ignore Case is a keyword
- `List<Student> findByAttendeeLastNameLike(String likeString)` - Like
  is a keyword
- `Student findFirstByOrderByAttendeeLastNameAsc()` - Finds the highest
  student in the alphabet
- `Student findTopByOrderByAgeDesc()` - find oldest student
- `List<Student> findTop3ByOrderByAgeDesc()` - find 3 oldest students

To find more keywords go to [this tutorial](https://www.baeldung.com/spring-data-derived-queries) or to the
[Repository Keywords docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-keywords).

See some examples in action in `StudentQueryRepository` and
`QueryDemos#intermediateQueryExamples`


## @Query annotated query methods

`@Query` annotated methods are another way to declare a query method.
They are used for when the auto generated queries using expressions are
not enough.

- `@Query` methods can use JPQL or the language specific to the database
  technology we are using.
  - The default is JPQL
  - If you need to go with the native query language, you will need to
    use the `nativeQuery=true` option. The results will still be mapped
    to the JPA entity defined in the Repo's generic types.
- When using `@Query` methods in repos, the method name and arguments
  can be anything you want. They don't have to follow any rules.
- Go to `CourseQueryRepository` and `QueryDemos#jpqlAndNativeQueries`
  for some examples of these type of methods.

**When to use custom annotated `@Queries`:**
- When the names of the auto generated queries gets too awkward.
  Sometimes simple domain level queries turn into complex names because
  of the naming convention.
- When queries are too complex for autogenerated queries built out of
  clauses and expressions. Some cases of "too complex" are:
  - Mapping data from the DB out to objects that are not entities. See
    the example of `CourseView` in the `CourseQueryRepository`.
  - Joins that are not handled by DB relationship annotations and eager
    loading.

## Paging and Sorting

In spring data, we can add pagination to any auto-generated query in any
repository just by adding `Page` to the return type and passing in a
`Pageable` parameter.

```java
public interface CourseRepository extends CrudRepoitory<Course,Integer> {
    List<Course> findByCredits(int credits);
    Page<Course> findByCredits(int credits, Pageable pageable);    
}

// Usage
Pageable pageable = PageRequest.of(0, 25, Sort,Direction.ASC, "credits", "name");
courseRepository.findByCredits(3, pageable);
// 0 -> The page number (0 is first page)
// 4 -> Number of entities per page
// direction of sort
// ordered list of properties to sort by
// The Page object holds: 
// - A list of results `#getTotalElements()`
// - The total number of results `#getTotalPages()`
// - The number of pages
```

Additionally, Spring data also comes with the
`PagingAndSortingRepository`, which extend the `CrudRepository` but
already has built-in pagination and sorting for the `#findAll` method.

See the`CourseQueryRepository`, `StaffRepository` and
`QueryDemos#pagingAndSortingQueries` for examples.

**Side Note:** Pagination brings a challenge to your application
architecture since it suggests you need to pass around `Page` and
`Pageable` objects outside the data access layer into the use case
layer. Moreover, both of these classes are framework classes that you
don't control.

## Query by Example

Spring data supports query by example with the `QueryByExampleExecutor`.
The `JpaRepository` extends `QueryByExampleExecutor`.

`QueryByExampleExecutor` provided the following methods. All take an
`Example` attribute:

```
List<Department> findAll(Example<Department> example);
List<Department> findAll(Example<Department> example, Sort sort);
Optional<Department> findOne(Example<Department> example);
Page<Department> findAll(Example<Department> example, Pageable pageable);
long count(Example<Department> example);
boolean exists(Example<Department> example);
```

To use any of these methods, we need to instantiate an `Example`. One
way to do this is to use `Example.of(T probe)`. Probe is an instance of
the related entity with only the attributes we want to query for set
(the rest null).

```
// Find the department with the name "Humanities"
Example example = Example.of(new Department("Humanities", null)
departmentRepository.findOne(example)

// Find all department whose chair has a the first name "John"
Example example = Example.of(new Department(
    null,
    new Staff(new Person("John", null))
);
departmentRepository.findAll(example)
```

To further refine how the query by example will work, we can also
provide an `ExampleMatcher` to the `Example`. `ExampleMatcher`s allow
ous to enable query options like ignore case, or "strings ending with".

```
System.out.println("\nFind All Departments with the name ending in 'sciences', case insensitive");
        Example<Department> example = Example.of(
                new Department("sciences", null),
                ExampleMatcher.matching().
                        withIgnoreCase().
                        withStringMatcher(ExampleMatcher.StringMatcher.ENDING)
        );
        departmentRepository.findAll(example).forEach(System.out::println);

```

Go to the `DepartmentRepository` and `QueryDemos#queryByExample` for
examples.

Advantages of query by example:
- User-friendly alternative to SQL
- Lookup objects similar to another object
- Independent of underlying datastore
- Good to test recently refactored code

Limitations: restricted nesting, property constraints and string
matching.

## Optional<> query response

Spring Data 2 introduced support for Java Optionals as a return type for
queries that look for a single element. This means that these queries
can now return an `Optional<Course>` instead of returning null.

To enable this, we only need to add `Optional<MyEntity>` to the return
type of any repository method. If we don't use `Optional`, then Spring
data will return `null` if a record is not found.

This feature applies for both auto-generated queries (i.e. created by
name convention) and `@Query` annotated queries that use JPQL or the
native query language (e.g. SQL).

- See examples of declaring the repository methods in `CourseRepository`
- See examples on the usage of `Optional` in `DebuggingDemos` and
  `QueryDemos > Complex queries`.

 Optional is a wonderful feature to avoid nasty null checks and
 introduce null safety.

# Other topics

- `@Embeddable`: go to `Person.java` and `Student.java` to understand
  what an embeddable class is.
- `Course.java` has an example of a circular reference of a table to the
  same table

# Open questions
Can we remap an awkwardly named autogenerated query to a better name?