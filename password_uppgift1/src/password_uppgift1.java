import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class password_uppgift1 {
    // sql path and queries, do not need to be global but easier to manage
    static String dbPath = "jdbc:sqlite:db/users.sqlite"; 
    String getPassSQL = "SELECT password, allowed FROM users WHERE username=?"; 
    String getUserSQL = "SELECT username FROM users WHERE username=?";
    String banUserSQL = "UPDATE users SET username=?, password=?, allowed=? WHERE username=?"; 
    String regUserSQL = "INSERT INTO users (username, password, allowed) VALUES (?, ?, ?);";
    String makeTableSQL = "CREATE TABLE IF NOT EXISTS users (id integer PRIMARY KEY, username text NOT NULL UNIQUE, password text NOT NULL, allowed integer DEFAULT 1);";

    public static void main(String[] args) {
        password_uppgift1 database = new password_uppgift1(); // need to call non-static methods
        Connection connection = database.dbGet(dbPath); // create db if it doesn't exist
        if (connection != null) {
            database.showOptions(); 
        }
        else {
            exit("Kunde inte koppla upp mot databasen");
        }
    }

    // options by user input
    private void showOptions() {
        Scanner input = new Scanner(System.in); // init scanner
        
        System.out.println("Vad vill du göra?");
        System.out.println("[0] Registrera användare");
        System.out.println("[1] Logga in");
        System.out.println("[2] Avsluta");

        if(input.hasNextInt()) {
           int selected = input.nextInt();
           switch (selected) {
                case 0: {
                    registerUser(); // cannot be static, input cache will stick or cannot be closed and re-opened
                    break;
                }
                case 1: {
                    loginUser(); // cannot be static, input cache will stick or cannot be closed and re-opened 
                    break;
                }
                case 2: {
                    exit("Programmet avslutas.");
                    break;
                }
            }
        }
        System.out.println("Välj ett av alternativen (skriv en siffra).");
    }

    // register a new user if it doesn't exist
    private void registerUser() {
        Scanner input = new Scanner(System.in); // init scanner
        String username = "";
        String password = "";
        String dbUserExists = "";
        int allowed = 1;
        System.out.println("Användarnamn:");
        if (input.hasNextLine()) {
            username = input.nextLine().trim(); // remove newlines and whitespace
            dbUserExists = dbGetUser(getUserSQL, username, "register");
        }
        if (username.length() > 0 && username.equals(dbUserExists)) {
            System.out.println("Användaren finns redan.");
        }
        else {
            System.out.println("Lösenord:");
            if (input.hasNextLine()) {
                password = input.nextLine().trim();
            }

            if (username.length() > 0 && password.length() > 0) { // save if not empty
                if (dbAction(regUserSQL, username, password, allowed)) {
                    System.out.println("Användaren har registrerats, du kan nu logga in.");
                }
            }
            else {
                System.out.println("Användarnamn och lösenord kan inte vara tomma");
            }
        }
        showOptions();
    }

    // user login
    private void loginUser() {
        Scanner input = new Scanner(System.in); // init scanner
        int numberOfAttempts = 0;
        int maxAttempts = 3; // max number of attempts
        String writtenUsername = "";
        String writtenPass; // declare the input string
        String dbPass = ""; // is never set to "" because we check for hasNextLine
        
        System.out.println("Logga in. Skriv användarnamn:");
        
        if (input.hasNextLine()) {
            writtenUsername = input.nextLine();
            dbPass = dbGetUser(getPassSQL, writtenUsername, "login");
        }

        if (dbPass.length() == 0) {
            System.out.println("Användaren finns inte");
        }
        else if (dbPass.equals("Du har försökt logga in felaktigt för många gånger, utstängd!")) { // not very pretty but unlikely to be set as password, could be checked with a global bool or something otherwise
            System.out.println("Du har försökt logga in felaktigt för många gånger, utstängd!");
        }
        else {
            System.out.println("Skriv lösenord:");

            while (numberOfAttempts < maxAttempts) { // retry until maxattemps is reached
                writtenPass = input.nextLine(); // read input
                if (writtenPass.equals(dbPass)) { // compare string
                    input.close(); // close input
                    exit("Rätt lösenord.");
                }
                else {
                    System.out.println("Fel lösenord, försök igen.");
                    numberOfAttempts++; // inc. attempts
                }
            }
            dbAction(banUserSQL, writtenUsername, "", 0);
            System.out.println("För många försök, ditt konto har blivit spärrat.");
        }
        showOptions();
    }

    // get users password
    private String dbGetUser(String sql, String username, String action) {
        int allowed = -1; // init with something not 0 or 1
        String password = "";
        String dbUsername = "";
        try (Connection conn = this.dbGet(dbPath);
            PreparedStatement pstmt = conn.prepareStatement(sql)) { // prepare statement to avoid sql injection
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();            
            while (rs.next()) { // loop through results
                if (action.equals("login")) {
                    allowed = rs.getInt("allowed");
                    password = rs.getString("password");    
                }
                else {
                    dbUsername = rs.getString("username");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        if (allowed == 0) {
            return "Du har försökt logga in felaktigt för många gånger, utstängd!"; 
        }
        else if (username.equals(dbUsername)) { // username is only selected when trying to register user, won't be returned when checking password
            return username;
        }
        else {
            return password;
        }
    }

    // save and ban user
    private boolean dbAction(String sql, String username, String password, int allowed) {
        boolean action = false;
        try (Connection conn = this.dbGet(dbPath);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setInt(3, allowed);
            if (allowed == 0) {
                pstmt.setString(4, username); // additional query var when banning user (where user =)
            }
            int count = pstmt.executeUpdate();
            action = (count > 0);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return action;
    }

    // make db and set up table
    private Connection dbGet(String dbPath) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbPath);
            Statement stmt = conn.createStatement();
            stmt.execute(makeTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    // quit
    private static void exit(String exitMsg) {
        System.out.println(exitMsg); // exit message
        System.exit(0); // graceful exit
    }
}