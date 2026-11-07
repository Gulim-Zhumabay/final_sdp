package storage;

import account.Account;
import factory.AccountFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import decorator.DecoratorFactory;

public class AccountCsvStore {
    private static final String HEADER = "id,type,owner,balance,termMonths,monthsElapsed,options";
    private static final String SEPARATOR = ",";
    private static final String NO_OPTIONS = "-";

    private final Path file;

    public AccountCsvStore(Path file) {
        this.file = file;
    }

    public void saveAll(Collection<Account> accounts) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Account account : accounts) {
            lines.add(toLine(account));
        }
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot save accounts", e);
        }
    }

    public List<Account> loadAll() {
        if (Files.notExists(file)) {
            return List.of();
        }
        try (Stream<String> lines = Files.lines(file)) {
            return lines.skip(1)
                    .map(this::parse)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load accounts", e);
        }
    }

    private String toLine(Account account) {
        String options = account.getOptionsCode();
        return String.join(SEPARATOR,
                account.getId(),
                account.getType(),
                account.getOwner(),
                String.valueOf(account.getBalance()),
                String.valueOf(account.getTermMonths()),
                String.valueOf(account.getMonthsElapsed()),
                options.isEmpty() ? NO_OPTIONS : options);
    }

    private Account parse(String line) {
        String[] parts = line.split(SEPARATOR);
        Account base = AccountFactory.restore(
                parts[0], parts[1], parts[2],
                Double.parseDouble(parts[3]),
                Integer.parseInt(parts[4]),
                Integer.parseInt(parts[5]));
        return DecoratorFactory.applyOptions(base, parts[6]);
    }
}