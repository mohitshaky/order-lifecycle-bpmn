# Order Lifecycle BPMN

> **What problem this solves:** Orchestrates telecom/banking order fulfillment via BPMN — so orders don't get stuck, lost, or require human intervention.

[![CI](https://github.com/mohitshaky/order-lifecycle-bpmn/actions/workflows/ci.yml/badge.svg)](https://github.com/mohitshaky/order-lifecycle-bpmn/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)

## Key Results
- ✅ Handles 50K+ orders/day capacity
- ✅ Multi-tenant across 4 Kafka topics
- ✅ End-to-end distributed tracing

## Tech Stack
Java 17 · Spring Boot · Flowable BPMN · Kafka · PostgreSQL · Docker · Zipkin

## What It Does
A BPMN-driven order orchestration engine for telecom and banking domains. Orders flow through configurable process definitions — validation, provisioning, fulfilment, and confirmation — with Kafka handling inter-service messaging and distributed tracing providing full observability across every step.

## Quick Start
```bash
# clone and run
git clone https://github.com/mohitshaky/order-lifecycle-bpmn.git
cd order-lifecycle-bpmn
./gradlew bootRun
```

## License
MIT — see [LICENSE](LICENSE)