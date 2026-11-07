package decorator;

import account.Account;
import account.TransactionType;

public class SmsDecorator extends AccountDecorator {

    public SmsDecorator(Account wrapped) {
        super(wrapped);
    }

    @Override
    public void deposit(double amount, TransactionType type) {
        super.deposit(amount, type);
        sendSms(type, amount);
    }

    @Override
    public void withdraw(double amount, TransactionType type) {
        super.withdraw(amount, type);
        sendSms(type, amount);
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + SMS";
    }

    private void sendSms(TransactionType type, double amount) {
        System.out.println("[SMS to " + getOwner() + "] " + type + " " + amount
                + ". Balance: " + getBalance());
    }
    @Override
    protected String getOwnCode() {
        return "SMS";
    }
}