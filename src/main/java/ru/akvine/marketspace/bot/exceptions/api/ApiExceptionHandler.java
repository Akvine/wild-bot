package ru.akvine.marketspace.bot.exceptions.api;

import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.akvine.marketspace.bot.admin.dto.common.ErrorResponse;
import ru.akvine.marketspace.bot.constants.ApiErrorConstants;
import ru.akvine.marketspace.bot.exceptions.*;

import java.util.List;

import static ru.akvine.marketspace.bot.constants.ApiErrorConstants.BLOCKED_ERROR;
import static ru.akvine.marketspace.bot.constants.ApiErrorConstants.GENERAL_ERROR;
import static ru.akvine.marketspace.bot.constants.ApiErrorConstants.Validation.BAD_CREDENTIALS_ERROR;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        ErrorResponse errorResponse = new ErrorResponse(GENERAL_ERROR, exception.getMessage(), exception.getMessage());
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException exception) {
        ErrorResponse errorResponse = new ErrorResponse(BAD_CREDENTIALS_ERROR, exception.getMessage(), exception.getMessage());
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

    @ExceptionHandler(AdvertAlreadyInPauseStateException.class)
    public ResponseEntity<ErrorResponse> handleAdvertAlreadyInPauseStateException(AdvertAlreadyInPauseStateException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.ADVERT_ALREADY_IN_PAUSE_STATE_ERROR,
                exception.getMessage(),
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClientNotFoundException(ClientNotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.CLIENT_NOT_FOUND_ERROR,
                exception.getMessage(),
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.info("Url is incorrect");
        String url = ((ServletWebRequest) request).getRequest().getRequestURI();
        String message = String.format("Resource by url = [%s] not exists", url);
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.RESOURCE_NOT_FOUND_ERROR,
                message,
                message
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.info("Method argument is not presented", exception);
        List<ErrorField> errorFields = ErrorMessageHelper.extractErrorField(exception);
        String errorMessage = ErrorMessageHelper.toErrorMessage(errorFields);
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.Validation.FIELD_NOT_PRESENTED_ERROR,
                errorMessage,
                errorMessage);
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.info("Error while parsing json body");
        String errorMessage = "Error while parsing json body";
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.JSON_BODY_INVALID_ERROR,
                errorMessage,
                errorMessage
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
