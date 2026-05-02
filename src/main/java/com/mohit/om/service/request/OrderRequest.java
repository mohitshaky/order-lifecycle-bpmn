package com.mohit.om.service.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * OrderRequest class - REST API request DTO for starting an order process
 *
 * @author mohit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderRequest {

    private String orderId;
    private String customerId;
    private String tenantId;
    private String orderType;
    private String productId;
    private String serviceType;
    private Map<String, Object> additionalVariables;
}
