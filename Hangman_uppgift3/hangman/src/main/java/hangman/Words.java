package hangman;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class Words {
    final TextFlow alphabet = new TextFlow();
    PrivSecretWord privSecretWord = new PrivSecretWord(); // inst here or a new word will be generated each time getRandom() is called
        
    // Använd er av minst en Class, varav en ska vara “private” class med själva ordet som ska gissas
    private class PrivSecretWord {
        ArrayList<String> secretWord = new ArrayList<String>();
        private ArrayList<String> getRandomWord() {
            if (secretWord.size() == 0) {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("https://random-word-api.herokuapp.com/word?number=1")))
                .GET()
                .build();

                try{
                    HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                    String sSecretWord = response.body().replaceAll("[^a-zA-Z]", "").toUpperCase();
                    for (char c : sSecretWord.toCharArray()) {
                        secretWord.add(Character.toString(c));
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            System.out.println(secretWord);
            return secretWord;
        }
    }

    public ArrayList<String> getRandom(boolean newGame) {
        if (newGame) {
            privSecretWord = new PrivSecretWord();
        }
        ArrayList<String> secretWord = privSecretWord.getRandomWord();
        return secretWord;
    }

    public void styleAlphabet(String enteredLetter) {
        boolean doCheck = false;
        for (Node child : alphabet.getChildren()){
            if (child instanceof Text) {
                String letter = ((Text)child).getText();
                if (letter.equals(enteredLetter) && !child.getStyleClass().contains("charUsed")) { // only do if not already used
                    rotate(child);
                    child.getStyleClass().add("charUsed");
                    doCheck = true;
                }
                else {
                    child.getStyleClass().add("charUnused");
                }
            }
        }
        if (doCheck) {
            checkSecretWord(enteredLetter);
        }
    }

    // increase font size of "_"-characters (and correct guesses)
    public void styleGuessesAndScore() {
        for (Node child : FXElements.hiddenWordFlow.getChildren()) {
            child.getStyleClass().add("underline");
        }
        for (Node child : FXElements.scoreFlow.getChildren()) {
            child.getStyleClass().add("score");
        }
    }

    public void checkSecretWord(String enteredLetter) {
        ArrayList<String> secretWord = getRandom(false);
        boolean hit = false;

        if (enteredLetter.length() == 1) {
            for (int i=0; i< secretWord.size(); i++){
                if (secretWord.get(i).equals(enteredLetter)) {
                    hit = true;
                    UserData.charsCorrect.set(i*2, secretWord.get(i)); // * 2 because of whitespace
                }
            }
        }
        else if (enteredLetter.length() > 1) { // whole word guessed
            StringBuffer sb = new StringBuffer();
            for (String s : secretWord) {
               sb.append(s);
            }
            String secretCombined = sb.toString();
            System.out.println("secretcombined " + secretCombined);
            System.out.println("entered " + enteredLetter);
            if (enteredLetter.equals(secretCombined)) {
                System.out.println("Match");
                hit = true;
                for (int i=0; i< secretWord.size(); i++){
                    UserData.charsCorrect.set(i*2, secretWord.get(i)); // * 2 because of whitespace
                }
            }
        }

        FXElements.hiddenWordFlow.getChildren().clear();
        for (String s : UserData.charsCorrect) {
            FXElements.hiddenWordFlow.getChildren().add(new Text(s)); // adding does not, can't be used in loop 
        }
        setScore(hit);
        styleGuessesAndScore();
    }

    // update score text
    private void setScore(boolean hit) {
        if (!UserData.charsCorrect.contains("_") && hit) {
            System.out.println("Victory!");                       
        }
        else if (!hit) {
            UserData.livesCount--;
            FXElements.scoreFlow.getChildren().clear();
            FXElements.scoreFlow.getChildren().add(new Text("Attemps left: " + UserData.livesCount));
            FXElements.drawHead();
        }
    }

    private void rotate(Node letter) {
        RotateTransition transition = new RotateTransition(Duration.millis(100), letter);
        transition.setByAngle(360);
        transition.setCycleCount(3);
        transition.play();  
    }
}