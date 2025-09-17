package com.viniss.todo.activity.persistence.jpa;

import com.viniss.todo.activity.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SpringDataActivityRepository extends JpaRepository<Activity, Long> {
  List<Activity> findByProjectIdOrderByAtDesc(String projectId);
}
