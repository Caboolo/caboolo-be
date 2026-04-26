package com.caboolo.backend.core.controller;

import com.caboolo.backend.core.dto.RestEntity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * Centralised exception handling for all REST controllers.
 * Every handler logs the exception (with full stack trace) and returns
 * a consistent {@link RestEntity} body via {@link BaseController#errorResponse}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseController {

    // -------------------------------------------------------------------------
    // Validation & binding errors
    // -------------------------------------------------------------------------

    /**
     * Triggered when a {@code @Valid} / {@code @Validated} annotated request body
     * fails validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestEntity<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        String message = "Validation failed: " + details;
        log.warn("Validation error — {}", message, ex);
        return errorResponse(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Triggered when a {@code @RequestParam} or {@code @PathVariable} type cannot
     * be converted (e.g. passing "abc" for a Long parameter).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public RestEntity<Void> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "Parameter '%s' must be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        log.warn("Type mismatch — {}", message, ex);
        return errorResponse(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Triggered when a required {@code @RequestParam} is absent.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public RestEntity<Void> handleMissingRequestParam(MissingServletRequestParameterException ex) {
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        log.warn("Missing request parameter — {}", message, ex);
        return errorResponse(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Triggered when the request body cannot be parsed (e.g. malformed JSON).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public RestEntity<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request body — {}", ex.getMessage(), ex);
        return errorResponse("Request body is missing or malformed", HttpStatus.BAD_REQUEST);
    }

    /**
     * Triggered by Bean Validation constraint violations outside of a request body
     * (e.g. service-level validation, {@code @RequestParam} with {@code @Validated}).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public RestEntity<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));

        log.warn("Constraint violation — {}", details, ex);
        return errorResponse("Constraint violation: " + details, HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------
    // Routing errors
    // -------------------------------------------------------------------------

    /**
     * Triggered when no handler mapping is found for the requested path.
     * Requires {@code spring.mvc.throw-exception-if-no-handler-found=true} and
     * {@code spring.web.resources.add-mappings=false} in application.properties.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public RestEntity<Void> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = String.format("No endpoint found for %s %s", ex.getHttpMethod(), ex.getRequestURL());
        log.warn("No handler found — {}", message, ex);
        return errorResponse(message, HttpStatus.NOT_FOUND);
    }

    /**
     * Triggered when the HTTP method used is not supported by the matched endpoint.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public RestEntity<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String message = String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod());
        log.warn("Method not supported — {}", message, ex);
        return errorResponse(message, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // -------------------------------------------------------------------------
    // Security errors
    // -------------------------------------------------------------------------

    /**
     * Triggered when an authenticated user tries to access a resource they are
     * not authorised for.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public RestEntity<Void> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied — {}", ex.getMessage(), ex);
        return errorResponse("You do not have permission to perform this action", HttpStatus.FORBIDDEN);
    }

    /**
     * Triggered when authentication fails (invalid credentials, expired token, etc.).
     */
    @ExceptionHandler(AuthenticationException.class)
    public RestEntity<Void> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failure — {}", ex.getMessage(), ex);
        return errorResponse("Authentication failed: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Specific handler for bad credentials (wrong username / password).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public RestEntity<Void> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials — {}", ex.getMessage(), ex);
        return errorResponse("Invalid username or password", HttpStatus.UNAUTHORIZED);
    }

    // -------------------------------------------------------------------------
    // Database errors
    // -------------------------------------------------------------------------

    /**
     * Triggered by unique-constraint, foreign-key, or not-null violations at the
     * database level.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public RestEntity<Void> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation — {}", ex.getMostSpecificCause().getMessage(), ex);
        return errorResponse(
                "A data integrity violation occurred. The record may already exist or a required field is missing.",
                HttpStatus.CONFLICT);
    }

    // -------------------------------------------------------------------------
    // General Data & State errors
    // -------------------------------------------------------------------------

    /**
     * Triggered for bad input logic or invalid workflow states within services.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public RestEntity<Void> handleIllegalArgumentsAndState(RuntimeException ex) {
        log.warn("Illegal argument/state — {}", ex.getMessage(), ex);
        return errorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Triggered when an entity is not found in the database by JPA.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public RestEntity<Void> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found — {}", ex.getMessage(), ex);
        return errorResponse("The requested resource could not be found", HttpStatus.NOT_FOUND);
    }

    /**
     * Triggered when a concurrent update collision occurs (optimistic locking).
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public RestEntity<Void> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        log.error("Optimistic locking failure — {}", ex.getMessage(), ex);
        return errorResponse("The record was updated by another user. Please refresh and try again.", HttpStatus.CONFLICT);
    }

    // -------------------------------------------------------------------------
    // HTTP & Web Request errors
    // -------------------------------------------------------------------------

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public RestEntity<Void> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("Media type not supported — {}", ex.getMessage(), ex);
        return errorResponse("Unsupported media type. Please use application/json.", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public RestEntity<Void> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        log.warn("Missing request header — {}", ex.getMessage(), ex);
        return errorResponse(String.format("Required header '%s' is missing", ex.getHeaderName()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public RestEntity<Void> handleMissingPathVariable(MissingPathVariableException ex) {
        log.warn("Missing path variable — {}", ex.getMessage(), ex);
        return errorResponse(String.format("Required path variable '%s' is missing", ex.getVariableName()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public RestEntity<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("Max upload size exceeded — {}", ex.getMessage(), ex);
        return errorResponse("The uploaded file exceeds the maximum allowed size.", HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // -------------------------------------------------------------------------
    // Common Runtime Exceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(ArithmeticException.class)
    public RestEntity<Void> handleArithmeticException(ArithmeticException ex) {
        log.error("Arithmetic exception (e.g. division by zero) — {}", ex.getMessage(), ex);
        return errorResponse("A mathematical error occurred during processing.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IndexOutOfBoundsException.class)
    public RestEntity<Void> handleIndexOutOfBounds(IndexOutOfBoundsException ex) {
        log.error("Index out of bounds exception — {}", ex.getMessage(), ex);
        return errorResponse("A processing error occurred due to an invalid data index.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Ultimate fallback handler for any {@link Exception} not caught above.
     */
    @ExceptionHandler(Exception.class)
    public RestEntity<Void> handleGenericException(Exception ex) {
        log.error("Unhandled exception — {}", ex.getMessage(), ex);
        return errorResponse("An internal server error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
