package com.mohit.om.service.wrapper;

import com.mohit.om.service.constants.Constants;
import com.mohit.om.service.exception.OrderManagementException;
import com.mohit.om.service.model.ProcessInstanceDetails;
import com.mohit.om.service.request.OrderRequest;
import com.mohit.om.service.response.OrderResponse;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * OrderWorkflowWrapper class - Implements IOrderWorkflowWrapper, wraps Flowable RuntimeService and TaskService
 *
 * @author mohit
 */
@Component
public class OrderWorkflowWrapper implements IOrderWorkflowWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(OrderWorkflowWrapper.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * Starts a new Flowable process instance for the given process key
     *
     * @param processKey   BPMN process definition key
     * @param orderRequest the order request payload
     * @param transactionId transaction identifier
     * @param correlationId correlation identifier
     * @return OrderResponse with processInstanceId and initial status
     */
    @Override
    public OrderResponse startOrderProcess(String processKey, OrderRequest orderRequest,
                                           String transactionId, String correlationId) {
        LOG.info("OrderWorkflowWrapper:: startOrderProcess method started");
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put(Constants.VAR_ORDER_ID, orderRequest.getOrderId() != null ? orderRequest.getOrderId() : "");
            variables.put(Constants.VAR_CUSTOMER_ID, orderRequest.getCustomerId() != null ? orderRequest.getCustomerId() : "");
            variables.put(Constants.VAR_TENANT_ID, orderRequest.getTenantId() != null ? orderRequest.getTenantId() : "");
            variables.put(Constants.VAR_ORDER_STATUS, Constants.ORDER_STATUS_RECEIVED);
            variables.put(Constants.HEADER_TRANSACTION_ID, transactionId != null ? transactionId : "");
            variables.put(Constants.HEADER_CORRELATION_ID, correlationId != null ? correlationId : "");

            if (orderRequest.getAdditionalVariables() != null) {
                variables.putAll(orderRequest.getAdditionalVariables());
            }

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, variables);
            LOG.info("OrderWorkflowWrapper:: Process instance started :: {}", processInstance.getId());

            ProcessInstanceDetails details = ProcessInstanceDetails.builder()
                    .orderId(orderRequest.getOrderId())
                    .processInstanceId(processInstance.getId())
                    .processKey(processKey)
                    .status(Constants.ORDER_STATUS_RECEIVED)
                    .customerId(orderRequest.getCustomerId())
                    .tenantId(orderRequest.getTenantId())
                    .transactionId(transactionId)
                    .correlationId(correlationId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            mongoTemplate.save(details, Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);

            OrderResponse response = new OrderResponse();
            response.setOrderId(orderRequest.getOrderId());
            response.setProcessInstanceId(processInstance.getId());
            response.setStatus(Constants.ORDER_STATUS_RECEIVED);
            response.setMessage(Constants.MSG_ORDER_STARTED);
            response.setTransactionId(transactionId);
            response.setCorrelationId(correlationId);
            return response;
        } catch (Exception e) {
            LOG.error("Exception in startOrderProcess: ", e);
            throw new OrderManagementException("Failed to start order process: " + e.getMessage(),
                    e, HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERR_PROCESS_START_FAILED);
        }
    }

    /**
     * Completes a Flowable user task by taskId with optional variables
     *
     * @param taskId    Flowable task identifier
     * @param variables variables to set on task completion
     */
    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        LOG.info("OrderWorkflowWrapper:: completeTask method started for taskId :: {}", taskId);
        try {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                LOG.error("Exception in completeTask: task not found for taskId :: {}", taskId);
                throw new OrderManagementException("Task not found for taskId: " + taskId,
                        HttpStatus.NOT_FOUND, Constants.ERR_TASK_COMPLETE_FAILED);
            }
            taskService.complete(taskId, variables != null ? variables : new HashMap<>());
            LOG.info("OrderWorkflowWrapper:: Task completed successfully :: {}", taskId);
        } catch (OrderManagementException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Exception in completeTask: ", e);
            throw new OrderManagementException("Failed to complete task: " + e.getMessage(),
                    e, HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERR_TASK_COMPLETE_FAILED);
        }
    }

    /**
     * Sends a named signal to a running process instance
     *
     * @param processInstanceId Flowable process instance identifier
     * @param signalName        name of the signal event
     * @param variables         variables to attach to the signal
     */
    @Override
    public void sendSignalToProcess(String processInstanceId, String signalName, Map<String, Object> variables) {
        LOG.info("OrderWorkflowWrapper:: sendSignalToProcess method started for processInstanceId :: {}", processInstanceId);
        try {
            runtimeService.signalEventReceived(signalName, processInstanceId,
                    variables != null ? variables : new HashMap<>());
            LOG.info("OrderWorkflowWrapper:: Signal sent successfully :: {} to processInstance :: {}", signalName, processInstanceId);
        } catch (Exception e) {
            LOG.error("Exception in sendSignalToProcess: ", e);
            throw new OrderManagementException("Failed to send signal: " + e.getMessage(),
                    e, HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERR_SIGNAL_FAILED);
        }
    }
}
