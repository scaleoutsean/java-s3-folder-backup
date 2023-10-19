package executormode;

import java.io.*;
import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

import net.lingala.zip4j.ZipFile;

public class UserInputOutputHandler {

    private static final String WELCOME_PROMPT =
            "=========================================================================\n" +
                    "====================Welcome to the S3 backup utility!====================\n" +
                    "To upload a file or folder to S3 backup, type 'b'\n" +
                    "To restore a backup from S3, type 'r'\n" +
                    "To repeat this prompt, type 'p'\n" +
                    "To exit from the top level of the program, type 'exit' or 'quit'\n" +
                    "=========================================================================\n";
    private static final String TERMINAL_PROMPT = "s3-helper: ";
    private static final String GOODBYE_PROMPT = "Thank you for using S3 backup utility!";

    private static S3ApiHandler s3Handler;

    /**
     * Prints the instructions on how to use the utility
     */
    public static void printWelcomePrompt() {
        System.out.print(WELCOME_PROMPT);
    }

    /**
     * Parses the user's top level command, which can be to store, recover,
     * view instructions, or exit. Handles the request locally if the
     * instruction is to exit or print instructions, else delegates to
     * helpers that communicate with S3 API
     *
     * @param cmd string representing user's desired action
     */
    public static void handleTopLevelUserCommand() throws InterruptedException, InvocationTargetException {
        System.out.print(TERMINAL_PROMPT);
        Scanner input = new Scanner(System.in);
        String cmd = input.nextLine().strip().toLowerCase();

        if (cmd.equals("b")) {
            handleStoreRequest();
        } else if (cmd.equals("r")) {
            handleRecoverRequest();
        } else if (cmd.equals("p")) {
            System.out.println(WELCOME_PROMPT);
        } else if (cmd.equals("exit") || cmd.equals("quit")) {
            System.out.println(GOODBYE_PROMPT);
            System.exit(0);
        } else {
            System.out.println("Invalid input! To repeat the instructions prompt, press 'p'");
        }
    }

    /**
     * Creates a JavaSwing GUI File dialogue for the user to select either which folder they
     * want to backup into S3 or which folder they want to store a recovered S3 blob in
     *
     * @return the directory that the user specifies
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public static File selectDirUISequence() throws InterruptedException, InvocationTargetException {
        JFileChooser fileChooser = new JFileChooser();

        // allow the user to only select directories
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        boolean[] exitedCorrectly = {false};
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                // wait until user actually selects a file
                int result;
                boolean firstTime = true;
                do {
                    if (!firstTime) System.out.println("You need to specify a directory:");
                    firstTime = false;
                    result = fileChooser.showOpenDialog(null);

                    // if user cancels action, return
                    if (result == JFileChooser.CANCEL_OPTION) {
                        System.out.println("Action cancelled");
                        return;
                    }
                } while (result != JFileChooser.APPROVE_OPTION);

                exitedCorrectly[0] = true;
            }
        });

        if (exitedCorrectly[0]) {
            return fileChooser.getSelectedFile();
        }

        return null;
    }

    /**
     * Handles the user's request to store a folder in S3 by converting the folder to
     * zip, converting the zip to a blob and sending to the utility's S3 bucket using
     * a helper in the S3ApiHandler class
     *
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public static void handleStoreRequest() throws InterruptedException, InvocationTargetException {
        System.out.println("Choose a directory to store in S3 (a pop-up window will open)");
        File selectedDir = selectDirUISequence();
        if (selectedDir == null) {
            return;
        }

        // change dir to zip, convert to blob, then call s3 helper to actually upload
        try {
            System.out.println("Processing object...");
            ZipFile zipFile = new ZipFile(String.format("%s.zip", selectedDir.getName()));
            zipFile.addFolder(new File(selectedDir.getAbsolutePath()));
            File zipAsFile = zipFile.getFile();
            byte[] dirBinary = new byte[(int) zipAsFile.length()];
            FileInputStream fis = new FileInputStream(zipAsFile);
            zipAsFile.delete();
            fis.read(dirBinary);

            System.out.println("Uploading object...");
            s3Handler.uploadBlobToS3(selectedDir.getName(), dirBinary);
        } catch (Exception e) {
            System.out.println("We had trouble processing your folder. Try again");
        }
    };

    /**
     * Handles the user's request to recover a backup stored in the utility's S3 bucket
     * by displaying the files stored in the user's S3 bucket and prompting the user to
     * select the one they want to recover. The function also prompts the user to specify
     * the directory in which they want to store their recovered backup. The function
     * proceeds by fetching the specified file from S3, converting it from a blob to a zip
     * file, and then extracting that zip file and deleting it
     *
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public static void handleRecoverRequest() throws InterruptedException, InvocationTargetException {
        // display all files in recovery bucket
        System.out.println("Please see the files in your bucket and type the number of the one you want to recover:");
        Map<Integer, String> contents = s3Handler.printS3BucketContents();

        if (contents == null) { // encountered error when fetching
            return;
        }

        // validate the user-input index
        System.out.print(TERMINAL_PROMPT);
        Scanner input = new Scanner(System.in);
        int chosenIdx;
        try {
            chosenIdx = input.nextInt();
            if (chosenIdx < 0 || !contents.containsKey(chosenIdx)) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            System.out.println("Please choose a valid file index");
            System.out.println(WELCOME_PROMPT);
            return;
        }

        // select location to store file we get from S3
        System.out.println("Choose a directory to store the recovered file (a pop-up window will open)");
        File selectedDir = selectDirUISequence();
        if (selectedDir == null) {
            return;
        }

        // fetch the user-specified file (as zip) from specified path and expand it
        String userSpecifiedFile = contents.get(chosenIdx);
        try {
            String recoveredFilePath = String.format("%s/%s.zip", selectedDir.getAbsolutePath(), userSpecifiedFile);

            Files.copy(
                    s3Handler.fetchObject(userSpecifiedFile),
                    Paths.get(recoveredFilePath)
            );

            // get the zip we generated, expand it and delete the zip, only leaving the folder behind
            ZipFile recoveredZip = new ZipFile(recoveredFilePath);
            recoveredZip.extractAll(selectedDir.getAbsolutePath());
            recoveredZip.getFile().delete();
        } catch (Exception e) {
            System.out.println("We had trouble processing the specified S3 file. Try again.");
            return;
        }

        System.out.println(String.format("Finished copying the blob into '%s'!", selectedDir.getAbsolutePath()));
    }
}
