package com.viniss.todo.gateway.http;

import com.viniss.todo.common.dto.CreateTaskRequest;
import com.viniss.todo.common.dto.TaskResponse;
import com.viniss.todo.common.dto.UpdateTaskRequest;
import com.viniss.todo.common.feign.FeignHeadersConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "task", url = "${clients.task.url}", configuration = FeignHeadersConfig.class)
public interface TaskClient {
  @PostMapping(path = "/tasks")
  TaskResponse create(@RequestBody CreateTaskRequest req);

  @PutMapping(path = "/tasks/{id}")
  TaskResponse  update(@PathVariable("id") String id, @RequestBody UpdateTaskRequest req);
}
