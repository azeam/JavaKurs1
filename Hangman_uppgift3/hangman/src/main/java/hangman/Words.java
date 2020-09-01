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
        public ArrayList<String> getRandomWord() {
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

    private void rotate(Node letter) {
        RotateTransition transition = new RotateTransition(Duration.millis(100), letter);
        transition.setByAngle(360);
        transition.setCycleCount(3);
        transition.play();  
    }

    public void styleAlphabet(String enteredLetter, boolean clear) {
        boolean doCheck = false;
        enteredLetter = enteredLetter.toUpperCase();
        for (Node child : alphabet.getChildren()){
            if (child instanceof Text) {
                String letter = ((Text)child).getText();
                if (!clear && letter.equals(enteredLetter) && !child.getStyleClass().contains("charUsed")) { // only do if not already used
                    rotate(child);
                    child.getStyleClass().add("charUsed");
                    doCheck = true;
                }
                else if (clear) {
                    child.getStyleClass().remove("charUsed");
                    child.getStyleClass().add("charUnused");
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
    public void styleGuesses() {
        for (Node child : FXElements.hiddenWordFlow.getChildren()) {
            child.getStyleClass().add("underline");
        }
    }

    private void checkSecretWord(String enteredLetter) {
        ArrayList<String> secretWord = getRandom(false);

        for (int i=0; i< secretWord.size(); i++){
            if (ArrayLists.charsCorrect.contains(secretWord.get(i))){
                ArrayLists.charsCorrect.set(i, secretWord.get(i));
            }
            if (secretWord.get(i).equals(enteredLetter)) {
                ArrayLists.charsCorrect.set(i, secretWord.get(i));
            }
        }

        FXElements.hiddenWordFlow.getChildren().clear();
        for (String s : ArrayLists.charsCorrect) {
            FXElements.hiddenWordFlow.getChildren().add(new Text(s)); // adding does not, can't be used in loop 
        }

        styleGuesses();
    }
}