package executormode;

import java.lang.reflect.InvocationTargetException;

public class App {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        UserInputOutputHandler.printWelcomePrompt();

        while (true) {
            UserInputOutputHandler.handleTopLevelUserCommand();
        }
    }
}
