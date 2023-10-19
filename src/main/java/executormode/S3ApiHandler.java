package executormode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class S3ApiHandler {
    private static final S3Client s3Client = DependencyFactory.s3Client();
    private static final String S3_UTIL_BUCKET = "native"; // store into one bucket in S3

    /**
     * Uploads a blob to the utility's designated S3 bucket
     *
     * @param dirName key under which the blob will be stored in S3
     * @param binDir the blob stored as an array of bytes
     */
    public static void uploadBlobToS3(String dirName, byte[] binDir) {
        createBucket(s3Client, S3_UTIL_BUCKET);

        try {
            s3Client.putObject(PutObjectRequest.builder().bucket(S3_UTIL_BUCKET).key(dirName + ".zip")
                            .build(), RequestBody.fromBytes(binDir));
            System.out.println(String.format("Upload of your folder '%s' completed successfully", dirName));
        } catch (Exception e) {
            System.out.println("We had trouble uploading your folder to S3. Try again");
        }
    }

    /**
     * Prints the contents of the utility's designated bucket while also displaying
     * each file with an index that allows the user to easily specify which file they
     * want to restore.
     *
     * @return map of format {file_index : filename} passed to the caller in
     * UserInputOutputHandler to allow for easy subsequent fetching of the file
     * that the user specified
     */
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
            System.out.println("We had trouble reading the backup bucket contents. Permissions issue?");
            return null;
        }
    }

    /**
     * Fetches Blob specified by filename which is stored as the key of an object in the
     * utility's bucket
     *
     * @param fileName name of the file to fetch from S3
     * @return InputStream corresponding to the blob that the user wanted to recover
     */
    public static InputStream fetchObject(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(S3_UTIL_BUCKET)
                .key(fileName)
                .build();

        return s3Client.getObject(getObjectRequest);
    }

    /**
     * Creates a designated bucket for the utility if it doesn't exist
     *
     * @param s3Client S3 client used to make calls to AWS
     * @param bucketName name of the bucket designated to store files by this utility
     */
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
