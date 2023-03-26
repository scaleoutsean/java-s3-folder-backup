package executormode;

import java.io.*;
import javax.swing.JFileChooser;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class UserInputOutputHandler {
    private static S3ApiHandler s3Handler;

    private static final String WELCOME_PROMPT =
                    "=========================================================================\n" +
                    "====================Welcome to the S3 backup utility!====================\n" +
                    "To upload a file to S3 backup, type 'b'\n" +
                    "To restore a backup from S3, type 'r'\n" +
                    "To repeat this prompt, type 'p'\n" +
                    "To exit from the top level of the program, type 'exit' or 'quit'\n" +
                    "=========================================================================\n";
    private static final String TERMINAL_PROMPT = "s3-helper: ";
    private static final String GOODBYE_PROMPT = "Thank you for using S3 backup utility!";

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
    public static void handleTopLevelUserCommand() {
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
    };


    public static File selectDirUISequence () {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.requestFocus();

        // allow the user to only select directories and store the selected dir
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

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
                return null;
            }
        } while (result != JFileChooser.APPROVE_OPTION);

        // actually returns the directory
        return fileChooser.getSelectedFile();
    }

    public static void handleStoreRequest() {
        System.out.println("Choose a directory to store in S3 (a pop-up window will open)");
        File selectedDir = selectDirUISequence();
        if (selectedDir == null) {
            return;
        }

        // change dir to zip, convert to blob, then call s3 helper to actually upload
        try {
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

    public static void handleRecoverRequest() {
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
