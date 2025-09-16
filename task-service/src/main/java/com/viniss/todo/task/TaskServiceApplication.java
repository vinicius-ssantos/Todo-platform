package com.viniss.todo.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.viniss.todo")
public class TaskServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(TaskServiceApplication.class, args);
  }
}
