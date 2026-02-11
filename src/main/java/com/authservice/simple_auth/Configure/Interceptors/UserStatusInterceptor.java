package com.authservice.simple_auth.Configure.Interceptors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class UserStatusInterceptor implements HandlerInterceptor {

    // @Autowired
    // private authservice authService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid token");
            return false;
        }

        // boolean isActive = authService.isUserActive(userId);

        // if (!isActive) {
        //     response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        //     response.getWriter().write("User is inactive");
        //     return false;
        // }

        return true; // allow request
    }
}
