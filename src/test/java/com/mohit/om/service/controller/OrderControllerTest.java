package com.mohit.om.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.om.service.exception.OrderManagementException;
import com.mohit.om.service.request.OrderRequest;
import com.mohit.om.service.response.SuccessResponse;
import com.mohit.om.service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OrderControllerTest class - Unit tests for OrderController REST endpoints
 *
 * @author mohit
 */
@WebMvcTest(OrderController.class)
@WithMockUser(username = "admin", roles = {"USER"})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderRequest orderRequest;
    private SuccessResponse successResponse;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setOrderId("ORD-001");
        orderRequest.setCustomerId("CUST-001");
        orderRequest.setTenantId("TENANT-001");
        orderRequest.setOrderType("MOBILE");
        orderRequest.setProductId("PROD-001");

        successResponse = SuccessResponse.builder()
                .status("SUCCESS")
                .message("Order process started successfully")
                .data(null)
                .build();
    }

    @Test
    @DisplayName("POST /order/start/{processKey} - should return 200 when process started successfully")
    void startOrderProcess_success() throws Exception {
        when(orderService.startOrderProcess(anyString(), any(OrderRequest.class), anyString(), anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/order/start/orderFulfillmentProcess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Order process started successfully"));
    }

    @Test
    @DisplayName("POST /order/start/{processKey} - should return 500 when service throws exception")
    void startOrderProcess_serviceException() throws Exception {
        when(orderService.startOrderProcess(anyString(), any(OrderRequest.class), anyString(), anyString()))
                .thenThrow(new OrderManagementException("Failed to start process",
                        HttpStatus.INTERNAL_SERVER_ERROR, "OM-002"));

        mockMvc.perform(post("/order/start/orderFulfillmentProcess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("OM-002"));
    }

    @Test
    @DisplayName("PATCH /order/task/{taskId}/complete - should return 200 when task completed successfully")
    void completeTask_success() throws Exception {
        SuccessResponse taskResponse = SuccessResponse.builder()
                .status("SUCCESS")
                .message("Task completed successfully")
                .data(null)
                .build();
        when(orderService.completeTask(anyString(), any())).thenReturn(taskResponse);

        Map<String, Object> variables = new HashMap<>();
        variables.put("orderStatus", "PROVISIONED");

        mockMvc.perform(patch("/order/task/TASK-001/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001")
                        .content(objectMapper.writeValueAsString(variables)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Task completed successfully"));
    }

    @Test
    @DisplayName("PATCH /order/task/{taskId}/complete - should return 404 when task not found")
    void completeTask_notFound() throws Exception {
        when(orderService.completeTask(anyString(), any()))
                .thenThrow(new OrderManagementException("Task not found", HttpStatus.NOT_FOUND, "OM-003"));

        mockMvc.perform(patch("/order/task/INVALID-TASK/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("OM-003"));
    }

    @Test
    @DisplayName("PATCH /order/{processInstanceId}/{signalName}/signal - should return 200 when signal sent successfully")
    void sendSignal_success() throws Exception {
        SuccessResponse signalResponse = SuccessResponse.builder()
                .status("SUCCESS")
                .message("Signal sent successfully")
                .data(null)
                .build();
        when(orderService.sendSignalToProcess(anyString(), anyString(), any())).thenReturn(signalResponse);

        mockMvc.perform(patch("/order/PI-001/provisioningComplete/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Signal sent successfully"));
    }

    @Test
    @DisplayName("PATCH /order/{processInstanceId}/{signalName}/signal - should return 500 when signal fails")
    void sendSignal_serviceException() throws Exception {
        when(orderService.sendSignalToProcess(anyString(), anyString(), any()))
                .thenThrow(new OrderManagementException("Signal failed",
                        HttpStatus.INTERNAL_SERVER_ERROR, "OM-004"));

        mockMvc.perform(patch("/order/PI-INVALID/someSignal/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("OM-004"));
    }
}
