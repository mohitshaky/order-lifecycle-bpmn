package com.mohit.om.service.exception;

import org.springframework.http.HttpStatus;

/**
 * OrderManagementException class - Custom runtime exception for order management errors
 *
 * @author mohit
 */
public class OrderManagementException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    /**
     * Constructor with message, httpStatus and errorCode
     *
     * @param message   error message
     * @param httpStatus HTTP status code
     * @param errorCode  application error code
     */
    public OrderManagementException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    /**
     * Constructor with message, cause, httpStatus and errorCode
     *
     * @param message    error message
     * @param cause      root cause
     * @param httpStatus HTTP status code
     * @param errorCode  application error code
     */
    public OrderManagementException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    /**
     * Gets the HTTP status associated with this exception
     *
     * @return HttpStatus
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Gets the application-specific error code
     *
     * @return errorCode string
     */
    public String getErrorCode() {
        return errorCode;
    }
}
