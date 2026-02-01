package com.authservice.simple_auth.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.authservice.simple_auth.Model.authData;
import com.authservice.simple_auth.Repository.ApplicationRepo;
import com.authservice.simple_auth.Repository.authrepo;
import com.authservice.simple_auth.jwt.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class authservice {
    @Autowired
    authrepo authrepo;

    @Autowired
    ApplicationRepo applicationRepo;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /*-----------------------------User Derails services------------------------ */

    public List<authData> giveAll() {
        return authrepo.findAll();
    }

    public authData getUserById(String id) {
        return authrepo.findById(id).get();
    }

    public void deleteUser(String email) {
        authrepo.deleteById(email);
    }

    public void updateUser(authData user) {
        authrepo.save(user);
    }

    public void updateStatus(String userEmail, Boolean status) {
        Query query = new Query(Criteria.where("_email").is(userEmail));
        Update update = new Update().set("active", status);
        mongoTemplate.updateFirst(query, update, authData.class);
    }


    public boolean checkUser(String email, String password) {
        authData user = authrepo.findById(email).get();
        return user.getPassword().equals(password);
    }

    public boolean checkUserExists(String email) {
        return authrepo.existsById(email);
    }

    public Object getUserData(Object body) {
        try {
            String userId = getUserIdFromJwt(body);
            Query query = new Query(Criteria.where("_id").is(userId));
            query.fields().exclude("password");
            return mongoTemplate.findOne(query, authData.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing request");
        }
    }

    public boolean checkUserForApplication(String username, String email, String applicationName){
         try {
            Optional<authData> userOpt = authrepo.findApplicationsByUsernameAndEmail(username, email);
            if (userOpt.isEmpty()) {
                return false;
            }

            // Only applications are fetched
            return userOpt.get().getApplications().stream()
                        .anyMatch(app -> app.getApplicationName().equals(applicationName));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkApplicationRegesterd(String ApplicationName){
        return !applicationRepo.findByApplicationName(ApplicationName).isEmpty();
    }

    /*------------------------------jwt services------------------------------------------*/

    public ResponseEntity<?> getdatafromjwt(String token) {
        try {
            String jwt = token.substring(7);  // Remove "Bearer " prefix
            String username = jwtUtils.getUserNameFromJwtToken(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                return ResponseEntity.ok(userDetails);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or user not authenticated");
            }
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing user details");
        }
    }
    
    public String getUserIdFromJwt(Object object){
        try{
            String jsonString = objectMapper.writeValueAsString(object);
            JsonNode userDetailsNode = objectMapper.readTree(jsonString);
            return userDetailsNode.get("id").asText();
        }
        catch(Exception e){
            return e.getMessage();
        }
    }
}
