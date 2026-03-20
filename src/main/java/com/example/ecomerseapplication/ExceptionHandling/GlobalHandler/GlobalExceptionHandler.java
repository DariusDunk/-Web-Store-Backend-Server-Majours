package com.example.ecomerseapplication.ExceptionHandling.GlobalHandler;

import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.*;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {

        ErrorResponse error = new ErrorResponse(
                ErrorType.VALIDATION_ERROR,
                "Невалидни данни",
                400,
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(UserAlreadyExistsException ex) {

        HttpStatus errorStatus = HttpStatus.CONFLICT;

        ErrorResponse errorResponse = new ErrorResponse(ErrorType.USER_ALREADY_EXISTS,
                "Съществуващ потребител",
                errorStatus.value(),
                ex.getMessage());
        return ResponseEntity.status(errorStatus).body(errorResponse);
    }

    @ExceptionHandler(RegistrationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(RegistrationFailedException ex) {

        ErrorResponse errorResponse = new ErrorResponse(ErrorType.REGISTRATION_FAILED,
                "Неуспешна регистрация",
                400,
                "");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<?> handleAllExceptions() {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(RefreshRequestFailedException.class)
    public ResponseEntity<?> handleAllRefreshExceptions() {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(FavouriteInsertFailedException.class)
    public ResponseEntity<?> handleGenericFavouriteExceptions() {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(FavouriteSizeLimitReachedException.class)
    public ResponseEntity<?> handleFavouriteLimitReachedExceptions() {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.SIZE_LIMIT_REACHED,
                    "Достигнат лимит на любими",
                    HttpStatus.CONFLICT.value(), "Достигнахте максималният лимит на списъка с любими!"));
    }

    @ExceptionHandler(ProductAlreadyInFavouritesException.class)
    public ResponseEntity<?> handleFavouriteDuplicationExceptions() {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.DUPLICATION_OF_DATA,
                    "Продуктът вече е в любими",
                    HttpStatus.CONFLICT.value(), "Избраният продукт вече е в списъка ви с любими"));
    }

}
