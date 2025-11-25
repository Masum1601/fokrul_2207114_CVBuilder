package com.example._207114;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LoadResumesController {

    @FXML
    private TableView<ResumeListItem> resumeTable;

    @FXML
    private TableColumn<ResumeListItem, Integer> idColumn;

    @FXML
    private TableColumn<ResumeListItem, String> nameColumn;

    @FXML
    private TableColumn<ResumeListItem, String> emailColumn;

    @FXML
    private Button loadButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    private DatabaseManager dbManager;
    private ObservableList<ResumeListItem> resumeList;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        resumeList = FXCollections.observableArrayList();

        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().id()).asObject());
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().fullName()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().email()));

        loadResumeList();
    }


    private void loadResumeList() {
        resumeList.clear();

        try {
            ResultSet rs = dbManager.getAllResumes();
            if (rs != null) {
                while (rs.next()) {
                    ResumeListItem item = new ResumeListItem(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("email")
                    );
                    resumeList.add(item);
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error loading resumes: " + e.getMessage());
        }

        resumeTable.setItems(resumeList);
    }

    @FXML
    public void onLoad() throws IOException {
        ResumeListItem selected = resumeTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a resume to load.");
            return;
        }

        Resume resume = dbManager.getResumeById(selected.id());

        if (resume != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("form.fxml"));
            Scene scene = new Scene(loader.load());

            formcontroller controller = loader.getController();
            controller.loadResumeData(resume);

            Stage stage = (Stage) loadButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load resume.");
        }
    }

    @FXML
    public void onDelete() {
        ResumeListItem selected = resumeTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a resume to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Resume");
        confirmAlert.setContentText("Are you sure you want to delete the resume for " + selected.fullName() + "?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            if (dbManager.deleteResume(selected.id())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Resume deleted successfully.");
                loadResumeList();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete resume.");
            }
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

    public void setFormController(formcontroller controller) {
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

        public record ResumeListItem(int id, String fullName, String email) {
    }
}