import java.io.*;
import java.net.*;
 
public class Client {
    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1";
        int portNumber = 6666;
        String newline = System.getProperty("line.separator"); // os independent newline

        try (
            Socket socket = new Socket(host, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverResponse = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;
            
            while ((fromServer = serverResponse.readLine()) != null) {
                fromServer = fromServer.replaceAll(";;;", newline); // split string to newline in client, multilines from server messes things up...
                System.out.println(fromServer);
                
                fromUser = input.readLine();
                if (fromUser.equalsIgnoreCase("quit") || fromUser.equalsIgnoreCase("exit")) {
                    break;
                }
                if (fromUser != null) {
                    out.println(fromUser); // send user input to socket
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