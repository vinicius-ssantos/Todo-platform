/**
 * Application layer for task-service.
 *
 * Orchestrates use cases for tasks and coordinates domain + ports. This
 * package hosts application services (e.g., TaskAppService) and application
 * ports (in/out) under subpackages when applicable.
 *
 * Keep it free of infrastructure concerns; adapters (HTTP, Kafka, JPA) should
 * implement the required ports in their own packages.
 */
package com.viniss.todo.task.application;