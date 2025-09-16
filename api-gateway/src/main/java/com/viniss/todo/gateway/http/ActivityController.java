package com.viniss.todo.gateway.http;

import com.viniss.todo.common.dto.ActivityResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/activities", produces = MediaType.APPLICATION_JSON_VALUE)
public class ActivityController {
  private final ActivityClient client;

  public ActivityController(ActivityClient client) { this.client = client; }

  @GetMapping(path = "/project/{projectId}")
  public List<ActivityResponse> byProject(@PathVariable("projectId") String projectId) {
    return client.listByProject(projectId);
  }
}