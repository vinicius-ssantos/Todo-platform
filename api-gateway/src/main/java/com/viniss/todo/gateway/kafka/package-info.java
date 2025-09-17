/**
 * Kafka consumer adapter for api-gateway.
 *
 * Consumes task-related events to broadcast updates over WebSocket to clients.
 * Keep messaging concerns isolated here; do not place business logic in this layer.
 */
package com.viniss.todo.gateway.kafka;