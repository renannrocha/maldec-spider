package org.maldeclabs.spider.gateways.rest.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.maldeclabs.spider.application.services.exceptions.AccountStatusException;
import org.maldeclabs.spider.gateways.rest.responses.StandardErrorResponse;
import org.maldeclabs.spider.application.services.exceptions.DatabaseException;
import org.maldeclabs.spider.application.services.exceptions.ResourceNotFoundException;
import org.maldeclabs.spider.gateways.rest.responses.StandardErrorsResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardErrorsResponse> methodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request){
        List<String> errors = e.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorsResponse err = new StandardErrorsResponse(Instant.now(), status.value(), errors, request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<StandardErrorResponse> accountStatusException(AccountStatusException e, HttpServletRequest request){
        String error = "Status Error";
        HttpStatus status = HttpStatus.OK;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardErrorResponse> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request){
        String error = "Resource not found";
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<StandardErrorResponse> database(DatabaseException e, HttpServletRequest request){
        String error = "Database error";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<StandardErrorResponse> methodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request){
        String error = "Method not supported";
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardErrorResponse> duplicateData(DataIntegrityViolationException e, HttpServletRequest request){
        String error = "Duplicate data";
        HttpStatus status = HttpStatus.CONFLICT;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardErrorResponse> illegalArgument(IllegalArgumentException e, HttpServletRequest request){
        String error = "Invalid argument";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardErrorResponse> accessDenied(AccessDeniedException e, HttpServletRequest request){
        String error = "Access denied";
        HttpStatus status = HttpStatus.FORBIDDEN;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<StandardErrorResponse> mediaTypeNotSupported(HttpMediaTypeNotSupportedException e, HttpServletRequest request){
        String error = "Media type not supported";
        HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardErrorResponse> globalExceptionHandler(Exception e, HttpServletRequest request){
        String error = "Internal server error";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<StandardErrorResponse> transactionSystemException(TransactionSystemException e, HttpServletRequest request){
        String error = "Transaction error";
        HttpStatus status = HttpStatus.CONFLICT;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardErrorResponse> badCredentialsException(BadCredentialsException e, HttpServletRequest request){
        String error = "Bad Credentials";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, "invalid password or email", request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<StandardErrorResponse> usernameNotFoundException(UsernameNotFoundException e, HttpServletRequest request){
        String error = "Username not Find";
        HttpStatus status = HttpStatus.CONFLICT;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardErrorResponse> httpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request){
        String error = "HTTP Message Not Readable";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, "Fields not found", request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StandardErrorResponse> constraintViolationException(ConstraintViolationException e, HttpServletRequest request){
        String error = "Validation Error";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorResponse err = new StandardErrorResponse(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }
}
