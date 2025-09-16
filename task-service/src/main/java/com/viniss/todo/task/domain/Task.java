package com.viniss.todo.task.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task {
  @Id
  private String id;
  private String projectId;
  private String title;
  private String description;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;

  @ElementCollection
  private List<String> labels = new ArrayList<>();

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getProjectId() { return projectId; }
  public void setProjectId(String projectId) { this.projectId = projectId; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
  public List<String> getLabels() { return labels; }
  public void setLabels(List<String> labels) { this.labels = labels; }
}
