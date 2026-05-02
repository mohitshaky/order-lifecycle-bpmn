package com.mohit.om.service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.om.service.constants.Constants;
import com.mohit.om.service.model.ProcessInstanceDetails;
import com.mohit.om.service.model.ProvisioningEvent;
import lombok.AllArgsConstructor;
import org.flowable.engine.RuntimeService;
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
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ProvisioningEventListener class - Kafka listener for OM_PROVISIONING_STATUS topic events.
 * Advances Flowable user tasks upon receiving provisioning completion events from downstream systems.
 *
 * @author mohit
 */
@Component
@AllArgsConstructor
@KafkaListener(topics = Constants.PROVISIONING_STATUS_TOPIC,
        concurrency = "#{${provisioningConcurrency}}",
        groupId = "OM_PROVISIONING_STATUS_GRP",
        containerFactory = "provisioningListenerFactory")
public class ProvisioningEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisioningEventListener.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    MongoTemplate mongoTemplate;

    @Autowired
    TaskService taskService;

    @Autowired
    RuntimeService runtimeService;

    /**
     * Handles incoming ProvisioningEvent messages from OM_PROVISIONING_STATUS topic.
     * Completes the waiting Flowable user task to advance the order fulfillment process.
     *
     * @param provisioningEvent the deserialized ProvisioningEvent payload
     */
    @KafkaHandler
    public void listen(@Payload ProvisioningEvent provisioningEvent) {
        LOG.debug("ProvisioningEvent listener {} {}", provisioningEvent.getOrderId(), provisioningEvent.getProvisioningStatus());
        LOG.info("ProvisioningEventListener:: listen method started for orderId :: {}", provisioningEvent.getOrderId());
        try {
            String processInstanceId = getProcessInstanceId(provisioningEvent.getOrderId());
            if (processInstanceId != null && !processInstanceId.isEmpty()) {
                completeProvisioningTask(processInstanceId, provisioningEvent);
                updateProcessInstanceStatus(provisioningEvent.getOrderId(), provisioningEvent.getProvisioningStatus());
            } else {
                LOG.error("Exception in listen: processInstanceId not found for orderId :: {}",
                        provisioningEvent.getOrderId());
            }
        } catch (Exception e) {
            LOG.error("Exception in listen: ", e);
        }
    }

    /**
     * Completes the active Flowable user task for a given processInstanceId with provisioning variables.
     *
     * @param processInstanceId the Flowable process instance identifier
     * @param event             the ProvisioningEvent containing status and metadata
     */
    private void completeProvisioningTask(String processInstanceId, ProvisioningEvent event) {
        LOG.info("ProvisioningEventListener:: completeProvisioningTask method started for processInstanceId :: {}", processInstanceId);
        try {
            Task task = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .active()
                    .singleResult();
            if (task != null) {
                Map<String, Object> variables = new HashMap<>();
                variables.put(Constants.VAR_ORDER_STATUS, event.getProvisioningStatus() != null ? event.getProvisioningStatus() : "");
                variables.put(Constants.VAR_STATUS, event.getProvisioningStatus() != null ? event.getProvisioningStatus() : "");
                taskService.complete(task.getId(), variables);
                LOG.info("ProvisioningEventListener:: provisioning task completed :: {} for processInstanceId :: {}",
                        task.getId(), processInstanceId);
            } else {
                LOG.info("ProvisioningEventListener:: no active task found for processInstanceId :: {}", processInstanceId);
            }
        } catch (Exception e) {
            LOG.error("Exception in completeProvisioningTask: ", e);
        }
    }

    /**
     * Queries MongoDB using MongoTemplate + Criteria pattern to find the processInstanceId for a given orderId.
     *
     * @param orderId the order identifier to look up
     * @return processInstanceId string, or empty string if not found
     */
    private String getProcessInstanceId(String orderId) {
        LOG.info("ProvisioningEventListener:: getProcessInstanceId method started for orderId :: {}", orderId);
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
        LOG.info("ProvisioningEventListener:: updateProcessInstanceStatus method started for orderId :: {}", orderId);
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
