package com.mohit.om.service.service;

import com.mohit.om.service.constants.Constants;
import com.mohit.om.service.model.ProcessInstanceDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OrderQueryService class - Provides MongoDB queries for order history and process instance details
 *
 * @author mohit
 */
@Service
public class OrderQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderQueryService.class);

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * Retrieves the ProcessInstanceDetails for a given orderId
     *
     * @param orderId the order identifier to search for
     * @return ProcessInstanceDetails or null if not found
     */
    public ProcessInstanceDetails getProcessInstanceByOrderId(String orderId) {
        LOG.info("OrderQueryService:: getProcessInstanceByOrderId method started");
        try {
            Query query = new Query(Criteria.where("orderId").is(orderId));
            return mongoTemplate.findOne(query, ProcessInstanceDetails.class,
                    Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in getProcessInstanceByOrderId: ", e);
            return null;
        }
    }

    /**
     * Retrieves the ProcessInstanceDetails for a given processInstanceId
     *
     * @param processInstanceId Flowable process instance identifier
     * @return ProcessInstanceDetails or null if not found
     */
    public ProcessInstanceDetails getByProcessInstanceId(String processInstanceId) {
        LOG.info("OrderQueryService:: getByProcessInstanceId method started");
        try {
            Query query = new Query(Criteria.where("processInstanceId").is(processInstanceId));
            return mongoTemplate.findOne(query, ProcessInstanceDetails.class,
                    Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in getByProcessInstanceId: ", e);
            return null;
        }
    }

    /**
     * Retrieves all ProcessInstanceDetails for a given customerId
     *
     * @param customerId the customer identifier
     * @return List of ProcessInstanceDetails
     */
    public List<ProcessInstanceDetails> getOrdersByCustomerId(String customerId) {
        LOG.info("OrderQueryService:: getOrdersByCustomerId method started");
        try {
            Query query = new Query(Criteria.where("customerId").is(customerId));
            return mongoTemplate.find(query, ProcessInstanceDetails.class,
                    Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in getOrdersByCustomerId: ", e);
            return List.of();
        }
    }

    /**
     * Retrieves all ProcessInstanceDetails matching a given status
     *
     * @param status the order status to filter by
     * @return List of ProcessInstanceDetails
     */
    public List<ProcessInstanceDetails> getOrdersByStatus(String status) {
        LOG.info("OrderQueryService:: getOrdersByStatus method started");
        try {
            Query query = new Query(Criteria.where("status").is(status));
            return mongoTemplate.find(query, ProcessInstanceDetails.class,
                    Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in getOrdersByStatus: ", e);
            return List.of();
        }
    }
}
