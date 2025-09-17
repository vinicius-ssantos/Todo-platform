/**
 * HTTP adapters for api-gateway.
 *
 * Exposes public REST APIs and uses Feign clients to call downstream services.
 * Contains controllers and Feign interfaces; apply resilience, mapping, and validation here.
 * Keep business logic out of this layer.
 */
package com.viniss.todo.gateway.http;