package com.mohit.om.service.wrapper;

import com.mohit.om.service.request.OrderRequest;
import com.mohit.om.service.response.OrderResponse;

import java.util.Map;

/**
 * IOrderWorkflowWrapper interface - Contract for Flowable workflow operations
 *
 * @author mohit
 */
public interface IOrderWorkflowWrapper {

    /**
     * Starts a new Flowable process instance for the given process key
     *
     * @param processKey  BPMN process definition key
     * @param orderRequest the order request payload
     * @param transactionId transaction identifier header value
     * @param correlationId correlation identifier header value
     * @return OrderResponse containing processInstanceId and status
     */
    OrderResponse startOrderProcess(String processKey, OrderRequest orderRequest,
                                    String transactionId, String correlationId);

    /**
     * Completes a Flowable user task by taskId with optional variables
     *
     * @param taskId    Flowable task identifier
     * @param variables variables to set on task completion
     */
    void completeTask(String taskId, Map<String, Object> variables);

    /**
     * Sends a named signal to a running process instance
     *
     * @param processInstanceId Flowable process instance identifier
     * @param signalName        name of the signal event to trigger
     * @param variables         variables to attach to the signal
     */
    void sendSignalToProcess(String processInstanceId, String signalName, Map<String, Object> variables);
}
