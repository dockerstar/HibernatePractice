package sorokin.java.course.operations.commands;

import org.springframework.stereotype.Component;
import sorokin.java.course.account.Account;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.config.TransactionHelper;
import sorokin.java.course.console.ConsoleInput;
import sorokin.java.course.operations.ConsoleOperationType;
import sorokin.java.course.operations.OperationCommand;
import sorokin.java.course.user.User;
import sorokin.java.course.user.UserService;

@Component
public class AccountCreateCommand implements OperationCommand {
    private final AccountService accountService;
    private final UserService userService;
    private final ConsoleInput consoleInput;
    private final TransactionHelper transactionHelper;

    public AccountCreateCommand(AccountService accountService, UserService userService, ConsoleInput consoleInput, TransactionHelper transactionHelper) {
        this.accountService = accountService;
        this.userService = userService;
        this.consoleInput = consoleInput;
        this.transactionHelper = transactionHelper;
    }

    @Override
    public void execute() {
        int userId = consoleInput.readPositiveInt("Enter user id:", "user id");
        User user = userService.findUserById(userId);
        Account account = accountService.createAccount(user);
        transactionHelper.executeTransaction(session -> {
            session.merge(user);
            user.getAccountList().add(account);
        });
        System.out.println("Account created: " + account);
    }

    @Override
    public ConsoleOperationType getOperationType() {
        return ConsoleOperationType.ACCOUNT_CREATE;
    }
}
