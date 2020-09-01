package hangman;

import java.util.ArrayList;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class FXElements {
    Button newGameBtn = new Button();
    Button guessWordBtn = new Button();
    GridPane gridpane = new GridPane();
    HBox btnBox = new HBox(8); // spacing = 8
    
    Insets gridPadding = new Insets(20.0, 20.0, 20.0, 20.0);
    public static TextFlow hiddenWordFlow = new TextFlow();
    public static TextFlow scoreFlow = new TextFlow();

    

    public void buildElements() { 
        final Words words = new Words();
        boolean newGame = true;
        UserData.livesCount = UserData.startLives;

        gridpane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());        
        gridpane.setPadding(gridPadding);
        
        gridpane.setVgap(30);
        newGameBtn.setText("New game (random word)");
        newGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newGame(words);
            }
        });

        final TextInputDialog wordDialog = new TextInputDialog(); 
        wordDialog.setHeaderText("Enter word"); 

        guessWordBtn.setText("Guess whole word");
        guessWordBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Optional<String> result = wordDialog.showAndWait();
                if (result.isPresent()) { // = ok was pressed
                    words.checkSecretWord(wordDialog.getEditor().getText().toUpperCase());
                } 
            }
        });
        
        // key press listener, grey out and check for match 
        gridpane.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent key) {
                boolean clear = false;
                if (UserData.livesCount > 0) {
                    String letter = key.getText().toUpperCase();
                    words.styleAlphabet(letter, clear);
                }
                else {
                    System.out.println("YOU DEAD!");
                }
            }
        });

        scoreFlow.getChildren().add(new Text("Attemps left: " + UserData.livesCount));
        btnBox.getChildren().addAll(newGameBtn, guessWordBtn);
        gridpane.add(btnBox, 0, 1); // column, row
        gridpane.add(words.alphabet, 0, 2); 
        gridpane.add(FXElements.hiddenWordFlow, 0, 3);
        gridpane.add(FXElements.scoreFlow, 0, 4);
        words.getRandom(newGame); // get random word and set as secret
        buildAlphabet(words); // show alphabet
        buildHidden(words); // show "_"-characters
        words.styleGuessesAndScore(); // increase fontsize

    }

    // check length of secret word and build line with "_"-characters
    private void buildHidden(Words words) {
        ArrayList<String> secretWord = words.getRandom(false);
        for (int i = 0; i < secretWord.size(); i++) {
            UserData.charsCorrect.add("_");
            UserData.charsCorrect.add(" ");
            FXElements.hiddenWordFlow.getChildren().add(new Text("_"));
            FXElements.hiddenWordFlow.getChildren().add(new Text(" "));
        }
    }

    // show alphabet
    private void buildAlphabet(Words words) {
        for (int i = 65; i < 91; i++) {
            String letter = Character.toString(i);
            words.alphabet.getChildren().add(new Text(letter));
        }
        words.styleAlphabet("", true); // clear used chars
    }

    private void newGame(Words words) {
        boolean newGame = true;
        UserData.livesCount = UserData.startLives;
        FXElements.hiddenWordFlow.getChildren().clear();
        UserData.charsCorrect = new ArrayList<String>();
        words.getRandom(newGame); // generate new word
        buildHidden(words);
        
        scoreFlow.getChildren().clear();
        scoreFlow.getChildren().add(new Text("Attemps left: " + UserData.livesCount));
        
        words.styleGuessesAndScore();
        words.styleAlphabet("", newGame);
    }
}