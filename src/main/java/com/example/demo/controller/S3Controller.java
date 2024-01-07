package com.example.demo.controller;

import com.example.demo.service.S3Service;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping(value={"/example"})
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping(value = "/pdf-down")
    public StreamingResponseBody downloadFile(HttpServletResponse response) {
        // https://localhost:8080/example/pdf-down
        return s3Service.downloadFileV3(response);
    }
}
