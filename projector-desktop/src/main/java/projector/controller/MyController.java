package projector.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import projector.application.Settings;
import projector.controller.song.SongController;
import projector.network.TCPClient;
import projector.network.TCPServer;

import java.util.ResourceBundle;

public class MyController {

    @FXML
    private ProjectionScreenController projectionScreenController;
    @FXML
    private BibleController bibleController;
    @FXML
    private BibleSearchController bibleSearchController;
    @FXML
    private SongController songController;
    @FXML
    private RecentController recentController;
    @FXML
    private HistoryController historyController;
    @FXML
    private ScheduleController scheduleController;
    @FXML
    private SettingsController settingsController;
    @FXML
    private Button previewButton;
    @FXML
    private ToggleButton blankButton;
    @FXML
    private ToggleButton lockButton;
    @FXML
    private TabPane tabPane;
    //    @FXML
//    private Slider slider;
    // @FXML
    // private ToggleButton accentsButton;
    @FXML
    private Stage primaryStage;
    private Settings settings;
    @FXML
    private Tab songsTab;
    @FXML
    private Tab bibleSearchTab;
    @FXML
    private Tab bibleTab;
    @FXML
    private Tab recentTab;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // primaryStage.setMaximized(true);
        // primaryStage.setX(0);
        // primaryStage.setY(0);
    }

    public void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        this.projectionScreenController = projectionScreenController;
        bibleController.setProjectionScreenController(projectionScreenController);
        songController.setProjectionScreenController(projectionScreenController);
        settingsController.setProjectionScreenController(projectionScreenController);
        settingsController.setSongController(songController);
        projectionScreenController.setBlank(true);
        projectionScreenController.setSongController(songController);
        projectionScreenController.setPrimaryStage(primaryStage);
        if (settings.isPreviewLoadOnStart()) {
            projectionScreenController.createPreview();
        }
        primaryStage.toFront();
        primaryStage.requestFocus();
    }

    public void initialize() {
        settings = Settings.getInstance();
        bibleSearchController.setBibleController(bibleController);
        // bibleSearchController.setSettings();
        bibleController.setMainController(this);
        songController.setMainController(this);
        bibleController.setBibleSearchController(bibleSearchController);
        bibleController.setRecentController(recentController);
        bibleController.setHistoryController(historyController);
        songController.setRecentController(recentController);
        scheduleController = new ScheduleController();
        songController.setScheduleController(scheduleController);
        recentController.setSongController(songController);
        recentController.setBibleController(bibleController);
        scheduleController.setBibleController(bibleController);
        scheduleController.setSongController(songController);
        settingsController.setSettings(settings);
        settingsController.setBibleController(bibleController);
        historyController.setBibleController(bibleController);
        blankButton.setFocusTraversable(false);
        lockButton.setFocusTraversable(false);
        previewButton.setFocusTraversable(false);
        // accentsButton.setFocusTraversable(false);
        // accentsButton.setSelected(settings.isWithAccents());
        blankButton.setSelected(true);
        // recentController.setBlank(blankButton.isSelected());
//        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println(newValue.getText());
//        });
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(5)) {
                settingsController.setOldParallelBibleIndex();
            } else if (newValue.equals(4)) {
                historyController.loadRecents();
            }
        });
        tabPane.getSelectionModel().select(recentTab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(songsTab)) {
                songController.initializeSongs();
            } else if (newValue.equals(bibleSearchTab)) {
                bibleSearchController.stripBooks();
            } else if (newValue.equals(bibleTab)) {
                bibleController.initializeBibles();
            }
        });
        tabPane.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex()).getContent().requestFocus();
            }
        });
        tabPane.setFocusTraversable(false);
//        slider.valueProperty().addListener(new ChangeListener<Number>() {
//
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                System.out.println(newValue.doubleValue());
//                projectionScreenController.getStage().setOpacity(newValue.doubleValue());
//            }
//        });
    }

    public void setBlank() {
        blankButton.setSelected(!blankButton.isSelected());
        blankButtonOnAction();
    }

    public void setBlank(boolean selected) {
        if (blankButton.isSelected() != selected) {
            blankButton.setSelected(selected);
            projectionScreenController.setBlank(selected);
            songController.onBlankButtonSelected(selected);
        }
    }

    public void previewButtonOnAction() {
        projectionScreenController.createPreview();
    }

    public void blankButtonOnAction() {
        projectionScreenController.setBlank(blankButton.isSelected());
        // recentController.setBlank(blankButton.isSelected());
        songController.onBlankButtonSelected(blankButton.isSelected());
    }

    public void lockButtonOnAction() {
        projectionScreenController.setLock(lockButton.isSelected());
        final ResourceBundle resourceBundle = settings.getResourceBundle();
        if (lockButton.isSelected()) {
            lockButton.setText(resourceBundle.getString("Unlock"));
        } else {
            lockButton.setText(resourceBundle.getString("Lock"));
        }
    }

    // public void accentsButtonOnAction() {
    // bibleSearchController.setWithAccents(accentsButton.isSelected());
    // songController.setWithAccents(accentsButton.isSelected());
    // settings.setWithAccents(accentsButton.isSelected());
    // }

    public void close() {
        recentController.close();
        songController.onClose();
        bibleController.onClose();
        if (settings.isShareOnNetwork()) {
            TCPServer.close();
        }
        if (settings.isConnectedToShared()) {
            TCPClient.close();
        }
    }

    public void selectTab(int tabIndex) {
        tabPane.getSelectionModel().select(tabIndex - 1);
    }

    public void setSelecting(boolean isSelecting) {
        bibleController.setSelecting(isSelecting);
    }

    ToggleButton getBlankButton() {
        return blankButton;
    }

    public void goPrev() {
        if (tabPane.getSelectionModel().getSelectedIndex() == 0) {
            if (bibleController.getVerseListView().getSelectionModel().getSelectedIndex() - 1 >= 0) {
                bibleController.getVerseListView().getSelectionModel()
                        .clearAndSelect(bibleController.getVerseListView().getSelectionModel().getSelectedIndex() - 1);
            }
        } else if (tabPane.getSelectionModel().getSelectedIndex() == 2) {
            if (songController.getSongListView().getSelectionModel().getSelectedIndex() - 1 >= 0) {
                songController.getSongListView().getSelectionModel()
                        .clearAndSelect(songController.getSongListView().getSelectionModel().getSelectedIndex() - 1);
            }
        }
    }

    public void goNext() {
        if (tabPane.getSelectionModel().getSelectedIndex() == 0) {
            if (bibleController.getVerseListView().getSelectionModel().getSelectedIndex() + 1 < bibleController
                    .getVerseListView().getItems().size()) {
                bibleController.getVerseListView().getSelectionModel()
                        .clearAndSelect(bibleController.getVerseListView().getSelectionModel().getSelectedIndex() + 1);
            }
        } else if (tabPane.getSelectionModel().getSelectedIndex() == 2) {
            if (songController.getSongListView().getSelectionModel().getSelectedIndex() + 1 < songController
                    .getSongListView().getItems().size()) {
                songController.getSongListView().getSelectionModel()
                        .clearAndSelect(songController.getSongListView().getSelectionModel().getSelectedIndex() + 1);
            }
        }
    }

    public void duplicateCanvas() {
        projectionScreenController.duplicate();
    }

    public void previewCanvas() {
        projectionScreenController.createPreview();
    }

    public void onKeyPressed(KeyEvent event) {
        songController.onKeyPressed(event);
    }
}
