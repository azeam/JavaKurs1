import java.util.Arrays;
import java.util.Scanner;
// sql
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
// hash
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

// set/get hash and salt
final class Pair {
	private byte[] hashedPassword;
	private byte[] salt;
	public Pair(byte[] hashedPassword, byte[] salt) {
		this.hashedPassword = hashedPassword;
		this.salt = salt;
	}
	public byte[] getHashedPassword() { return hashedPassword; }
	public byte[] getSalt() { return salt; }
}

public class password_uppgift1 {
	// sql path and queries, do not need to be "global" but easier to overview from here (for now)
	static String dbPath = "jdbc:sqlite:users.sqlite"; 
	String getPassSQL = "SELECT password, salt FROM users WHERE username=?"; 
	String getUserSQL = "SELECT username, allowed FROM users WHERE username=?";
	String banUserSQL = "UPDATE users SET allowed=? WHERE username=?"; 
	String regUserSQL = "INSERT INTO users (username, password, salt) VALUES (?, ?, ?);";
	String makeTableSQL = "CREATE TABLE IF NOT EXISTS users (id integer PRIMARY KEY, username text NOT NULL UNIQUE, password blob NOT NULL, salt blob NOT NULL, allowed integer DEFAULT 1);";

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in); // init scanner
		password_uppgift1 database = new password_uppgift1(); // need to call non-static methods
		Connection connection = database.dbGet(dbPath); // create db if it doesn't exist
		if (connection != null) {
			database.showOptions(input); 
		}
		else {
			exit(input, "Kunde inte koppla upp mot databasen");
		}
	}

	// hash + salt password, from https://www.javainterviewpoint.com/java-salted-password-hashing/
	public Pair hashPassword(String password, byte[] salt) {
		MessageDigest md;
		byte[] hashedPassword = null;
		try {
			// Select the message digest for the hash computation -> SHA-256
			md = MessageDigest.getInstance("SHA-512");

			if (salt == null) { // generate pass, for registering, otherwise check salt from db
				// Generate the random salt
				SecureRandom random = new SecureRandom();
				salt = new byte[16];
				random.nextBytes(salt);
			}

			// Passing the salt to the digest for the computation
			md.update(salt);

			// Generate the salted hash
			hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

			StringBuilder sb = new StringBuilder();
			for (byte b : hashedPassword)
				sb.append(String.format("%02x", b));

		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		}
		return new Pair(hashedPassword, salt);
	}

	// options by user input
	private void showOptions(Scanner input) {
		System.out.println("\nVad vill du göra?\n[0] Registrera användare\n[1] Logga in\n[2] Avsluta");

		while (input.hasNext()) {
			if(input.hasNextInt()) {
				int selected = input.nextInt();
				switch (selected) {
						case 0: {
							registerUser(input); // cannot be static, input cache will stick or cannot be closed and re-opened
							break;
						}
						case 1: {
							loginUser(input); // cannot be static, input cache will stick or cannot be closed and re-opened 
							break;
						}
						case 2: {
							exit(input, "Programmet avslutas.");
							break;
						}
					}
			}
			else {
				System.out.println("Välj ett av alternativen (skriv en siffra).");
				input.next(); // skip ahead
			}
		}
	}

	// register a new user if it doesn't exist
	private void registerUser(Scanner input) {
		String username = "";
		String password = "";
		int dbUserExists = 0;
		
		System.out.println("Registrera användare.\nAnvändarnamn:");
		input.nextLine(); // clear cache
		if (input.hasNextLine()) {
			username = input.nextLine().trim(); // remove newlines and whitespace
			dbUserExists = dbGetUser(getUserSQL, username);
		}
		if (username.length() > 0 && dbUserExists == 1 || dbUserExists == 0) {
			System.out.println("Användaren finns redan.");
		}
		else {
			System.out.println("Lösenord:");
			if (input.hasNextLine()) {
				password = input.nextLine().trim();
			}
			if (username.length() > 0 && password.length() > 0) { // save if not empty
				if (dbReg(regUserSQL, username, password)) {
					System.out.println("Användaren har registrerats, du kan nu logga in.");
				}
			}
			else {
				System.out.println("Användarnamn och lösenord kan inte vara tomma");
			}
		}
		showOptions(input);
	}

	// user login
	private void loginUser(Scanner input) {
		int numberOfAttempts = 0;
		int maxAttempts = 3; // max number of attempts
		int dbCheckUser = 1; // any not 0 or 2
		String writtenUsername = "";
		String writtenPass; // declare the input string
		
		System.out.println("Logga in.\nSkriv användarnamn:");
		input.nextLine(); // clear cache
		if (input.hasNextLine()) {
			writtenUsername = input.nextLine();
			dbCheckUser = dbGetUser(getUserSQL, writtenUsername);
		}

		if (dbCheckUser == 2) {
			System.out.println("Användaren finns inte");
		}
		else if (dbCheckUser == 0) { 
			System.out.println("Du har försökt logga in felaktigt för många gånger, utstängd!");
		}
		else {
			System.out.println("Skriv lösenord:");
			while (numberOfAttempts < maxAttempts) { // retry until maxattemps is reached
				writtenPass = input.nextLine(); // read input
				
				if (comparePasswords(getPassSQL, writtenUsername, writtenPass)) { // compare string
					exit(input, "Rätt lösenord.");
				}
				else {
					System.out.println("Fel lösenord, försök igen.");
					numberOfAttempts++; // inc. attempts
				}
			}
			dbBan(banUserSQL, writtenUsername, 0);
			System.out.println("För många försök, ditt konto har blivit spärrat.");
		}
		showOptions(input);
	}

	// get input password and compare with db pass using db salt
	private boolean comparePasswords (String sql, String username, String password) {
		byte[] dbPass = null;
		byte[] dbSalt = null;
		byte[] hashedUserPass = null;
		try (Connection conn = this.dbGet(dbPath);
			PreparedStatement pstmt = conn.prepareStatement(sql)) { // prepare statement to avoid sql injection
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();            
			while (rs.next()) { // loop through results
				dbSalt = rs.getBytes("salt");
				dbPass = rs.getBytes("password");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		if (dbPass != null && dbSalt != null) {
			Pair userPass = hashPassword(password, dbSalt);
			hashedUserPass = userPass.getHashedPassword();
		}

		if (Arrays.equals(hashedUserPass, dbPass) && dbPass != null && dbSalt != null) {
			return true;
		} 
		else {
			return false;
		}
	}

	// get user details
	private int dbGetUser(String sql, String username) {
		int allowed = -1; // init with something not 0 or 1
		String dbUsername = "";
		try (Connection conn = this.dbGet(dbPath);
			PreparedStatement pstmt = conn.prepareStatement(sql)) { // prepare statement to avoid sql injection
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();            
			while (rs.next()) { // loop through results
				allowed = rs.getInt("allowed");
				dbUsername = rs.getString("username");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		if (allowed == 0) { // user banned
			return 0; 
		}
		else if (username.equals(dbUsername)) { // user exists, username is only selected when trying to register user, won't be returned when checking password
			return 1;
		}
		else {
			return 2;
		}
	}

	// register user
	private boolean dbReg(String sql, String username, String password) {
		boolean action = false;
		Pair hashReg = hashPassword(password, null);
		byte[] hashedPass = hashReg.getHashedPassword();
		byte[] salt = hashReg.getSalt();
		try (Connection conn = this.dbGet(dbPath);
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, username);
			pstmt.setBytes(2, hashedPass);
			pstmt.setBytes(3, salt);
			
			int count = pstmt.executeUpdate();
			action = (count > 0); // confirm execute was successful
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return action;
	}

	// ban user
	private boolean dbBan(String sql, String username, int allowed) {
		boolean action = false;
		try (Connection conn = this.dbGet(dbPath);
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, allowed);
			pstmt.setString(2, username); 
			int count = pstmt.executeUpdate();
			action = (count > 0); // confirm execute was successful
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return action;
	}    

	// make db and set up table if it doesn't exist and return connection
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
	private static void exit(Scanner input, String exitMsg) {
		input.close(); // close input
		System.out.println(exitMsg); // exit message
		System.exit(0); // graceful exit
	}
}