package com.example.lostfound.security;

import com.example.lostfound.handler.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
public class SecurityErrors {
    private final ObjectMapper mapper;
    public SecurityErrors(ObjectMapper mapper) { this.mapper = mapper; }

    public void write(HttpServletRequest request, HttpServletResponse response,
                      int status, String code, String message) throws IOException {
        String id = request.getHeader("X-Correlation-Id");
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), new ApiError(code, message, id));
    }
}
