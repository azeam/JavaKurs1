import java.util.HashMap;
import java.util.Map;

public class App {
    public static void main(String[] args) throws Exception {
        Map<Integer,Character> map = new HashMap<Integer,Character>();
        
        for(int i=0; i < 26; i++) {
            map.put(i,(char) (i + 97)); // map char to decimal value and add to hashmap with index 0-26 (only for lowercase letters = decimal values 97-122)
        }

        String str = "welcome to java programming"; // cipher is -10

        for(int i=0; i < str.length();i++){
            int input = (int) str.charAt(i); // map char in string to decimal value
            if (input == 32) {
                System.out.println(" "); // print space instead of null
            }
            else if (input >= 113) { // 122 -10 + 1
                System.out.println(map.get((input - 113))); 
            }
            else { // bigger than 10, start from end of alphabet
                System.out.println(map.get((input - 87)));
            }
        }
    }
}
