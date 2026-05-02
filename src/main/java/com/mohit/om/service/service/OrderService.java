package com.mohit.om.service.service;

import com.mohit.om.service.constants.Constants;
import com.mohit.om.service.request.OrderRequest;
import com.mohit.om.service.response.OrderResponse;
import com.mohit.om.service.response.SuccessResponse;
import com.mohit.om.service.wrapper.IOrderWorkflowWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * OrderService class - Thin service layer that delegates order workflow operations to the wrapper
 *
 * @author mohit
 */
@Service
public class OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private IOrderWorkflowWrapper orderWorkflowWrapper;

    /**
     * Starts a new order fulfillment process for the given process key
     *
     * @param processKey    BPMN process definition key
     * @param orderRequest  the order request payload
     * @param transactionId transaction identifier header value
     * @param correlationId correlation identifier header value
     * @return SuccessResponse wrapping the OrderResponse
     */
    public SuccessResponse startOrderProcess(String processKey, OrderRequest orderRequest,
                                             String transactionId, String correlationId) {
        LOG.info("OrderService:: startOrderProcess method started");
        OrderResponse orderResponse = orderWorkflowWrapper.startOrderProcess(
                processKey, orderRequest, transactionId, correlationId);
        return SuccessResponse.builder()
                .status("SUCCESS")
                .message(Constants.MSG_ORDER_STARTED)
                .data(orderResponse)
                .build();
    }

    /**
     * Completes a Flowable user task by taskId
     *
     * @param taskId    Flowable task identifier
     * @param variables variables to pass on task completion
     * @return SuccessResponse confirming task completion
     */
    public SuccessResponse completeTask(String taskId, Map<String, Object> variables) {
        LOG.info("OrderService:: completeTask method started");
        orderWorkflowWrapper.completeTask(taskId, variables);
        return SuccessResponse.builder()
                .status("SUCCESS")
                .message(Constants.MSG_TASK_COMPLETED)
                .data(null)
                .build();
    }

    /**
     * Sends a signal to a running Flowable process instance
     *
     * @param processInstanceId Flowable process instance identifier
     * @param signalName        name of the signal event
     * @param variables         variables to attach to the signal
     * @return SuccessResponse confirming signal was sent
     */
    public SuccessResponse sendSignalToProcess(String processInstanceId, String signalName,
                                               Map<String, Object> variables) {
        LOG.info("OrderService:: sendSignalToProcess method started");
        orderWorkflowWrapper.sendSignalToProcess(processInstanceId, signalName, variables);
        return SuccessResponse.builder()
                .status("SUCCESS")
                .message(Constants.MSG_SIGNAL_SENT)
                .data(null)
                .build();
    }
}
