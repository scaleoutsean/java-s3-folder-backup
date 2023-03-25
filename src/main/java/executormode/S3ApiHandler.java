package executormode;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;


import java.sql.Blob;
import java.sql.SQLException;

public class S3ApiHandler {
    private static final S3Client s3Client = DependencyFactory.s3Client();
    private static final String S3_UTIL_BUCKET = "backup-util-bucket"; // store into one bucket in S3

    public static void uploadBlobToS3(String dirName, Blob dirBlob) {
        createBucket(s3Client, S3_UTIL_BUCKET);

        System.out.println("Uploading object...");

        try {
            s3Client.putObject(PutObjectRequest.builder().bucket(S3_UTIL_BUCKET).key(dirName)
                            .build(), RequestBody.fromString("Testing with the {sdk-java}"));
            System.out.println(String.format("Upload of your folder '%s' completed successfully", dirName));
        } catch (Exception e) {
            System.out.println("We had trouble uploading your folder to S3. Try again");
        }
    }
    public static void createBucket(S3Client s3Client, String bucketName) {
        try {
            s3Client.createBucket(CreateBucketRequest
                    .builder()
                    .bucket(bucketName)
                    .build());
            System.out.println("Creating bucket: " + bucketName);
            s3Client.waiter().waitUntilBucketExists(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            System.out.println(bucketName + " is ready.");
            System.out.printf("%n");
        } catch (S3Exception e) {}
    }
}