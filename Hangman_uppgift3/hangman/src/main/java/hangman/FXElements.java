package hangman;

import java.util.ArrayList;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class FXElements {
    static int gcWidth = 300;
    static int gcHeight = 300;

    // icons for btns
    Image newGameImg = new Image(getClass().getResourceAsStream("res/dice.png"));
    Image guessWordImg = new Image(getClass().getResourceAsStream("res/keyboard.png"));
    
    Button newGameBtn = new Button("New game", new ImageView(newGameImg));
    Button guessWordBtn = new Button("Guess whole word", new ImageView(guessWordImg));

    // containers
    GridPane gridpane = new GridPane();
    HBox btnBox = new HBox(8); // spacing = 8
    HBox bottomBox = new HBox(80);

    TextInputDialog wordDialog = new TextInputDialog(); 

    static Canvas canvas = new Canvas(gcWidth, gcHeight);
    static GraphicsContext gc = canvas.getGraphicsContext2D();
    static TextFlow hiddenWordFlow = new TextFlow();
    static HBox scoreBox = new HBox(8);
    static Words words = new Words();
    Insets gridPadding = new Insets(20.0, 20.0, 20.0, 20.0);

    public void buildElements() {
        gridpane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        gridpane.setPadding(gridPadding);
        gridpane.setVgap(30);
        wordDialog.setHeaderText("Enter word");

        setListeners();

        btnBox.getChildren().addAll(newGameBtn, guessWordBtn);
        bottomBox.getChildren().addAll(canvas, FXElements.scoreBox);
        gridpane.add(btnBox, 0, 1); // column, row
        gridpane.add(words.alphabet, 0, 2);
        gridpane.add(FXElements.hiddenWordFlow, 0, 3);
        gridpane.add(bottomBox, 0, 4);
        newGame(words);
    }

    public static void drawHead() {
        // head
        gc.strokeOval(110, 14, 40, 40);

        // eyes
        gc.fillOval(120, 30, 5, 5);
        gc.fillOval(130, 30, 5, 5);

        // mouth
        gc.strokeLine(122, 42, 132, 42);

        gc.fill();
        gc.stroke();
    }

    public static void drawTorso() {
        gc.strokeOval(110, 54, 40, 80);
        gc.stroke();
    }

    private static void drawGallows() {
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.strokeLine(128, 14, 128, 1);
        gc.strokeLine(128, 1, 170, 1);
        gc.strokeLine(170, 1, 170, 200);
        gc.stroke();
        gc.fill();
    }

    private static void drawLegs() {
        gc.strokeLine(120, 130, 120, 160);
        gc.strokeLine(140, 130, 140, 162);
        gc.stroke();
    }

    private static void drawArms() {
        gc.strokeArc(101, 70, 30, 30, 100, 120, ArcType.OPEN);
        gc.strokeArc(128, 70, 30, 30, -30, 110, ArcType.OPEN);

        // dead eyes
        gc.strokeLine(120, 32, 126, 32);
        gc.strokeLine(123, 28, 123, 36);
        gc.strokeLine(130, 32, 136, 32);
        gc.strokeLine(133, 28, 133, 36);

        gc.stroke();
    }

    public static void drawParts(int livesCount, Words words) {
        switch (livesCount) {
            case 4:
                drawGallows();
                break;
            case 3:
                drawHead();
                break;
            case 2:
                drawTorso();
                break;
            case 1:
                drawLegs();
                break;
            case 0:
                drawArms();
                words.styleGuesses(); // increase fontsize
                String secretCombined = words.getSecretAsString();
                Alert newGameAlert = new Alert(AlertType.CONFIRMATION, "You died, the word was \"" + secretCombined +"\". Play again?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                Optional<ButtonType> result = newGameAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.YES) { 
                    newGame(words);
                }
                break;
        }
    }

    // check length of secret word and build line with "_"-characters
    private static void buildHidden(Words words) {
        ArrayList<String> secretWord = words.getRandom(false);
        for (int i = 0; i < secretWord.size(); i++) {
            UserData.charsCorrect.add("_"); // build default guess list
            UserData.charsCorrect.add(" "); // setting margin between text does not seem to be possible, adding
                                            // blankspace instead
            FXElements.hiddenWordFlow.getChildren().add(new Text("_")); // display _
            FXElements.hiddenWordFlow.getChildren().add(new Text(" "));
        }
    }

    // show alphabet
    private static void buildAlphabet(Words words) {
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
                if (UserData.livesCount > 0) {
                    Optional<String> result = wordDialog.showAndWait();
                    if (result.isPresent()) { // = ok was pressed
                        words.checkSecretWord(wordDialog.getEditor().getText().toUpperCase());
                    }
                }
            }
        });
        // key press listener, grey out and check for match
        gridpane.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent key) {
                if (UserData.livesCount > 0) {
                    String letter = key.getText().toUpperCase();
                    words.styleAlphabet(letter);
                } else {
                    System.out.println("YOU DEAD!");
                }
            }
        });
    }

    static void newGame(Words words) {
        boolean newGame = true;
        UserData.livesCount = UserData.startLives;
        UserData.charsCorrect = new ArrayList<String>();

        // clear and rebuild
        words.alphabet.getChildren().clear();
        hiddenWordFlow.getChildren().clear();
        scoreBox.getChildren().clear();
        gc.clearRect(0, 0, gcWidth, gcHeight);

        words.getRandom(newGame); // get random word and set as secret
        buildAlphabet(words); // show alphabet
        buildHidden(words); // show "_"-characters
        words.updateLives();        
        words.styleGuesses(); // increase fontsize
        words.styleAlphabet("");
    }
}