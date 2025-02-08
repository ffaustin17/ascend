package ServerSide;
import java.sql.*;
import java.io.*;
import java.util.*;

public class Database
{
    private Connection conn;
    private static final String ENCRYPTION_KEY = "superSecretKey";
    
    //constructor
    public Database()
    {
        try {
            //create a properties object
            Properties props = new Properties();

            //open file inpu stream
            FileInputStream fis = new FileInputStream(new File("./resources/db.properties"));

            System.out.println("Read the db.props file did not cause any errors.");

            props.load(fis);
            System.out.println("Loading db.props file did not cause any errors.");

            String url = props.getProperty("url");
            String user = props.getProperty("user");
            String pass = props.getProperty("password");

            System.out.println("getting the properties did not cause any errors.");
            // set the connection
            conn = DriverManager.getConnection(url,user,pass);

            if(!isDatabaseInitialized()){
                System.out.println("Database is empty. Creating tables...");
                createTables();
            }
            else{
                System.out.println("Database already contains tables. Skipping initialization.");
            }

            System.out.println("driver manager connection test.");
        }catch(Exception e){
            e.printStackTrace();

        }
    }

    /**
     * Checks if the database has at least one existing table.
     * @return true if tables exist, false otherwise.
     */
    private boolean isDatabaseInitialized() {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, "users", null)) { // Change "users" to a known table
                return tables.next(); // If at least one table exists, return true
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Assume database is not initialized on error
        }
    }

    /**
     * Creates all necessary tables in the database if they do not exist.
     */
    private void createTables() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARBINARY(255) NOT NULL);";

        // Add additional tables as needed
        String createScoresTable = "CREATE TABLE IF NOT EXISTS scores (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id INT, " +
                "score INT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createUsersTable);
            stmt.executeUpdate(createScoresTable); // Add more tables as needed
            System.out.println("Tables created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<String> query(String query)
    {
        //Add your code here
        //variable declirations
        ArrayList<String>list = new ArrayList<String>();
        int count = 0;
        int numColumns = 0;
        try {
            //create db statment
            Statement statement = conn.createStatement();

            ResultSet rs = statement.executeQuery(query);

            //get result set meta data
            ResultSetMetaData rmd = rs.getMetaData();

            numColumns = rmd.getColumnCount();

            while(rs.next()) {
                String record = "" ;

                for (int i = 0; i < numColumns; i++) {
                    record += rs.getString(i+1);
                    record += ",";
                }
                list.add(record);
            }

            //check for empty list
            if(list.size() == 0) {
                return null;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return list;

    }

    public void executeDML(String dml) throws SQLException
    {
        //Add your code here

        Statement statement = conn.createStatement();
        statement.execute(dml);
    }

    public boolean verifyLogin(String username, String password) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ? AND AES_DECRYPT(password, ?) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, ENCRYPTION_KEY);
            stmt.setString(3, password);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createAccount(String username, String password) {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO users (username, password) VALUES (?, AES_ENCRYPT(?, ?))";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                return false; // Username already exists
            }
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, ENCRYPTION_KEY);
            insertStmt.executeUpdate();
            return true; // Account created successfully
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Account creation failed
        }
    }




}
