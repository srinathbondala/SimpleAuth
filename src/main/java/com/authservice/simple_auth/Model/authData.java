package com.authservice.simple_auth.Model;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@EnableMongoAuditing
@Document(collection = "auth")
public class authData {
    @Id
    private String id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;
    private String phone;

    private Boolean active = true;

    @DBRef
    private Set<Role> roles = new HashSet<>();

    private String defaultAddress;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @DBRef
    private Set<Application> applications = new HashSet<>();
    
    private HashMap<String,Integer> deviceCount = new HashMap<>();

    public String getDefaultAddress() {
      return defaultAddress;
    }

    public void setDefaultAddress(String defaultAddress) {
      this.defaultAddress = defaultAddress;
    }

    public authData() {
    }

    public authData(String username, String email, String phone, String password) {
      this.username = username;
      this.email = email;
      this.phone=phone;
      this.password = password;
    }

    public String getPhone() {
      return phone;
    }
    
    public void setPhone(String phone) {
      this.phone = phone;
    }

    public Integer getDeviceCount(String application) {
      return deviceCount.get(application);
    }

    public void setDeviceCount(String application,Integer Count) {
      this.deviceCount.put(application,Count);
    }

    public String getId() {
      return id;
    }
  
    public void setId(String id) {
      this.id = id;
    }
  
    public String getUsername() {
      return username;
    }
  
    public void setUsername(String username) {
      this.username = username;
    }
  
    public String getEmail() {
      return email;
    }
  
    public void setEmail(String email) {
      this.email = email;
    }
  
    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public Set<Role> getRoles() {
      return roles;
    }
  
    public void setRoles(Set<Role> roles) {
      this.roles = roles;
    }
}
