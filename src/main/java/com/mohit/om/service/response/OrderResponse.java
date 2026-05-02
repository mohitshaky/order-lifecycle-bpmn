package com.mohit.om.service.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * OrderResponse class - REST API response DTO for order process operations
 *
 * @author mohit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderResponse {

    private String orderId;
    private String processInstanceId;
    private String status;
    private String message;
    private String transactionId;
    private String correlationId;
}
