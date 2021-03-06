# Spring Data 2 Notes

This repo contains some notes of taken from the LinkedIn Learning course
[Spring: Spring Data 2](https://www.linkedin.com/learning/spring-spring-data-2/welcome?contextUrn=urn%3Ali%3AlyndaLearningPath%3A5b101b04498e06fb6e2d8785&u=2094516)
.

# Table of Content
- [Chapter 1 - The Spring Data Umbrella Project](#chapter-1---the-spring-data-umbrella-project)
  * [Spring Data Commons](#spring-data-commons)
- [Chapter 2 - Understanding JPA for Object-Relational Mapping](#chapter-2---understanding-jpa-for-object-relational-mapping)
  * [Logical vs Physical models](#logical-vs-physical-models)
  * [Brief history of the Java Persistence API](#brief-history-of-the-java-persistence-api)
  * [Mapping a Single Database Table to a Java Class](#mapping-a-single-database-table-to-a-java-class)
  * [Mapping table relationships to Java Classes](#mapping-table-relationships-to-java-classes)
    + [One-to-many and Many-to-One](#one-to-many-and-many-to-one)
    + [Many-to-Many via join tables](#many-to-many-via-join-tables)
  * [The Java Persistence Query Language (JPQL)](#the-java-persistence-query-language-jpql)
    + [A common exception to lookout for: Lazy Initialization Exception](#a-common-exception-to-lookout-for-lazy-initialization-exception)
- [Chapter 3 - Introduction to Spring Data JPA](#chapter-3---introduction-to-spring-data-jpa)
  * [JPA without Spring Data](#jpa-without-spring-data)
  * [Spring Data repository interfaces](#spring-data-repository-interfaces)
    + [The most basic repository](#the-most-basic-repository)
  * [The Crud Repository](#the-crud-repository)
  * [Database specific repositories](#database-specific-repositories)
    + [The JPA Repository](#the-jpa-repository)
  * [Other DB specific repositories](#other-db-specific-repositories)
- [Chapter 4 - Querying with Spring Data](#chapter-4---querying-with-spring-data)
  * [Property expression query methods](#property-expression-query-methods)
  * [Query methods with clauses and expressions](#query-methods-with-clauses-and-expressions)
  * [@Query annotated query methods](#query-annotated-query-methods)
  * [Paging and Sorting](#paging-and-sorting)
  * [Query by Example](#query-by-example)
  * [Optional<> query response](#optional-query-response)
- [Chapter 5 - More Repository Types](#chapter-5---more-repository-types)
  * [Spring Data MongoDb Example](#spring-data-mongodb-example)
  * [Spring Data JDBC example](#spring-data-jdbc-example)
    + [Why use JPA?](#why-use-jpa)
      - [Pros of JPA](#pros-of-jpa)
      - [Cons of JPA](#cons-of-jpa)
    + [Why use JDBC](#why-use-jdbc)
      - [Pros of  JDBC](#pros-of--jdbc)
      - [Cons of JDBC](#cons-of-jdbc)
      - [Important traits of JDBC](#important-traits-of-jdbc)
  * [Spring Data Reactive Repository Example](#spring-data-reactive-repository-example)
  * [Other Supported Data Sources in Spring Data](#other-supported-data-sources-in-spring-data)
- [Chapter 6 - Special Features](#chapter-6---special-features)
  * [Spring Data Rest](#spring-data-rest)
  * [QueryDSL Spring Data Extension](#querydsl-spring-data-extension)
    + [How it works](#how-it-works)
    + [Using a QueryDSL-enabled repository](#using-a-querydsl-enabled-repository)
    + [Resources](#resources)
    + [Caveats](#caveats)
  * [Auditing](#auditing)
    + [Option 1: Annotations in the Entity](#option-1-annotations-in-the-entity)
    + [Option 2: Implement `Auditable` and extend `AbstractAuditable`](#option-2-implement-auditable-and-extend-abstractauditable)
    + [Getting access to the current `User`](#getting-access-to-the-current-user)
  * [Read-Only repository pattern](#read-only-repository-pattern)
- [Other topics](#other-topics)
  * [Database migrations](#database-migrations)
  * [Miscellaneous](#miscellaneous)
- [Open questions](#open-questions)

<small><i><a href='http://ecotrust-canada.github.io/markdown-toc/'>Table of contents generated with markdown-toc</a></i></small>


# Chapter 1 - The Spring Data Umbrella Project

- The Spring Data project aims to provide a familiar and consistent data
  access API while still retaining the special traits of the underlying
  data store.
- Spring Data is an umbrella of multiple projects.
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
- Every DB has a DB specific repository interface (e.g. `JpaRepository`
  (for relational DBs), `MongoRepository`) that extends from the Spring
  Data Commons repositories.


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
- Good to test recently refactored code.
- `QueryByExample` is an example of *dynamic* querying. This means that
  the user of the repository can define what it wants to query without
  the repository prescribing it. A (perhaps better) alternative for
  doing dynamic querying is [QueryDSL](#querydsl-spring-data-extension).

Limitations:
- Restricted nesting, property constraints and string matching.
- Syntax for queries other than an exact match (i.e. when we need to use
  `ExampleMatchers`) gets weird really fast.

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

# Chapter 5 - More Repository Types
Notes for these sections were not taken in detail. I recommend you go
and watch the matching LinkedIn Learning video if you will be working
with any of these repository types.

## Spring Data MongoDb Example
- When we use Mongo as a data source, `Entities` in our codebase will
  need to be annotated with slightly different annotations that are
  specific to storing data as Documents.
- Spring data JPA will make sure that repositories like the
  `CrudRepository` and the `PagingAndSortingRepository` repositories
  work without us having to know too much about the specific data
  source.
- In addition to the db-independent repository, for mongo we can use the
  `MongoRepository`. This repository extends
  `PagingAndSortingRepository`, `QueryByExampleExecutor` and
  `CrudRepository`.
- When using Mongo as a data source the `@Query` annotation can be used
  to issue JSON queries.

## Spring Data JDBC example

Spring Data JDBC is a relatively new addition to the Spring Data
umbrella project. The key points are summarized below. However, the
LinkedIn video does not go into great detail about JDBC, so I recommend
you look for alternative sources if you need to work with this.

- Like JPA, JDBC repositories interact with **relational DBs**. However,
  JDBC does not rely on JPA; in fact, the way JDBC models the data is
  quite different.
- Unlike JPA, JDBC does NOT have auto schema generation.

###  Why use JPA?
####  Pros of JPA
- JPA has built-in features like lazy loading, caching and dirty
    tracking. These features are also the source of its weaknesses.

#### Cons of JPA
- Lazy loading can cause expensive SQL statements or unexpected
  exceptions.
- Caching can cause problems because external DB updates do not get
  saved in cache.
- Dirty tracking makes it difficult to locate the point of operation
persistence.

### Why use JDBC
#### Pros of  JDBC
- JDBC Bypasses lazy loading, caching and dirty tracking in favour of a
  simpler model.
- SQL statements are issued when and only when you call a repo method
  and return a fully loaded object (no lazy loading).

#### Cons of JDBC
- Many-to-one and many-to-many relationships are not supported at the
  JDBC layer. The limitation is better explained through an example:
  - Imagine *Dean Joe Bloggs* is the chair of both the *Human Sciences*
    and the *Natural Sciences* department. In a JDBC data model we will
    need to persist TWO *Dean Joe Bloggs*, one for each department. This
    happens because many-to-one relationships are not supported.
  - As of the recording of the LinkedIn video course, auto generated
    expression queries were not possible in JDBC. Queries needed to be
    manually implemented in SQL.

#### Important traits of JDBC
- Parent and child object lifecycles are coupled. JDBC follows the
  principles of Domain Driven Design.

## Spring Data Reactive Repository Example
Spring Boot 2 introduced the reactive stack to provided non-blocking IO.
The traditional Spring Servlet Stack serves one request per thread and
that thread gets blocked.

Spring Data followed along by providing a non-blocking Reactive
Repository that interacts with non-blocking data stores like Mongo,
Cassandra, Redis or CouchBase. By using the reactive stack and the
reactive repository, we can harness the power of reactive programming.

- The Reactive Repository is based on the reactor pattern. Methods in
  the repository MUST return either `Mono` or `Flux` types.
- `Mono` is an async publisher of 0 or 1 results.
- `Flux` is an async publisher of 0 or many results.
- Methods that return `Mono` or `Flux` return immediately as they are
  asynchronous. The code only waits when a subscriber subscribes to the
  `Mono` or `Flux` because it needs a result.

## Other Supported Data Sources in Spring Data

Here we show a list of other Spring Data modules that allow you to
interact with even more specialized data sources. All follow the
familiar repository pattern and are compatible with the property
expression auto-generated queries. However, they also provide access to
the specific traits of the data source.
- Spring Data Gemfire
- Spring Data Key Value: Provide abstractions to interact with any in
  memory key-value data store.
- Spring Data Redis: Spring Data Key Value that also allows you tap into
  the specific features of the Redis data store.
- Spring Data LDAP: access object directory mapping data sources like
  Microsoft's Active Directory or Linux's LDAP.
- Spring Data for Apache Cassandra: for interaction with the Cassandra
  DB.
- Spring Data for Apache Solr: Solr is a full text search server based
  on Lucene.
- Spring Data for Apache Hadoop: this one is NOT based on the repository
  pattern.
- Community Modules: many other independently developed Spring Data
  modules to support other data sources.

# Chapter 6 - Special Features

## Spring Data Rest

Spring Data REst is a module that exposes repository methods as REST
endpoints with no configuration. By including the
`spring-boot-starter-data-rest` dependency, we get a web server, the
routing, and the serialization / deserialization from JSON to Java
entities for free.

With this dependency include, our application will do the following at
startup:
-   Find all the Spring Data repositories
-   Create an endpoint that matches the entity name appending an "s" to
    the endpoint.
-   Expose all the operations as RESTful endpoints.

Here is the mapping between the created REST endpoints and the
`CrudRepository` methods.

| HTTP                                        | CrudRepository method |
|:--------------------------------------------|:----------------------|
| GET /resource *                             | #findAll()            |
| GET /resource/:id                           | #findOne(id)          |
| GET /resource/search/:queryMethod?:param ** | #queryMethod(param)   |
| POST /resource ***                          | #save(entity)         |
| PUT/PATCH /resource/:id ****                | #save(entity)         |
| DELETE /resource/:id                        | #delete(entity)       |

- *resource would be for `students` (in plural) for example.
- ** `/resource/search` returns documentation of all the possible search
  endpoints that can be used. These endpoints are auto-generated based
  on the available repository methods. For example, for
  `StudentQueryRepository`, we would get endpoints like
  `/students/search/findByFulltime`,
  `/students/search/findByAttendeeFirstNameAndLastName`
   - Unfortunately, the generated endpoints based on repository methods
     that take parameters don't work straight out of the box. There is
     some configuration to be done to be able to map the query string
     param to the repository param.
-  ***The format for the data sent needs to be the JSON representation
   of the entity. For example, for the `Student` entity in this repo's
   example, the data would look like this: `{"attendee": { firstName:
   "Patrick", lastName: "Black" }, "fulltime": true, "age": 20}`. Note
   that this takes into account the `@Embedded` attribute `attendee`.
- ****For updating a resource, we only need to send the data of the
  attribute we want to change. For example `{"age": 22}`.

The returned autogenerated JSON responses also include in-response
HATEOAS documentation.

## QueryDSL Spring Data Extension

By default, spring data query methods are static, meaning that the
repository pre-defines what queries can be done. In certain cases, this
is problematic in two ways:
1. Enumerating all the query methods to enable querying by the
   combination of all (or many) attributes quickly becomes cumbersome.
   This is the case regardless of us using auto-generated queries with
   Spring Data expressions, JPQL, etc.
2. In some use cases, we don't want the repository to be restrictive, and
   we need to give the client (the user of the repository) full freedom
   to define the query it needs.

QueryDSL was created to overcome these two problems. QueryDSL is a
dynamic search criteria framework available for JPA (relational DBs) and
Mongo DB.

### How it works

QueryDSL has four main building blocks:
1. `QueryDslPredicateExecutor<Entity>` is used to extend your
   repository, which typically also extends from CrudRepository,
   JPARepository or MongoRepository. `QueryDslPredicateExecutor`
   overloads some methods to allow them to take a `Predicate` as an
   argument (see below).
2. A `Predicate` is essentially the query we want to make. A `Predicate`
   object is one or more `BooleanExpression`s chained together.
3. A `BooleanExpression` represents a query constraint over a particular
   attribute (e.g. `QStudent.student.age.gt(20)`). QueryDSL allows us to
   create `BooleanExpressions` through `Q{Entity}` objects (e.g.
   `QStudent`).
4. A `Q{Entity}` class is an auto-generated class created at compile
   time by QueryDSL which defines all the tooling for creating
   `BooleanExpressions`. QueryDSL reads all the classes annotated with
   `@Entity` and generates a sibling `Q{Entity}` class. This
   auto-generation requires a little of setup (see the
   [pom.xml](pom.xml) file for all the setup required).

```java
public interface StudentRepository extends CrudRepository<Student, Integer>,
        QueryDslPredicateExecutor<Student> {
    /* *
    * Available methods through QuerydslPredicateExecutor
    * ====================================================
    * Student findOne(Predicate predicate);
    * Iterable<Student> findAll(Predicate predicate);
    * Iterable<Student> findAll(Predicate predicate, Sort sort);
    * Page<Student> findAll(Predicate predicate, Pageable pageable);
    * long count(Predicate predicate);
    * boolean exists(Predicate predicate);
    * */
}
```

### Using a QueryDSL-enabled repository
The following files show how to use QueryDSL:
- [StudentQueryDSLRepository](src/main/java/com/example/university/repo/StudentQueryDSLRepository.java)
  shows how to set up the repo.
- [StudentQueryDSLExpressionsFactory](src/main/java/com/example/university/repo/StudentQueryDSLExpressionsFactory.java)
  shows a **suggested and optional** pattern to make it easier for repo
  clients to form `BooleanExpressions`.
- [StudentQueryDSLRepositoryDemo](src/test/java/com/example/university/StudentQueryDSLRepositoryDemo.java)
  shows how to actually query using the repo. It shows two variants:
  - The client freely building whatever predicate it wants through the
    direct use of `QStudent`.
  - The client uses the `StudentQueryDSLExpressionsFactory` helper class
    to build queries.

### Resources
There are other ways to use QueryDSL, these resources are useful:
- https://musibs.medium.com/using-querydsl-with-spring-data-jpa-a28bfda35ded
- https://querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration

### Caveats
Architecturally speaking, enabling repo clients to form the queries
generates a leak in the persistence logic and violates [the dependency
rule](https://github.com/serodriguez68/clean-architecture/blob/master/part-5-2-architecture.md#the-dependency-rule).
In **some** use cases, the value of enabling clients to query
dynamically might outweigh the negative impact in your architecture.
However, generally speaking this is not the case, so consider your
choices carefully.

QueryDSL can still be used **within the persistence layer** (without
leaking it to other layers) as a way to ease the implementation of
repository methods.

## Auditing

Note: the course only covered this topic in very little detail. We
suggest you find additional resources for this. [This YouTube video](https://www.youtube.com/watch?v=UTZJhILIpUs)
explains this topic in a nice way.

Auditing in the Spring Data context means tracking when a record was
created, updated and who did it. Spring Data comes with a feature to do
this and has two ways to set it up.

### Option 1: Annotations in the Entity

You can decorate your own entity attributes with pre-defined annotations
and Spring Data will do the rest.
```java
@CreatedDate
@Column
private ZonedDateTime createdAt;

@LastModifiedDate
@Column
private ZonedDateTime updatedAt;

@CreatedBy
@Column
private User createdBy;

@LastModifiedBy
@Column
private User updatedBy;
```


### Option 2: Implement `Auditable` and extend `AbstractAuditable`

This option allows us not to touch the entity attributes.
`AbstractAuditable` provides the implementation for the attributes that
store the audit data.

```java
public class Staff extends AbstractAuditable<User, Integer> implements Auditable<User, Integer> {
    // ...
}
```

### Getting access to the current `User`

Regardless of the option you use, you need to wire up a way to get the
`User` from the web session and inject it into the entity. Here is a
code snippet to extract the user from the session. This is done by
implementing `AuditorAware`.

```java
public class SpringSecurityAuditorAware implements AuditorAware<User> {
    public User getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return ((MyUserDetails) authentication.getPrincipal()).getUser();
    }
}
```

## Read-Only repository pattern

Out of the box, Spring Data does not come with a read-only version of a
repository. However, creating read-only repositories is a well followed
convention in the community.

Doing this is very easy and mostly relies on pure Java.
- Go to
  [ReadOnlyRepository](src/main/java/com/example/university/repo/ReadOnlyRepository.java)
  to see how to set up an interface that will make any repo that extends
  it read-only (as long as those repos themselves don't declare methods
  that write).
- Go to
  [CourseQueryRepository](src/main/java/com/example/university/repo/CourseQueryRepository.java)
  to see a repository that extends from the ReadOnlyRepository.

By convention, the repositories that extend from a read-only repository
are usually called `*QueryRepository`.

# Other topics

## Database migrations

By default, Spring Data does not come with a pre-determined way for
managing database migrations. By default, Spring's behaviour depends on
the type of DB being used:
- If the DB is an in-memory DB like H2, Spring activates the auto
  generation of the schema following the entities' structure. This can
  be controlled with the `spring.jpa.generate-ddl` or
  `spring.jpa.hibernate.ddl-auto` properties (
  [see documentation](https://docs.spring.io/spring-boot/docs/2.1.1.RELEASE/reference/html/howto-database-initialization.html#howto-initialize-a-database-using-jpa)).
- If the DB is a "real" database, then by default Spring Data doesn't do
  anything to auto-generate the schema, but also does not provide any
  tooling for doing it.

However, Spring Data is compatible by default with 2 popular database
migration tools: Flyway and Liquidbase. The usage of those libraries is
beyond the scope of this summary. However, here are some useful
resources to learn more about them.
- [Introduction to Flyway and Liquidbase with Spring Boot](https://www.youtube.com/watch?v=TMUMWfSS3yw)
  - [Introduction to Flyway](https://www.youtube.com/watch?v=ovG1wgEqE10)
  - [Introduction to Liquidbase](https://www.youtube.com/watch?v=SyrjSPC8EZ8)
- [Generating Flyway migrations based on the Project Entities with IntelliJ IDEA](https://www.baeldung.com/database-migrations-with-flyway#generate-versioned-migrations-in-intellij-idea)
- [Running Databse Migrations for Java Apps in Heroku](https://devcenter.heroku.com/articles/running-database-migrations-for-java-apps)

## Miscellaneous

- `@Embeddable`: go to `Person.java` and `Student.java` to understand
  what an embeddable class is.
- `Course.java` has an example of a circular reference of a table to the
  same table

# Open questions
Can we remap an awkwardly named autogenerated query to a better name?