package com.example.university.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.util.Optional;

/**
 * Interface for declaring Read-Only Query Repositories.
 * - The @NoRepositoryBean annotation tells the Spring repository scanner NOT to instantiate a class for this
 *   interface. This interface will truly be just an interface.
 *   - The way Spring Data makes all the auto-generated implementations
 *     from query expressions and JPQL is to auto-generate classes from the repositories
 *     during app initialization. This will not be the case for the ReadOnlyRepository.
 */
@NoRepositoryBean
public interface ReadOnlyRepository<T, ID extends Serializable> extends Repository<T,ID > {

    /*
    * The methods that we add match exactly the signature of the methods in CrudRepository.
    * We can select what methods we want all ReadOnlyRepositories to have by including / excluding
    * more methods here.
    * */
    Optional<T> findById(ID id);

    Iterable<T> findAll();

    Iterable<T> findAllById(Iterable<ID> iterable);

    Iterable<T> findAll(Sort sort);

    Page<T> findAll(Pageable pageable);

    long count();

    boolean existsById(ID id);
}