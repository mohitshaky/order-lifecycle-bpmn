package com.mohit.om.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * ProcessInstanceDetails class - MongoDB document storing Flowable process instance metadata
 *
 * @author mohit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "process_instance_details")
public class ProcessInstanceDetails {

    @Id
    private String id;

    @Indexed
    private String orderId;

    private String processInstanceId;

    private String processKey;

    private String status;

    private String customerId;

    private String tenantId;

    private String transactionId;

    private String correlationId;

    private String activeTaskId;

    private String activeTaskName;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
