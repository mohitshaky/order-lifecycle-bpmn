package com.mohit.om.service.constants;

/**
 * Constants class - holds all application-level string constants
 *
 * @author mohit
 */
public final class Constants {

    private Constants() {
        // utility class
    }

    // Kafka topics
    public static final String ORDER_STATUS_TOPIC = "OM_ORDER_STATUS";
    public static final String PROVISIONING_STATUS_TOPIC = "OM_PROVISIONING_STATUS";
    public static final String VALIDATION_STATUS_TOPIC = "OM_VALIDATION_STATUS";
    public static final String NOTIFICATION_TOPIC = "OM_NOTIFICATION";

    // Order status values
    public static final String ORDER_STATUS_RECEIVED = "RECEIVED";
    public static final String ORDER_STATUS_VALIDATED = "VALIDATED";
    public static final String ORDER_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String ORDER_STATUS_PROVISIONED = "PROVISIONED";
    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    public static final String ORDER_STATUS_FAILED = "FAILED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    // HTTP header names
    public static final String HEADER_TRANSACTION_ID = "transactionId";
    public static final String HEADER_CORRELATION_ID = "correlationId";
    public static final String HEADER_SOURCE_CHANNEL = "sourceChannel";
    public static final String HEADER_TENANT_ID = "tenantId";

    // Flowable process variables
    public static final String VAR_ORDER_ID = "orderId";
    public static final String VAR_ORDER_STATUS = "orderStatus";
    public static final String VAR_CUSTOMER_ID = "customerId";
    public static final String VAR_PROCESS_INSTANCE_ID = "processInstanceId";
    public static final String VAR_STATUS = "status";
    public static final String VAR_TENANT_ID = "tenantId";

    // MongoDB collections
    public static final String COLLECTION_PROCESS_INSTANCE_DETAILS = "process_instance_details";

    // Kafka consumer groups
    public static final String ORDER_STATUS_GRP = "OM_ORDER_STATUS_GRP";
    public static final String PROVISIONING_STATUS_GRP = "OM_PROVISIONING_STATUS_GRP";

    // Kafka container factories
    public static final String ORDER_STATUS_LISTENER_FACTORY = "orderStatusListenerFactory";
    public static final String PROVISIONING_LISTENER_FACTORY = "provisioningListenerFactory";

    // Flowable process keys
    public static final String PROCESS_KEY_ORDER_FULFILLMENT = "orderFulfillmentProcess";

    // Response messages
    public static final String MSG_ORDER_STARTED = "Order process started successfully";
    public static final String MSG_TASK_COMPLETED = "Task completed successfully";
    public static final String MSG_SIGNAL_SENT = "Signal sent successfully";

    // Error codes
    public static final String ERR_ORDER_NOT_FOUND = "OM-001";
    public static final String ERR_PROCESS_START_FAILED = "OM-002";
    public static final String ERR_TASK_COMPLETE_FAILED = "OM-003";
    public static final String ERR_SIGNAL_FAILED = "OM-004";
    public static final String ERR_INTERNAL_ERROR = "OM-500";
}
