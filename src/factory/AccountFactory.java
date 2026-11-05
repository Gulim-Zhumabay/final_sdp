package factory;

import account.Account;
import account.CheckingAccount;
import account.SavingsAccount;
import strategy.SavingsInterest;

public class AccountFactory {
    private static final String ID_PREFIX = "ACC-";
    private static int counter = 1;

    public static Account create(String type, String owner, double initialBalance) {
        return build(type, nextId(), owner, initialBalance);
    }

    public static Account restore(String id, String type, String owner, double balance) {
        registerExistingId(id);
        return build(type, id, owner, balance);
    }

    private static Account build(String type, String id, String owner, double balance) {
        return switch (type.toLowerCase()) {
            case "savings" -> createSavings(id, owner, balance);
            case "checking" -> new CheckingAccount(id, owner, balance);
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
    }

    private static Account createSavings(String id, String owner, double balance) {
        Account account = new SavingsAccount(id, owner, balance);
        account.setInterestStrategy(new SavingsInterest());
        return account;
    }

    private static String nextId() {
        return ID_PREFIX + counter++;
    }

    private static void registerExistingId(String id) {
        int number = Integer.parseInt(id.substring(ID_PREFIX.length()));
        counter = Math.max(counter, number + 1);
    }
}