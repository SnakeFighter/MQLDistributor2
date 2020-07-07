package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MQLDistributor2 extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("main/mql_distrib_form.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("mql_distrib_form.fxml"));
        primaryStage.setTitle("MQL Distributor");
        //primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
