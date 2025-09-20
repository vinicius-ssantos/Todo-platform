package com.viniss.todo.task.http;

import com.viniss.todo.common.dto.CreateTaskRequest;
import com.viniss.todo.common.dto.TaskResponse;
import com.viniss.todo.common.dto.UpdateTaskRequest;
import com.viniss.todo.task.service.TaskAppService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskRestController {
  private final TaskAppService service;

  public TaskRestController(TaskAppService service) { this.service = service; }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest req) {
    TaskResponse created = service.create(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public TaskResponse update(@PathVariable("id") String id, @Valid @RequestBody UpdateTaskRequest req) {
    return service.update(id, req);
  }

  @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public TaskResponse patch(@PathVariable("id") String id, @RequestBody UpdateTaskRequest req) {
    return service.patch(id, req);
  }
}
