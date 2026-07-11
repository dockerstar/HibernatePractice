package sorokin.java.course.operations.commands;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.console.ConsoleInput;
import sorokin.java.course.operations.ConsoleOperationType;
import sorokin.java.course.operations.OperationCommand;
import sorokin.java.course.user.User;
import sorokin.java.course.user.UserService;

@Component
public class CreateUserCommand implements OperationCommand {

    private final SessionFactory sessionFactory;
    private final UserService userService;
    private final ConsoleInput consoleInput;

    public CreateUserCommand(SessionFactory sessionFactory, UserService userService, ConsoleInput consoleInput) {
        this.sessionFactory = sessionFactory;
        this.userService = userService;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        String login = consoleInput.readRequiredString("Enter login:", "login");
        User user = userService.createUser(login);
        System.out.println("User created: " + user);
    }

    @Override
    public ConsoleOperationType getOperationType() {
        return ConsoleOperationType.USER_CREATE;
    }
}
