package executormode;

import java.lang.reflect.InvocationTargetException;

public class App {

    /**
     * Main loop of the program. For each iteration, one action out of 4 is performed
     * (assuming no errors): a directory is pushed to S3, a folder is restored from S3
     * in a folder designated by the user, instructions on how to read the utility are
     * displayed to the user, or the program exits after being prompted to do so by the
     * user. These four actions are supported by handleTopLevelUserCommand() which routes
     * each action to the relevant helpers
     *
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        UserInputOutputHandler.printWelcomePrompt();

        while (true) {
            UserInputOutputHandler.handleTopLevelUserCommand();
        }
    }
}
