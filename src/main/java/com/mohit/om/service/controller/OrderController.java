package com.mohit.om.service.controller;

import com.mohit.om.service.constants.Constants;
import com.mohit.om.service.request.OrderRequest;
import com.mohit.om.service.response.ErrorResponse;
import com.mohit.om.service.response.SuccessResponse;
import com.mohit.om.service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * OrderController class - REST controller for order management operations
 *
 * @author mohit
 */
@RestController
@RequestMapping("/order")
@Tag(name = "Order Management", description = "APIs for order fulfillment lifecycle management")
public class OrderController {

    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    /**
     * Starts a new order fulfillment BPMN process for the given process key
     *
     * @param processKey    BPMN process definition key
     * @param transactionId unique transaction identifier
     * @param correlationId correlation ID for distributed tracing
     * @param sourceChannel originating source channel (WEB, MOBILE, API)
     * @param tenantId      tenant identifier for multi-tenancy
     * @param orderRequest  the order payload
     * @return ResponseEntity with SuccessResponse
     */
    @Operation(
        summary = "Start Order Process",
        description = "Initiates a new Flowable BPMN order fulfillment process for the specified process key. " +
                      "Creates a process instance, persists details to MongoDB, and returns the processInstanceId."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order process started successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid credentials",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Process definition not found for given processKey",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Unprocessable Entity - invalid request payload",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Service unavailable - downstream dependency failure",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/start/{processKey}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse> startOrderProcess(
            @PathVariable String processKey,
            @Parameter(in = ParameterIn.HEADER, name = "transactionId",
                description = "Unique transaction identifier", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "transactionId", required = false) String transactionId,
            @Parameter(in = ParameterIn.HEADER, name = "correlationId",
                description = "Correlation ID for distributed tracing", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "correlationId", required = false) String correlationId,
            @Parameter(in = ParameterIn.HEADER, name = "sourceChannel",
                description = "Source channel (WEB, MOBILE, API)", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "sourceChannel", required = false) String sourceChannel,
            @Parameter(in = ParameterIn.HEADER, name = "tenantId",
                description = "Tenant identifier for multi-tenancy", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "tenantId", required = false) String tenantId,
            @RequestBody OrderRequest orderRequest) {
        LOG.info("OrderController:: startOrderProcess method started");
        try {
            SuccessResponse response = orderService.startOrderProcess(
                    processKey, orderRequest, transactionId, correlationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOG.error("Exception in startOrderProcess: ", e);
            throw e;
        }
    }

    /**
     * Completes a Flowable user task by taskId to advance the process workflow
     *
     * @param taskId        Flowable task identifier to complete
     * @param transactionId unique transaction identifier
     * @param correlationId correlation ID for distributed tracing
     * @param sourceChannel originating source channel
     * @param tenantId      tenant identifier
     * @param variables     optional variables to pass on task completion
     * @return ResponseEntity with SuccessResponse
     */
    @Operation(
        summary = "Complete Order Task",
        description = "Completes a Flowable user task identified by taskId, advancing the order fulfillment workflow. " +
                      "Optionally accepts variables to set on the process instance."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task completed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid credentials",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found for given taskId",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Unprocessable Entity - task cannot be completed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Service unavailable - downstream dependency failure",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping(value = "/task/{taskId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse> completeTask(
            @PathVariable String taskId,
            @Parameter(in = ParameterIn.HEADER, name = "transactionId",
                description = "Unique transaction identifier", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "transactionId", required = false) String transactionId,
            @Parameter(in = ParameterIn.HEADER, name = "correlationId",
                description = "Correlation ID for distributed tracing", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "correlationId", required = false) String correlationId,
            @Parameter(in = ParameterIn.HEADER, name = "sourceChannel",
                description = "Source channel (WEB, MOBILE, API)", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "sourceChannel", required = false) String sourceChannel,
            @Parameter(in = ParameterIn.HEADER, name = "tenantId",
                description = "Tenant identifier for multi-tenancy", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "tenantId", required = false) String tenantId,
            @RequestBody(required = false) Map<String, Object> variables) {
        LOG.info("OrderController:: completeTask method started");
        try {
            SuccessResponse response = orderService.completeTask(taskId, variables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOG.error("Exception in completeTask: ", e);
            throw e;
        }
    }

    /**
     * Sends a named signal to a running Flowable process instance
     *
     * @param processInstanceId Flowable process instance identifier
     * @param signalName        name of the BPMN signal event to trigger
     * @param transactionId     unique transaction identifier
     * @param correlationId     correlation ID for distributed tracing
     * @param sourceChannel     originating source channel
     * @param tenantId          tenant identifier
     * @param variables         optional variables to pass with the signal
     * @return ResponseEntity with SuccessResponse
     */
    @Operation(
        summary = "Send Signal to Process",
        description = "Sends a named BPMN signal event to a running Flowable process instance to advance or trigger " +
                      "conditional paths within the order fulfillment workflow."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Signal sent successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid credentials",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Process instance not found for given processInstanceId",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Unprocessable Entity - signal cannot be processed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Service unavailable - downstream dependency failure",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping(value = "/{processInstanceId}/{signalName}/signal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse> sendSignal(
            @PathVariable String processInstanceId,
            @PathVariable String signalName,
            @Parameter(in = ParameterIn.HEADER, name = "transactionId",
                description = "Unique transaction identifier", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "transactionId", required = false) String transactionId,
            @Parameter(in = ParameterIn.HEADER, name = "correlationId",
                description = "Correlation ID for distributed tracing", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "correlationId", required = false) String correlationId,
            @Parameter(in = ParameterIn.HEADER, name = "sourceChannel",
                description = "Source channel (WEB, MOBILE, API)", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "sourceChannel", required = false) String sourceChannel,
            @Parameter(in = ParameterIn.HEADER, name = "tenantId",
                description = "Tenant identifier for multi-tenancy", required = true,
                schema = @Schema(type = "string"))
            @RequestHeader(value = "tenantId", required = false) String tenantId,
            @RequestBody(required = false) Map<String, Object> variables) {
        LOG.info("OrderController:: sendSignal method started");
        try {
            SuccessResponse response = orderService.sendSignalToProcess(
                    processInstanceId, signalName, variables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOG.error("Exception in sendSignal: ", e);
            throw e;
        }
    }
}
