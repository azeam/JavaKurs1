package hangman;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage window) {
        window.setTitle("(Don't) hang the man!");
        FXElements fxElements = new FXElements();
        fxElements.buildElements();
        
        window.setScene(new Scene(fxElements.gridpane));        
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}