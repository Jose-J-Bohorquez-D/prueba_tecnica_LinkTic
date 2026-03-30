package com.linktic.test.inventory.exception;

import com.linktic.test.inventory.dto.ErrorObject;
import com.linktic.test.inventory.dto.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ErrorObject error = ErrorObject.builder()
                .status(String.valueOf(status.value()))
                .title(status.getReasonPhrase())
                .detail(ex.getReason() != null ? ex.getReason() : status.getReasonPhrase())
                .build();
        return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(error)), status);
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<Void> handleDuplicateRequest(DuplicateRequestException ex) {
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        ErrorObject error = ErrorObject.builder()
                .status(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .title("Resource Not Found")
                .detail(ex.getMessage())
                .build();
        return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(error)), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ErrorObject error = ErrorObject.builder()
                .status(String.valueOf(HttpStatus.CONFLICT.value()))
                .title("Data Integrity Violation")
                .detail("A resource with the same unique identifier already exists.")
                .build();
        return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(error)), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorObject error = ErrorObject.builder()
                .status(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .title("Validation Failed")
                .detail(details)
                .build();
        return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(error)), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorObject error = ErrorObject.builder()
                .status(String.valueOf(HttpStatus.CONFLICT.value()))
                .title("Conflict")
                .detail(ex.getMessage())
                .build();
        return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(error)), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorObject error = ErrorObject.builder()
                .status(String.valueOf(HttpStatus.CONFLICT.value()))
                .title("Conflict")
                .detail(ex.getMessage())
                .build();
        return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(error)), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorObject error = ErrorObject.builder()
                .status(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .title("Internal Server Error")
                .detail(ex.getMessage())
                .build();
        return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(error)), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
