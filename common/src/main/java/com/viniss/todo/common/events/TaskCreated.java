package com.viniss.todo.common.events;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Evento de integração disparado quando uma Task é criada.
 * Compatível com TaskAppService e aderente ao contrato TaskEvent.
 */
public record TaskCreated(
    String taskId,
    String projectId,
    String title,
    String status,
    OffsetDateTime occurredAt,
    List<String> labels
) implements TaskEvent { }