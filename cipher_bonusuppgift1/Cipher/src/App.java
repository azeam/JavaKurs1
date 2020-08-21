public class App {
    public static void main(String[] args) throws Exception {
        String str = "welcome to java programming"; // cipher is +10/-16
        char[] buffer = new char[str.length()];
        for(int i=0; i < str.length();i++){
            int input = (int) str.charAt(i); // map char in string to decimal value
            if (input == 32 || (input >= 97 && input <=122)) { // only print lowercase (ascii 97-122)
                // java switches can't use operators, using if/else
                if (input == 32) {
                    buffer[i] = ' ';
                    System.out.println(" "); // print space instead of null
                }
                else if (input >= 113) { // 97 + 16, too big to fit, start alphabet from the beginning
                    buffer[i] = (char) (input - 16);
                }
                else { // fits in alphabet, add 10
                    buffer[i] = (char) (input + 10);
                }
            }
        }
        String encoded = new String(buffer);
        System.out.println(encoded);
    }
}