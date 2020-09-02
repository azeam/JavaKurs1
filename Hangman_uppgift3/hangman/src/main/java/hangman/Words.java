package hangman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javafx.animation.RotateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class Words {
    final TextFlow alphabet = new TextFlow();
    PrivSecretWord privSecretWord = new PrivSecretWord(); // inst here or a new word will be generated each time
                                                          // getRandom() is called

    // "Använd er av minst en Class, varav en ska vara “private” class med själva
    // ordet som ska gissas"
    private class PrivSecretWord {
        ArrayList<String> secretWord = new ArrayList<String>();

        private ArrayList<String> getRandomWord() {
            if (secretWord.size() == 0) {
                // get random word from online API
                CloseableHttpClient httpClient = HttpClients.createDefault();
                try {
                    HttpGet request = new HttpGet("https://random-word-api.herokuapp.com/word?number=1");
                    CloseableHttpResponse response = httpClient.execute(request);
                    try {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            String result = EntityUtils.toString(entity);
                            String sSecretWord = result.replaceAll("[^a-zA-Z]", "").toUpperCase();
                            for (char c : sSecretWord.toCharArray()) {
                                secretWord.add(Character.toString(c));
                            }
                            EntityUtils.consume(entity);
                        }
                    } finally {
                        response.close();
                    }
                } catch (ClientProtocolException e) {
                    System.out.println("Error " + e.getMessage());
                } catch (IOException e) {
                    System.out.println("Error " + e.getMessage());
                } finally {
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        System.out.println("Error " + e.getMessage());
                    }
                }
            }
//          System.out.println(secretWord);
            return secretWord;
        }
    }

    // public access to the private class
    public ArrayList<String> getRandom(boolean newGame) {
        if (newGame) {
            privSecretWord = new PrivSecretWord();
        }
        ArrayList<String> secretWord = privSecretWord.getRandomWord();
        return secretWord;
    }

    // set large font size, and pass to check secret 
    public void styleAlphabet(String enteredLetter) {
        boolean doCheck = false;
        for (Node child : alphabet.getChildren()) {
            if (child instanceof Text) {
                String letter = ((Text) child).getText();
                if (letter.equals(enteredLetter) && !child.getStyleClass().contains("charUsed")) { // only do if not
                                                                                                   // already used
                    rotate(child);
                    child.getStyleClass().add("charUsed");
                    doCheck = true;
                } else {
                    child.getStyleClass().add("charUnused");
                }
            }
        }
        if (doCheck) {
            // if a new character is entered, check if it pertains to the secret word
            checkSecretWord(enteredLetter);
        }
    }

    // increase font size of "_"-characters (and correct guesses)
    public void styleGuesses() {
        for (Node child : FXElements.hiddenWordFlow.getChildren()) {
            child.getStyleClass().add("underline");
        }
    }

    // compare entered letter/word with secret
    public void checkSecretWord(String enteredLetter) {
        ArrayList<String> secretWord = getRandom(false);
        boolean hit = false;
        if (enteredLetter.length() == 1) {
            for (int i = 0; i < secretWord.size(); i++) {
                if (secretWord.get(i).equals(enteredLetter)) {
                    hit = true;
                    UserData.charsCorrect.set(i * 2, secretWord.get(i)); // * 2 because of whitespace
                }
            }
        } else if (enteredLetter.length() > 1) { // whole word guessed
            String secretCombined = getSecretAsString();
            if (enteredLetter.equals(secretCombined)) {
                hit = true;
                for (int i = 0; i < secretWord.size(); i++) {
                    UserData.charsCorrect.set(i * 2, secretWord.get(i)); // * 2 because of whitespace
                }
            }
        }

        // clear and rebuild the _-line
        FXElements.hiddenWordFlow.getChildren().clear();
        for (String s : UserData.charsCorrect) {
            FXElements.hiddenWordFlow.getChildren().add(new Text(s)); // adding does not, can't be used in loop
        }
        setScore(hit);
        styleGuesses();
    }

    // secret is stored string array because of javafx, rebuild to string for display
    String getSecretAsString() {
        ArrayList<String> secretWord = getRandom(false);
        StringBuffer sb = new StringBuffer();
        for (String s : secretWord) {
            sb.append(s);
        }
        String secretCombined = sb.toString();
        return secretCombined;
    }

    // update score text
    private void setScore(boolean hit) {
        if (!UserData.charsCorrect.contains("_") && hit) {
            styleGuesses(); // increase fontsize
            Alert newGameAlert = new Alert(AlertType.CONFIRMATION, "Congratulations, you won! Play again?",
                    ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            Optional<ButtonType> result = newGameAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                FXElements.newGame(this);
            }
        } else if (!hit) {
            UserData.livesCount--;
            updateLives();
            if (UserData.livesCount > 0) {
                rotate(FXElements.canvas);
            }
            FXElements.drawParts(UserData.livesCount, this);
        }
    }

    // clear and rebuild hearts list
    public void updateLives() {
        FXElements.scoreBox.getChildren().clear();
        ObservableList<Node> lives = FXCollections.observableArrayList();
        for (int i = 0; i < UserData.livesCount; i++) {
            lives.add(new ImageView(new Image(getClass().getResourceAsStream("res/heart2.png"))));    
        }
        FXElements.scoreBox.getChildren().addAll(lives);
    }

    // simple rotate animation
    private void rotate(Node letter) {
        RotateTransition transition = new RotateTransition(Duration.millis(100), letter);
        transition.setByAngle(360);
        transition.setCycleCount(3);
        transition.play();  
    }
}