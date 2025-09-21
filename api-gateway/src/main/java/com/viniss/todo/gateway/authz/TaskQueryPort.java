package com.viniss.todo.gateway.authz;// package com.viniss.todo.gateway.authz;

import java.util.Optional;

public interface TaskQueryPort {
    Optional<String> findProjectIdByTaskId(String taskId);
}
