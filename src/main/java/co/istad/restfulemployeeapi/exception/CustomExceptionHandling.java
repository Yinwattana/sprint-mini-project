package co.istad.restfulemployeeapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class CustomExceptionHandling {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleApiValidation(MethodArgumentNotValidException e) {
        List<FieldErrorResponse> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();
        return errorResponse(400, "Validation failed", errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleParameterValidation(HandlerMethodValidationException e) {
        List<FieldErrorResponse> errors = new ArrayList<>();
        e.getParameterValidationResults().forEach(result -> {
            RequestParam parameter = result.getMethodParameter().getParameterAnnotation(RequestParam.class);
            String field = parameter == null ? result.getMethodParameter().getParameterName() : parameter.name();
            result.getResolvableErrors().forEach(error ->
                    errors.add(new FieldErrorResponse(field, error.getDefaultMessage())));
        });
        return errorResponse(400, "Validation failed", errors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleApiError(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(
                errorResponse(e.getStatusCode().value(), e.getReason(), List.of()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParameter(MissingServletRequestParameterException e) {
        return errorResponse(400, "Validation failed", List.of(
                new FieldErrorResponse(e.getParameterName(), "This query parameter is required")));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidParameter(MethodArgumentTypeMismatchException e) {
        return errorResponse(400, "Validation failed", List.of(
                new FieldErrorResponse(e.getName(), "Invalid parameter value")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidBody(HttpMessageNotReadableException e) {
        return errorResponse(400,
                "Malformed JSON or invalid field value; use yyyy-MM-dd for dates", List.of());
    }

    private ErrorResponse errorResponse(int code, String message, List<FieldErrorResponse> errors) {
        return new ErrorResponse(false, code, message, errors, LocalDateTime.now());
    }
}
