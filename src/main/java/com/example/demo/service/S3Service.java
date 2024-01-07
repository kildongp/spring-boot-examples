package com.example.demo.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.springframework.util.FileCopyUtils.BUFFER_SIZE;

@Service
@Slf4j
public class S3Service {

    @Value("${ms.s3pdf.aws.access.key}")
    private String accessKey;

    @Value("${ms.s3pdf.aws.secret.key}")
    private String secretKey;

    @Value("${ms.s3pdf.aws.s3.region}")
    private String region;

    @Value("${ms.s3pdf.aws.s3.bucket}")
    private String bucketName;

    @Value("${ms.s3pdf.aws.s3.bucket.root}")
    private String bucketRoot;

    private AmazonS3 amazonS3;

    @Autowired
    public void initS3() {
        AWSCredentials awsCredentials =
                new BasicAWSCredentials(accessKey, secretKey);
        amazonS3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
        log.info("aws s3 initializayion completed");
        log.info("aws s3 bucket :{}",bucketName);
    }

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    public StreamingResponseBody downloadFileV3(HttpServletResponse response) {

        //https://localhost:8080/example/pdf-down

        //s3Key = "assets/pdf/pdf_30000146.pdf";
        String s3Key = "test.pdf";
        log.info("pdf file s3 bucket : {}", bucketName);
        log.info("pdf file s3 key : {}", s3Key);

        S3Object s3Object = amazonS3.getObject(bucketName, s3Key);
        String downloadFileName = String.format("report-%s-%s.pdf", "myfile", sdf.format(new Date()));
        log.info("pdf file download name : {}", downloadFileName);

        response.setContentType(s3Object.getObjectMetadata().getContentType());
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + downloadFileName + "\"");

        return outputStream -> {
            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        };
    }
}
