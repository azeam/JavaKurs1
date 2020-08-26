import java.io.*;
import java.net.*;
 
public class Client {
    public static void main(String[] args) throws IOException {
        String host = "213.64.134.25";
        int portNumber = 6666;
        String newline = System.getProperty("line.separator"); // os independent newline

        try (
            Socket socket = new Socket(host, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverResponse = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String fromServer;
            String fromUser;
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            while ((fromServer = serverResponse.readLine()) != null) {
                fromServer = fromServer.replaceAll(";;;", newline); // split string to newline in client, multilines from server messes things up...
                
                System.out.println(fromServer);
                
                if (!fromServer.equals("Weapon chosen. Waiting for other players.")) {
                    fromUser = input.readLine();
                    if (fromUser.equalsIgnoreCase("quit") || fromUser.equalsIgnoreCase("exit") || fromServer.equalsIgnoreCase("Bye.")) { 
                        break;
                    }
                    if (fromUser != null) {
                        out.println(fromUser); // send user input to socket
                    }
                }
                else {
                    int sleepTime = 5;
                    while (sleepTime > 0 && fromServer.equals("Weapon chosen. Waiting for other players.")) { 
                        System.out.println(sleepTime + "...");
                        try {
                            Thread.sleep(1000);
                            sleepTime--;
                        } catch (InterruptedException e) {
                            System.out.println("Thread error: " + e);
                        }
                    }
                    System.out.println("Battle begins!");
                    out.println("start"); // can be any string, not a pretty solution but not sure how else to make this work...
                    sleepTime = 0; // reset counter for next round    
                }
            }
            
            
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host);
            System.exit(1);
        }
    }
}