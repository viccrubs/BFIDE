package viccrubs.bfide.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import viccrubs.bfide.client.controllers.LoginPanelController;
import viccrubs.bfide.client.log.ApplicationLog;
import viccrubs.bfide.client.models.Log;
import viccrubs.bfide.models.response.LoginResponse;

public class MainClient extends Application {

    private Stage mainStage;

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        mainStage.setTitle("Login");

        initRootLayout();

    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            new ApplicationLog().addLog(new Log("App started."));
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/LoginPanel.fxml"));
            // Show the scene containing the root layout.
            Scene scene = new Scene(loader.load());
            LoginPanelController controller = loader.getController();
            controller.setStage(mainStage);
            mainStage.setScene(scene);

            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






    public static void main(String[] args) {
        launch(args);
    }
}