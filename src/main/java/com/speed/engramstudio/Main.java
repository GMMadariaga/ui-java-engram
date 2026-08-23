package com.speed.engramstudio;

import com.speed.engramstudio.bootstrap.ApplicationBootstrap;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        ApplicationBootstrap bootstrap = new ApplicationBootstrap();
        bootstrap.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}