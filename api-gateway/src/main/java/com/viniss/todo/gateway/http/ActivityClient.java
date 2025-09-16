package com.viniss.todo.gateway.http;

import com.viniss.todo.common.dto.ActivityResponse;
import com.viniss.todo.common.feign.FeignHeadersConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "activity", url = "${clients.activity.url}", configuration = FeignHeadersConfig.class)
public interface ActivityClient {
  @GetMapping(path = "/activities")
  List<ActivityResponse> listByProject(@RequestParam("projectId") String projectId);
}