package com.mohit.om.service.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.om.service.constants.Constants;
import com.mohit.om.service.model.OrderStatusEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UpdateOrderStatusDelegateTest class - Unit tests for UpdateOrderStatusDelegate Flowable delegate
 *
 * @author mohit
 */
@ExtendWith(MockitoExtension.class)
class UpdateOrderStatusDelegateTest {

    @InjectMocks
    private UpdateOrderStatusDelegate updateOrderStatusDelegate;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DelegateExecution execution;

    @BeforeEach
    void setUp() {
        when(execution.getVariable("orderId")).thenReturn("ORD-001");
        when(execution.getVariable("orderStatus")).thenReturn("PROVISIONED");
        when(execution.getVariable("customerId")).thenReturn("CUST-001");
        when(execution.getVariable("tenantId")).thenReturn("TENANT-001");
        when(execution.getProcessInstanceId()).thenReturn("PI-001");
        when(execution.getVariable("transactionId")).thenReturn("TXN-001");
        when(execution.getVariable("correlationId")).thenReturn("CORR-001");
    }

    @Test
    @DisplayName("execute - should publish OrderStatusEvent to OM_ORDER_STATUS topic")
    void execute_publishesKafkaEvent() {
        updateOrderStatusDelegate.execute(execution);

        verify(kafkaTemplate, times(1)).send(eq(Constants.ORDER_STATUS_TOPIC), any(OrderStatusEvent.class));
    }

    @Test
    @DisplayName("execute - should set orderId correctly from execution variable")
    void execute_setsOrderIdCorrectly() {
        ArgumentCaptor<OrderStatusEvent> eventCaptor = ArgumentCaptor.forClass(OrderStatusEvent.class);

        updateOrderStatusDelegate.execute(execution);

        verify(kafkaTemplate).send(eq(Constants.ORDER_STATUS_TOPIC), eventCaptor.capture());
        OrderStatusEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("ORD-001", capturedEvent.getOrderId());
    }

    @Test
    @DisplayName("execute - should set status correctly from orderStatus execution variable")
    void execute_setsStatusCorrectly() {
        ArgumentCaptor<OrderStatusEvent> eventCaptor = ArgumentCaptor.forClass(OrderStatusEvent.class);

        updateOrderStatusDelegate.execute(execution);

        verify(kafkaTemplate).send(eq(Constants.ORDER_STATUS_TOPIC), eventCaptor.capture());
        OrderStatusEvent capturedEvent = eventCaptor.getValue();
        assertEquals("PROVISIONED", capturedEvent.getStatus());
    }

    @Test
    @DisplayName("execute - should set status variable on execution after publishing")
    void execute_setsStatusVariableOnExecution() {
        updateOrderStatusDelegate.execute(execution);

        verify(execution, times(1)).setVariable(eq("status"), any());
    }

    @Test
    @DisplayName("execute - should use empty string when orderId variable is null")
    void execute_nullOrderId_usesEmptyString() {
        when(execution.getVariable("orderId")).thenReturn(null);
        ArgumentCaptor<OrderStatusEvent> eventCaptor = ArgumentCaptor.forClass(OrderStatusEvent.class);

        updateOrderStatusDelegate.execute(execution);

        verify(kafkaTemplate).send(eq(Constants.ORDER_STATUS_TOPIC), eventCaptor.capture());
        assertEquals("", eventCaptor.getValue().getOrderId());
    }
}
