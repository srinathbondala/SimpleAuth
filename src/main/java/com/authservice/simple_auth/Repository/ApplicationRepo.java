package com.authservice.simple_auth.Repository;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.authservice.simple_auth.Model.Application;

public interface  ApplicationRepo extends MongoRepository<Application, String> {
    Optional<Application> findByApplicationName(String applicationName);
}
