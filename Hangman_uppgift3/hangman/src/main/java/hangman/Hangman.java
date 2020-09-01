package hangman;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
 
public class Hangman extends Application {
    
    


    public static void main(String[] args) {
        launch(args);
    }
    
    // Game status = Visar antalet gissningar och hur många spelaren har kvar.
    // Guess char= Låter spelaren gissa en bokstav
    // Guess word = låter spelaren gissa hela ordet

    // show hint (synonym etc.?)
    // select number of chars

    // anim, crying stick figure, cracking chair

    @Override
    public void start(Stage window) {
        window.setTitle("Hang the man!");
        final Words words = new Words();
        
        FXElements fxElements = new FXElements();
        
        boolean newGame = true;
        words.getRandom(newGame);
        fxElements.gridpane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());        
        fxElements.gridpane.setPadding(fxElements.gridPadding);
        fxElements.btn.setText("Get random word");
        fxElements.btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                boolean newGame = true;
                words.getRandom(newGame); // generate new word
            }
        });
        
        buildAlphabet(words);
        buildHidden(words);

        // key press listener, grey out and check for match 
        fxElements.gridpane.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent key) {
                boolean clear = false;
                words.styleAlphabet(key.getText(), clear);
            }
        });
        
        words.styleGuesses();
        fxElements.gridpane.add(words.alphabet, 0, 1); // column, row
        fxElements.gridpane.add(fxElements.btn, 0, 2);
        fxElements.gridpane.add(FXElements.hiddenWordFlow, 0, 3);
        window.setScene(new Scene(fxElements.gridpane));
        window.show();
    }

    

    // show alphabet
    private void buildAlphabet(Words words) {
        for (int i = 65; i < 91; i++) {
            String letter = Character.toString(i);
            words.alphabet.getChildren().add(new Text(letter));
        }
        words.styleAlphabet("", true); // clear used chars
    }

    // check length of secret word and build line with "_"-characters
    private void buildHidden(Words words) {
        ArrayList<String> secretWord = words.getRandom(false);
        for (int i = 0; i < secretWord.size(); i++) {
            ArrayLists.charsCorrect.add("_");
            ArrayLists.charsCorrect.add(" ");
            FXElements.hiddenWordFlow.getChildren().add(new Text("_"));
            FXElements.hiddenWordFlow.getChildren().add(new Text(" "));
        }
    }
    
}