import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// based on the knock knock tutorial from Oracle, https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
public class Protocol {
    private static final int START = 0;
    private static final int ENTERNAME = 1;
    private static final int WEAPONCHOSEN = 2;
    private static final int BATTLE = 3;
    private static final int REMATCH = 4;
 
    private int state = START;
    
    private HashSet<String> users = new HashSet<String>();
    private HashMap<String, Integer> scoreboard = new HashMap<String, Integer>();
    private Map<String, Integer> sortedScoreboard = sortByValue(scoreboard); 
    private HashMap<String, Integer> battleground = new HashMap<String, Integer>();

    // sort scoreboard by score, from https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> scoreboard) 
    { 
        // Create a list from elements of HashMap 
        List<Map.Entry<String, Integer> > list = new LinkedList<Map.Entry<String, Integer> >(scoreboard.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() { 
            public int compare(Map.Entry<String, Integer> o1,  
                               Map.Entry<String, Integer> o2) 
            { 
                return (o1.getValue()).compareTo(o2.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>(); 
        for (Map.Entry<String, Integer> userScore : list) { 
            temp.put(userScore.getKey(), userScore.getValue()); 
        } 
        return temp; 
    } 

    public String processInput(String input) {
        if (input != null) {
            input = input.trim();
        }
        
        char rock = 0x270A;
        char paper = 0x270B;
        char scissors = 0x270C;

        String RPC = Character.toString(rock) + " " + Character.toString(paper) + " " + Character.toString(scissors); // Unicode RPC symbols, unlikely to work under Windows
        String output = "";
        String buffer = ""; // need a buffer to be able to print multilines from loop
        String name = "";

        if (state == START) {
            buffer = "Welcome to MMO " + RPC + " 2020!;;;";
            buffer += users.size() + " users online:;;;";
            // TODO: if users size == 0, option to play against computer
            
            // print all online users
            for (String connectedName : users) {
                buffer += connectedName + ";;;";
            }
            if (sortedScoreboard.size() > 0) {
                buffer += "Scoreboard:" + ";;;";
                for (Map.Entry<String, Integer> score : sortedScoreboard.entrySet()) { 
                    buffer += score.getKey() + ": " + score.getValue()  + " points;;;"; 
                }
            }
            buffer += "Your name:";
            output = buffer;
            state = ENTERNAME;
        } else if (state == ENTERNAME) {
            if (input.length() > 0) {
                name = input;                
                synchronized (users) { // add user thread safely
                    if (name != null && name.length() > 0 && !users.contains(name)) { // add to hashmap if username doesn't exist
                        users.add(name);
                        output = "User " + name + " registered. " + 
                        "Let's play, choose weapon - Rock [0], Paper [1] or Scissors [2]";
                        state = WEAPONCHOSEN;
                    }
                    else {
                        output = "Not a valid name, try again.";   
                        state = ENTERNAME;      
                    }
                }
            } else {
                output = "Name can not be empty, try again.";
                state = ENTERNAME;
            }
        } else if (state == WEAPONCHOSEN) {
            if (input.equalsIgnoreCase("rock") || input.equals("0")) {
                output = "Rock selected. Waiting for other players.";
                // loop until battleground size == users (or 10? seconds) => battle
            } else {
                output = "Not a valid weapon, choose between Rock [0], Paper [1] or Scissors [2]";
                state = WEAPONCHOSEN;
            }
        } else if (state == BATTLE) {
            // add user and weapon to battleground
            // calc scores
            // show scores
            // clear battleground
            // ask to play again
            state = REMATCH;
        } else if (state == REMATCH) {
            if (input.equalsIgnoreCase("y")) {
                output = "Let's play, choose weapon - Rock [0], Paper [1] or Scissors [2]"; 
                state = WEAPONCHOSEN;
            } else {
                users.remove(name);
                output = "Bye.";
                state = START;
            }
        }
        return output;
    }
}