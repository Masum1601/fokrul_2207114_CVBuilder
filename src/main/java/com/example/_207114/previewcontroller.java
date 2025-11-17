package com.example._207114;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class previewcontroller {

    @FXML
    private Label previewFullName;
    @FXML
    private Label previewEmail;
    @FXML
    private Label previewPhone;
    @FXML
    private Label previewAddress;
    @FXML
    private Label previewSSC;
    @FXML
    private Label previewHSC;
    @FXML
    private Label previewBSC;
    @FXML
    private Label previewMSC;
    @FXML
    private Label previewSkills;
    @FXML
    private Label previewExperience;
    @FXML
    private Label previewProjects;

    public void setData(
            String fullName,
            String email,
            String phone,
            String country,
            String division,
            String city,
            String house,
            String ssc,
            String hsc,
            String bsc,
            String msc,
            String skills,
            String experience,
            String projects
    ) {
        previewFullName.setText(fullName);
        previewEmail.setText(email);
        previewPhone.setText(phone);
        previewAddress.setText(house + ", " + city + ", " + division + ", " + country);
        previewSSC.setText(ssc);
        previewHSC.setText(hsc);
        previewBSC.setText(bsc);
        previewMSC.setText(msc);
        previewSkills.setText(skills);
        previewExperience.setText(experience);
        previewProjects.setText(projects);
    }
}
