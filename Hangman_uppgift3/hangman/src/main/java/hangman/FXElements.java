package hangman;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextFlow;

public class FXElements {
    Button btn = new Button();
    GridPane gridpane = new GridPane();
    Insets gridPadding = new Insets(20.0, 20.0, 20.0, 20.0);
    public static TextFlow hiddenWordFlow = new TextFlow();
}