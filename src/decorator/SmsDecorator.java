package decorator;

import account.Account;

public class SmsDecorator extends AccountDecorator {

    public SmsDecorator(Account wrapped) {
        super(wrapped);
    }

    @Override
    public void deposit(double amount) {
        super.deposit(amount);
        System.out.println("[SMS to " + getOwner() + "] Deposited " + amount + ". Balance: " + getBalance());
    }

    @Override
    public void withdraw(double amount) {
        super.withdraw(amount);
        System.out.println("[SMS to " + getOwner() + "] Withdrawn " + amount + ". Balance: " + getBalance());
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + SMS";
    }
}