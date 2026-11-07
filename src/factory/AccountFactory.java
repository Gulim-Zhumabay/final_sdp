package factory;

import account.Account;
import account.CheckingAccount;
import account.DepositAccount;
import strategy.DepositInterest;

public class AccountFactory {
    private static final String ID_PREFIX = "ACC-";
    private static int counter = 1;

    public static Account create(String type, String owner, double initialBalance, int termMonths) {
        return build(type, nextId(), owner, initialBalance, termMonths, 0);
    }

    public static Account restore(String id, String type, String owner, double balance,
                                  int termMonths, int monthsElapsed) {
        registerExistingId(id);
        return build(type, id, owner, balance, termMonths, monthsElapsed);
    }

    private static Account build(String type, String id, String owner, double balance,
                                 int termMonths, int monthsElapsed) {
        return switch (type.toLowerCase()) {
            case "checking" -> new CheckingAccount(id, owner, balance);
            case "deposit" -> createDeposit(id, owner, balance, termMonths, monthsElapsed);
            default -> throw new IllegalArgumentException(
                    "Unknown account type: " + type + " (use checking or deposit)");
        };
    }

    private static Account createDeposit(String id, String owner, double balance,
                                         int termMonths, int monthsElapsed) {
        Account account = new DepositAccount(id, owner, balance, termMonths, monthsElapsed);
        account.setInterestStrategy(new DepositInterest());
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