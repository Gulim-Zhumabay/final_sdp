package storage;

import customer.Customer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomerCsvStore {
    private static final String HEADER = "username,salt,passwordHash";
    private static final String SEPARATOR = ",";

    private final Path file;

    public CustomerCsvStore(Path file) {
        this.file = file;
        createFileIfMissing();
    }

    public void append(Customer customer) {
        String line = String.join(SEPARATOR,
                customer.getUsername(), customer.getSalt(), customer.getPasswordHash());
        try {
            Files.writeString(file, line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot save customer", e);
        }
    }

    public Optional<Customer> findByUsername(String username) {
        return loadAll().stream()
                .filter(customer -> customer.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    private List<Customer> loadAll() {
        try (Stream<String> lines = Files.lines(file)) {
            return lines.skip(1)
                    .map(this::parse)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load customers", e);
        }
    }

    private Customer parse(String line) {
        String[] parts = line.split(SEPARATOR);
        return new Customer(parts[0], parts[1], parts[2]);
    }

    private void createFileIfMissing() {
        try {
            if (Files.notExists(file)) {
                Files.createDirectories(file.getParent());
                Files.writeString(file, HEADER + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create customers file", e);
        }
    }
}
