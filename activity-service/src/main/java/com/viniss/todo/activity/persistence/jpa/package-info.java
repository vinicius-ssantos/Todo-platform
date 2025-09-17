/**
 * Persistence adapters (JPA) for activity-service.
 *
 * Provides Spring Data implementations that fulfill the domain port
 * {@link com.viniss.todo.activity.domain.ActivityRepository}. Keep persistence
 * technology isolated from the domain; expose only the domain interfaces to
 * other layers (controllers, listeners, services).
 */
package com.viniss.todo.activity.persistence.jpa;