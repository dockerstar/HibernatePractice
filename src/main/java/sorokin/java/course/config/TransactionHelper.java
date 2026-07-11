package sorokin.java.course.config;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class TransactionHelper {
    private final SessionFactory sessionFactory;

    public TransactionHelper(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void executeTransaction(Consumer<Session> action) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.getTransaction();
            transaction.begin();

            action.accept(session);

            transaction.commit();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            if (transaction!=null) {
                transaction.rollback();
            }
        }
    }

    public<T> T executeTransaction(Function<Session, T> action) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.getTransaction();
            transaction.begin();

            var result = action.apply(session);

            transaction.commit();
            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            if (transaction!=null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
