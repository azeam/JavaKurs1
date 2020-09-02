package hangman;

import java.util.ArrayList;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class FXElements {
    static int gcWidth = 300;
    static int gcHeight = 300;

    Image newGameImg = new Image(getClass().getResourceAsStream("res/dice.png"));
    Image guessWordImg = new Image(getClass().getResourceAsStream("res/keyboard.png"));
    
    Button newGameBtn = new Button("New game", new ImageView(newGameImg));
    Button guessWordBtn = new Button("Guess whole word", new ImageView(guessWordImg));

    GridPane gridpane = new GridPane();
    HBox btnBox = new HBox(8); // spacing = 8
    HBox bottomBox = new HBox(80);

    TextInputDialog wordDialog = new TextInputDialog(); 

    static Canvas canvas = new Canvas(gcWidth, gcHeight);
    static GraphicsContext gc = canvas.getGraphicsContext2D();

    Insets gridPadding = new Insets(20.0, 20.0, 20.0, 20.0);
    static TextFlow hiddenWordFlow = new TextFlow();
    static TextFlow scoreFlow = new TextFlow();

    static Words words = new Words();

    public void buildElements() { 
        UserData.livesCount = UserData.startLives;

        gridpane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());        
        gridpane.setPadding(gridPadding);
        gridpane.setVgap(30);
        wordDialog.setHeaderText("Enter word");
        
        setListeners();

        scoreFlow.getChildren().add(new Text("Attemps left: " + UserData.livesCount));
        btnBox.getChildren().addAll(newGameBtn, guessWordBtn);
        bottomBox.getChildren().addAll(FXElements.scoreFlow, canvas);
        gridpane.add(btnBox, 0, 1); // column, row
        gridpane.add(words.alphabet, 0, 2); 
        gridpane.add(FXElements.hiddenWordFlow, 0, 3);
        gridpane.add(bottomBox, 0, 4);
        newGame(words);
    }

    public static void drawHead() {
        // head
        gc.setFill(Color.BLACK);
        gc.fillOval(10, 14, 40, 40);
        gc.fill();

        // eyes
        gc.setFill(Color.WHITE);
        gc.fillOval(20, 30, 5, 5);
        gc.fill();
        gc.stroke();

        gc.setFill(Color.WHITE);
        gc.fillOval(30, 30, 5, 5);
        gc.fill();
        gc.stroke();

        // mouth
        gc.beginPath();
        gc.setFill(Color.WHITE);
        gc.rect(22, 42, 15, 2);
        gc.fill();
    }

    private void drawGallows() {
        gc.beginPath();
        gc.setFill(Color.BLACK);
        gc.rect(30, 1, 4, 15);
        gc.fill();
    }

    // check length of secret word and build line with "_"-characters
    private void buildHidden(Words words) {
        ArrayList<String> secretWord = words.getRandom(false);
        for (int i = 0; i < secretWord.size(); i++) {
            UserData.charsCorrect.add("_");
            UserData.charsCorrect.add(" "); // setting margin between text does not seem to be possible, adding blankspace instead
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
    }

    private void setListeners() {
        newGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newGame(words);
            }
        });
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
                if (UserData.livesCount > 0) {
                    String letter = key.getText().toUpperCase();
                    words.styleAlphabet(letter);
                }
                else {
                    System.out.println("YOU DEAD!");
                }
            }
        });
    }

    private void newGame(Words words) {
        boolean newGame = true;
        UserData.livesCount = UserData.startLives;
        UserData.charsCorrect = new ArrayList<String>();
        
        words.alphabet.getChildren().clear();
        hiddenWordFlow.getChildren().clear();
        scoreFlow.getChildren().clear();
        gc.clearRect(0, 0, gcWidth, gcHeight);

        words.getRandom(newGame); // get random word and set as secret
        buildAlphabet(words); // show alphabet
        buildHidden(words); // show "_"-characters
        scoreFlow.getChildren().add(new Text("Attemps left: " + UserData.livesCount)); // show attemps left
        
        words.styleGuessesAndScore(); // increase fontsize
        words.styleAlphabet("");

        drawGallows();
    }
}