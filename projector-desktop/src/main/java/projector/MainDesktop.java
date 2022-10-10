package projector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ApplicationUtil;
import projector.application.ApplicationVersion;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.application.Updater;
import projector.controller.BibleController;
import projector.controller.FirstSetupController;
import projector.controller.MyController;
import projector.controller.ProjectionScreenController;
import projector.controller.song.SongController;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ListIterator;

import static java.lang.Thread.sleep;
import static projector.utils.SceneUtils.addIconToStage;
import static projector.utils.SceneUtils.addStylesheetToSceneBySettings;

public class MainDesktop extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MainDesktop.class);
    private static Pane globalRoot;
    private MyController myController;
    private ProjectionScreenController projectionScreenController;
    private double canvasHeight;
    private double canvasWidth;
    private Stage tmpStage;
    private Popup popup;
    private Pane root;
    private Stage primaryStage;
    private Scene primaryScene;
    private Stage canvasStage;
    private ObservableList<Screen> screen;
    private Settings settings;
    private Date startDate;

    public static void main(String[] args) {
        launch(args);
    }

    public static Pane getRoot() {
        return globalRoot;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            ApplicationUtil.getInstance().setPrimaryStage(primaryStage);
            if (ApplicationVersion.getInstance().getVersion() < 25 && ApplicationVersion.getInstance().isNotTesting()) {
                openFirstSetupView(primaryStage);
            } else {
                openLauncherView(primaryStage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
        }
    }

    private void openLauncherView(Stage primaryStage) throws IOException {
        startDate = new Date();
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/LauncherView.fxml"));
        BorderPane borderPane = loader.load();
        Scene scene = new Scene(borderPane, borderPane.getPrefWidth(), borderPane.getPrefHeight());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        addIconToStage(stage, getClass());
        stage.setTitle("Projector - starting");
        Thread thread = new Thread(() -> {
            try {
                sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Platform.runLater(() -> {
                try {
                    start2(primaryStage);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                stage.close();
            });
        });
        stage.show();
        if (ApplicationVersion.getInstance().isNotTesting()) {
            thread.start();
        } else {
            start2(primaryStage);
            primaryStage.show();
        }
    }

    private void openFirstSetupView(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/FirstSetupView.fxml"));
        BorderPane borderPane = loader.load();
        FirstSetupController firstSetupController = loader.getController();
        firstSetupController.setListener(() -> start2(primaryStage));
        Scene scene = new Scene(borderPane, borderPane.getPrefWidth(), borderPane.getPrefHeight());
        primaryStage.setScene(scene);
        Class<?> aClass = getClass();
        addIconToStage(primaryStage, aClass);
        primaryStage.setTitle("Projector - setup");
        primaryStage.show();
    }

    public void start2(Stage primaryStage) {
        loadInBackGround();
        addIconToStage(primaryStage, getClass());
        primaryStage.setMinHeight(600);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
        primaryStage.setTitle("Projector");
        primaryStage.setX(0);
        primaryStage.setY(0);
        myController.setPrimaryStage(primaryStage);
        primaryStage.requestFocus();
        if (canvasStage != null) {
            canvasStage.show();
        }
        myController.initialTabSelect();
    }

    public void loadInBackGround() {
        Settings.shouldBeNull();
        Updater.getInstance().saveApplicationStartedWithVersion();
        primaryScene = null;
        settings = Settings.getInstance();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/MainView.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            MainDesktop.globalRoot = root;
            myController = loader.getController();
            BibleController bibleController = myController.getBibleController();
            SongController songController = myController.getSongController();
            primaryScene = new Scene(root, settings.getMainWidth(), settings.getMainHeight());
            primaryScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.isAltDown()) {
                    event.consume();
                }
                bibleController.onKeyEvent(event);
                songController.onKeyEvent(event);
            });
            addStylesheetToSceneBySettings(primaryScene, getClass());
            primaryScene.setOnKeyPressed(event -> {
                KeyCode keyCode = event.getCode();
                if (event.isControlDown()) {
                    if (keyCode == KeyCode.DIGIT1) {
                        myController.selectTab(1);
                    } else if (keyCode == KeyCode.DIGIT2) {
                        myController.selectTab(2);
                    } else if (keyCode == KeyCode.DIGIT3) {
                        myController.selectTab(3);
                    } else if (keyCode == KeyCode.DIGIT4) {
                        myController.selectTab(4);
                    } else if (keyCode == KeyCode.DIGIT5) {
                        myController.selectTab(5);
                    }
                }
                if (keyCode == KeyCode.PAGE_DOWN) {
                    myController.goNext();
                }
                if (keyCode == KeyCode.PAGE_UP) {
                    myController.goPrev();
                }
                if (event.isControlDown()) {
                    myController.setSelecting(true);
                }
                if (keyCode == KeyCode.F1) {
                    myController.setBlank();
                }
//                if (event.getCode() == KeyCode.F3) {
//                    setCanvasToSecondScreen();
//                }
                if (keyCode == KeyCode.F7) {
                    myController.createCustomCanvas();
                }
                if (keyCode == KeyCode.F8) {
                    myController.duplicateCanvas();
                }
                if (keyCode == KeyCode.F5) {
                    myController.previewCanvas();
                }
                if (event.isAltDown()) {
                    event.consume();
                }
            });
            primaryScene.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.CONTROL) {
                    myController.setSelecting(false);
                }
            });
            Scene tmpScene = primaryScene;
            primaryScene.addEventFilter(MouseEvent.DRAG_DETECTED, mouseEvent -> tmpScene.startFullDrag());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("primary stage loaded---------------");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
            root = loader.load();
            projectionScreenController = loader.getController();
            setProjectionScreen();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something wrong!");
        }
        myController.setProjectionScreenController(projectionScreenController);
        myController.setMain(this);
        myController.getSettingsController().addOnSaveListener(() -> {
            ObservableList<String> stylesheets = primaryScene.getStylesheets();
            stylesheets.clear();
            URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
            if (resource == null) {
                return;
            }
            String url = resource.toExternalForm();
            setUserAgentStylesheet(null);
            stylesheets.add(url);
        });
        projectionScreenController.setText("<color=\"0xffffff0c\">.</color>", ProjectionType.REFERENCE);
        projectionScreenController.setBlank(false);
        Updater.getInstance().checkForUpdate();
        if (startDate != null) {
            Date date1 = new Date();
            LOG.info((date1.getTime() - startDate.getTime()) + " ms");
        }
    }

    private void setProjectionScreen() {
        screen = Screen.getScreens();
        screen.addListener((ListChangeListener<Screen>) c -> setProjectionScreenStage());
        setProjectionScreenStage();
        primaryStage.setOnCloseRequest(we -> closeApplication());
        ApplicationUtil.getInstance().setListener(this::closeApplication);
        primarySceneEventHandler();
    }

    private void closeApplication() {
        System.out.println("Stage is closing");
        Settings settings = Settings.getInstance();
        settings.setApplicationRunning(false);
        settings.setMainHeight(primaryStage.getScene().getHeight());
        settings.setMainWidth(primaryStage.getScene().getWidth());
        settings.save();
        if (tmpStage != null) {
            tmpStage.close();
        }
        myController.close();
        projectionScreenController.onClose();
    }

    private void primarySceneEventHandler() {
        if (primaryScene != null) {
            primaryScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (tmpStage != null) {
                    if (event.getCode() == KeyCode.F2) {
                        tmpStage.show();
                        primaryStage.show();
                    }
                    if (event.getCode() == KeyCode.F3) {
                        tmpStage.setFullScreen(true);
                        tmpStage.show();
                    }
                } else if (popup != null) {
                    if (event.getCode() == KeyCode.F2) {
                        popup.show(primaryStage);
                    }
                }
            });
        }
    }

    public void setProjectionScreenStage() {
        ListIterator<Screen> it = screen.listIterator();
        if (!it.hasNext()) {
            return;
        }
        Screen mainScreen = it.next();
        Screen nextScreen;
        Scene scene;
        double positionX;
        double positionY;
        if (it.hasNext() && (canvasStage == null || !canvasStage.isShowing())) {
            nextScreen = it.next();
            Rectangle2D bounds = nextScreen.getVisualBounds();
            positionX = bounds.getMinX() + 0;
            positionY = 0;
            canvasWidth = bounds.getWidth();
            canvasHeight = bounds.getHeight();
            projectionScreenController.setWidth(canvasWidth);
            projectionScreenController.setHeight(canvasHeight);
            boolean loadEmpty = true;
            if (popup != null) {
                popup.getContent().clear();
                popup.hide();
                popup = null;
                loadEmpty = false;
            }
            popup = new Popup();
            popup.getContent().add(root);
            popup.show(primaryStage, positionX, 0);
            popup.widthProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() != canvasWidth) {
                    popup.setWidth(canvasWidth);
                    projectionScreenController.setWidth(canvasWidth);
                }
            });
            popup.heightProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() != canvasHeight) {
                    popup.setHeight(canvasHeight);
                    projectionScreenController.setHeight(canvasHeight);
                }
            });
            popup.setWidth(canvasWidth);
            popup.setHeight(canvasHeight);
            popup.setX(positionX);
            popup.setY(positionY);
            popup.setHideOnEscape(false);
            scene = popup.getScene();
            scene.setCursor(Cursor.NONE);
            projectionScreenController.setScene(scene);
            if (loadEmpty) {
                projectionScreenController.loadEmpty();
            }
            popup.xProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("newValue = " + newValue);
                double v = newValue.doubleValue();
                Rectangle2D mainScreenBounds = mainScreen.getBounds();
                if (mainScreenBounds.getMinX() <= v && v < mainScreenBounds.getMaxX()) {
                    if (popup != null) {
                        popup.getContent().clear();
                        popup.hide();
                        popup = null;
                    }
                }
            });
        } else {
            if (popup != null) {
                popup.getContent().clear();
                popup.hide();
                popup = null;
            }
            if (canvasStage != null) {
                canvasStage.show();
                return;
            }
            scene = new Scene(root, 800, 600);
            canvasStage = new Stage();
            canvasStage.setScene(scene);
            canvasStage.setTitle(Settings.getInstance().getResourceBundle().getString("Canvas"));
            tmpStage = canvasStage;
            canvasStage.setX(800);
            canvasStage.setY(0);
            canvasStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    tmpStage.setFullScreen(true);
                }
            });
            canvasStage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    tmpStage.setMaximized(false);
                }
            });
            canvasStage.setOnCloseRequest(event -> tmpStage.hide());
            addIconToStage(canvasStage, getClass());
            projectionScreenController.setStage(canvasStage);
            scene.widthProperty().addListener((observable, oldValue, newValue) -> projectionScreenController.repaint());
            scene.heightProperty().addListener((observable, oldValue, newValue) -> projectionScreenController.repaint());
            scene.setOnKeyPressed(event -> {
                KeyCode keyCode = event.getCode();
                if (keyCode == KeyCode.DOWN || keyCode == KeyCode.RIGHT) {
                    myController.goNext();
                } else if (keyCode == KeyCode.UP || keyCode == KeyCode.LEFT) {
                    myController.goPrev();
                } else if (keyCode == KeyCode.F3) {
                    if (tmpStage.isFullScreen()) {
                        tmpStage.setFullScreen(false);
                        primaryStage.requestFocus();
                    } else {
                        tmpStage.setFullScreen(true);
                        tmpStage.show();
                    }
                }
                myController.onKeyPressed(event);
            });
        }
        addStylesheetToSceneBySettings(scene, getClass());
    }

    public void hideProjectionScreen() {
        if (popup != null) {
            popup.hide();
            return;
        }
        if (canvasStage != null) {
            canvasStage.hide();
        }
    }
}
