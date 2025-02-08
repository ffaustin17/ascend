package ServerSide;

import java.util.UUID;

public class HostOrJoinGameData {

    // Fields
    private boolean isHost;
    private String roomCode;

    // Setter for isHost
    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    // Method to generate a unique room code
    public void generateRoomCode() {
        roomCode = UUID.randomUUID().toString().substring(0, 8); // Generates an 8-character room code
    }

    // Getter for roomCode
    public String getRoomCode() {
        return roomCode;
    }
}

