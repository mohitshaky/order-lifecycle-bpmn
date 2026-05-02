package com.mohit.om.service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ErrorResponse class - Generic error response wrapper
 *
 * @author mohit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorResponse {

    private String status;
    private String errorCode;
    private String message;
    private String details;
    private String timestamp;
}
