package ru.akvine.wild.bot.exceptions.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.akvine.wild.bot.admin.dto.common.ErrorResponse;
import ru.akvine.wild.bot.constants.ApiErrorConstants;
import ru.akvine.wild.bot.exceptions.BlockedCredentialsException;
import ru.akvine.wild.bot.exceptions.*;
import ru.akvine.wild.bot.exceptions.security.*;
import ru.akvine.wild.bot.exceptions.security.registration.RegistrationNotStartedException;
import ru.akvine.wild.bot.exceptions.security.registration.RegistrationWrongStateException;
import ru.akvine.wild.bot.helpers.SecurityHelper;

import java.util.List;

import static ru.akvine.wild.bot.constants.ApiErrorConstants.BLOCKED_ERROR;
import static ru.akvine.wild.bot.constants.ApiErrorConstants.GENERAL_ERROR;
import static ru.akvine.wild.bot.constants.ApiErrorConstants.Validation.BAD_CREDENTIALS_ERROR;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private final SecurityHelper securityHelper;

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

    @ExceptionHandler({OtpInvalidAttemptException.class})
    public ResponseEntity<ErrorResponse> handleOtpInvalidAttemptException(OtpInvalidAttemptException exception) {
        logger.info("Login=[{}] entered wrong otp! Attempts left=[{}], exceptionMessage=[{}]",
                exception.getLogin(), exception.getAttemptsLeft(), exception.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.Security.INVALID_ATTEMPT_ERROR,
                ApiErrorConstants.Security.INVALID_ATTEMPT_ERROR,
                ApiErrorConstants.Security.INVALID_ATTEMPT_ERROR
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NoSessionException.class})
    public ResponseEntity<ErrorResponse> handleNoSessionException(NoSessionException exception) {
        logger.info("No active session found.", exception);
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.NO_SESSION_ERROR,
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WrongSessionException.class)
    public ResponseEntity<ErrorResponse> handleWrongSessionException(WrongSessionException exception) {
        logger.info("Invalid session id found!");
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.Security.INVALID_SESSION_ERROR,
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RegistrationNotStartedException.class)
    public ResponseEntity<ErrorResponse> handleRegistrationNotStartedException(RegistrationNotStartedException exception) {
        logger.info("Registration not started yet", exception);
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.Security.ACTION_NOT_STARTED_ERROR,
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RegistrationWrongStateException.class)
    public ResponseEntity<ErrorResponse> handleRegistrationWrongStateException(RegistrationWrongStateException exception) {
        logger.warn("Registration in wrong state", exception);
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.Security.INVALID_STATE_ERROR,
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOtpExpiredException(OtpExpiredException exception) {
        logger.info("One-time-password is expired, another one should be generated: message={}, otpCountLeft={}",
                exception.getMessage(), exception.getOtpCountLeft());
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.Security.OTP_EXPIRED_ERROR,
                "One time password is expired! You need to receive another one! OtpCountLeft=" + exception.getOtpCountLeft()
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoMoreNewOtpAvailableException.class)
    public ResponseEntity<ErrorResponse> handleNoMoreNewOtpAvailableException(NoMoreNewOtpAvailableException exception, HttpServletRequest request) {
        logger.info("No more new one-time-password can be generated: userWasBlocked={}, exceptionMessage={} ",
                exception.userWasBlocked(), exception.getMessage());
        if (exception.userWasBlocked()) {
            securityHelper.doLogout(request);
        }
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.Security.LIMIT_REACHED_ERROR,
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ActionNotStartedException.class})
    public ResponseEntity<ErrorResponse> handleActionNotStartedException(ActionNotStartedException ex) {
        logger.info("Action not initiated, wrong step. Exception message={}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.Security.ACTION_NOT_STARTED_ERROR,
                ex.getMessage());
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(org.springframework.security.authentication.BadCredentialsException exception) {
        logger.info("Bad credentials", exception);
        ErrorResponse errorResponse = new ErrorResponse(
                ApiErrorConstants.Security.BAD_CREDENTIALS_ERROR,
                exception.getMessage()
        );
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
