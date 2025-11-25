package com.example._207114;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

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

    @FXML
    private Button saveBtn;

    @FXML
    private Button loadBtn;

    private Image selectedImage;
    private String savedImagePath;
    private Resume currentResume; // For editing existing resume
    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
    }

    @FXML
    public void onUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) uploadImageBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedImage = new Image(file.toURI().toString());
            profileImageView.setImage(selectedImage);

            // Save image to local directory
            savedImagePath = saveImageToLocal(file);
        }
    }

    private String saveImageToLocal(File sourceFile) {
        try {
            // Create images directory if it doesn't exist
            Path imagesDir = Paths.get("resume_images");
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }

            // Copy file with unique name
            String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
            Path targetPath = imagesDir.resolve(fileName);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return targetPath.toString();
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            return null;
        }
    }

    @FXML
    public void onSave() {
        if (!validateFields()) {
            return;
        }

        Resume resume = new Resume(
                fullNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                countryField.getText().trim(),
                divisionField.getText().trim(),
                cityField.getText().trim(),
                houseField.getText().trim(),
                sscField.getText().trim(),
                hscField.getText().trim(),
                bscField.getText().trim(),
                mscField.getText().trim(),
                skillsField.getText().trim(),
                experienceField.getText().trim(),
                projectsField.getText().trim(),
                savedImagePath
        );

        if (currentResume != null && currentResume.getId() > 0) {
            // Update existing resume
            resume.setId(currentResume.getId());
            if (dbManager.updateResume(resume)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Resume updated successfully!");
                currentResume = resume;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update resume.");
            }
        } else {
            // Save new resume
            int id = dbManager.saveResume(resume);
            if (id > 0) {
                resume.setId(id);
                currentResume = resume;
                showAlert(Alert.AlertType.INFORMATION, "Success", "Resume saved successfully! ID: " + id);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save resume.");
            }
        }
    }

    @FXML
    public void onLoad() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("load-resumes.fxml"));
        Scene scene = new Scene(loader.load());

        LoadResumesController controller = loader.getController();
        controller.setFormController(this);

        Stage stage = (Stage) loadBtn.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public void loadResumeData(Resume resume) {
        this.currentResume = resume;

        fullNameField.setText(resume.getFullName());
        emailField.setText(resume.getEmail());
        phoneField.setText(resume.getPhone());
        countryField.setText(resume.getCountry());
        divisionField.setText(resume.getDivision());
        cityField.setText(resume.getCity());
        houseField.setText(resume.getHouse());
        sscField.setText(resume.getSsc());
        hscField.setText(resume.getHsc());
        bscField.setText(resume.getBsc());
        mscField.setText(resume.getMsc());
        skillsField.setText(resume.getSkills());
        experienceField.setText(resume.getExperience());
        projectsField.setText(resume.getProjects());

        // Load image
        if (resume.getImagePath() != null && !resume.getImagePath().isEmpty()) {
            File imageFile = new File(resume.getImagePath());
            if (imageFile.exists()) {
                selectedImage = new Image(imageFile.toURI().toString());
                profileImageView.setImage(selectedImage);
                savedImagePath = resume.getImagePath();
            }
        }
    }

    @FXML
    public void ongenerate() throws IOException {
        if (!validateFields()) {
            return;
        }

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

    private boolean validateFields() {
        if (fullNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter your full name.");
            return false;
        }
        if (emailField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter your email.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}