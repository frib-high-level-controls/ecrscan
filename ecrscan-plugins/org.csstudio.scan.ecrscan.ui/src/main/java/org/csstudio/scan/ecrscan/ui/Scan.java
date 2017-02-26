package org.csstudio.scan.ecrscan.ui;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

public class Scan extends VBox {
    
    public Scan() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/Scan.fxml"));

        fxmlLoader.setRoot(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Scene createScene() throws Exception {
        Parent root = FXMLLoader.load(Scan.class.getResource("/fxml/Scan_main.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        return scene;
    }


}