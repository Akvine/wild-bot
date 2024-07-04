package ru.akvine.marketspace.bot.exceptions.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.akvine.marketspace.bot.admin.dto.common.ErrorResponse;
import ru.akvine.marketspace.bot.exceptions.BlockedCredentialsException;
import ru.akvine.marketspace.bot.exceptions.ValidationException;

import static ru.akvine.marketspace.bot.exceptions.handler.CommonErrorCodes.BLOCKED_ERROR;
import static ru.akvine.marketspace.bot.exceptions.handler.CommonErrorCodes.GENERAL_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        ErrorResponse errorResponse = new ErrorResponse(GENERAL_ERROR, exception.getMessage(), exception.getMessage());
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BlockedCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleBlockedCredentialsException(BlockedCredentialsException exception) {
        ErrorResponse errorResponse = new ErrorResponse(BLOCKED_ERROR, exception.getMessage(), exception.getMessage());
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getErrorCode(), exception.getMessage(), exception.getMessage());
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
