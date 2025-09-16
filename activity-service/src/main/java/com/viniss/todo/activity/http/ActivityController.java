package com.viniss.todo.activity.http;

import com.viniss.todo.activity.domain.Activity;
import com.viniss.todo.activity.domain.ActivityRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/activities", produces = MediaType.APPLICATION_JSON_VALUE)
public class ActivityController {
  private final ActivityRepository repository;

  public ActivityController(ActivityRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<Activity> list(@RequestParam(name = "projectId", required = false) String projectId) {
    if (projectId != null && !projectId.isBlank()) {
      return repository.findByProjectIdOrderByAtDesc(projectId);
    }
    return repository.findAll();
  }

  @GetMapping(path = "/project/{projectId}")
  public List<Activity> listByProject(@PathVariable("projectId") String projectId) {
    return repository.findByProjectIdOrderByAtDesc(projectId);
  }
}
