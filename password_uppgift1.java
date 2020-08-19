import java.util.Scanner;

public class password_uppgift1 {
    public static void main(String[] args) {
        int numberOfAttempts = 0;
        int maxAttempts = 3; // max number of attempts
        String setPass = "1234"; // the secure password
        String writtenPass; // declare the input string
        Scanner input = new Scanner(System.in); // init scanner
        System.out.println("Enter password:");
        while (numberOfAttempts < maxAttempts) { // retry until maxattemps is reached
            writtenPass = input.nextLine(); // read input
            if (writtenPass.equals(setPass)) { // compare string
                exit(input, "Rätt lösenord.");
            }
            else {
                System.out.println("Fel lösenord, försök igen.");
                numberOfAttempts++; // inc. attempts
            }
        }
        exit(input, "För många försök.");
    }    

    private static void exit(Scanner input, String text) {
        System.out.println(text); // exit message
        input.close(); // close input
        System.exit(0); // graceful exit
    }
}