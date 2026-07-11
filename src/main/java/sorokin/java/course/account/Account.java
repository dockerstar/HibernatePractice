package sorokin.java.course.account;

import jakarta.persistence.*;
import sorokin.java.course.user.User;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private int moneyAmount;

    public Account() {
    }

    public Account(User user, int moneyAmount) {
        this.user = user;
        this.moneyAmount = moneyAmount;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public int getMoneyAmount() {
        return moneyAmount;
    }

    public void setMoneyAmount(int moneyAmount) {
        if (moneyAmount < 0) {
            throw new IllegalArgumentException("Attempted to set moneyAmount less than 0");
        }
        this.moneyAmount = moneyAmount;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", user_id: " + user.getId() +
                ", moneyAmount=" + moneyAmount +
                '}';
    }
}
