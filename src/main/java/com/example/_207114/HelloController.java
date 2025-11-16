package com.example._207114;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    @FXML
    private Button create;

    @FXML
    public void oncreate() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("form.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) create.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
