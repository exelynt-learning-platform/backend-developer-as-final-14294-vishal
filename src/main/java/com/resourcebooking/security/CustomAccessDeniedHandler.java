package com.resourcebooking.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourcebooking.common.response.ApiError;
import com.resourcebooking.common.response.ApiResponse;
import com.resourcebooking.common.response.constant.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        ApiError error = ApiError.builder()
                .code(ErrorCode.FORBIDDEN)
                .build();

        ApiResponse<Void> apiResponse = ApiResponse.failure(
                "Access denied. You do not have permission to perform this action.",
                error
        );

        response.getWriter().write(
                objectMapper.writeValueAsString(apiResponse)
        );
    }

}