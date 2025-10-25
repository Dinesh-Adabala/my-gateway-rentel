//package com.ads.mygateway.s3_bucket.controller;
//
//import com.ads.mygateway.s3_bucket.service.S3Service;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api/dashboard/files")
//public class FileController {
//
//    private final S3Service s3Service;
//
//    public FileController(S3Service s3Service) {
//        this.s3Service = s3Service;
//    }
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
//        try {
//            String key = s3Service.uploadFile(file);
//            return ResponseEntity.ok("File uploaded successfully: " + key);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
//        }
//    }
//}
