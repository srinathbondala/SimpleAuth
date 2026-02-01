package com.authservice.simple_auth.Configure;

import com.authservice.simple_auth.Model.ERole;
import com.authservice.simple_auth.Model.Role;
import com.authservice.simple_auth.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DbSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            List<Role> roles = Arrays.asList(
                    new Role(ERole.ROLE_USER),
                    new Role(ERole.ROLE_MODERATOR),
                    new Role(ERole.ROLE_ADMIN)
            );
            roleRepository.saveAll(roles);
        }
    }
}