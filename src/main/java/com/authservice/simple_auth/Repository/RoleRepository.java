package com.authservice.simple_auth.Repository;

import com.authservice.simple_auth.Model.ERole;
import com.authservice.simple_auth.Model.Role;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String>{
    Optional<Role> findByName(ERole name);
}