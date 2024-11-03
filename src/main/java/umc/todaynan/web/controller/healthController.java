package umc.todaynan.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class healthController {
    @GetMapping("/health")
    public String healthCheck(){
        return "healthy";
    }
}

