package ie.tus.jeff.assignment2.handler;

import ie.tus.jeff.assignment2.dto.ErrorResponse;
import ie.tus.jeff.assignment2.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleDepartmentNotFound_returns404WithMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDepartmentNotFound(new DepartmentNotFoundException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("1");
    }

    @Test
    void handleEmployeeNotFound_returns404WithMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleEmployeeNotFound(new EmployeeNotFoundException(5L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("5");
    }

    @Test
    void handleDeletion_returns409WithMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDeletion(new DepartmentDeletionException(2L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).contains("2");
    }

    @Test
    void handleDuplicate_returns409WithMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicate(new DepartmentAlreadyExistsException("Engineering"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("Engineering");
    }

    @Test
    void handleMismatch_returns400WithMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleMismatch(new EmployeeDepartmentMismatchException(10L, 2L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("10").contains("2");
    }

    @Test
    void handleGeneric_returns500WithGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneric(new RuntimeException("something unexpected"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Unexpected error occurred");
    }

    @Test
    void errorResponse_timestampIsNotNull() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDepartmentNotFound(new DepartmentNotFoundException(1L));

        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}