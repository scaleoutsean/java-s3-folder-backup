package executormode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

import executormode.UserInputOutputHandler;


public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static UserInputOutputHandler IOHandler;
    private static final String TERMINAL_PROMPT = "s3-helper: ";

    public static void main(String... args) {

        IOHandler.printWelcomePrompt();

        while (true) {
            System.out.print(TERMINAL_PROMPT);
            Scanner input = new Scanner(System.in);
            IOHandler.handleTopLevelUserCommand(input.nextLine());
        }
    }
}
