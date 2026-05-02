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
 * ProvisionOrderDelegate class - Flowable JavaDelegate that triggers provisioning and updates process variables
 *
 * @author mohit
 */
@Component
public class ProvisionOrderDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisionOrderDelegate.class);

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * Executes the delegate — reads order variables, triggers provisioning via Kafka event,
     * and updates order status to IN_PROGRESS on the process execution context.
     *
     * @param execution the Flowable DelegateExecution providing access to process variables
     */
    @Override
    public void execute(DelegateExecution execution) {
        LOG.info("=======ProvisionOrderDelegate============");
        String orderId = execution.getVariable("orderId") != null ? execution.getVariable("orderId").toString() : "";
        String customerId = execution.getVariable("customerId") != null ? execution.getVariable("customerId").toString() : "";
        String tenantId = execution.getVariable("tenantId") != null ? execution.getVariable("tenantId").toString() : "";
        String processInstanceId = execution.getProcessInstanceId() != null ? execution.getProcessInstanceId() : "";
        LOG.info("ProvisionOrderDelegate:: provisioning orderId :: {} processInstanceId :: {}", orderId, processInstanceId);

        execution.setVariable(Constants.VAR_ORDER_STATUS, Constants.ORDER_STATUS_IN_PROGRESS);

        OrderStatusEvent provisioningEvent = new OrderStatusEvent();
        provisioningEvent.setOrderId(orderId);
        provisioningEvent.setStatus(Constants.ORDER_STATUS_IN_PROGRESS);
        provisioningEvent.setCustomerId(customerId);
        provisioningEvent.setTenantId(tenantId);
        provisioningEvent.setProcessInstanceId(processInstanceId);
        provisioningEvent.setTransactionId(execution.getVariable("transactionId") != null ? execution.getVariable("transactionId").toString() : "");
        provisioningEvent.setCorrelationId(execution.getVariable("correlationId") != null ? execution.getVariable("correlationId").toString() : "");

        kafkaTemplate.send(Constants.ORDER_STATUS_TOPIC, provisioningEvent);
        LOG.info("ProvisionOrderDelegate:: ProvisioningEvent published for orderId :: {}", orderId);
    }
}
