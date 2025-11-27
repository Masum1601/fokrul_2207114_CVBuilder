package com.example._207114;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
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
import java.util.concurrent.CompletableFuture;

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
    @FXML
    private ProgressIndicator progressIndicator;

    private Image selectedImage;
    private String savedImagePath;
    private Resume currentResume;
    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        System.out.println("Form controller initialized");
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

            CompletableFuture.runAsync(() -> {
                savedImagePath = saveImageToLocal(file);
                System.out.println("Image saved to: " + savedImagePath);
            });
        }
    }

    private String saveImageToLocal(File sourceFile) {
        try {
            Path imagesDir = Paths.get("resume_images");
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }

            String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
            Path targetPath = imagesDir.resolve(fileName);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void onSave() {
        System.out.println("Save button clicked");

        if (!validateFields()) {
            return;
        }

        Resume resume = createResumeFromFields();
        System.out.println("Resume created: " + resume);

        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        setButtonsDisabled(true);

        if (currentResume != null && currentResume.getId() > 0) {
            resume.setId(currentResume.getId());
            System.out.println("Updating resume with ID: " + resume.getId());

            dbManager.updateResumeAsync(resume).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (progressIndicator != null) progressIndicator.setVisible(false);
                    setButtonsDisabled(false);

                    if (success) {
                        currentResume = resume;
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Resume updated successfully!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update resume. Please check console for details.");
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    if (progressIndicator != null) progressIndicator.setVisible(false);
                    setButtonsDisabled(false);
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Error: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
        } else {
            System.out.println("Saving new resume");

            dbManager.saveResumeAsync(resume).thenAccept(id -> {
                Platform.runLater(() -> {
                    if (progressIndicator != null) progressIndicator.setVisible(false);
                    setButtonsDisabled(false);

                    if (id > 0) {
                        resume.setId(id);
                        currentResume = resume;
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Resume saved successfully!\nID: " + id);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to save resume. Please check console for details.");
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    if (progressIndicator != null) progressIndicator.setVisible(false);
                    setButtonsDisabled(false);
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Error: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
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

        fullNameField.setText(resume.getFullName() != null ? resume.getFullName() : "");
        emailField.setText(resume.getEmail() != null ? resume.getEmail() : "");
        phoneField.setText(resume.getPhone() != null ? resume.getPhone() : "");
        countryField.setText(resume.getCountry() != null ? resume.getCountry() : "");
        divisionField.setText(resume.getDivision() != null ? resume.getDivision() : "");
        cityField.setText(resume.getCity() != null ? resume.getCity() : "");
        houseField.setText(resume.getHouse() != null ? resume.getHouse() : "");
        sscField.setText(resume.getSsc() != null ? resume.getSsc() : "");
        hscField.setText(resume.getHsc() != null ? resume.getHsc() : "");
        bscField.setText(resume.getBsc() != null ? resume.getBsc() : "");
        mscField.setText(resume.getMsc() != null ? resume.getMsc() : "");
        skillsField.setText(resume.getSkills() != null ? resume.getSkills() : "");
        experienceField.setText(resume.getExperience() != null ? resume.getExperience() : "");
        projectsField.setText(resume.getProjects() != null ? resume.getProjects() : "");

        if (resume.getImagePath() != null && !resume.getImagePath().isEmpty()) {
            CompletableFuture.runAsync(() -> {
                File imageFile = new File(resume.getImagePath());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    Platform.runLater(() -> {
                        selectedImage = image;
                        profileImageView.setImage(selectedImage);
                        savedImagePath = resume.getImagePath();
                    });
                }
            });
        }

        System.out.println("Resume data loaded into form");
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

    private Resume createResumeFromFields() {
        return new Resume(
                getValue(fullNameField),
                getValue(emailField),
                getValue(phoneField),
                getValue(countryField),
                getValue(divisionField),
                getValue(cityField),
                getValue(houseField),
                getValue(sscField),
                getValue(hscField),
                getValue(bscField),
                getValue(mscField),
                getValue(skillsField),
                getValue(experienceField),
                getValue(projectsField),
                savedImagePath
        );
    }

    private String getValue(TextField field) {
        return field.getText() != null ? field.getText().trim() : "";
    }

    private void setButtonsDisabled(boolean disabled) {
        saveBtn.setDisable(disabled);
        loadBtn.setDisable(disabled);
        genrate.setDisable(disabled);
        uploadImageBtn.setDisable(disabled);
    }

    private boolean validateFields() {
        if (getValue(fullNameField).isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter your full name.");
            return false;
        }
        if (getValue(emailField).isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter your email.");
            return false;
        }
        System.out.println("Validation passed");
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
