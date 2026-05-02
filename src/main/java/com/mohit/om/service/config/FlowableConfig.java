package com.mohit.om.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * FlowableConfig class - Flowable ProcessEngine configuration notes.
 *
 * <p>Flowable auto-configuration is handled by flowable-spring-boot-starter.
 * The process engine is configured via application.yml (spring.flowable.*).
 * BPMN process definitions are automatically deployed from src/main/resources/processes/.</p>
 *
 * <p>Key configuration:
 * <ul>
 *   <li>spring.flowable.database-schema-update=true — auto-creates Flowable schema</li>
 *   <li>Delegates are Spring @Component beans, injected by Flowable via Spring context</li>
 *   <li>RuntimeService — used to start/signal process instances</li>
 *   <li>TaskService — used to query and complete user tasks</li>
 * </ul>
 * </p>
 *
 * @author mohit
 */
@Configuration
public class FlowableConfig {

    private static final Logger LOG = LoggerFactory.getLogger(FlowableConfig.class);

    /**
     * Flowable engine configuration is handled by auto-configuration.
     * Custom beans (e.g., custom activity behaviors) can be added here if needed.
     */
    public FlowableConfig() {
        LOG.info("FlowableConfig:: initialized — Flowable ProcessEngine configured via spring-boot-starter");
    }
}
