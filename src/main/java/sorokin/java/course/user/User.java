package sorokin.java.course.user;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import sorokin.java.course.account.Account;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_login", nullable = false)
    private String login;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private List<Account> accountList = new ArrayList<>();

    public User() {
    }

    public User(String login) {
        this.login = login;
    }

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<Account> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }


    public User(Integer id, String login, List<Account> accountList) {
        this.id = id;
        this.login = login;
        this.accountList = accountList;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", accountList=" +
                accountList.stream()
                    .map(account -> "id: " + account.getId() +
                            " user_id: " + account.getUser().getId() +
                            " amount: " + account.getMoneyAmount())
                    .toList() +
                '}';
    }
}
