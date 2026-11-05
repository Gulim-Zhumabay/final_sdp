package customer;

public class Customer {
    private final String username;
    private final String salt;
    private final String passwordHash;

    public Customer(String username, String salt, String passwordHash) {
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    public String getUsername() { return username; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
}