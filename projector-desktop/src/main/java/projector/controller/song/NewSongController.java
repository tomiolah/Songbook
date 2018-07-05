package projector.controller.song;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.Main;
import projector.api.ApiException;
import projector.api.SongApiBean;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.LoginController;
import projector.controller.ProjectionScreenController;
import projector.controller.song.util.SearchedSong;
import projector.model.Language;
import projector.model.Song;
import projector.model.SongVerse;
import projector.service.LanguageService;
import projector.service.ServiceException;
import projector.service.ServiceManager;
import projector.service.SongService;
import projector.service.SongVerseService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class NewSongController {

    private static final Logger LOG = LoggerFactory.getLogger(NewSongController.class);
    @FXML
    private CheckBox uploadCheckBox;
    @FXML
    private ComboBox<Language> languageComboBox;
    @FXML
    private ToggleButton secondTextToggleButton;
    @FXML
    private Button uploadButton;
    @FXML
    private Button saveButton;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextArea textArea;
    @FXML
    private RadioButton verseEditorRadioButton;
    @FXML
    private RadioButton rawTextEditorRadioButton;
    @FXML
    private BorderPane rawTextBorderPane;
    @FXML
    private ScrollPane verseEditorScrollPane;
    @FXML
    private VBox textAreas;
    @FXML
    private Button newVerseButton;
    @FXML
    private BorderPane borderPane;
    private SongController songController;
    private Stage stage;
    private boolean edit;
    @FXML
    private ProjectionScreenController previewProjectionScreenController;
    private Stage stage2;
    private SearchedSong selectedSong;
    private ArrayList<VerseController> verseControllers = new ArrayList<>();
    private Song editingSong;
    private Song newSong;
    private SongService songService = ServiceManager.getSongService();
    private VerseController lastFocusedVerse;
    private SongVerseService songVerseService = ServiceManager.getSongVerseService();
    private List<Language> languages;

    public void initialize() {
        edit = false;
        textArea.textProperty().addListener((observable, oldValue, newValue) -> toProjectionScreen());
        textArea.setWrapText(true);
        textArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> toProjectionScreen());
        initializeRadioButtons();
        verseEditorScrollPane.setFitToWidth(true);
        initializeNewVerseButton();
        textAreas.getChildren().clear();
        colorPicker.setFocusTraversable(false);
        colorPicker.setOnAction(event -> {
            Color value = colorPicker.getValue();
            TextArea lastFocusedVerseTextArea = lastFocusedVerse.getTextArea();
            IndexRange selection = lastFocusedVerseTextArea.getSelection();
            System.out.println("selection = " + selection);
            String text = lastFocusedVerseTextArea.getText();
            String left = text.substring(0, selection.getStart());
            String selected = text.substring(selection.getStart(), selection.getEnd());
            selected = selected.replaceAll("<color=\"0x.{0,9}>", "");
            selected = selected.replaceAll("</color>", "");
            String right = text.substring(selection.getEnd(), text.length());
            lastFocusedVerseTextArea.setText(left + "<color=\"" + value.toString() + "\">" + selected + "</color>" + right);
        });
        LanguageService languageService = ServiceManager.getLanguageService();
        languages = languageService.findAll();
        languageComboBox.getItems().addAll(languages);
        textAreas.getChildren().clear();
        verseControllers.clear();
    }

    private void initializeNewVerseButton() {
        newVerseButton.setOnAction(event -> addNewSongVerse(new SongVerse()));
    }

    private void toProjectionScreen() {
        int caretPosition = textArea.getCaretPosition();

        System.out.println(caretPosition);
        String[] split = textArea.getText().split("\n\n");
        for (String i : split) {
            if (i.length() >= caretPosition) {
                StringBuilder result = new StringBuilder();
                for (String j : i.split("\n")) {
                    if (!j.startsWith(SongVerse.CHORUS) && !j.toLowerCase().startsWith("[verse]")) {
                        result.append(j).append("\n");
                    }
                }
                previewProjectionScreenController.setText(result.substring(0, result.length() - 1),
                        ProjectionType.SONG);
                break;
            } else {
                caretPosition -= (i.length() + 2);
                System.out.println(caretPosition);
            }
        }
    }

    private void initializeRadioButtons() {
        final ToggleGroup toggleGroup = new ToggleGroup();
        verseEditorRadioButton.setToggleGroup(toggleGroup);
        rawTextEditorRadioButton.setToggleGroup(toggleGroup);
        verseEditorRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            borderPane.getChildren().clear();
            if (newValue) {
                borderPane.setCenter(verseEditorScrollPane);
                verseControllers.clear();
                textAreas.getChildren().clear();
                String textAreaText = textArea.getText();
                String[] split = textAreaText.split("\n\n");
                for (String i : split) {
                    SongVerse songVerse = new SongVerse();
                    String s = i.trim();
                    if (s.startsWith(SongVerse.CHORUS + "\n")) {
                        songVerse.setChorus(true);
                        s = s.substring(SongVerse.CHORUS.length() + 1, s.length());
                    }
                    songVerse.setText(s);
                    addNewSongVerse(songVerse);
                }
                colorPicker.setDisable(false);
                saveButton.setDisable(false);
                uploadButton.setDisable(false);
            } else {
                colorPicker.setDisable(true);
                saveButton.setDisable(true);
                uploadButton.setDisable(true);
                String text = getRawTextFromVerses();
                textArea.setText(text);
                borderPane.setCenter(rawTextBorderPane);
            }
        });
        verseEditorRadioButton.setSelected(true);
    }

    private String getRawTextFromVerses() {
        StringBuilder text = new StringBuilder();
        for (VerseController verseController : verseControllers) {
            final SongVerse songVerse = verseController.getSongVerse();
            if (songVerse.isChorus()) {
                text.append(SongVerse.CHORUS + "\n");
            }
            final String rawText = verseController.getRawText();
            if (!rawText.isEmpty()) {
                text.append(rawText).append("\n\n");
            }
        }
        return text.toString().trim();
    }

    private List<SongVerse> getVerses() {
        List<SongVerse> songVerses = new ArrayList<>(verseControllers.size());
        for (VerseController verseController : verseControllers) {
            final SongVerse songVerse = verseController.getSongVerse();
            songVerses.add(songVerse);
        }
        return songVerses;
    }

    void setPreviewProjectionScreenController(ProjectionScreenController previewProjectionScreenController) {
        this.previewProjectionScreenController = previewProjectionScreenController;
    }

    private boolean isEdit() {
        return edit;
    }

    void setEdit() {
        this.edit = true;
    }

    void setSongController(SongController songController) {
        this.songController = songController;
    }

    void setStage(Stage stage, Stage stage2) {
        this.stage = stage;
        this.stage2 = stage2;
    }

    public void saveButtonOnAction() {
        if (saveSong()) {
            songController.addSong(newSong);
        }
    }

    private boolean saveSong() {
        Language selectedLanguage = languageComboBox.getSelectionModel().getSelectedItem();
        if (selectedLanguage == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("No language!");
            alert.setContentText("Please select a language! Or create a new one at https://projector-songbook.herokuapp.com");
            alert.showAndWait();
            return false;
        }
        if (titleTextField.getText().trim().isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("No Title!");
            alert.setContentText("Please type a title");
            alert.showAndWait();
            return false;
        }
        final Date createdDate = new Date();
        if (isEdit()) {
            songController.removeSongFromList(selectedSong);
            songVerseService.delete(editingSong.getVerses());
            newSong = editingSong;
        } else {
            newSong = new Song();
            newSong.setCreatedDate(createdDate);
        }
        newSong.setLanguage(selectedLanguage);
        newSong.setTitle(titleTextField.getText().trim());
        newSong.setModifiedDate(createdDate);
        newSong.setPublished(false);
        newSong.setPublish(uploadCheckBox.isSelected());
        if (verseEditorRadioButton.isSelected()) {
            newSong.setVerses(getVerses());
        } else {
            throw new NotImplementedException();
        }
        try {
            songService.create(newSong);
        } catch (ServiceException e) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Could not save song!");
            alert.setContentText("Please try again later");
            alert.showAndWait();
            return false;
        }
        stage.close();
        stage2.close();
        return true;
    }

    void setTitleTextFieldText(String text) {
        titleTextField.setText(text);
    }

    void setEditingSong(Song selectedSong) {
        editingSong = selectedSong;
        textAreas.getChildren().clear();
        verseControllers.clear();
        for (SongVerse songVerse : selectedSong.getVerses()) {
            if (!songVerse.isRepeated()) {
                addNewSongVerse(songVerse);
            }
        }
    }

    private void addNewSongVerse(SongVerse songVerse) {
        ObservableList<Node> textAreasChildren = textAreas.getChildren();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/view/song/Verse.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            VerseController verseController = loader.getController();
            verseController.setSongVerse(songVerse);
            verseControllers.add(verseController);
            final TextArea textArea = verseController.getTextArea();
            textArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    previewProjectionScreenController.setText(verseController.getRawText(), ProjectionType.SONG);
                    lastFocusedVerse = verseController;
                }
            });
            textArea.textProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.setText(verseController.getRawText(), ProjectionType.SONG));
            TextArea secondTextArea = verseController.getSecondTextArea();
            secondTextArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    previewProjectionScreenController.setText(secondTextArea.getText(), ProjectionType.SONG);
                }
            });
            secondTextArea.textProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.setText(secondTextArea.getText(), ProjectionType.SONG));
            textAreasChildren.add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setSelectedSong(SearchedSong selectedSong) {
        this.selectedSong = selectedSong;
        Language language = selectedSong.getSong().getLanguage();
        if (language != null) {
            for (Language language1 : languages) {
                if (language1.getId().equals(language.getId())) {
                    languageComboBox.getSelectionModel().select(language1);
                    break;
                }
            }
        }
    }

    public void uploadButtonOnAction() {
        if (saveSong()) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Main.class.getResource("/view/Login.fxml"));
                loader.setResources(Settings.getInstance().getResourceBundle());
                Pane root = loader.load();
                LoginController loginController = loader.getController();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();
                loginController.addListener(user -> {
                    SongApiBean songApiBean = new SongApiBean();
                    Language byId = ServiceManager.getLanguageService().findById(newSong.getLanguage().getId());
                    newSong.setLanguage(byId);
                    try {
                        final Song song = songApiBean.updateSong(newSong, user);
                        if (song == null) {
                            LOG.info("Cannot update");
                        } else {
                            newSong.setUuid(song.getUuid());
                            newSong.setModifiedDate(song.getModifiedDate());
                            newSong.setPublished(true);
                            songService.create(newSong);
                            songController.addSong(newSong);
                            stage.close();
                        }
                    } catch (ApiException e) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        final ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
                        alert.setTitle("Error");
                        alert.setHeaderText(resourceBundle.getString(e.getMessage()));
                        alert.showAndWait();
                    }
                });
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public void secondTextOnAction() {
        boolean selected = secondTextToggleButton.isSelected();
        for (VerseController verseController : verseControllers) {
            verseController.showSecondText(selected);
        }
    }
}
