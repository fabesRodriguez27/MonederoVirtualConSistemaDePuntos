package monedero.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import monedero.utilidades.PathsFxml;

public class App extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //AppSetup app = new AppSetup();
        AnchorPane loader = FXMLLoader.load(getClass().getResource(PathsFxml.PATH_LOGIN));
        Scene scene = new Scene(loader);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}