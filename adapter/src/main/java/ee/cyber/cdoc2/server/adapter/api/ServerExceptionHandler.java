package ee.cyber.cdoc2.server.adapter.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ee.cyber.cdoc2.server.adapter.exception.ServerException;

@RestControllerAdvice
public class ServerExceptionHandler {

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ProblemDetail> handleServerException(
        ServerException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setProperties(
            Map.of(
                "errorCode", exception.getCode(),
                "errorMessage", exception.getMessage()
            )
        );

        return ResponseEntity.internalServerError().body(problem);
    }
}
