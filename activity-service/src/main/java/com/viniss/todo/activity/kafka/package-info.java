/**
 * Kafka adapter (inbound) for activity-service.
 *
 * Listens to task events from Kafka and transforms them into Activity entries for auditing/history.
 * Keep messaging technology details here and delegate persistence/business meaning to services.
 */
package com.viniss.todo.activity.kafka;