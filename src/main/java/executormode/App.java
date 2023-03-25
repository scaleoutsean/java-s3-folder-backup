package executormode;

public class App {

    public static void main(String[] args) {
        UserInputOutputHandler.printWelcomePrompt();

        while (true) {
            UserInputOutputHandler.handleTopLevelUserCommand();
        }
    }
}
