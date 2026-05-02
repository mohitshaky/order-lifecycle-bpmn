package com.mohit.om.service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * SuccessResponse class - Generic success response wrapper
 *
 * @author mohit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SuccessResponse {

    private String status;
    private String message;
    private Object data;
}
