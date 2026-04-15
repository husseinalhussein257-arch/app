package com.uniquest.backend.repository;

import com.uniquest.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    /** Used by login flow and JWT filter to resolve username -> User. */
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
