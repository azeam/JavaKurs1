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

    private String glName = "";
    private String glWeapon = "";
    private int glScore = 0;

    // sort scoreboard by score, from
    // https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> scoreboard) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(scoreboard.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
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

    private String checkOpponents(HashMap<String, Integer> battleground, String weaponName, int weaponType, HashMap<String, Integer> scoreboard) {
        String output = "";
        
        for(Map.Entry<String, Integer> userResult : battleground.entrySet()) {
            String opName = userResult.getKey();
            int opWeapon = userResult.getValue();
            String opWeaponName = getWeaponName(opWeapon);
            
            if (!glName.equals(opName)) {
                output += opName + " chose " + opWeaponName + ";;;";
                switch (weaponType) {
                    case 0: // user rock
                        switch (opWeapon) {
                            case 1: // op paper
                                output += opName + " beats you (-1 points);;;";
                                glScore--;
                                break;
                            case 2: // op scissors
                                output += "you beat " + opName + " (+1 points);;;";
                                glScore++;
                                break;
                        }
                        break;
                    case 1: // user paper
                        switch (opWeapon) {
                            case 0: // op rock
                                output += "you beat " + opName + " (+1 points);;;";
                                glScore++;
                                break;
                            case 2: // op scissors
                                output += opName + " beats you (-1 points);;;";
                                glScore--;
                                break;
                        }
                        break;
                    case 2: // user scissors
                        switch (opWeapon) {
                            case 0: // op rock
                                output += opName + " beats you (-1 points);;;";
                                glScore--;
                                break;
                            case 1: // op paper
                                output += "you beat " + opName + " (+1 points);;;";
                                glScore++;
                                break;
                        }
                        break;
                    default: // same, draw
                        output += "you draw against " + opName + " (0 points);;;";
                }
            }
        }
        return output;
    }

    private String calcScore(HashMap<String, Integer> battleground, HashMap<String, Integer> scoreboard) {
        String output = "";
        String weaponName = "";
        int weapon = - 1;
        for(Map.Entry<String, Integer> userResult : battleground.entrySet()) {
            if (userResult.getKey().equals(glName)) {
                weapon = userResult.getValue();
            }
        }
        weaponName = getWeaponName(weapon); 
        output += "You chose " + weaponName + ";;;";
        output += checkOpponents(battleground, weaponName, weapon, scoreboard);

        output += "Score: " + glScore + " points.;;;";
        synchronized (scoreboard) {
            scoreboard.put(glName, scoreboard.get(glName) + glScore);
        }
        glScore = 0; // reset score
        output += "Play again? [Y]/[n]"; 
        return output; 
    }

    private String getWeaponName(int weapon) {
        String weaponName = "";
        switch(weapon) {
            case 0:
                weaponName = "Rock"; 
                break;
            case 1:
                weaponName = "Paper";
                break;
            case 2:
                weaponName = "Scissors";
                break;
            default: 
                weaponName = "Nothing";
                break; 
        }
        return weaponName;
    }

    public String processInput(String input, HashSet<String> users, HashMap<String, Integer> scoreboard,
            HashMap<String, Integer> battleground) {
        if (input != null) {
            input = input.trim();
        }
        Map<String, Integer> sortedScoreboard = sortByValue(scoreboard);

        char rock = 0x270A;
        char paper = 0x270B;
        char scissors = 0x270C;

        String RPC = Character.toString(rock) + " " + Character.toString(paper) + " " + Character.toString(scissors); // Unicode
                                                                                                                      // RPC
                                                                                                                      // symbols,
                                                                                                                      // unlikely
                                                                                                                      // to
                                                                                                                      // work
                                                                                                                      // under
                                                                                                                      // Windows
        String output = "";
        String buffer = ""; // need a buffer to be able to print multilines from loop

        if (state == START) {
            buffer = "Welcome to MMO " + RPC + " 2020!;;;";
            buffer += "Quit the game at any time by writing \"quit\";;;";
            buffer += users.size() + " users online:;;;";
            // TODO: if users size == 0, option to play against computer

            // print all online users
            for (String connectedName : users) {
                buffer += connectedName + ";;;";
            }
            if (sortedScoreboard.size() > 0) {
                buffer += "Scoreboard:" + ";;;";
                for (Map.Entry<String, Integer> score : sortedScoreboard.entrySet()) {
                    buffer += score.getKey() + ": " + score.getValue() + " points;;;";
                }
            }
            buffer += "Your name:";
            output = buffer;
            state = ENTERNAME;
        } else if (state == ENTERNAME) {
            if (input.length() > 0) {
                glName = input;
                synchronized (users) { // add user thread safely
                    if (glName != null && glName.length() > 0 && !users.contains(glName)) { // add to hashmap if username
                                                                                      // doesn't exist
                        users.add(glName);
                        output = "User " + glName + " registered. "
                                + "Let's play, choose weapon - Rock [0], Paper [1] or Scissors [2]";
                        state = WEAPONCHOSEN;
                    } else {
                        output = "Not a valid name, try again.";
                        state = ENTERNAME;
                    }
                }
            } else {
                output = "Name can not be empty, try again.";
                state = ENTERNAME;
            }
        } else if (state == WEAPONCHOSEN) {
            int weapon = -1;
            switch (input.toLowerCase()) {
                case "rock":
                case "0":
                    weapon = 0;
                    break;
                case "paper":
                case "1":
                    weapon = 1;
                    break;
                case "scissors":
                case "2":
                    weapon = 2;
                    break;
            }
            if (weapon == -1) {
                output = "Not a valid weapon, choose between Rock [0], Paper [1] or Scissors [2]";
                state = WEAPONCHOSEN;
            } else {
                synchronized (battleground) {
                    battleground.put(glName, weapon);
                }
                output = "Weapon chosen. Waiting for other players.";
                
                state = BATTLE;
            }
        } else if (state == BATTLE) {
            output = calcScore(battleground, scoreboard);
            state = REMATCH;
        } else if (state == REMATCH) {
            if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
                synchronized (users) {
                    users.remove(glName);
                }
                synchronized (battleground) {
                    battleground.remove(glName);
                }
                output = "Bye.";
                state = START;
            } else {             
                output = "Let's play, choose weapon - Rock [0], Paper [1] or Scissors [2]"; 
                state = WEAPONCHOSEN;                
            }
        }
        return output;
    }
}