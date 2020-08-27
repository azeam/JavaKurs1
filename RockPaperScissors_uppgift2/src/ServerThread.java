import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

public class ServerThread extends Thread {
        protected Socket socket;
        private HashSet<String> users;
        private HashMap<String, Integer> scoreboard;
        private HashMap<String, Integer> battleground;
            
        // get socket and serverwide maps 
        public ServerThread(Socket clientSocket, HashSet<String> users, HashMap<String, Integer> scoreboard, HashMap<String, Integer> battleground) {
            this.socket = clientSocket;
            this.users = users;
            this.scoreboard = scoreboard;
            this.battleground = battleground;
        }
    
        // new thread 
        public void run() {
            String input, output, threadUser = "";
            try (
                // 
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                // start communication protocol between server and client
                Protocol communicate = new Protocol();
                output = communicate.processInput(null, users, scoreboard, battleground);
                
                out.println(output); // output from server
                while ((input = in.readLine()) != null) { // read client input
                    output = communicate.processInput(input, users, scoreboard, battleground); // get server response
                    if (output.contains(" registered.")) {
                        threadUser = output.split(" registered")[0];
                        threadUser = threadUser.split("User")[1].trim();
                    }
                    if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit") || output.equals("Bye.")) { // exit loop on quit
                        out.println(output); // send bye message to client before exit
                        break;
                    }
                    out.println(output);
                }
            }
            catch (IOException e) {
                System.out.println("I/O error (Thread): " + e);
                users.remove(threadUser); // if user quits because of timeout, clear users list 
                battleground.remove(threadUser); // if user quits because of timeout, clear battleground 
            } 
    }
}