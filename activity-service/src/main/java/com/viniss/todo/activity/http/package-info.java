/**
 * HTTP adapter for activity-service.
 *
 * Exposes REST endpoints to query recorded activities. This layer should only
 * perform HTTP concerns (validation, mapping) and delegate to application/services
 * when needed. Keep it free from business rules.
 */
package com.viniss.todo.activity.http;