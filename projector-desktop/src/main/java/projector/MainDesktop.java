package projector;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.application.Updater;
import projector.controller.BibleController;
import projector.controller.MyController;
import projector.controller.ProjectionScreenController;
import projector.controller.song.SongController;
import projector.controller.util.ProjectionScreenHolder;
import projector.controller.util.ProjectionScreensUtil;

import java.net.URL;
import java.util.Date;
import java.util.ListIterator;

public class MainDesktop extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MainDesktop.class);
    private MyController myController;
    private ProjectionScreenController projectionScreenController;
    private Stage tmpStage;
    private Stage primaryStage;
    private Scene primaryScene;
    private Stage canvasStage;
    private ObservableList<Screen> screen;
    private Settings settings;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Date date = new Date();
        this.primaryStage = primaryStage;
        primaryScene = null;
        settings = Settings.getInstance();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/MainView.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
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
            primaryScene.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
            primaryStage.setMinHeight(600);
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

            primaryStage.setScene(primaryScene);
            primaryStage.show();
            primaryStage.setTitle("Projector");
            primaryStage.setX(0);
            primaryStage.setY(0);
            myController.setPrimaryStage(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("primary stage loaded---------------");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
            Pane root = loader.load();
            projectionScreenController = loader.getController();
            projectionScreenController.setRoot(root);
            ProjectionScreensUtil.getInstance().addProjectionScreenController(projectionScreenController, "Main projection");
            setProjectionScreen();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something wrong!");
        }
        primaryStage.requestFocus();
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        myController.setProjectionScreenController(projectionScreenController);
        myController.setMain(this);
        myController.getSettingsController().addOnSaveListener(() -> {
            primaryScene.getStylesheets().clear();
            URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
            String url = resource.toExternalForm();
            setUserAgentStylesheet(null);
            primaryScene.getStylesheets().add(url);
        });
        projectionScreenController.setText("<color=\"0xffffff0c\">.</color>", ProjectionType.REFERENCE);
        projectionScreenController.setBlank(false);
        Updater.getInstance().checkForUpdate();
        Date date1 = new Date();
        System.out.println(date1.getTime() - date.getTime());
    }

    private void setProjectionScreen() {
        screen = Screen.getScreens();
        screen.addListener((ListChangeListener<Screen>) c -> setProjectionScreenStage());
        setProjectionScreenStage();
        primaryStage.setOnCloseRequest(we -> {
            System.out.println("Stage is closing");
            Settings settings = Settings.getInstance();
            settings.setMainHeight(primaryStage.getScene().getHeight());
            settings.setMainWidth(primaryStage.getScene().getWidth());
            settings.save();
            settings.setApplicationRunning(false);
            if (tmpStage != null) {
                tmpStage.close();
            }
            myController.close();
            projectionScreenController.onClose();
        });
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
                } else {
                    Popup popup = projectionScreenController.getPopup();
                    if (popup != null) {
                        if (event.getCode() == KeyCode.F2) {
                            popup.show(primaryStage);
                        }
                    }
                }
            });
        }
    }

    public void setProjectionScreenStage() {
        ListIterator<Screen> it = screen.listIterator(0);
        if (!it.hasNext()) {
            return;
        }
        it.next(); // primary screen
        showProjectionScreenOnNextScreen(it);
        Integer index = 0;
        while (it.hasNext()) {
            Screen nextScreen = it.next();
            try {
                ProjectionScreenController projectionScreenController = getProjectionScreenControllerOrDuplicate(index);
                projectionScreenController.setPrimaryStageVariable(primaryStage);
                createPopupForNextScreen(nextScreen, projectionScreenController);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            ++index;
        }
    }

    private ProjectionScreenController getProjectionScreenControllerOrDuplicate(Integer index) {
        ProjectionScreenHolder projectionScreenHolder = ProjectionScreensUtil.getInstance().getScreenHolderByIndex(index);
        ProjectionScreenController projectionScreenController;
        if (projectionScreenHolder != null) {
            projectionScreenController = projectionScreenHolder.getProjectionScreenController();
        } else {
            projectionScreenController = this.projectionScreenController.duplicate2();
        }
        return projectionScreenController;
    }

    private void showProjectionScreenOnNextScreen(ListIterator<Screen> it) {
        try {
            if (it.hasNext() && (canvasStage == null || !canvasStage.isShowing())) {
                projectionScreenController.setPrimaryStageVariable(primaryStage);
                createPopupForNextScreen(it.next(), projectionScreenController);
            } else {
                createCanvasStage();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void createPopupForNextScreen(Screen nextScreen, ProjectionScreenController projectionScreenController) {
        if (projectionScreenController == null || nextScreen == null) {
            return;
        }
        projectionScreenController.setMainDesktop(this);
        projectionScreenController.setScreen(nextScreen);
        Rectangle2D bounds = nextScreen.getBounds();
        double positionX = bounds.getMinX() + 0;
        //            GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        //            double mainScreenWidth = mainScreen.getBounds().getWidth();
        //            int mainScreenTrueWidth = screenDevices[0].getDisplayMode().getWidth();
        //            double scale = mainScreenTrueWidth / mainScreenWidth;
        //            positionX = mainScreenWidth + (mainScreenTrueWidth - mainScreenWidth) / scale;
        double positionY = bounds.getMinY();
        double canvasWidth = bounds.getWidth();
        double canvasHeight = bounds.getHeight();
        projectionScreenController.setWidth(canvasWidth);
        projectionScreenController.setHeight(canvasHeight);
        boolean loadEmpty = true;
        Popup popup = projectionScreenController.getPopup();
        if (popup != null) {
            popup.getContent().clear();
            popup.hide();
            loadEmpty = false;
        }
        popup = new Popup();
        projectionScreenController.setPopup(popup);
        popup.getContent().add(projectionScreenController.getRoot());
        popup.setAutoFix(false);
        Stage primaryStage = projectionScreenController.getPrimaryStageVariable();
        popup.show(primaryStage, positionX, positionY);
        popup.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != canvasWidth) {
                projectionScreenController.getPopup().setWidth(canvasWidth);
                projectionScreenController.setWidth(canvasWidth);
            }
        });
        popup.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != canvasHeight) {
                projectionScreenController.getPopup().setHeight(canvasHeight);
                projectionScreenController.setHeight(canvasHeight);
            }
        });
        popup.setWidth(canvasWidth);
        popup.setHeight(canvasHeight);
        popup.setX(positionX);
        popup.setY(positionY);
        popup.setHideOnEscape(false);
        Scene scene = popup.getScene();
        scene.setCursor(Cursor.NONE);
        projectionScreenController.setScene(scene);
        if (loadEmpty) {
            projectionScreenController.loadEmpty();
        }
        setSceneStyleSheet(scene);
    }

    private void createCanvasStage() {
        Popup popup = projectionScreenController.getPopup();
        if (popup != null) {
            popup.getContent().clear();
            popup.hide();
            projectionScreenController.setPopup(null);
        }
        if (canvasStage != null) {
            canvasStage.show();
            return;
        }
        Scene scene = new Scene(projectionScreenController.getRoot(), 800, 600);
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

        projectionScreenController.setStage(canvasStage);
        scene.widthProperty().addListener((observable, oldValue, newValue) -> projectionScreenController.repaint());
        scene.heightProperty().addListener((observable, oldValue, newValue) -> projectionScreenController.repaint());
        canvasStage.show();
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
        setSceneStyleSheet(scene);
    }

    private void setSceneStyleSheet(Scene scene) {
        URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
        if (resource == null) {
            return;
        }
        scene.getStylesheets().add(resource.toExternalForm());
    }

    public void hideProjectionScreen() {
        projectionScreenController.hidePopups();
        if (canvasStage != null) {
            canvasStage.hide();
        }
    }
}
