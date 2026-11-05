package storage;

import account.Transaction;
import account.TransactionType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransactionCsvStore {
    private static final String HEADER = "time,accountId,owner,type,amount";
    private static final String SEPARATOR = ",";

    private final Path file;

    public TransactionCsvStore(Path file) {
        this.file = file;
        createFileIfMissing();
    }

    public void append(Transaction transaction) {
        String line = String.join(SEPARATOR,
                transaction.getTime().toString(),
                transaction.getAccountId(),
                transaction.getOwner(),
                transaction.getType().name(),
                String.valueOf(transaction.getAmount()));
        try {
            Files.writeString(file, line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write transaction history", e);
        }
    }

    public List<Transaction> findByAccount(String accountId) {
        try (Stream<String> lines = Files.lines(file)) {
            return lines.skip(1)
                    .map(this::parse)
                    .filter(transaction -> transaction.getAccountId().equals(accountId))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read transaction history", e);
        }
    }

    private Transaction parse(String line) {
        String[] parts = line.split(SEPARATOR);
        return new Transaction(
                parts[1],
                parts[2],
                TransactionType.valueOf(parts[3]),
                Double.parseDouble(parts[4]),
                LocalDateTime.parse(parts[0]));
    }

    private void createFileIfMissing() {
        try {
            if (Files.notExists(file)) {
                Files.createDirectories(file.getParent());
                Files.writeString(file, HEADER + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create history file", e);
        }
    }
}
