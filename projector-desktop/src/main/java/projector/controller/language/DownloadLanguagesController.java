package projector.controller.language;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.Main;
import projector.api.LanguageApiBean;
import projector.application.Settings;
import projector.controller.song.SongController;
import projector.model.Language;
import projector.service.LanguageService;
import projector.service.ServiceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class DownloadLanguagesController {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadLanguagesController.class);
    private final Settings settings = Settings.getInstance();
    @FXML
    private Label label;
    @FXML
    private Button selectButton;
    @FXML
    private VBox listView;
    private List<Language> languages;
    private List<CheckBox> checkBoxes;
    private SongController songController;
    private LanguageService languageService;
    private Stage stage;

    public void initialize() {
        languageService = ServiceManager.getLanguageService();
        languages = languageService.findAll();
        checkBoxes = new ArrayList<>(languages.size());
        for (Language language : languages) {
            addLanguageToVBox(language);
        }
        LanguageApiBean languageApiBean = new LanguageApiBean();
        Thread thread = new Thread(() -> {
            List<Language> onlineLanguages = languageApiBean.getLanguages();
            if (onlineLanguages == null) {
                noInternetMessage();
                return;
            }
            HashMap<String, Boolean> hashMap = new HashMap<>();
            for (Language language : languages) {
                hashMap.put(language.getUuid(), true);
            }
            for (Language language : onlineLanguages) {
                if (!hashMap.containsKey(language.getUuid())) {
                    addLanguageToVBox(language);
                    languages.add(language);
                }
            }
        });
        thread.start();
        selectButton.setOnAction(event -> {
            try {
                for (int i = 0; i < languages.size(); ++i) {
                    languages.get(i).setSelected(checkBoxes.get(i).isSelected());
                }
                languageService.create(languages);
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Main.class.getResource("/view/song/DownloadSongs.fxml"));
                loader.setResources(settings.getResourceBundle());
                Pane root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle(Settings.getInstance().getResourceBundle().getString("Download songs"));
                stage.show();
                stage.setOnCloseRequest(event1 -> {
                    songController.initializeSongs();
                    songController.initializeLanguageComboBox();
                });
                this.stage.close();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    private void addLanguageToVBox(Language language) {
        CheckBox checkBox = new CheckBox(language.getEnglishName() + " - " + language.getNativeName());
        checkBox.setSelected(language.isSelected());
        Platform.runLater(() -> listView.getChildren().add(checkBox));
        checkBoxes.add(checkBox);
    }

    private void noInternetMessage() {
        final ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        final String no_internet_connection = resourceBundle.getString("No internet connection");
        final String try_again_later = resourceBundle.getString("Try again later");
        Platform.runLater(() -> label.setText(no_internet_connection + "! " + try_again_later + "!"));
    }

    public void setSongController(SongController songController) {
        this.songController = songController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
