package executormode;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class UserInputOutputHandlerDebug {
    private static S3ApiHandler s3Handler;

    /**
     * Helps test the handleStore functionality of the app by storing a blob for a dummy folder called /dummyForUtility
     */
    public static void handleStoreRequestDebug() throws IOException {
        File selectedDir = new File(String.format("%s/dummyForUtility", System.getProperty("user.dir")));
        selectedDir.mkdirs();
        ZipFile zipFile = new ZipFile(String.format("%s.zip", String.format("%s/dummyForUtility",
                System.getProperty("user.dir"))));
        zipFile.addFolder(new File(selectedDir.getAbsolutePath()));
        File zipAsFile = zipFile.getFile();
        byte[] dirBinary = new byte[(int) zipAsFile.length()];
        FileInputStream fis = new FileInputStream(zipAsFile);
        zipAsFile.delete();
        fis.read(dirBinary);
        s3Handler.uploadBlobToS3("dummyForUtility", dirBinary);
        selectedDir.delete();
    };

    /**
     * Helps test the handleRecover functionality of the app by recovering the blob for the /dummyForUtility folder
     * and converting it back into folder form
     *
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public static void handleRecoverRequestDebug() throws InterruptedException, InvocationTargetException {
        File selectedDir = new File(System.getProperty("user.dir"));
        try {
            String recoveredFilePath = String.format("%s/%s.zip", selectedDir.getAbsolutePath(), "dummyForUtility");
            Files.copy(
                    s3Handler.fetchObject("dummyForUtility"),
                    Paths.get(recoveredFilePath)
            );
            ZipFile recoveredZip = new ZipFile(recoveredFilePath);
            recoveredZip.extractAll(selectedDir.getAbsolutePath());
            recoveredZip.getFile().delete();
        } catch (Exception e) {}
    }
}
