package com.viniss.todo.activity.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
  List<Activity> findByProjectIdOrderByAtDesc(String projectId);
}
