package com.example._207114;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LoadResumesController {

    @FXML
    private TableView<DatabaseManager.ResumeListItem> resumeTable;

    @FXML
    private TableColumn<DatabaseManager.ResumeListItem, Integer> idColumn;

    @FXML
    private TableColumn<DatabaseManager.ResumeListItem, String> nameColumn;

    @FXML
    private TableColumn<DatabaseManager.ResumeListItem, String> emailColumn;

    @FXML
    private Button loadButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    @FXML
    private ProgressIndicator progressIndicator;

    private DatabaseManager dbManager;
    private formcontroller formController;
    private ObservableList<DatabaseManager.ResumeListItem> resumeList;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        resumeList = FXCollections.observableArrayList();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        loadResumeListAsync();
    }

    private void loadResumeListAsync() {
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        setButtonsDisabled(true);

        dbManager.getAllResumesAsync().thenAccept(resumes -> {
            Platform.runLater(() -> {
                resumeList.clear();
                resumeList.addAll(resumes);
                resumeTable.setItems(resumeList);

                if (progressIndicator != null) {
                    progressIndicator.setVisible(false);
                }
                setButtonsDisabled(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load resumes: " + ex.getMessage());
                if (progressIndicator != null) {
                    progressIndicator.setVisible(false);
                }
                setButtonsDisabled(false);
            });
            ex.printStackTrace();
            return null;
        });
    }

    @FXML
    public void onLoad() {
        DatabaseManager.ResumeListItem selected = resumeTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a resume to load.");
            return;
        }

        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        setButtonsDisabled(true);

        dbManager.getResumeByIdAsync(selected.getId()).thenAccept(resume -> {
            Platform.runLater(() -> {
                if (resume != null) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("form.fxml"));
                        Scene scene = new Scene(loader.load());

                        formcontroller controller = loader.getController();
                        controller.loadResumeData(resume);

                        Stage stage = (Stage) loadButton.getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to load form: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load resume.");
                }

                if (progressIndicator != null) {
                    progressIndicator.setVisible(false);
                }
                setButtonsDisabled(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load resume: " + ex.getMessage());
                if (progressIndicator != null) {
                    progressIndicator.setVisible(false);
                }
                setButtonsDisabled(false);
            });
            ex.printStackTrace();
            return null;
        });
    }

    @FXML
    public void onDelete() {
        DatabaseManager.ResumeListItem selected = resumeTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a resume to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Resume");
        confirmAlert.setContentText("Are you sure you want to delete the resume for " + selected.getFullName() + "?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            if (progressIndicator != null) {
                progressIndicator.setVisible(true);
            }
            setButtonsDisabled(true);

            dbManager.deleteResumeAsync(selected.getId()).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Resume deleted successfully.");
                        loadResumeListAsync();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete resume.");
                        if (progressIndicator != null) {
                            progressIndicator.setVisible(false);
                        }
                        setButtonsDisabled(false);
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete resume: " + ex.getMessage());
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    setButtonsDisabled(false);
                });
                ex.printStackTrace();
                return null;
            });
        }
    }

    @FXML
    public void onBack() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("form.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void onRefresh() {
        loadResumeListAsync();
    }

    public void setFormController(formcontroller controller) {
        this.formController = controller;
    }

    private void setButtonsDisabled(boolean disabled) {
        loadButton.setDisable(disabled);
        deleteButton.setDisable(disabled);
        backButton.setDisable(disabled);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
