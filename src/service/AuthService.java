package service;

import customer.Customer;
import security.PasswordHasher;
import storage.CustomerCsvStore;

public class AuthService {
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final String WRONG_CREDENTIALS = "Wrong username or password";

    private final CustomerCsvStore store;
    private final PasswordHasher hasher;

    public AuthService(CustomerCsvStore store, PasswordHasher hasher) {
        this.store = store;
        this.hasher = hasher;
    }

    public Customer register(String username, String password) {
        validate(username, password);
        if (store.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("This username is already taken");
        }
        String salt = hasher.generateSalt();
        Customer customer = new Customer(username, salt, hasher.hash(password, salt));
        store.append(customer);
        return customer;
    }

    public Customer login(String username, String password) {
        Customer customer = store.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(WRONG_CREDENTIALS));
        String hash = hasher.hash(password, customer.getSalt());
        if (!hash.equals(customer.getPasswordHash())) {
            throw new IllegalArgumentException(WRONG_CREDENTIALS);
        }
        return customer;
    }

    private void validate(String username, String password) {
        if (username.length() < MIN_USERNAME_LENGTH || username.contains(",") || username.contains(" ")) {
            throw new IllegalArgumentException(
                    "Username: at least " + MIN_USERNAME_LENGTH + " characters, no spaces or commas");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }
}