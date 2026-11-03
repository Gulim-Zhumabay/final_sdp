package factory;

import account.Account;
import account.CheckingAccount;
import account.SavingsAccount;

public class AccountFactory {
    private static int counter = 1;

    public static Account create(String type, String owner, double initialBalance) {
        String id = "ACC-" + counter++;

        switch (type.toLowerCase()) {
            case "savings":
                return new SavingsAccount(id, owner, initialBalance);
            case "checking":
                return new CheckingAccount(id, owner, initialBalance);
            default:
                throw new IllegalArgumentException("Unknown account type: " + type);
        }
    }
}