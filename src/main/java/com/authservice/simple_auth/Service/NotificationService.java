package com.authservice.simple_auth.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public String buildMessage(String code) {
        String fileName = switch (code) {
            case "LOGIN_CONFIRMATION" -> "templates/login-confirmation.html";
            case "REGISTRATION_CONFIRMATION" -> "templates/registration-confirmation.html";
            case "OTP" -> "templates/otp.html";
            default -> throw new RuntimeException("Invalid template code: " + code);
        };
        try (InputStream inputStream =
                    new ClassPathResource(fileName).getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            return reader.lines().collect(Collectors.joining("\n"));

        } catch (Exception e) {
            throw new RuntimeException("Error loading template", e);
        }
    }

    public void sendEmail(String to, String from, String subject, String htmlBody) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setFrom(from);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }
}
