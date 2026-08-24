package com.speed.engramstudio;

import com.speed.engramstudio.bootstrap.ApplicationBootstrap;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX application entry point kept separate from the native launcher class.
 *
 * <p>Keeping the launcher class free of the {@link Application} superclass
 * avoids the Java launcher treating the packaged entry point as a special
 * JavaFX application before it invokes its {@code main} method.</p>
 */
public class EngramStudioApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        ApplicationBootstrap bootstrap = new ApplicationBootstrap();
        bootstrap.start(primaryStage);
    }
}
