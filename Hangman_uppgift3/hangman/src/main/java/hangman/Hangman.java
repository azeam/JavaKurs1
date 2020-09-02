package hangman;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
 
public class Hangman extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    
    // TODO: show hint (synonym etc.?)
    // TODO: select number of chars
    // TODO: animation

    @Override
    public void start(Stage window) {
        window.setTitle("Hang the man!");
        FXElements fxElements = new FXElements();
        fxElements.buildElements();
        
        window.setScene(new Scene(fxElements.gridpane));        
        window.show();
    }   
}