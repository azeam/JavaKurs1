import java.io.*;
import java.net.*;
 
public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) { // confirm host is set
            System.out.println("Error - No server specified, start application with \"java Client.java 123.123.321.321\"");
            System.exit(0);
        }
        
        int portNumber = 6666;
        String newline = System.getProperty("line.separator"); // os independent newline

        // connect to socket
        try (
            Socket socket = new Socket(args[0], portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverResponse = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String fromServer;
            String fromUser;
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            // communicate with server
            while ((fromServer = serverResponse.readLine()) != null) {
                fromServer = fromServer.replaceAll(";;;", newline); // split string to newline in client, multilines from server messes things up...
                
                System.out.println(fromServer);
                
                if (!fromServer.equals("Weapon chosen. Waiting for other players.")) {
                    fromUser = input.readLine();
                    if (fromUser.equalsIgnoreCase("quit") || fromUser.equalsIgnoreCase("exit") || fromServer.equalsIgnoreCase("Bye.")) { 
                        System.out.println("Bye, see you again!");
                        System.exit(0);
                    }
                    if (fromUser != null) {
                        out.println(fromUser); // send user input to socket
                    }
                }
                else { // very ugly but sort of functional, wait for other users to choose weapon
                    int sleepTime = 5;
                    while (sleepTime > 0) { 
                        System.out.println(sleepTime + "...");
                        try {
                            Thread.sleep(1000);
                            sleepTime--;
                        } catch (InterruptedException e) {
                            System.out.println("Thread error: " + e);
                        }
                    }
                    System.out.println("Battle begins!");
                    out.println("start"); // can be any string, not a pretty solution but not sure how else to make this work (will make the state move forward)...
                }
            }
            System.out.println("You waited too long, please reconnect");
        } catch (UnknownHostException e) {
            System.err.println("Server at " + args[0] + " not found.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + args[0]);
            System.exit(1);
        } 
    }
}