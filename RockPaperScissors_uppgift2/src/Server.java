import java.net.*;
import java.io.*;
 
public class Server {
    public static void main(String[] args) throws IOException {
        int portNumber = 6666; // > 1000 does not require root permission
 
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                // new thread for each client, this way multiple clients can connect
                new ServerThread(clientSocket).start();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
        }
    }
}

