package com.authservice.simple_auth.jwt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.authservice.simple_auth.Service.UserDetailsImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  // @Value("${spring.app.jwtSecret}")
  // private String jwtSecret;

  @Value("${jwt.private-key}")
  private String privateKeyPem;

  @Value("${jwt.private-path}")
  private String privateKeyPath;

  @Value("${jwt.public-path}")
  private String publicKeyPath;

  @Value("${jwt.public-key}")
  private String publicKeyPem;

  @Value("${spring.app.jwtExpirationMs}")
  private int jwtExpirationMs;

  private PrivateKey privateKey;
  private PublicKey publicKey;

  @PostConstruct
  public void init() {
      this.privateKey = loadPrivateKey();
      this.publicKey = loadPublicKey();
      logger.info("RSA keys loaded successfully.");
  }

  public String generateJwtToken(Authentication authentication) {

    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

    return Jwts.builder()   
        .setSubject((userPrincipal.getEmail()))
        .claim("username", userPrincipal.getUsername())
        .claim("roles",
                userPrincipal.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();
  }
  
  /*  JWT using single secret key */
  // private Key key() {
  //   return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  // }

  private PrivateKey loadPrivateKey() {
    try {
        String pem;

        if (privateKeyPem != null && !privateKeyPem.isBlank()) {
            pem = privateKeyPem.replace("\\n", "\n");
        } else if (privateKeyPath != null && !privateKeyPath.isBlank()) {
            pem = Files.readString(Path.of(privateKeyPath));
        } else {
            throw new IllegalStateException("Private key not configured");
        }

        pem = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "")
                .trim();

        byte[] keyBytes = Base64.getDecoder().decode(pem);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        return KeyFactory.getInstance("RSA").generatePrivate(spec);

    } catch (Exception e) {
        throw new RuntimeException("Failed to load private key", e);
    }
}


  public String getUserNameFromJwtToken(String token) {
    return Jwts.parserBuilder().setSigningKey(publicKey).build()
               .parseClaimsJws(token).getBody().getSubject();
  }

  private PublicKey loadPublicKey() {
    try {
        String pem;

        if (publicKeyPem != null && !publicKeyPem.isBlank()) {
            pem = publicKeyPem.replace("\\n", "\n");
        } else if (publicKeyPath != null && !publicKeyPath.isBlank()) {
            pem = Files.readString(Path.of(publicKeyPath));
        } else {
            throw new IllegalStateException("Public key not configured");
        }

        pem = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "")
                .trim();

        byte[] keyBytes = Base64.getDecoder().decode(pem);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

        return KeyFactory.getInstance("RSA").generatePublic(spec);

    } catch (Exception e) {
        throw new RuntimeException("Failed to load public key", e);
    }
}


  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parserBuilder().setSigningKey(publicKey).build().parse(authToken);
      return true;
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }

    return false;
  }
}
