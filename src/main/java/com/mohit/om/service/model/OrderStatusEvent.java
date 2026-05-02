package com.mohit.om.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * OrderStatusEvent class - Kafka event model for order status updates
 *
 * @author mohit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderStatusEvent {

    private String orderId;
    private String status;
    private String customerId;
    private String tenantId;
    private String processInstanceId;
    private String transactionId;
    private String correlationId;
    private String message;
    private String timestamp;
}
