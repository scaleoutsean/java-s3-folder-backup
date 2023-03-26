package executormode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AppTest {

    @Test
    public void testUploadAndRecover() throws InterruptedException, InvocationTargetException, IOException {
        S3Client s3Client = DependencyFactory.s3Client();
        S3ApiHandler.createBucket(s3Client, "backup-util-bucket");

        // dummy file that we create not yet in directory where we create it
        Assertions.assertFalse(Files.exists(Paths.get(String.format("/dummyForUtility",
                System.getProperty("user.dir")))));

        // file that we didn't store not yet found in S3
        GetObjectRequest getObjectRequest1 = GetObjectRequest.builder()
                .bucket("backup-util-bucket")
                .key("dummyForUtility")
                .build();
        Assertions.assertThrows(NoSuchKeyException.class, () -> {s3Client.getObject(getObjectRequest1);});

        UserInputOutputHandlerDebug.handleStoreRequestDebug();
        // file that we stored now in S3
        GetObjectRequest getObjectRequest2 = GetObjectRequest.builder()
                .bucket("backup-util-bucket")
                .key("dummyForUtility")
                .build();
        Assertions.assertNotNull(s3Client.getObject(getObjectRequest2));

        UserInputOutputHandlerDebug.handleRecoverRequestDebug();

        // after recovery file exists and the zip doesn't
        Assertions.assertTrue(Files.exists(Paths.get(String.format("%s/dummyForUtility",
                System.getProperty("user.dir")))));
        Assertions.assertFalse(Files.exists(Paths.get(String.format("%s/dummyForUtility.zip",
                System.getProperty("user.dir")))));

        // to facilitate next run of test, delete file from S3 and make sure its deleted
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket("backup-util-bucket")
                .key("dummyForUtility")
                .build();
        s3Client.deleteObject(deleteObjectRequest);

        GetObjectRequest getObjectRequest3 = GetObjectRequest.builder()
                .bucket("backup-util-bucket")
                .key("dummyForUtility")
                .build();
        Assertions.assertThrows(NoSuchKeyException.class, () -> {s3Client.getObject(getObjectRequest3);});
    }
}
