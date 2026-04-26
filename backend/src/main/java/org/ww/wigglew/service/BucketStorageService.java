package org.ww.wigglew.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class BucketStorageService {

    @Value("${DO_BUCKET_NAME:}")
    private String bucketName;

    final URI serverEndpoint = URI.create("https://nyc3.digitaloceanspaces.com");

    @Value("${DO_ACCESS_KEY:}")
    private String accessKey;

    @Value("${DO_SECRET_KEY:}")
    private String secretKey;

    final Duration urlValPeriod = Duration.ofMinutes(30);

    public String getPreSignedUrl(String fileExtension){
        String objectKey = UUID.randomUUID().toString().replace("-", "").substring(0, 20) + "." + fileExtension;  // Generate a random name of 20 characters for the file.

        try{
            if (bucketName == null || bucketName.isBlank() || accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            return new ObjectMapper().writeValueAsString(
                Map.of(
                    "error", "DigitalOcean Spaces is not configured",
                    "requiredEnv", "DO_BUCKET_NAME, DO_ACCESS_KEY, DO_SECRET_KEY"
                )
            );
            }

            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

            // URL generator object.
            S3Presigner preSignedUrlGenerator = S3Presigner.builder()
                    .region(Region.US_EAST_1).credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).endpointOverride(serverEndpoint).build();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(objectKey).build();
            PutObjectPresignRequest preSignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(putObjectRequest)
                    .signatureDuration(urlValPeriod)
                    .build();

            // Generate URL.
            URL presignedUrl = preSignedUrlGenerator.presignPutObject(preSignRequest).url();

            preSignedUrlGenerator.close();
            return new ObjectMapper().writeValueAsString(Map.of("preSignedUrl", presignedUrl.toString(), "uuid", objectKey));
        }
        catch (Exception e){
            return e.getMessage();
        }
    }

}
