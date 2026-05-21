package com.co.oscar.login.infrastructure.entrypoints.controllers;


import com.co.oscar.login.domain.exceptions.UserAlreadyExistsException;
import com.co.oscar.login.domain.exceptions.UserException;
import com.co.oscar.login.domain.exceptions.UserNotFoundException;
import com.co.oscar.login.infrastructure.entrypoints.dtos.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice // 💡 Usamos RestControllerAdvice para asegurar compatibilidad estricta con JSON
public class GlobalExceptionHandler {

    // Este método atrapará ESPECÍFICAMENTE la UserException de tu traza
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserException(UserException ex) {
        ApiResponseDto<Void> errorResponse = ApiResponseDto.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 2. Error cuando el JSON está mal armado (ej. comas sueltas, llaves sin cerrar) -> HTTP 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Malformed JSON request body or invalid format."));
    }

    // 3. Error cuando envían un tipo de dato incorrecto en la URL (ej. /users/letras en vez de /users/id) -> HTTP 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("The parameter '%s' expects a value of type '%s'", ex.getName(), ex.getRequiredType().getSimpleName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(message));
    }

    // 4. LA RED DE SEGURIDAD ABSOLUTA: Atrapa cualquier error inesperado del sistema (NullPointer, caídas de BD, etc.) -> HTTP 500 controlado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAllUnexpectedExceptions(Exception ex) {
        // Logueamos internamente el error real para que tú lo veas en la consola del IDE
        System.err.println("Unexpected Error Intercepted: " + ex.getMessage());
        ex.printStackTrace();

        // Al cliente exterior le devolvemos un mensaje seguro y con tu estructura limpia
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("An unexpected internal error occurred. Please try again later."));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // Le decimos explícitamente que devuelva HTTP 404
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    // 🔴 ESTADO 409 - Conflicto (El recurso ya existe)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // HTTP 409
                .body(ApiResponseDto.error(ex.getMessage()));
    }

}
