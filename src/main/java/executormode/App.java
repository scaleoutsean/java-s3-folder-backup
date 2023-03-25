package executormode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String... args) {
        String prompt = "=========================================================================\n" +
                        "Welcome to the S3 backup utility!\n" +
                        "To exit at any point in time, type 'exit', 'quit'\n" +
                        "To upload a file to S3 backup, type 'b'\n" +
                        "To restore a backup from S3, type 'r'\n" +
                        "To repeat this prompt, type 'p'\n" +
                        "=========================================================================";
        System.out.println(prompt);

        while (true) {
            System.out.print("s3-helper: ");
            Scanner dog = new Scanner(System.in);
            System.out.println(dog.nextLine());
//            S3ApiHandler s3Handler = new S3ApiHandler();
//            s3Handler.sendRequest();
        }
    }
}
