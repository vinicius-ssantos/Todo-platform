package com.viniss.todo.gateway.http;

import com.viniss.todo.common.dto.CreateTaskRequest;
import com.viniss.todo.common.dto.TaskResponse;

import com.viniss.todo.common.dto.UpdateTaskRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskController {
  private final TaskClient client;

  public TaskController(TaskClient client) { this.client = client; }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public TaskResponse create(@RequestBody CreateTaskRequest req) { return client.create(req); }

  @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public TaskResponse update(@PathVariable("id") String id, @RequestBody UpdateTaskRequest req) { return client.update(id, req); }
}
