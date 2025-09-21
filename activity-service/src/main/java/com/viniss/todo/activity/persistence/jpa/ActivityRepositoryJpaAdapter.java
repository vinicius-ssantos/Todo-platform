package com.viniss.todo.activity.persistence.jpa;

import com.viniss.todo.activity.domain.Activity;
import com.viniss.todo.activity.domain.ActivityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ActivityRepositoryJpaAdapter implements ActivityRepository {
  private final SpringDataActivityRepository delegate;

  public ActivityRepositoryJpaAdapter(SpringDataActivityRepository delegate) {
    this.delegate = delegate;
  }

  @Override
  public Activity save(Activity activity) {
    return delegate.save(activity);
  }
  
  @Override
  public List<Activity> saveAll(List<Activity> activities) {
    return delegate.saveAll(activities);
  }

  @Override
  public List<Activity> findAll() {
    return delegate.findAll();
  }

  @Override
  public List<Activity> findByProjectIdOrderByAtDesc(String projectId) {
    if (projectId == null || projectId.isBlank()) {
      return List.of();
    }
    return delegate.findByProjectIdOrderByAtDesc(projectId);
  }
  
  @Override
  public void deleteAll() {
    delegate.deleteAll();
  }
}