package projector.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.MainDesktop;
import projector.application.Settings;
import projector.controller.song.ScheduleController;
import projector.controller.song.SongController;
import projector.network.TCPClient;
import projector.network.TCPServer;
import projector.remote.RemoteServer;
import projector.utils.GlobalKeyListenerExample;

import java.awt.*;
import java.io.IOException;
import java.util.ResourceBundle;

import static projector.controller.BibleController.setSceneStyleFile;

public class MyController {

    private static final Logger LOG = LoggerFactory.getLogger(MyController.class);
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu settingsMenu;
    @FXML
    private ToggleButton showProjectionScreenToggleButton;
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
    private UtilsController utilsController;
    @SuppressWarnings("FieldCanBeLocal")
    @FXML
    private ScheduleController scheduleController;
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
    private Settings settings;
    @FXML
    private Tab songsTab;
    @FXML
    private Tab bibleSearchTab;
    @FXML
    private Tab bibleTab;
    @FXML
    private Tab recentTab;
    private MainDesktop mainDesktop;
    private Stage settingsStage;

    public static double calculateSizeByScale(int size) {
        double screenScale = screenScale();
        return size / screenScale;
    }

    private static double screenScale() {
        double screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
        return screenResolution / 96;
    }

    public void setPrimaryStage(Stage primaryStage) {
        projectionScreenController.setPrimaryStage(primaryStage);
        primaryStage.toFront();
        primaryStage.requestFocus();
    }

    public void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        this.projectionScreenController = projectionScreenController;
        bibleController.setProjectionScreenController(projectionScreenController);
        songController.setProjectionScreenController(projectionScreenController);
        settingsController.setProjectionScreenController(projectionScreenController);
        settingsController.setSongController(songController);
        utilsController.setProjectionScreenController(projectionScreenController);
        projectionScreenController.setBlank(true);
        projectionScreenController.setSongController(songController);
        if (settings.isPreviewLoadOnStart()) {
            projectionScreenController.createPreview();
        }
        if (settings.isAllowRemote()) {
            RemoteServer.startRemoteServer(projectionScreenController, songController);
        }
        //initializeGlobalKeyListener(projectionScreenController);
        automaticNetworks();
    }

    @SuppressWarnings("unused")
    private void initializeGlobalKeyListener(ProjectionScreenController projectionScreenController) {
        try {
            GlobalScreen.registerNativeHook();
            GlobalKeyListenerExample nativeKeyListener = new GlobalKeyListenerExample();
            nativeKeyListener.setProjectionScreenController(projectionScreenController);
            GlobalScreen.addNativeKeyListener(nativeKeyListener);
        } catch (Exception ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
        }
    }

    public void initialize() {
        settings = Settings.getInstance();
        settings.setBibleController(bibleController);
        initializeSettingsController();
        bibleSearchController.setBibleController(bibleController);
        bibleSearchController.setMainController(this);
        bibleController.setMainController(this);
        songController.setMainController(this);
        bibleController.setBibleSearchController(bibleSearchController);
        bibleController.setRecentController(recentController);
        bibleController.setHistoryController(historyController);
        bibleController.setSettingsController(settingsController);
        songController.setRecentController(recentController);
        scheduleController = new ScheduleController();
        songController.setScheduleController(scheduleController);
        recentController.setSongController(songController);
        recentController.setBibleController(bibleController);
        scheduleController.setSongController(songController);
        settingsController.setSettings(settings);
        historyController.setBibleController(bibleController);
        blankButton.setFocusTraversable(false);
        lockButton.setFocusTraversable(false);
        previewButton.setFocusTraversable(false);
        // accentsButton.setFocusTraversable(false);
        // accentsButton.setSelected(settings.isWithAccents());
        blankButton.setSelected(false);
        // recentController.setBlank(blankButton.isSelected());
//        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println(newValue.getText());
//        });
        SingleSelectionModel<Tab> tabPaneSelectionModel = tabPane.getSelectionModel();
        tabPaneSelectionModel.selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(4)) {
                historyController.loadRecents();
            }
        });
        tabPaneSelectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(songsTab)) {
                songController.lazyInitialize();
            } else if (newValue.equals(bibleSearchTab)) {
                bibleSearchController.lazyInitialize();
                bibleSearchController.initializeBibles();
            } else if (newValue.equals(bibleTab)) {
                bibleController.lazyInitialize();
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

    public void initialTabSelect() {
        tabPane.getSelectionModel().select(songsTab);
    }

    private void automaticNetworks() {
        if (settings.isShareOnLocalNetworkAutomatically()) {
            TCPServer.startShareNetwork(projectionScreenController, songController);
        }
        if (settings.isConnectToSharedAutomatically()) {
            TCPClient.connectToShared(projectionScreenController);
        }
    }

    private void initializeSettingsController() {
        ResourceBundle resourceBundle = settings.getResourceBundle();
        String title = resourceBundle.getString("Settings");
        Label menuLabel = new Label(title);
        menuLabel.setOnMouseClicked(event -> onSettingsMenu());
        settingsMenu.setText("");
        settingsMenu.setGraphic(menuLabel);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/Settings.fxml"));
        loader.setResources(resourceBundle);
        try {
            Pane root = loader.load();
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int height = gd.getDisplayMode().getHeight();
            Scene scene = new Scene(root, 850, calculateSizeByScale(height - 100));
            setSceneStyleFile(scene);
            settingsStage = new Stage();
            settingsStage.setScene(scene);
            settingsStage.setTitle(title);
            settingsController = loader.getController();
            settingsController.setStage(settingsStage);
        } catch (IOException ignored) {
        }
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
        if (settings.isAllowRemote()) {
            RemoteServer.close();
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
            songController.selectNextSongFromScheduleIfLastIndex();
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

    public void showHideProjectionScreen() {
        if (showProjectionScreenToggleButton.isSelected()) {
            mainDesktop.setProjectionScreenStage();
        } else {
            mainDesktop.hideProjectionScreen();
        }
    }

    public void setMain(MainDesktop mainDesktop) {
        this.mainDesktop = mainDesktop;
    }

    public EventHandler<KeyEvent> globalKeyEventHandler() {
        return event -> {
            try {
                if (event.getCode() == KeyCode.F1) {
                    setBlank();
                    event.consume();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        };
    }

    public SettingsController getSettingsController() {
        return settingsController;
    }

    public void onSettingsMenu() {
        settingsController.lazyInitialize();
        settingsStage.show();
        settingsStage.toFront();
    }

    public BibleController getBibleController() {
        return bibleController;
    }

    public SongController getSongController() {
        return songController;
    }

    public void createCustomCanvas() {
        projectionScreenController.createCustomStage(settings.getCustomCanvasWidth(), settings.getCustomCanvasHeight());
    }
}
