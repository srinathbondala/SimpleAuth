package com.authservice.simple_auth.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/info")
public class healthcheckController {
    @GetMapping("/healthcheck")
    public String healthCheck(){
        return "v1.0.0";
    }

    @GetMapping("/**")
    public ResponseEntity<?> info(){
         String html = """
        <html>
            <body>
                <h1>Server is healthy</h1>
                <p>Version: v1.0.0</p>
            </body>
        </html>
        """;
        return ResponseEntity.ok()
        .header("Content-Type", "text/html")
        .body(html);
    }
}