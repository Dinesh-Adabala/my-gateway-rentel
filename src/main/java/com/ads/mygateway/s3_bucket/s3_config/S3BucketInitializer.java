//package com.ads.mygateway.s3_bucket.s3_config;
//import jakarta.annotation.PostConstruct;
//import org.springframework.stereotype.Component;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.*;
//
//@Component
//public class S3BucketInitializer {
//
//    private final S3Client s3Client;
//
//    private final String bucketName = "homepage-components";
//
//    public S3BucketInitializer(S3Client s3Client) {
//        this.s3Client = s3Client;
//    }
//
//    @PostConstruct
//    public void init() {
//        if (!bucketExists(bucketName)) {
//            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
//                    .bucket(bucketName)
//                    .build();
//
//            s3Client.createBucket(createBucketRequest);
//            System.out.println("✅ Bucket created: " + bucketName);
//        } else {
//            System.out.println("ℹ️ Bucket already exists: " + bucketName);
//        }
//    }
//
//    private boolean bucketExists(String bucketName) {
//        ListBucketsResponse buckets = s3Client.listBuckets();
//        return buckets.buckets().stream()
//                .anyMatch(b -> b.name().equals(bucketName));
//    }
//}