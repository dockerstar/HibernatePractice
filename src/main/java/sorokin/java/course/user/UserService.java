package sorokin.java.course.user;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.config.TransactionHelper;
import sorokin.java.course.user.User;

import java.util.*;

@Service
public class UserService {
    private final SessionFactory sessionFactory;
    private final AccountService accountService;
    private final TransactionHelper transactionHelper;

    public UserService(SessionFactory sessionFactory, AccountService accountService, TransactionHelper transactionHelper) {
        this.sessionFactory = sessionFactory;
        this.accountService = accountService;
        this.transactionHelper = transactionHelper;
    }

    public User createUser(String login) {
        String normalizedLogin = validateLogin(login);
        return transactionHelper.executeTransaction(session -> {
            User user = new User(normalizedLogin);
            session.persist(user);
            return user;
        });
    }

    public User findUserById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("user id must be > 0");
        }
        try (Session session = sessionFactory.openSession()) {
            User user = session.find(User.class, id);
            if (user == null) {
                throw new IllegalArgumentException("No such user with id=%s".formatted(id));
            }
            return user;
        }
    }

    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "select u from User u", User.class
            ).list();
        }
    }

    private String validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login must not be blank");
        }
        return login.trim();
    }
}
