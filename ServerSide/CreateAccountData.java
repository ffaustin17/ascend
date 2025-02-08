package ServerSide;

public class CreateAccountData {

    // Fields
    private String username;
    private String password;
    private String avatar;

    // Constructor
    public CreateAccountData(String username, String password, String avatar) {
        this.username = username;
        this.password = password;
        this.avatar = avatar;
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    // Method to get avatar
    public String getAvatar() {
        return avatar;
    }

    // toString method
    @Override
    public String toString() {
        return "CreateAccountData{" +
                "username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
