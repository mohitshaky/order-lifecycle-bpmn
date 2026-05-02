package com.mohit.om.service.handler;

import com.mohit.om.service.constants.Constants;
import com.mohit.om.service.model.ProcessInstanceDetails;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.FlowableProcessStartedEvent;
import org.flowable.engine.delegate.event.FlowableProcessEngineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * OrderProcessEventHandler class - Handles Flowable process lifecycle events such as process start, end and deletion.
 * Implements FlowableEventListener to receive global engine-level events.
 *
 * @author mohit
 */
@Component
public class OrderProcessEventHandler implements FlowableEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrderProcessEventHandler.class);

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * Handles a FlowableEvent — logs and acts on process instance lifecycle changes.
     *
     * @param event the Flowable engine event
     */
    @Override
    public void onEvent(FlowableEvent event) {
        LOG.info("OrderProcessEventHandler:: onEvent method started :: eventType :: {}", event.getType());
        try {
            if (FlowableEngineEventType.PROCESS_STARTED.equals(event.getType())) {
                handleProcessStarted(event);
            } else if (FlowableEngineEventType.PROCESS_COMPLETED.equals(event.getType())) {
                handleProcessCompleted(event);
            } else if (FlowableEngineEventType.PROCESS_CANCELLED.equals(event.getType())) {
                handleProcessCancelled(event);
            }
        } catch (Exception e) {
            LOG.error("Exception in onEvent: ", e);
        }
    }

    /**
     * Returns false — this listener does not fail silently on exception (exceptions are propagated).
     *
     * @return false
     */
    @Override
    public boolean isFailOnException() {
        return false;
    }

    /**
     * Returns true — this listener fires on transaction events.
     *
     * @return true
     */
    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    /**
     * Returns null — no specific transaction lifecycle event filter applied.
     *
     * @return null
     */
    @Override
    public String getOnTransaction() {
        return null;
    }

    /**
     * Handles process started event — logs the process instance ID.
     *
     * @param event the FlowableEvent for process started
     */
    private void handleProcessStarted(FlowableEvent event) {
        LOG.info("OrderProcessEventHandler:: handleProcessStarted method started");
        if (event instanceof FlowableProcessEngineEvent engineEvent) {
            LOG.info("OrderProcessEventHandler:: Process started :: processInstanceId :: {}",
                    engineEvent.getProcessInstanceId());
        }
    }

    /**
     * Handles process completed event — updates MongoDB status to COMPLETED.
     *
     * @param event the FlowableEvent for process completed
     */
    private void handleProcessCompleted(FlowableEvent event) {
        LOG.info("OrderProcessEventHandler:: handleProcessCompleted method started");
        if (event instanceof FlowableProcessEngineEvent engineEvent) {
            String processInstanceId = engineEvent.getProcessInstanceId();
            LOG.info("OrderProcessEventHandler:: Process completed :: processInstanceId :: {}", processInstanceId);
            updateStatusByProcessInstanceId(processInstanceId, Constants.ORDER_STATUS_COMPLETED);
        }
    }

    /**
     * Handles process cancelled event — updates MongoDB status to CANCELLED.
     *
     * @param event the FlowableEvent for process cancelled
     */
    private void handleProcessCancelled(FlowableEvent event) {
        LOG.info("OrderProcessEventHandler:: handleProcessCancelled method started");
        if (event instanceof FlowableProcessEngineEvent engineEvent) {
            String processInstanceId = engineEvent.getProcessInstanceId();
            LOG.info("OrderProcessEventHandler:: Process cancelled :: processInstanceId :: {}", processInstanceId);
            updateStatusByProcessInstanceId(processInstanceId, Constants.ORDER_STATUS_CANCELLED);
        }
    }

    /**
     * Updates the status field of a process instance document in MongoDB using MongoTemplate + Criteria.
     *
     * @param processInstanceId Flowable process instance identifier
     * @param status            new status value to persist
     */
    private void updateStatusByProcessInstanceId(String processInstanceId, String status) {
        LOG.info("OrderProcessEventHandler:: updateStatusByProcessInstanceId method started");
        try {
            Query query = new Query(Criteria.where("processInstanceId").is(processInstanceId));
            Update update = new Update()
                    .set("status", status)
                    .set("updatedAt", LocalDateTime.now());
            mongoTemplate.updateFirst(query, update, Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in updateStatusByProcessInstanceId: ", e);
        }
    }
}
