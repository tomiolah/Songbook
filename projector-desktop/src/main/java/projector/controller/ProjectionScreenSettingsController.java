package projector.controller;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import projector.application.ProjectionScreenSettings;
import projector.controller.util.ProjectionScreenHolder;

import static projector.controller.SettingsController.addFonts;
import static projector.controller.SettingsController.getFontWeightByString;

public class ProjectionScreenSettingsController {
    public BorderPane mainBorderPain;
    public ScrollPane scrollPane;
    public Slider maxFontSlider;
    public CheckBox breakLinesCheckbox;
    public Slider breakAfterSlider;
    public Slider slider;
    public CheckBox showSongSecondTextCheckBox;
    public ColorPicker songSecondTextColorPicker;
    public ListView<Text> listView;
    public ColorPicker colorPicker;
    public RadioButton imageRadioButton;
    public TextField imagePathTextField;
    public Button imageBrowseButton;
    public RadioButton colorRadioButton;
    public ColorPicker backgroundColorPicker;
    public ColorPicker progressLineColorPicker;
    public Spinner<Integer> progressLineThicknessSpinner;
    public RadioButton progressLinePositionTopRadioButton;
    public RadioButton progressLinePositionBottomRadioButton;
    public ComboBox<String> fontWeightComboBox;
    private Stage stage;
    private ProjectionScreenSettings projectionScreenSettings;
    private ProjectionScreenHolder projectionScreenHolder;

    public void onImageBrowseButtonAction() {
    }

    public void onSaveButtonAction() {
        projectionScreenSettings.setMaxFont((int) maxFontSlider.getValue());

        projectionScreenSettings.setBackgroundColor(backgroundColorPicker.getValue());
        projectionScreenSettings.save();
        projectionScreenHolder.getProjectionScreenController().onSettingsChanged();
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setProjectionScreenHolder(ProjectionScreenHolder projectionScreenHolder) {
        this.projectionScreenHolder = projectionScreenHolder;
        projectionScreenSettings = projectionScreenHolder.getProjectionScreenSettings();
        maxFontSlider.setValue(projectionScreenSettings.getMaxFont());
        boolean breakLines = projectionScreenSettings.isBreakLines();
        breakLinesCheckbox.setSelected(breakLines);
        breakAfterSlider.setDisable(breakLines);
        breakAfterSlider.setValue(projectionScreenSettings.getBreakAfter());
        breakLinesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> breakAfterSlider.setDisable(newValue));
        String iK = projectionScreenSettings.getFont();
        Text tmpK = new Text(iK);
        tmpK.setFont(Font.font(iK));
        listView.getItems().add(tmpK);
        listView.getSelectionModel().select(0);
        FontWeight fontWeight = projectionScreenSettings.getFontWeight();
        addFonts(fontWeight, listView);
        fontWeightComboBox.getItems().add("NORMAL");
        fontWeightComboBox.getItems().add("BOLD");
        fontWeightComboBox.getSelectionModel().select(projectionScreenSettings.getFontWeightString());
        fontWeightComboBox.valueProperty().addListener((observable, oldValue, newValue) -> fontWeightValueChange(newValue));
        listView.getSelectionModel().selectedItemProperty().addListener(this::changed);
        slider.setMax(10);
        slider.valueChangingProperty().addListener(this::changed);
        initializeProgressLine();
        backgroundColorPicker.setValue(projectionScreenSettings.getBackgroundColor());
        progressLineThicknessSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, projectionScreenSettings.getProgressLineThickness()));
        showSongSecondTextCheckBox.setSelected(projectionScreenSettings.isShowSongSecondText());
        songSecondTextColorPicker.setValue(projectionScreenSettings.getSongSecondTextColor());
    }

    private void initializeProgressLine() {
        ToggleGroup group = new ToggleGroup();
        progressLinePositionTopRadioButton.setToggleGroup(group);
        progressLinePositionBottomRadioButton.setToggleGroup(group);
        if (projectionScreenSettings.isProgressLinePositionIsTop()) {
            progressLinePositionTopRadioButton.setSelected(true);
        } else {
            progressLinePositionBottomRadioButton.setSelected(true);
        }
    }

    private synchronized void changed(ObservableValue<? extends Text> observable, Text oldValue, Text newValue) {
        if (newValue != null && !newValue.getText().isEmpty()) {
            projectionScreenSettings.setFont(newValue.getText());
        }
    }

    private synchronized void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        projectionScreenSettings.setLineSpace(slider.getValue());
    }

    private void fontWeightValueChange(String newValue) {
        projectionScreenSettings.setFontWeight(newValue);
        listView.getItems().clear();
        String iK1 = projectionScreenSettings.getFont();
        Text tmpK1 = new Text(iK1);
        tmpK1.setFont(Font.font(iK1));
        listView.getItems().add(tmpK1);
        FontWeight fontWeight1 = getFontWeightByString(newValue);
        addFonts(fontWeight1, listView);
        listView.getSelectionModel().select(0);
        //        projectionScreenController.reload();
    }

    public void resetBackgroundColor() {
        projectionScreenSettings.setDefaultBackgroundColor(true);
    }
}
