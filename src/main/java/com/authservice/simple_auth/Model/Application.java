package com.authservice.simple_auth.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "applications")
public class Application {
    @Id
    private String id;
    @Indexed(unique = true)
    private String applicationName;
    private int code;
    private String description;

    public Application() {}
}
