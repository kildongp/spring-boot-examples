# Getting Started

https://start.spring.io/

maven
* Lombok
* Spring Boot DevTools
* Spring Web
* Thymeleaf

https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.2.1&packaging=jar&jvmVersion=17&groupId=com.example&artifactId=demo&name=demo&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.demo&dependencies=lombok,devtools,web,thymeleaf



## Add aws s3 in pom.xml

```xml
<!-- https://mvnrepository.com/artifact/software.amazon.awssdk/bom -->
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.11.789</version>
</dependency>
```

## edit properties
```properties
# AWS Microsoft Service Report S3
ms.s3pdf.aws.access.key=AWS-ACCESS-KEY
ms.s3pdf.aws.secret.key=AWS-SECRET
ms.s3pdf.aws.s3.region=us-east-1
ms.s3pdf.aws.s3.bucket=[s3 bucket]

```

## Test it

https://localhost:8080/

https://localhost:8080/example/pdf-down



## 
```java
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

```

* Add Service

```java

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
        
        //s3Key = "assets/pdf/pdf_30000146.pdf";
        String s3Key = "test.pdf";
        log.info("pdf file s3 bucket : {}", bucketName);
        log.info("pdf file s3 bucketRoot : {}", bucketRoot);
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
```

### Reference
* https://stackoverflow.com/questions/38578937/spring-boot-amazon-aws-s3-bucket-file-download-access-denied