package com.example._207114;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;


public class formcontroller {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;

    @FXML
    private TextField countryField;
    @FXML
    private TextField divisionField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField houseField;

    @FXML
    private TextField sscField;
    @FXML
    private TextField hscField;
    @FXML
    private TextField bscField;
    @FXML
    private TextField mscField;

    @FXML
    private TextField skillsField;
    @FXML
    private TextField experienceField;
    @FXML
    private TextField projectsField;


    @FXML
    private ImageView profileImageView;
    @FXML
    private Button uploadImageBtn;

    @FXML
    private Button genrate;

    private Image selectedImage;
    @FXML
    public void onUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) uploadImageBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImage = new Image(file.toURI().toString());
            profileImageView.setImage(selectedImage);
        }
    }
    @FXML
    public void ongenerate() throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("preview.fxml"));
        Scene previewScene = new Scene(loader.load());
        previewcontroller controller = loader.getController();
        controller.setData(
                fullNameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                countryField.getText(),
                divisionField.getText(),
                cityField.getText(),
                houseField.getText(),
                sscField.getText(),
                hscField.getText(),
                bscField.getText(),
                mscField.getText(),
                skillsField.getText(),
                experienceField.getText(),
                projectsField.getText(),
                selectedImage
        );

        Stage stage = (Stage) genrate.getScene().getWindow();
        stage.setScene(previewScene);
        stage.show();
    }
}
