package by.brsm.app;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Точка входа приложения «Генератор протоколов и постановлений БРСМ».
 */
public class BrsmApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Генератор протоколов и постановлений БРСМ");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
