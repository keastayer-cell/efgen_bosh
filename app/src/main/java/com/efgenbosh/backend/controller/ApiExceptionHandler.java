package com.efgenbosh.backend.controller;

import com.efgenbosh.backend.dto.ApiErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log =
        LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
        IllegalArgumentException exception,
        HttpServletRequest request
    ) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getAllErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("Некорректные данные.");
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
            fieldErrors.putIfAbsent(
                fieldError.getField(),
                fieldError.getDefaultMessage()
            )
        );
        return ResponseEntity.badRequest().body(
            new ApiErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                message,
                request.getRequestURI(),
                fieldErrors
            )
        );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleJwt(
        JwtException exception,
        HttpServletRequest request
    ) {
        return error(
            HttpStatus.UNAUTHORIZED,
            "Невалидный или истекший токен.",
            request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
        AccessDeniedException exception,
        HttpServletRequest request
    ) {
        return error(HttpStatus.FORBIDDEN, "Недостаточно прав доступа.", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
        ResponseStatusException exception,
        HttpServletRequest request
    ) {
        String message = exception.getReason() == null
            || exception.getReason().isBlank()
            ? "Не удалось выполнить запрос."
            : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode()).body(
            ApiErrorResponse.of(
                exception.getStatusCode().value(),
                message,
                request.getRequestURI()
            )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
        ConstraintViolationException exception,
        HttpServletRequest request
    ) {
        String message = exception.getConstraintViolations().stream()
            .findFirst()
            .map(violation -> violation.getMessage())
            .orElse("Некорректные данные.");
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
        Exception exception,
        HttpServletRequest request
    ) {
        return error(
            HttpStatus.BAD_REQUEST,
            "Некорректный формат запроса.",
            request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
        DataIntegrityViolationException exception,
        HttpServletRequest request
    ) {
        log.warn(
            "Data constraint rejected {} {}",
            request.getMethod(),
            request.getRequestURI()
        );
        return error(
            HttpStatus.CONFLICT,
            "Данные конфликтуют с уже существующей записью.",
            request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
        Exception exception,
        HttpServletRequest request
    ) {
        log.error(
            "Unhandled API error for {} {} type={}",
            request.getMethod(),
            request.getRequestURI(),
            exception.getClass().getSimpleName()
        );
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Внутренняя ошибка сервера.",
            request
        );
    }

    private ResponseEntity<ApiErrorResponse> error(
        HttpStatus status,
        String message,
        HttpServletRequest request
    ) {
        String safeMessage = message == null || message.isBlank()
            ? "Не удалось выполнить запрос."
            : message;
        return ResponseEntity.status(status).body(
            ApiErrorResponse.of(
                status.value(),
                safeMessage,
                request.getRequestURI()
            )
        );
    }
}
