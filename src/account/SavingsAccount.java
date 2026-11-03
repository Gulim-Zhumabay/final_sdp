package account;

public class SavingsAccount extends AbstractAccount {

    public SavingsAccount(String id, String owner, double initialBalance) {
        super(id, owner, initialBalance);
    }

    @Override
    public String getType() {
        return "Savings";
    }
}