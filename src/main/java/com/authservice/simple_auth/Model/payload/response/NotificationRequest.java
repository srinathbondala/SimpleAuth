package com.authservice.simple_auth.Model.payload.response;

import java.util.Map;

import lombok.Data;

@Data
public class NotificationRequest {
    private String to;
    private String from;
    private String body;
    private String code;
    private Map<String, String> variables;
}
