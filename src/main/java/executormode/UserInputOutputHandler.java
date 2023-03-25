package executormode;

import java.io.File;
import java.io.FileInputStream;

import javax.swing.JFileChooser;

import java.sql.Blob;
import java.sql.SQLException;
import javax.sql.rowset.serial.SerialBlob;

import java.util.Arrays;

import executormode.S3ApiHandler;


public class UserInputOutputHandler {
    private static S3ApiHandler s3Handler;

    private static final String WELCOME_PROMPT =
                    "=========================================================================\n" +
                    "Welcome to the S3 backup utility!\n" +
                    "To exit at any point in time, type 'exit', 'quit'\n" +
                    "To upload a file to S3 backup, type 'b'\n" +
                    "To restore a backup from S3, type 'r'\n" +
                    "To repeat this prompt, type 'p'\n" +
                    "=========================================================================\n";
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
    public static void handleTopLevelUserCommand(String cmd) {
        cmd = cmd.strip().toLowerCase();

        if (cmd.equals("b")) {
            handleStoreRequest();
        } else if (cmd.equals("r")) {
            // handle restoring from backup
        } else if (cmd.equals("p")) {
            System.out.println(WELCOME_PROMPT);
        } else if (cmd.equals("exit") || cmd.equals("quit")) {
            System.out.println(GOODBYE_PROMPT);
            System.exit(0);
        } else {
            System.out.println("Invalid input! To repeat the instructions prompt, press 'p'");
        }
    };

    public static void handleStoreRequest() {
        System.out.println("Choose a directory to store (a pop-up window will open):");
        JFileChooser fileChooser = new JFileChooser();

        // allow the user to only select directories
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
//        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

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

        File selectedDir = fileChooser.getSelectedFile();

        // read the directory into a blob, then call s3 helper to actually upload
        try {
            byte[] dirBinary = new byte[(int) selectedDir.length()];
//            System.out.println("a");
//            System.out.println(Arrays.toString(dirBinary));
//            FileInputStream inputStream = new FileInputStream(selectedDir);
//            System.out.println("b");
//            System.out.println(Arrays.toString(dirBinary));
//            inputStream.read(dirBinary);
//            System.out.println("c");
//            System.out.println(Arrays.toString(dirBinary));
//            inputStream.close();

            Blob dirBlob = new SerialBlob(dirBinary);

            s3Handler.uploadBlobToS3(selectedDir.getName(), dirBlob);
        } catch (Exception e) {
            System.out.println("We had trouble processing your folder. Try again");
        }
    };

    public static void handleRecoverRequest() {

    }
}
