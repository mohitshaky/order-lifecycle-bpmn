package com.mohit.om.service.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.om.service.constants.Constants;
import com.mohit.om.service.model.OrderStatusEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * ValidateOrderDelegate class - Flowable JavaDelegate that validates order data and publishes validation event to Kafka
 *
 * @author mohit
 */
@Component
public class ValidateOrderDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateOrderDelegate.class);

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * Executes the delegate — validates order details from the process execution context
     * and publishes a validation status event to the OM_VALIDATION_STATUS Kafka topic.
     *
     * @param execution the Flowable DelegateExecution providing access to process variables
     */
    @Override
    public void execute(DelegateExecution execution) {
        LOG.info("=======ValidateOrderDelegate============");
        String orderId = execution.getVariable("orderId") != null ? execution.getVariable("orderId").toString() : "";
        String customerId = execution.getVariable("customerId") != null ? execution.getVariable("customerId").toString() : "";
        String tenantId = execution.getVariable("tenantId") != null ? execution.getVariable("tenantId").toString() : "";
        LOG.info("ValidateOrderDelegate:: validating orderId :: {} for customerId :: {}", orderId, customerId);

        // set validated status on process variables
        execution.setVariable(Constants.VAR_ORDER_STATUS, Constants.ORDER_STATUS_VALIDATED);

        OrderStatusEvent validationEvent = new OrderStatusEvent();
        validationEvent.setOrderId(orderId);
        validationEvent.setStatus(Constants.ORDER_STATUS_VALIDATED);
        validationEvent.setCustomerId(customerId);
        validationEvent.setTenantId(tenantId);
        validationEvent.setProcessInstanceId(execution.getProcessInstanceId() != null ? execution.getProcessInstanceId() : "");
        validationEvent.setTransactionId(execution.getVariable("transactionId") != null ? execution.getVariable("transactionId").toString() : "");
        validationEvent.setCorrelationId(execution.getVariable("correlationId") != null ? execution.getVariable("correlationId").toString() : "");

        kafkaTemplate.send(Constants.VALIDATION_STATUS_TOPIC, validationEvent);
        LOG.info("ValidationEvent published for orderId :: {}", orderId);
    }
}
