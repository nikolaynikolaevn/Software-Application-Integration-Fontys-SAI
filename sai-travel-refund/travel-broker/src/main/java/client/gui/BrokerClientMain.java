package client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class BrokerClientMain extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception{
        URL url  = getClass().getClassLoader().getResource( "broker_client.fxml");
        if (url != null) {
            Parent root = FXMLLoader.load(url);
            primaryStage.setTitle("Broker Client");

            primaryStage.setOnCloseRequest(t -> {
                Platform.exit();
                System.exit(0);
            });
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/client.png")));
            primaryStage.setScene(new Scene(root, 477, 400));
            primaryStage.show();
        }else {
           final Logger logger = LoggerFactory.getLogger(getClass());
           logger.error("Could not load frame from broker_client.fxml");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


}
