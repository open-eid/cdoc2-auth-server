package ee.cyber.cdoc2.server.adapter.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ee.cyber.cdoc2.server.adapter.exception.ClientBadRequestException;
import ee.cyber.cdoc2.server.adapter.exception.ClientNotFoundException;
import ee.cyber.cdoc2.server.app.exception.Cdoc2AuthValidationException;

@RestControllerAdvice
public class ClientExceptionHandler {

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleClientNotFoundException(
        ClientNotFoundException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setProperties(
            Map.of(
                "errorCode", exception.getCode()
            )
        );

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ClientBadRequestException.class)
    public ResponseEntity<ProblemDetail> handleClientBadRequestException(
        ClientBadRequestException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setProperties(
            Map.of(
                "errorCode", exception.getCode()
            )
        );

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Cdoc2AuthValidationException.class)
    public ResponseEntity<ProblemDetail> handleCdoc2AuthValidationException(
        Cdoc2AuthValidationException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setProperties(
            Map.of(
                "errorCode", exception.getCode(),
                "errorMessage", exception.getMessage()
            )
        );

        return ResponseEntity.badRequest().body(problem);
    }
}
