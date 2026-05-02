package com.mohit.om.service.service;

import com.mohit.om.service.exception.OrderManagementException;
import com.mohit.om.service.request.OrderRequest;
import com.mohit.om.service.response.OrderResponse;
import com.mohit.om.service.response.SuccessResponse;
import com.mohit.om.service.wrapper.IOrderWorkflowWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderServiceTest class - Unit tests for OrderService business logic
 *
 * @author mohit
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private IOrderWorkflowWrapper orderWorkflowWrapper;

    private OrderRequest orderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setOrderId("ORD-001");
        orderRequest.setCustomerId("CUST-001");
        orderRequest.setTenantId("TENANT-001");
        orderRequest.setOrderType("MOBILE");

        orderResponse = new OrderResponse();
        orderResponse.setOrderId("ORD-001");
        orderResponse.setProcessInstanceId("PI-001");
        orderResponse.setStatus("RECEIVED");
        orderResponse.setMessage("Order process started successfully");
    }

    @Test
    @DisplayName("startOrderProcess - should return SuccessResponse when wrapper returns OrderResponse")
    void startOrderProcess_success() {
        when(orderWorkflowWrapper.startOrderProcess(anyString(), any(OrderRequest.class), anyString(), anyString()))
                .thenReturn(orderResponse);

        SuccessResponse result = orderService.startOrderProcess(
                "orderFulfillmentProcess", orderRequest, "TXN-001", "CORR-001");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Order process started successfully", result.getMessage());
        assertNotNull(result.getData());
        verify(orderWorkflowWrapper, times(1)).startOrderProcess(
                "orderFulfillmentProcess", orderRequest, "TXN-001", "CORR-001");
    }

    @Test
    @DisplayName("startOrderProcess - should propagate OrderManagementException from wrapper")
    void startOrderProcess_wrapperThrowsException() {
        when(orderWorkflowWrapper.startOrderProcess(anyString(), any(OrderRequest.class), anyString(), anyString()))
                .thenThrow(new OrderManagementException("Process start failed",
                        HttpStatus.INTERNAL_SERVER_ERROR, "OM-002"));

        assertThrows(OrderManagementException.class, () ->
                orderService.startOrderProcess("orderFulfillmentProcess", orderRequest, "TXN-001", "CORR-001"));

        verify(orderWorkflowWrapper, times(1)).startOrderProcess(
                anyString(), any(OrderRequest.class), anyString(), anyString());
    }

    @Test
    @DisplayName("completeTask - should return SuccessResponse when task completed successfully")
    void completeTask_success() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderStatus", "PROVISIONED");
        doNothing().when(orderWorkflowWrapper).completeTask(anyString(), any());

        SuccessResponse result = orderService.completeTask("TASK-001", variables);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Task completed successfully", result.getMessage());
        verify(orderWorkflowWrapper, times(1)).completeTask("TASK-001", variables);
    }

    @Test
    @DisplayName("completeTask - should return SuccessResponse with null variables")
    void completeTask_nullVariables() {
        doNothing().when(orderWorkflowWrapper).completeTask(anyString(), any());

        SuccessResponse result = orderService.completeTask("TASK-001", null);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    @DisplayName("completeTask - should propagate OrderManagementException when task not found")
    void completeTask_notFound() {
        doThrow(new OrderManagementException("Task not found", HttpStatus.NOT_FOUND, "OM-003"))
                .when(orderWorkflowWrapper).completeTask(anyString(), any());

        assertThrows(OrderManagementException.class, () ->
                orderService.completeTask("INVALID-TASK", null));
    }

    @Test
    @DisplayName("sendSignalToProcess - should return SuccessResponse when signal sent successfully")
    void sendSignalToProcess_success() {
        Map<String, Object> variables = new HashMap<>();
        doNothing().when(orderWorkflowWrapper).sendSignalToProcess(anyString(), anyString(), any());

        SuccessResponse result = orderService.sendSignalToProcess("PI-001", "provisioningComplete", variables);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Signal sent successfully", result.getMessage());
        verify(orderWorkflowWrapper, times(1)).sendSignalToProcess("PI-001", "provisioningComplete", variables);
    }

    @Test
    @DisplayName("sendSignalToProcess - should propagate exception when signal fails")
    void sendSignalToProcess_exception() {
        doThrow(new OrderManagementException("Signal failed", HttpStatus.INTERNAL_SERVER_ERROR, "OM-004"))
                .when(orderWorkflowWrapper).sendSignalToProcess(anyString(), anyString(), any());

        assertThrows(OrderManagementException.class, () ->
                orderService.sendSignalToProcess("PI-INVALID", "someSignal", null));
    }

    @Test
    @DisplayName("startOrderProcess - should pass all parameters correctly to wrapper")
    void startOrderProcess_parametersPassedCorrectly() {
        when(orderWorkflowWrapper.startOrderProcess("orderFulfillmentProcess", orderRequest, "TXN-999", "CORR-999"))
                .thenReturn(orderResponse);

        orderService.startOrderProcess("orderFulfillmentProcess", orderRequest, "TXN-999", "CORR-999");

        verify(orderWorkflowWrapper, times(1))
                .startOrderProcess("orderFulfillmentProcess", orderRequest, "TXN-999", "CORR-999");
    }
}
