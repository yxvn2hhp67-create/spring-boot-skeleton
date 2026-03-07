package com.example.starter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HealthCheckController {

  @GetMapping("/")
  public String index() {
    return "health_check";
  }
}
