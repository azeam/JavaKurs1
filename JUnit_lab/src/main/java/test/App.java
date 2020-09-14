package test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Hello world!
 */
public final class App {
    App() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
    public int add(int first, int second) {
        return first + second;
    }

    public int multiply(int first, int second) {
        return first * second;
    }

    public int divide(int first, int second) {
        return first / second;
    }

    public boolean palindrome(String word) {
        ArrayList<Character> toTest = new ArrayList<Character>();
        ArrayList<Character> rev = new ArrayList<Character>();
        for (char c : word.toCharArray()) {
            toTest.add(c);
            rev.add(c);
        }
        Collections.reverse(rev);
        return toTest.equals(rev) ? true : false;
    }
}
