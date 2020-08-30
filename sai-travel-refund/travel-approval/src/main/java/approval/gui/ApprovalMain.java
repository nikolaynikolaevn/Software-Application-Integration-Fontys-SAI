package approval.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class ApprovalMain extends Application {

    private static String queueName = null;
    private static String approvalAppName = null;

    public static void main(String[] args) {
        if (args.length < 2 ){
            throw new IllegalArgumentException("Arguments are missing. You must provide two arguments: APPROVAL_REQUEST_QUEUE and APPROVAL_NAME");
        }
        if (args[0] == null){
            throw new IllegalArgumentException("Please provide APPROVAL_REQUEST_QUEUE.");
        }
        if (args[1] == null){
            throw new IllegalArgumentException("Please provide APPROVAL_NAME.");
        }
        queueName = args[0];
        approvalAppName =args[1];
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        final Logger logger = LoggerFactory.getLogger(getClass());
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        URL url  = getClass().getClassLoader().getResource( "approval.fxml" );
        if (url != null) {


            FXMLLoader loader = new FXMLLoader(url);

            // Create a controller instance

            ApprovalController controller = new ApprovalController(approvalAppName, queueName);
            // Set it in the FXMLLoader
            loader.setController(controller);


            Parent root = loader.load();
            primaryStage.setTitle("APPROVAL - " + approvalAppName);

            primaryStage.setOnCloseRequest(t -> {
                Platform.exit();
                System.exit(0);
            });

            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/approval.png")));
            primaryStage.setScene(new Scene(root, 500, 300));
            primaryStage.show();
            logger.info("Started "+ approvalAppName +" approval on " + queueName + ".");
        }
        else {
            logger.error("Could not load frame from approval.fxml.");
        }
    }
}
