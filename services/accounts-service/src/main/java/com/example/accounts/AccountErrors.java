package com.example.accounts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestControllerAdvice
public class AccountErrors {
    record Error(String code,String message,String correlationId) {}
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Error> login(Exception e,HttpServletRequest r) { return error(401,"UNAUTHENTICATED","bad username or password",r); }
    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<Error> duplicate(Exception e,HttpServletRequest r) { return error(409,"DUPLICATE","username already exists",r); }
    @ExceptionHandler({MethodArgumentNotValidException.class,IllegalArgumentException.class})
    ResponseEntity<Error> validation(Exception e,HttpServletRequest r) { return error(400,"VALIDATION","registration details are invalid",r); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Error> internal(Exception e,HttpServletRequest r) { return error(500,"INTERNAL","unexpected error",r); }
    private ResponseEntity<Error> error(int status,String code,String message,HttpServletRequest request) {
        String id=request.getHeader("X-Correlation-Id");
        if(id==null || id.isBlank()) id=UUID.randomUUID().toString();
        return ResponseEntity.status(status).body(new Error(code,message,id));
    }
}
