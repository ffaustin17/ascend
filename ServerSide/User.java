package ServerSide;

public class User {

    // Fields
    private static String username;
    private String passwordHash;  // Stored as a hashed string
    private int gamesPlayed;
    private int gamesWon;

    // Constructor
    public User(String username, String password, int gamesPlayed, int gamesWon) {
        this.username = username;
        this.passwordHash = hashPassword(password);  // Hash the password upon creation
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
    }

    // Constructor to load user with hashed password (for database retrieval)
    public User(String username, String passwordHash, int gamesPlayed, int gamesWon, boolean isHashed) {
        this.username = username;
        this.passwordHash = isHashed ? passwordHash : hashPassword(passwordHash);  // Option to skip hashing
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
    }

    // Getters
    public static String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    // Setters
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    // Increment games played by 1
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    // Increment games won by 1
    public void incrementGamesWon() {
        this.gamesWon++;
    }

    // Method to hash password (e.g., using SHA-256)
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Validate a plain text password against the stored hash
    public boolean validatePassword(String password) {
        return hashPassword(password).equals(this.passwordHash);
    }

    // Display user information for debugging (optional)
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", gamesPlayed=" + gamesPlayed +
                ", gamesWon=" + gamesWon +
                '}';
    }
}
