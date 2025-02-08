package ServerSide;

public class LoginData {

    // Fields
    private String username;
    private String password;

    // Constructor
    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // toString method
    @Override
    public String toString() {
        return "LoginData{" +
                "username='" + username + '\'' +
                '}';
    }
}
