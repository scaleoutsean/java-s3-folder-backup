package executormode;

import java.io.InputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class S3ApiHandler {
    private static final S3Client s3Client = DependencyFactory.s3Client();
    private static final String S3_UTIL_BUCKET = "backup-util-bucket"; // store into one bucket in S3

    public static void uploadBlobToS3(String dirName, byte[] binDir) {
        createBucket(s3Client, S3_UTIL_BUCKET);
        System.out.println("Uploading object...");

        try {
            s3Client.putObject(PutObjectRequest.builder().bucket(S3_UTIL_BUCKET).key(dirName)
                            .build(), RequestBody.fromBytes(binDir));
            System.out.println(String.format("Upload of your folder '%s' completed successfully", dirName));
        } catch (Exception e) {
            System.out.println("We had trouble uploading your folder to S3. Try again");
        }
    }

    // return the last valid index for a file
    public static Map<Integer, String> printS3BucketContents() {
        try {
            Map<Integer, String> res = new HashMap<Integer, String>();
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(S3_UTIL_BUCKET)
                    .build();

            ListObjectsResponse response = s3Client.listObjects(listObjects);
            ArrayList<S3Object> files = new ArrayList(response.contents());

            // print each file with its index in the array list
            System.out.println();
            for (int i = 0; i < files.size(); i++) {
                String curFile = files.get(i).key();
                res.put(i, curFile);
                System.out.println(String.format("%d: %s", i, curFile));
            }
            System.out.println();

            return res;
        } catch (Exception e) {
            System.out.println("We had trouble reading the backup bucket contents, try again");
            return null;
        }
    }

    public static InputStream fetchObject(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(S3_UTIL_BUCKET)
                .key(fileName)
                .build();

        return s3Client.getObject(getObjectRequest);
    }

    public static void createBucket(S3Client s3Client, String bucketName) {
        try {
            s3Client.createBucket(CreateBucketRequest
                    .builder()
                    .bucket(bucketName)
                    .build());
            s3Client.waiter().waitUntilBucketExists(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } catch (S3Exception e) {}
    }
}