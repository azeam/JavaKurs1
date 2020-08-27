import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
 
public class Server {
    public static void main(String[] args) throws IOException {
        int portNumber = 6666; // > 1023 does not require root permission
        ServerSocket serverSocket = null;

        // these maps need to be accessible to all users between threads
        HashSet<String> users = new HashSet<String>();
        HashMap<String, Integer> scoreboard = new HashMap<String, Integer>();
        HashMap<String, Integer> battleground = new HashMap<String, Integer>();

        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(30000); // kick out user after 30 seconds of inactivity

                // spawn a new thread for each client, this way multiple clients can connect
                new ServerThread(clientSocket, users, scoreboard, battleground).start();
            } catch (IOException e) {
                System.out.println("I/O error (Server): " + e);
            }
        }
    }
}

