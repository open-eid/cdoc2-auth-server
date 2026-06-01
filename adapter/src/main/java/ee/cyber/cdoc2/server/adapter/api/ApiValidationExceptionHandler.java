package ee.cyber.cdoc2.server.adapter.api;

import jakarta.validation.ConstraintViolationException;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiValidationExceptionHandler {
    private static final String VALIDATION_PROBLEM_TITLE = "Request validation Failed";
    private static final String VALIDATION_PROBLEM_CODE_VALUE = "API_VALIDATION";
    private static final String VALIDATION_PROBLEM_CODE_FIELD = "errorCode";

    // Handles @RequestBody validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle(VALIDATION_PROBLEM_TITLE);
        problem.setDetail(ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(java.util.stream.Collectors.joining(", ")));

        problem.setProperties(Map.of(VALIDATION_PROBLEM_CODE_FIELD, VALIDATION_PROBLEM_CODE_VALUE));

        return ResponseEntity.badRequest().body(problem);
    }

    // Handles @PathVariable, @RequestParam validation failures
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
        ConstraintViolationException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle(VALIDATION_PROBLEM_TITLE);
        problem.setDetail(ex.getConstraintViolations().stream()
            .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
            .collect(java.util.stream.Collectors.joining(", ")));

        problem.setProperties(Map.of(VALIDATION_PROBLEM_CODE_FIELD, VALIDATION_PROBLEM_CODE_VALUE));

        return ResponseEntity.badRequest().body(problem);
    }

    // Handles malformed request body (e.g. invalid JSON, wrong types)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleNotReadable(
        HttpMessageNotReadableException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Malformed Request");
        problem.setDetail(ex.getMostSpecificCause().getMessage());

        problem.setProperties(Map.of(VALIDATION_PROBLEM_CODE_FIELD, VALIDATION_PROBLEM_CODE_VALUE));

        return ResponseEntity.badRequest().body(problem);
    }
}
