package com.beyond.ordersystem.common.exception;

import com.beyond.ordersystem.common.dtos.CommonErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthenticationHandler implements AuthenticationEntryPoint {
    private final  ObjectMapper objectMapper;
    public JwtAuthenticationHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        authException.printStackTrace();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //401 상태코드 세팅
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(401)
                .error_message("token이 없거나 유효하지 않습니다.")
                .build();
        String data = objectMapper.writeValueAsString(dto);
        PrintWriter printWriter =  response.getWriter();
        printWriter.write(data);
        printWriter.flush();
    }
}
