package sorokin.java.course.account;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sorokin.java.course.config.TransactionHelper;
import sorokin.java.course.user.User;
import sorokin.java.course.user.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountProperties accountProperties;
    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;

    public AccountService(AccountProperties accountProperties, SessionFactory sessionFactory, TransactionHelper transactionHelper) {
        this.sessionFactory = sessionFactory;
        this.accountProperties = accountProperties;
        this.transactionHelper = transactionHelper;
    }

    public Account createAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        return transactionHelper.executeTransaction(session -> {
            Account account = new Account(user, accountProperties.getDefaultAmount());
            session.persist(account);
            return account;
        });
    }

    public Optional<Account> findAccountById(Integer id) {
        validatePositiveId(id, "account id");
        try (Session session = sessionFactory.openSession()) {
            Account account = session.find(Account.class, id);
            return Optional.ofNullable(account);
        }
    }

    public List<Account> getUserAccounts(Integer userId) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.find(User.class, userId);
            return user.getAccountList();
        }
    }

    public void withdraw(Integer fromAccountId, Integer amount) {
        validatePositiveId(fromAccountId, "account id");
        validatePositiveAmount(amount);

        transactionHelper.executeTransaction(session -> {
            Account account = session.find(Account.class, fromAccountId);
            if (account == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId));
            }
            if (amount > account.getMoneyAmount()) {
                throw new IllegalArgumentException(
                        "insufficient funds on account id=%s, moneyAmount=%s, attempted withdraw=%s"
                                .formatted(account.getId(), account.getMoneyAmount(), amount)
                );
            }
            account.setMoneyAmount(account.getMoneyAmount() - amount);
        });
    }

    public void deposit(Integer toAccountId, Integer amount) {
        validatePositiveId(toAccountId, "account id");
        validatePositiveAmount(amount);
        transactionHelper.executeTransaction(session -> {
            Account account = session.find(Account.class, toAccountId);
            if (account == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(toAccountId));
            }
            account.setMoneyAmount(account.getMoneyAmount() + amount);
        });
    }

    public Optional<Account> closeAccount(Integer accountId) {
        validatePositiveId(accountId, "account id");
        return transactionHelper.executeTransaction(session -> {
            Account accountToClose = session.find(Account.class, accountId);
            if (accountToClose == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(accountId));
            }
            User user = accountToClose.getUser();
            int userAccountFind = user.getId();
            List<Account> userAccounts = getUserAccounts(userAccountFind);
            if (userAccounts.size() == 1) {
                throw new IllegalStateException("Can't close the only one account");
            }
            Account accountToTransferMoneyFind = userAccounts.stream()
                    .filter(it -> it.getId() != accountId)
                    .findFirst()
                    .orElseThrow();
            Account accountToTransferMoneyMerge = session.merge(accountToTransferMoneyFind);
            var newAmount = accountToTransferMoneyMerge.getMoneyAmount() + accountToClose.getMoneyAmount();
            accountToTransferMoneyMerge.setMoneyAmount(newAmount);
            session.remove(accountToClose);
            return Optional.of(accountToClose);
        });
    }

    public void transfer(int fromAccountId, int toAccountId, int amount) {
        validatePositiveId(fromAccountId, "source account id");
        validatePositiveId(toAccountId, "target account id");
        validatePositiveAmount(amount);
        transactionHelper.executeTransaction(session -> {
            if (fromAccountId == toAccountId) {
                throw new IllegalArgumentException("source and target account id must be different");
            }
            Account accountFrom = session.find(Account.class, fromAccountId);
            if (accountFrom == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId));
            }
            Account accountTo = session.find(Account.class, toAccountId);
            if (accountTo == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(toAccountId));
            }
            if (amount > accountFrom.getMoneyAmount()) {
                throw new IllegalArgumentException(
                        "insufficient funds on account id=%s, moneyAmount=%s, attempted transfer=%s"
                                .formatted(accountFrom.getId(), accountFrom.getMoneyAmount(), amount)
                );
            }
            accountFrom.setMoneyAmount(accountFrom.getMoneyAmount() - amount);

            int amountToTransfer = accountTo.getUser().getId() == accountFrom.getUser().getId()
                    ? amount
                    : (int) Math.round(amount * (1 - accountProperties.getTransferCommission()));
            accountTo.setMoneyAmount(accountTo.getMoneyAmount() + amountToTransfer);
        });
    }

    private void validatePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }
    }

    private void validatePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }
}
