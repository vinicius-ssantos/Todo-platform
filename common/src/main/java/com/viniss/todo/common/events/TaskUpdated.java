package com.viniss.todo.common.events;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Evento de integração disparado quando uma Task é atualizada.
 * Compatível com TaskAppService e aderente ao contrato TaskEvent.
 */
public record TaskUpdated(
        String taskId,
        String projectId,
        String title,
        String status,
        OffsetDateTime occurredAt,
        List<String> labels
) implements TaskEvent { }