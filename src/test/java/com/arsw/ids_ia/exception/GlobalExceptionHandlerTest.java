package com.arsw.ids_ia.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Pruebas de mapeo de excepciones")
class GlobalExceptionHandlerTest {

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("Debe retornar 403 FORBIDDEN para UnauthorizedException")
    void shouldReturn403_ForUnauthorizedException() {
        UnauthorizedException exception = new UnauthorizedException("Access denied");

        ResponseEntity<String> response = exceptionHandler.handleUnauthorizedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    @DisplayName("Debe retornar 400 BAD_REQUEST para IllegalArgumentException")
    void shouldReturn400_ForIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter");

        ResponseEntity<String> response = exceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid parameter", response.getBody());
    }

    @Test
    @DisplayName("Debe retornar 400 BAD_REQUEST para MethodArgumentNotValidException")
    void shouldReturn400_ForValidationException() {
        ObjectError error = new ObjectError("field", "must not be null");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        ResponseEntity<String> response = exceptionHandler.handleValidationException(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Validation error"));
        assertTrue(body.contains("must not be null"));
    }
}
