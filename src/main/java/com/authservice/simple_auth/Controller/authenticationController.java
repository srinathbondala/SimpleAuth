package com.authservice.simple_auth.Controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authservice.simple_auth.Model.ERole;
import com.authservice.simple_auth.Model.Role;
import com.authservice.simple_auth.Model.authData;
import com.authservice.simple_auth.Model.payload.response.JwtResponse;
import com.authservice.simple_auth.Model.payload.response.LoginRequest;
import com.authservice.simple_auth.Model.payload.response.MessageResponse;
import com.authservice.simple_auth.Model.payload.response.SignupRequest;
import com.authservice.simple_auth.Repository.RoleRepository;
import com.authservice.simple_auth.Repository.authrepo;
import com.authservice.simple_auth.Service.UserDetailsImpl;
import com.authservice.simple_auth.Service.authservice;
import com.authservice.simple_auth.jwt.JwtUtils;

import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class authenticationController {
    @Autowired
    authservice authservice;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    authrepo userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest , HttpServletResponse response) {
        final Logger logger = LoggerFactory.getLogger(authenticationController.class);
        try {
                logger.info("Inside authenticateUser");
                Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail().trim(), loginRequest.getPassword().trim()));
                logger.info("After authenticate "+authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
                List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
                logger.info("roles"+roles);
                 
            // Cookie cookie = new Cookie("jwtToken", jwt);
            // cookie.setHttpOnly(true);
            // cookie.setSecure(true);
            // cookie.setPath("/");
            // cookie.setMaxAge(8100);

            // response.addCookie(cookie);
            return ResponseEntity.ok(new JwtResponse(jwt, 
                userDetails.getId(), 
                userDetails.getUsername(), 
                userDetails.getEmail(), 
                roles));    
                // return ResponseEntity.ok("success");
        } catch (AuthenticationException e) {
            logger.info("--------------------------------------------");
            logger.error("Error: {}", e.getMessage());
            logger.info("--------------------------------------------");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body( "Invalid username or password");
        }
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if(authservice.checkApplicationRegesterd(signUpRequest.getApplicationName())){
            return ResponseEntity
            .badRequest()
            .body(new MessageResponse("Error: Client Not Reistered"));
        }
        if (authservice.checkUserForApplication(signUpRequest.getUsername(),signUpRequest.getEmail(),signUpRequest.getApplicationName())) {
            return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: Username is already taken!"));
        }
        if(userRepository.existsByUsername(signUpRequest.getUsername())){
            return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        authData user = new authData(signUpRequest.getUsername(), 
                signUpRequest.getEmail(),
                signUpRequest.getPhone(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ROLE_ADMIN" -> {
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                    }
                    case "ROLE_MODERATOR" -> {
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                    }
                    default -> {
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                    }
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            SecurityContextHolder.clearContext();
            
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            Cookie cookie = new Cookie("jwtToken", null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logout failed");
        }
    }

    @GetMapping("/details")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String token) {
        final Logger logger = LoggerFactory.getLogger(authenticationController.class);
        logger.info("Inside getUserDetails");
        // try {
        //     String jwt = token.substring(7);  // Remove "Bearer " prefix
        //     String username = jwtUtils.getUserNameFromJwtToken(jwt);

        //     if (username != null && SecurityContextHolder.getContext().getAuthentication() != null) {
        //         UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //         return ResponseEntity.ok(userDetails);
        //     } else {
        //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or user not authenticated");
        //     }
        // } catch (ClassCastException e) {
        //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing user details");
        // }
        return authservice.getdatafromjwt(token);
    }

    @RequestMapping("/")
    public String welcome(){
        final Logger logger = LoggerFactory.getLogger(authenticationController.class);
        SecretKeySpec secretKey = (SecretKeySpec) Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        byte[] keyBytes = secretKey.getEncoded();
        String base64EncodedKey = java.util.Base64.getEncoder().encodeToString(keyBytes);


        logger.info("Generated JWT Secret: " + base64EncodedKey);
        return "welcome";
    }
    @GetMapping("/data")
    public List<authData> getdata(){
        return authservice.giveAll();
    }
    @GetMapping("/checkemail/{email}")
    public String checkemail(@PathVariable String email) {
        if(authservice.checkUserExists(email)) {
            return "success";//return authservice.getUserById(email).getPassword();
        }
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestHeader("Authorization") String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing token");
        }

        String token = authorization.substring(7);

        if (!jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }

        return ResponseEntity.ok("Valid");
    }

    @DeleteMapping("/deleteuser/{email}")
    public ResponseEntity<?> updateStatus(@PathVariable String email) {
        authservice.updateStatus(email,false);
        return ResponseEntity.ok("Success");
    }
}
