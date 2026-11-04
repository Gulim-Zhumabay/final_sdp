package factory;

import account.Account;
import account.CheckingAccount;
import account.SavingsAccount;
import strategy.SavingsInterest;

public class AccountFactory {
    private static int counter = 1;

    public static Account create(String type, String owner, double initialBalance) {
        return switch (type.toLowerCase()) {
            case "savings" -> createSavings(nextId(), owner, initialBalance);
            case "checking" -> new CheckingAccount(nextId(), owner, initialBalance);
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
    }

    private static Account createSavings(String id, String owner, double initialBalance) {
        Account account = new SavingsAccount(id, owner, initialBalance);
        account.setInterestStrategy(new SavingsInterest());
        return account;
    }

    private static String nextId() {
        return "ACC-" + counter++;
    }
}