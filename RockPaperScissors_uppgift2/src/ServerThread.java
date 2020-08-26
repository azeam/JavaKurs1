import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {
        protected Socket socket;
            
        // get client socket
        public ServerThread(Socket clientSocket) {
            this.socket = clientSocket;
        }
    
        // new thread 
        public void run() {
            String input, output;
            try (
                // 
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                // start communication protocol between server and client
                Protocol communicate = new Protocol();
                output = communicate.processInput(null);
                
                out.println(output); // output from server
                
                while ((input = in.readLine()) != null) { // read client input
                    output = communicate.processInput(input); // get server response
                    out.println(output);
                    if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) { // exit loop on quit
                        break;
                    }
                }
            }
            catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
    }
}