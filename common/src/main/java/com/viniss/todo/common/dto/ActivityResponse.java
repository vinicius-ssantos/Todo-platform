package com.viniss.todo.common.dto;

import java.time.Instant;

public class ActivityResponse {
  private Long id;
  private String taskId;
  private String projectId;
  private String type;
  private Instant at;
  private String title;
  private String status;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getTaskId() { return taskId; }
  public void setTaskId(String taskId) { this.taskId = taskId; }
  public String getProjectId() { return projectId; }
  public void setProjectId(String projectId) { this.projectId = projectId; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public Instant getAt() { return at; }
  public void setAt(Instant at) { this.at = at; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}