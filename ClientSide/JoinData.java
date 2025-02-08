package ClientSide;

import java.io.Serializable;

public class JoinData implements Serializable {

    private String hostPassword;

    public String getHostPassword(){
        return hostPassword;
    }

    public void setHostPassword(String password){
        this.hostPassword = password;
    }

    public JoinData(String password){
        setHostPassword(password);
    }
}
