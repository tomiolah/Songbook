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
import projector.application.Settings;
import projector.controller.util.ProjectionScreenHolder;
import projector.ui.ResetButton;

import static projector.controller.SettingsController.addFonts;
import static projector.controller.SettingsController.getFontWeightByString;
import static projector.utils.NumberUtil.getIntegerFromNumber;

public class ProjectionScreenSettingsController {
    public BorderPane mainBorderPain;
    public ScrollPane scrollPane;
    public Slider maxFontSlider;
    public CheckBox breakLinesCheckbox;
    public Slider breakAfterSlider;
    public Slider lineSpaceSlider;
    public CheckBox showSongSecondTextCheckBox;
    public ColorPicker songSecondTextColorPicker;
    public ListView<Text> fontListView;
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
    public ResetButton colorReset;
    public ResetButton backgroundColorReset;
    public ResetButton maxFontSliderReset;
    public ResetButton breakLinesCheckboxReset;
    public ResetButton breakAfterSliderReset;
    public ResetButton lineSpaceSliderReset;
    public ResetButton imageRadioButtonReset;
    public ResetButton colorRadioButtonReset;
    public ResetButton progressLineColorPickerReset;
    public ResetButton progressLineThicknessSpinnerReset;
    public ResetButton progressLinePositionReset;
    public ResetButton fontWeightComboBoxReset;
    public ResetButton showSongSecondTextCheckBoxReset;
    public ResetButton songSecondTextColorPickerReset;
    public ResetButton fontListViewReset;
    private Stage stage;
    private ProjectionScreenSettings projectionScreenSettings;
    private ProjectionScreenSettings projectionScreenSettingsModel;
    private ProjectionScreenHolder projectionScreenHolder;

    public void onImageBrowseButtonAction() {
    }

    public void onSaveButtonAction() {
        projectionScreenSettings.setMaxFont(projectionScreenSettingsModel.getMaxFont());
        projectionScreenSettings.setBreakLines(projectionScreenSettingsModel.getBreakLines());
        projectionScreenSettings.setBreakAfter(projectionScreenSettingsModel.getBreakAfter());
        projectionScreenSettings.setLineSpace(projectionScreenSettingsModel.getLineSpace());
        projectionScreenSettings.setColor(projectionScreenSettingsModel.getColor());
        projectionScreenSettings.setIsBackgroundImage(projectionScreenSettingsModel.getIsBackgroundImage());
        projectionScreenSettings.setBackgroundColor(projectionScreenSettingsModel.getBackgroundColor());
        projectionScreenSettings.setProgressLineColor(projectionScreenSettingsModel.getProgressLineColor());
        projectionScreenSettings.setProgressLineThickness(projectionScreenSettingsModel.getProgressLineThickness());
        projectionScreenSettings.setProgressLinePositionIsTop(projectionScreenSettingsModel.getProgressLinePosition());
        projectionScreenSettings.setFontWeight(projectionScreenSettingsModel.getFontWeightString());
        projectionScreenSettings.setShowSongSecondText(projectionScreenSettingsModel.getShowSongSecondText());
        projectionScreenSettings.setSongSecondTextColor(projectionScreenSettingsModel.getSongSecondTextColor());
        projectionScreenSettings.setFont(projectionScreenSettingsModel.getFont());
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
        projectionScreenSettingsModel = new ProjectionScreenSettings(projectionScreenSettings);
        projectionScreenSettingsModel.setUseGlobalSettings(false);
        maxFontSlider.setValue(projectionScreenSettings.getMaxFont());
        boolean breakLines = projectionScreenSettings.isBreakLines();
        breakLinesCheckbox.setSelected(breakLines);
        breakAfterSlider.setDisable(breakLines);
        breakAfterSlider.setValue(projectionScreenSettings.getBreakAfter());
        breakLinesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> breakAfterSlider.setDisable(newValue));
        addAndSelectFirstFont(projectionScreenSettings.getFont());
        fontListView.getSelectionModel().selectedItemProperty().addListener(this::changed);
        lineSpaceSlider.setMax(10);
        lineSpaceSlider.valueChangingProperty().addListener(this::changed);
        initializeProgressLine();
        Settings settings = Settings.getInstance();
        initializeMaxFontSlider(settings);
        initializeBreakLinesCheckbox(settings);
        initializeBreakAfterSlider(settings);
        initializeLineSpaceSlider(settings);
        initializeColorPicker(settings);
        initializeBackgroundRadio();
        initializeImageRadioButton();
        initializeColorRadioButton();
        initializeBackgroundColorPicker(settings);
        initializeProgressLineColorPicker(settings);
        initializeProgressLineThicknessSpinner(settings);
        initializeProgressLinePosition(settings);
        initializeFontWeightComboBox(settings);
        initializeShowSongSecondTextCheckBox(settings);
        initializeSongSecondTextColorPicker(settings);
        initializeFontListView(settings);
        showSongSecondTextCheckBox.setSelected(projectionScreenSettings.isShowSongSecondText());
        songSecondTextColorPicker.setValue(projectionScreenSettings.getSongSecondTextColor());
    }

    private void addAndSelectFirstFont(String font) {
        Text tmpK = new Text(font);
        tmpK.setFont(Font.font(font));
        fontListView.getItems().add(tmpK);
        addFontsAndSelectFirstFont(projectionScreenSettings.getFontWeightString());
    }

    private void initializeBackgroundRadio() {
        ToggleGroup group = new ToggleGroup();
        colorRadioButton.setToggleGroup(group);
        imageRadioButton.setToggleGroup(group);
        setBackgroundImageRadioValue(projectionScreenSettings.isBackgroundImage());
    }

    private void setBackgroundImageRadioValue(boolean backgroundImage) {
        imageRadioButton.setSelected(backgroundImage);
        colorRadioButton.setSelected(!backgroundImage);
    }

    private void initializeMaxFontSlider(Settings settings) {
        maxFontSlider.setValue(projectionScreenSettings.getMaxFont());
        setMaxFontSliderResetVisibility();
        maxFontSliderReset.setOnAction2(event -> {
            maxFontSlider.setValue(settings.getMaxFont());
            projectionScreenSettingsModel.setMaxFont(null);
        });
        maxFontSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setMaxFont(getIntegerFromNumber(newValue));
            setMaxFontSliderResetVisibility();
        });
    }

    private void initializeBreakLinesCheckbox(Settings settings) {
        breakLinesCheckbox.setSelected(projectionScreenSettings.isBreakLines());
        setBreakLinesCheckboxResetVisibility();
        breakLinesCheckboxReset.setOnAction2(event -> {
            breakLinesCheckbox.setSelected(settings.isBreakLines());
            projectionScreenSettingsModel.setBreakLines(null);
        });
        breakLinesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setBreakLines(newValue);
            setBreakLinesCheckboxResetVisibility();
        });
    }

    private void initializeBreakAfterSlider(Settings settings) {
        breakAfterSlider.setValue(projectionScreenSettings.getBreakAfter());
        setBreakAfterSliderResetVisibility();
        breakAfterSliderReset.setOnAction2(event -> {
            breakAfterSlider.setValue(settings.getBreakAfter());
            projectionScreenSettingsModel.setBreakAfter(null);
        });
        breakAfterSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setBreakAfter(getIntegerFromNumber(newValue));
            setBreakAfterSliderResetVisibility();
        });
    }

    private void initializeLineSpaceSlider(Settings settings) {
        lineSpaceSlider.setValue(projectionScreenSettings.getLineSpace());
        setLineSpaceSliderResetVisibility();
        lineSpaceSliderReset.setOnAction2(event -> {
            lineSpaceSlider.setValue(settings.getLineSpace());
            projectionScreenSettingsModel.setLineSpace(null);
        });
        lineSpaceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setLineSpace((Double) newValue);
            setLineSpaceSliderResetVisibility();
        });
    }

    private void initializeImageRadioButton() {
        setImageRadioButtonResetVisibility();
        imageRadioButtonReset.setOnAction2(event -> backgroundButtonResetEvent());
        imageRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> onImageRadioButtonSelected(newValue));
    }

    private void initializeColorRadioButton() {
        setColorRadioButtonResetVisibility();
        colorRadioButtonReset.setOnAction2(event -> backgroundButtonResetEvent());
        colorRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> onImageRadioButtonSelected(!newValue));
    }

    private void initializeProgressLineColorPicker(Settings settings) {
        progressLineColorPicker.setValue(projectionScreenSettings.getProgressLineColor());
        setProgressLineColorResetVisibility();
        progressLineColorPickerReset.setOnAction2(event -> {
            progressLineColorPicker.setValue(settings.getProgressLineColor());
            projectionScreenSettingsModel.setProgressLineColor(null);
        });
        progressLineColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setProgressLineColor(newValue);
            setProgressLineColorResetVisibility();
        });
    }

    private void initializeProgressLineThicknessSpinner(Settings settings) {
        SpinnerValueFactory.IntegerSpinnerValueFactory spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, projectionScreenSettings.getProgressLineThickness());
        progressLineThicknessSpinner.setValueFactory(spinnerValueFactory);
        setProgressLineThicknessResetVisibility();
        progressLineThicknessSpinnerReset.setOnAction2(event -> {
            spinnerValueFactory.setValue(settings.getProgressLineThickness());
            projectionScreenSettingsModel.setProgressLineThickness(null);
        });
        progressLineThicknessSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setProgressLineThickness(newValue);
            setProgressLineThicknessResetVisibility();
        });
    }

    private void initializeProgressLinePosition(Settings settings) {
        ToggleGroup toggleGroup = new ToggleGroup();
        progressLinePositionTopRadioButton.setToggleGroup(toggleGroup);
        progressLinePositionBottomRadioButton.setToggleGroup(toggleGroup);
        setSelectedForProgressLinePosition(projectionScreenSettings.isProgressLinePositionIsTop());
        setProgressLinePositionResetVisibility();
        progressLinePositionReset.setOnAction2(event -> {
            setSelectedForProgressLinePosition(settings.isProgressLinePositionIsTop());
            projectionScreenSettingsModel.setProgressLinePositionIsTop(null);
        });
        progressLinePositionTopRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> setProgressLinePositionIsTopEvent(newValue));
        progressLinePositionBottomRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> setProgressLinePositionIsTopEvent(!newValue));
    }

    private void initializeFontWeightComboBox(Settings settings) {
        FontWeight fontWeight = projectionScreenSettings.getFontWeight();
        addFonts(fontWeight, fontListView);
        fontWeightComboBox.getItems().add("NORMAL");
        fontWeightComboBox.getItems().add("BOLD");
        fontWeightComboBox.getSelectionModel().select(projectionScreenSettings.getFontWeightString());
        fontWeightComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            fontWeightValueChange(newValue, projectionScreenSettings.getFont());
            setFontWeightResetVisibility();
        });
        setFontWeightResetVisibility();
        fontWeightComboBoxReset.setOnAction2(event -> {
            fontWeightComboBox.getSelectionModel().select(settings.getFontWeightString());
            projectionScreenSettingsModel.setFontWeight(null);
        });
    }

    private void initializeShowSongSecondTextCheckBox(Settings settings) {
        showSongSecondTextCheckBox.setSelected(projectionScreenSettings.isShowSongSecondText());
        setShowSongSecondTextCheckBoxResetVisibility();
        showSongSecondTextCheckBoxReset.setOnAction2(event -> {
            showSongSecondTextCheckBox.setSelected(settings.isShowSongSecondText());
            projectionScreenSettingsModel.setShowSongSecondText(null);
        });
        showSongSecondTextCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setShowSongSecondText(newValue);
            setShowSongSecondTextCheckBoxResetVisibility();
        });
    }

    private void initializeSongSecondTextColorPicker(Settings settings) {
        songSecondTextColorPicker.setValue(projectionScreenSettings.getSongSecondTextColor());
        setSongSecondTextColorResetVisibility();
        songSecondTextColorPickerReset.setOnAction2(event -> {
            songSecondTextColorPicker.setValue(settings.getSongSecondTextColor());
            projectionScreenSettingsModel.setSongSecondTextColor(null);
        });
        songSecondTextColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setSongSecondTextColor(newValue);
            setSongSecondTextColorResetVisibility();
        });
    }

    private void initializeFontListView(Settings settings) {
        setFontListViewResetVisibility();
        fontListViewReset.setOnAction2(event -> {
            addAndSelectFirstFont(settings.getFont());
            projectionScreenSettingsModel.setFont(null);
        });
    }

    private void setFontListViewResetVisibility() {
        fontListViewReset.setVisible(projectionScreenSettingsModel.getFont() != null);
    }

    private void setSongSecondTextColorResetVisibility() {
        songSecondTextColorPickerReset.setVisible(projectionScreenSettingsModel.getSongSecondTextColor() != null);
    }

    private void setShowSongSecondTextCheckBoxResetVisibility() {
        showSongSecondTextCheckBoxReset.setVisible(projectionScreenSettingsModel.getShowSongSecondText() != null);
    }

    private void setFontWeightResetVisibility() {
        fontWeightComboBoxReset.setVisible(projectionScreenSettingsModel.getFontWeightString() != null);
    }

    private void setProgressLinePositionIsTopEvent(Boolean newValue) {
        projectionScreenSettingsModel.setProgressLinePositionIsTop(newValue);
        setProgressLinePositionResetVisibility();
    }

    private void setSelectedForProgressLinePosition(boolean progressLinePositionIsTop) {
        progressLinePositionTopRadioButton.setSelected(progressLinePositionIsTop);
        progressLinePositionBottomRadioButton.setSelected(!progressLinePositionIsTop);
    }

    private void setProgressLinePositionResetVisibility() {
        progressLinePositionReset.setVisible(projectionScreenSettingsModel.getProgressLinePosition() != null);
    }

    private void setProgressLineThicknessResetVisibility() {
        progressLineThicknessSpinnerReset.setVisible(projectionScreenSettingsModel.getProgressLineThickness() != null);
    }

    private void setProgressLineColorResetVisibility() {
        progressLineColorPickerReset.setVisible(projectionScreenSettingsModel.getProgressLineColor() != null);
    }

    private void backgroundButtonResetEvent() {
        setBackgroundImageRadioValue(Settings.getInstance().isBackgroundImage());
        projectionScreenSettingsModel.setIsBackgroundImage(null);
        colorRadioButtonReset.setVisible(false);
        imageRadioButtonReset.setVisible(false);
    }

    private void onImageRadioButtonSelected(Boolean newValue) {
        projectionScreenSettingsModel.setIsBackgroundImage(newValue);
        setImageRadioButtonResetVisibility();
        setColorRadioButtonResetVisibility();
    }

    private void setColorRadioButtonResetVisibility() {
        colorRadioButtonReset.setVisible(projectionScreenSettingsModel.getIsBackgroundImage() != null);
    }

    private void setImageRadioButtonResetVisibility() {
        imageRadioButtonReset.setVisible(projectionScreenSettingsModel.getIsBackgroundImage() != null);
    }

    private void setLineSpaceSliderResetVisibility() {
        lineSpaceSliderReset.setVisible(projectionScreenSettingsModel.getLineSpace() != null);
    }

    private void setBreakAfterSliderResetVisibility() {
        breakAfterSliderReset.setVisible(projectionScreenSettingsModel.getBreakAfter() != null);
    }

    private void setBreakLinesCheckboxResetVisibility() {
        breakLinesCheckboxReset.setVisible(projectionScreenSettingsModel.getBreakLines() != null);
    }

    private void initializeBackgroundColorPicker(Settings settings) {
        backgroundColorReset.setOnAction2(event -> {
            backgroundColorPicker.setValue(settings.getBackgroundColor());
            projectionScreenSettingsModel.setBackgroundColor(null);
        });
        backgroundColorPicker.setValue(projectionScreenSettings.getBackgroundColor());
        setBackgroundColorResetVisibility();
        backgroundColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setBackgroundColor(newValue);
            setBackgroundColorResetVisibility();
        });
    }

    private void initializeColorPicker(Settings settings) {
        colorPicker.setValue(projectionScreenSettings.getColor());
        setColorResetVisibility();
        colorReset.setOnAction2(event -> {
            colorPicker.setValue(settings.getColor());
            projectionScreenSettingsModel.setColor(null);
        });
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setColor(newValue);
            setColorResetVisibility();
        });
    }

    private void setMaxFontSliderResetVisibility() {
        maxFontSliderReset.setVisible(projectionScreenSettingsModel.getMaxFont() != null);
    }

    private void setColorResetVisibility() {
        colorReset.setVisible(projectionScreenSettingsModel.getColor() != null);
    }

    private void setBackgroundColorResetVisibility() {
        backgroundColorReset.setVisible(projectionScreenSettingsModel.getBackgroundColor() != null);
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

    private void changed(ObservableValue<? extends Text> observable, Text oldValue, Text newValue) {
        if (newValue != null && !newValue.getText().isEmpty()) {
            projectionScreenSettingsModel.setFont(newValue.getText());
        }
        setFontListViewResetVisibility();
    }

    private synchronized void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        projectionScreenSettings.setLineSpace(lineSpaceSlider.getValue());
    }

    private void fontWeightValueChange(String fontWeight, String font) {
        projectionScreenSettings.setFontWeight(fontWeight);
        projectionScreenSettingsModel.setFontWeight(fontWeight);
        fontListView.getItems().clear();
        addAndSelectFirstFont(font);
        //        projectionScreenController.reload();
    }

    private void addFontsAndSelectFirstFont(String fontWeight) {
        FontWeight fontWeight1 = getFontWeightByString(fontWeight);
        addFonts(fontWeight1, fontListView);
        fontListView.getSelectionModel().select(0);
    }
}
