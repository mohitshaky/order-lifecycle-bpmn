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
 * NotifyCustomerDelegate class - Flowable JavaDelegate that publishes a customer notification event to Kafka
 *
 * @author mohit
 */
@Component
public class NotifyCustomerDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(NotifyCustomerDelegate.class);

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * Executes the delegate — reads order and customer details from execution context,
     * builds an OrderStatusEvent for customer notification and publishes it to OM_NOTIFICATION topic.
     *
     * @param execution the Flowable DelegateExecution providing access to process variables
     */
    @Override
    public void execute(DelegateExecution execution) {
        LOG.info("=======NotifyCustomerDelegate============");
        String orderId = execution.getVariable("orderId") != null ? execution.getVariable("orderId").toString() : "";
        String customerId = execution.getVariable("customerId") != null ? execution.getVariable("customerId").toString() : "";
        String tenantId = execution.getVariable("tenantId") != null ? execution.getVariable("tenantId").toString() : "";
        String orderStatus = execution.getVariable("orderStatus") != null ? execution.getVariable("orderStatus").toString() : Constants.ORDER_STATUS_COMPLETED;
        String processInstanceId = execution.getProcessInstanceId() != null ? execution.getProcessInstanceId() : "";

        LOG.info("NotifyCustomerDelegate:: notifying customer :: {} for orderId :: {}", customerId, orderId);

        execution.setVariable(Constants.VAR_ORDER_STATUS, Constants.ORDER_STATUS_COMPLETED);

        OrderStatusEvent notificationEvent = new OrderStatusEvent();
        notificationEvent.setOrderId(orderId);
        notificationEvent.setStatus(Constants.ORDER_STATUS_COMPLETED);
        notificationEvent.setCustomerId(customerId);
        notificationEvent.setTenantId(tenantId);
        notificationEvent.setProcessInstanceId(processInstanceId);
        notificationEvent.setTransactionId(execution.getVariable("transactionId") != null ? execution.getVariable("transactionId").toString() : "");
        notificationEvent.setCorrelationId(execution.getVariable("correlationId") != null ? execution.getVariable("correlationId").toString() : "");
        notificationEvent.setMessage("Your order " + orderId + " has been " + orderStatus + " successfully.");

        kafkaTemplate.send(Constants.NOTIFICATION_TOPIC, notificationEvent);
        LOG.info("NotifyCustomerDelegate:: NotificationEvent published for customerId :: {}", customerId);
    }
}
