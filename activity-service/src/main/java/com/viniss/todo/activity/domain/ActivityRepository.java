package com.viniss.todo.activity.domain;

import java.util.List;

/**
 * Domain port for activity persistence.
 *
 * This interface must not depend on Spring Data or any persistence technology.
 * Adapters (e.g., JPA) should implement this contract.
 */
public interface ActivityRepository {
  Activity save(Activity activity);
  List<Activity> saveAll(List<Activity> activities);
  List<Activity> findAll();
  List<Activity> findByProjectIdOrderByAtDesc(String projectId);
  void deleteAll();
}