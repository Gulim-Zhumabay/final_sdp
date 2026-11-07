package decorator;

import account.Account;

public class DecoratorFactory {
    private static final String OPTION_SEPARATOR = "\\|";
    private static final String FIELD_SEPARATOR = ":";
    private static final String NO_OPTIONS = "-";

    public static Account applyOptions(Account account, String optionsCode) {
        if (optionsCode.isBlank() || optionsCode.equals(NO_OPTIONS)) {
            return account;
        }
        Account result = account;
        for (String option : optionsCode.split(OPTION_SEPARATOR)) {
            result = applyOption(result, option);
        }
        return result;
    }

    private static Account applyOption(Account account, String option) {
        String[] parts = option.split(FIELD_SEPARATOR);
        return switch (parts[0]) {
            case "OVERDRAFT" -> new OverdraftDecorator(
                    account, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
            case "SMS" -> new SmsDecorator(account);
            default -> throw new IllegalArgumentException("Unknown account option: " + option);
        };
    }
}