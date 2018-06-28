package io.github.guilhermedelemos.fs.gui;

import io.github.guilhermedelemos.fs.gui.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

public class AppFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MainView view = new MainView(this, primaryStage);
        view.exibir();
    }
}
