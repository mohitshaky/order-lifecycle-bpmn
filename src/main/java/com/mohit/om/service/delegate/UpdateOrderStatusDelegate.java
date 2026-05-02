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
 * UpdateOrderStatusDelegate class - Flowable JavaDelegate that publishes order status events to Kafka
 *
 * @author mohit
 */
@Component
public class UpdateOrderStatusDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateOrderStatusDelegate.class);

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * Executes the delegate — reads order variables from the process execution context,
     * builds an OrderStatusEvent and publishes it to the OM_ORDER_STATUS Kafka topic.
     *
     * @param execution the Flowable DelegateExecution providing access to process variables
     */
    @Override
    public void execute(DelegateExecution execution) {
        LOG.info("=======UpdateOrderStatusDelegate============");
        OrderStatusEvent event = new OrderStatusEvent();
        event.setOrderId(execution.getVariable("orderId") != null ? execution.getVariable("orderId").toString() : "");
        event.setStatus(execution.getVariable("orderStatus") != null ? execution.getVariable("orderStatus").toString() : "");
        event.setCustomerId(execution.getVariable("customerId") != null ? execution.getVariable("customerId").toString() : "");
        event.setTenantId(execution.getVariable("tenantId") != null ? execution.getVariable("tenantId").toString() : "");
        event.setProcessInstanceId(execution.getProcessInstanceId() != null ? execution.getProcessInstanceId() : "");
        event.setTransactionId(execution.getVariable("transactionId") != null ? execution.getVariable("transactionId").toString() : "");
        event.setCorrelationId(execution.getVariable("correlationId") != null ? execution.getVariable("correlationId").toString() : "");
        // publish to kafka
        kafkaTemplate.send(Constants.ORDER_STATUS_TOPIC, event);
        LOG.info("OrderStatusEvent published :: {}", event);
        execution.setVariable("status", execution.getVariable("orderStatus"));
    }
}
