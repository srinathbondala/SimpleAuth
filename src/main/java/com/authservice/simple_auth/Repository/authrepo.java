package com.authservice.simple_auth.Repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.authservice.simple_auth.Model.authData;


public interface authrepo extends MongoRepository<authData,String> {
    Optional<authData> findByUsername(String username);
    Optional<authData> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    @Query(value = "{ 'username': ?0, 'email': ?1 }", fields = "{ 'applications': 1 }")
    Optional<authData> findApplicationsByUsernameAndEmail(String username, String email);
}
