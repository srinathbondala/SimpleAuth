package com.authservice.simple_auth.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authservice.simple_auth.Model.payload.response.MessageResponse;
import com.authservice.simple_auth.Model.payload.response.NotificationRequest;
import com.authservice.simple_auth.Service.NotificationService;

@CrossOrigin("*")
@RestController
@RequestMapping("api/notification")
public class notificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) throws Exception {
        try {
            String message = notificationService.buildMessage(
                request.getCode()
            );

            notificationService.sendEmail(
                request.getTo(),
                request.getFrom(),
                "Notification",
                message
            );

            return ResponseEntity.ok(new MessageResponse("Email Sent Successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occured while sending mail");
        }
    }
}
