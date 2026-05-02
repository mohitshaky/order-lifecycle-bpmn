package com.mohit.om.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ProvisioningEvent class - Kafka event model for provisioning status updates
 *
 * @author mohit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProvisioningEvent {

    private String orderId;
    private String provisioningStatus;
    private String customerId;
    private String tenantId;
    private String processInstanceId;
    private String transactionId;
    private String correlationId;
    private String errorCode;
    private String errorMessage;
    private String timestamp;
}
