package account;

public class CheckingAccount extends AbstractAccount {

    public CheckingAccount(String id, String owner, double initialBalance) {
        super(id, owner, initialBalance);
    }

    @Override
    public String getType() {
        return "Checking";
    }
}