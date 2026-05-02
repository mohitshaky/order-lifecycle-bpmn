package com.mohit.om.service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.om.service.constants.Constants;
import com.mohit.om.service.model.OrderStatusEvent;
import com.mohit.om.service.model.ProcessInstanceDetails;
import lombok.AllArgsConstructor;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * OrderStatusEventListener class - Kafka listener for OM_ORDER_STATUS topic events.
 * Completes Flowable user tasks based on received order status events.
 *
 * @author mohit
 */
@Component
@AllArgsConstructor
@KafkaListener(topics = Constants.ORDER_STATUS_TOPIC,
        concurrency = "#{${orderStatusConcurrency}}",
        groupId = "OM_ORDER_STATUS_GRP",
        containerFactory = "orderStatusListenerFactory")
public class OrderStatusEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrderStatusEventListener.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    MongoTemplate mongoTemplate;

    @Autowired
    TaskService taskService;

    /**
     * Handles incoming OrderStatusEvent messages from OM_ORDER_STATUS topic.
     * Looks up the process instance from MongoDB and completes the associated Flowable user task.
     *
     * @param orderStatusEvent the deserialized OrderStatusEvent payload
     */
    @KafkaHandler
    public void listen(@Payload OrderStatusEvent orderStatusEvent) {
        LOG.debug("OrderStatusEvent listener {} {}", orderStatusEvent.getOrderId(), orderStatusEvent.getStatus());
        LOG.info("OrderStatusEventListener:: listen method started for orderId :: {}", orderStatusEvent.getOrderId());
        try {
            String processInstanceId = getProcessInstanceId(orderStatusEvent.getOrderId());
            if (processInstanceId != null && !processInstanceId.isEmpty()) {
                completeAssigneeTask(processInstanceId, orderStatusEvent.getStatus(), orderStatusEvent);
                updateProcessInstanceStatus(orderStatusEvent.getOrderId(), orderStatusEvent.getStatus());
            } else {
                LOG.error("Exception in listen: processInstanceId not found for orderId :: {}",
                        orderStatusEvent.getOrderId());
            }
        } catch (Exception e) {
            LOG.error("Exception in listen: ", e);
        }
    }

    /**
     * Completes the active Flowable user task for a given processInstanceId and sets status variables.
     *
     * @param processInstanceId the Flowable process instance identifier
     * @param status            the order status to set on task completion
     * @param event             the full OrderStatusEvent for additional variable mapping
     */
    private void completeAssigneeTask(String processInstanceId, String status, OrderStatusEvent event) {
        LOG.info("OrderStatusEventListener:: completeAssigneeTask method started for processInstanceId :: {}", processInstanceId);
        try {
            Task task = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .active()
                    .singleResult();
            if (task != null) {
                Map<String, Object> variables = new HashMap<>();
                variables.put(Constants.VAR_ORDER_STATUS, status != null ? status : "");
                variables.put(Constants.VAR_STATUS, status != null ? status : "");
                taskService.complete(task.getId(), variables);
                LOG.info("OrderStatusEventListener:: task completed :: {} for processInstanceId :: {}",
                        task.getId(), processInstanceId);
            } else {
                LOG.info("OrderStatusEventListener:: no active task found for processInstanceId :: {}", processInstanceId);
            }
        } catch (Exception e) {
            LOG.error("Exception in completeAssigneeTask: ", e);
        }
    }

    /**
     * Queries MongoDB using MongoTemplate + Criteria pattern to find the processInstanceId for a given orderId.
     *
     * @param orderId the order identifier to look up
     * @return processInstanceId string, or empty string if not found
     */
    private String getProcessInstanceId(String orderId) {
        LOG.info("OrderStatusEventListener:: getProcessInstanceId method started for orderId :: {}", orderId);
        try {
            Query query = new Query(Criteria.where("orderId").is(orderId));
            ProcessInstanceDetails details = mongoTemplate.findOne(
                    query, ProcessInstanceDetails.class, Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
            if (details != null) {
                return details.getProcessInstanceId() != null ? details.getProcessInstanceId() : "";
            }
        } catch (Exception e) {
            LOG.error("Exception in getProcessInstanceId: ", e);
        }
        return "";
    }

    /**
     * Updates the order status in MongoDB for the given orderId.
     *
     * @param orderId the order identifier to update
     * @param status  the new status value
     */
    private void updateProcessInstanceStatus(String orderId, String status) {
        LOG.info("OrderStatusEventListener:: updateProcessInstanceStatus method started for orderId :: {}", orderId);
        try {
            Query query = new Query(Criteria.where("orderId").is(orderId));
            Update update = new Update()
                    .set("status", status != null ? status : "")
                    .set("updatedAt", LocalDateTime.now());
            mongoTemplate.updateFirst(query, update, Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in updateProcessInstanceStatus: ", e);
        }
    }
}
