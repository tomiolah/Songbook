package projector;

import javafx.application.Application;
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
import projector.application.Settings;
import projector.controller.MyController;
import projector.controller.ProjectionScreenController;
import projector.repository.ormLite.ConvertSongs;

import java.io.File;
import java.util.ListIterator;

public class Main extends Application {

    private MyController myController;
    private ProjectionScreenController projectionScreenController;
    private double canvasHeight;
    private double canvasWidth;
    private Stage tmpStage;
    private Popup popup;

    public static void main(String[] args) {
        File file = new File("data/projector.mv.db");
        if (!file.exists()) {
            ConvertSongs.convertToDatabase();
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ObservableList<Screen> screen = Screen.getScreens();
        ListIterator<Screen> it = screen.listIterator();
        Screen nextScreen;
        it.next();
        Scene primaryScene = null;
        Stage canvasStage;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/MainView.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            myController = loader.getController();
            Settings settings = Settings.getInstance();
            primaryScene = new Scene(root, settings.getMainWidth(), settings.getMainHeight());
            primaryScene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
            primaryStage.setMinHeight(600);
            primaryScene.setOnKeyPressed(event -> {
                if (event.isControlDown()) {
                    if (event.getCode() == KeyCode.DIGIT1) {
                        myController.selectTab(1);
                    } else if (event.getCode() == KeyCode.DIGIT2) {
                        myController.selectTab(2);
                    } else if (event.getCode() == KeyCode.DIGIT3) {
                        myController.selectTab(3);
                    } else if (event.getCode() == KeyCode.DIGIT4) {
                        myController.selectTab(4);
                    } else if (event.getCode() == KeyCode.DIGIT5) {
                        myController.selectTab(5);
                    }
                }
                if (event.isControlDown()) {
                    myController.setSelecting(true);
                }
                if (event.getCode() == KeyCode.F1) {
                    myController.setBlank();
                }
//                if (event.getCode() == KeyCode.F3) {
//                    setCanvasToSecondScreen();
//                }
                if (event.getCode() == KeyCode.F8) {
                    myController.duplicateCanvas();
                }
                if (event.getCode() == KeyCode.F5) {
                    myController.previewCanvas();
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
            loader.setLocation(Main.class.getResource("/view/ProjectionScreen.fxml"));
            Pane root = loader.load();
            projectionScreenController = loader.getController();
            Scene scene;

            double positionX;
            double positionY;
            if (it.hasNext()) {
                nextScreen = it.next();
                Rectangle2D bounds = nextScreen.getVisualBounds();
                positionX = bounds.getMinX() + 0;
                positionY = bounds.getMinY() + 0;
                canvasWidth = bounds.getWidth();
                canvasHeight = bounds.getHeight();
                projectionScreenController.setWidth(canvasWidth);
                projectionScreenController.setHeight(canvasHeight);
                popup = new Popup();
                popup.getContent().add(root);
                popup.show(primaryStage, 80, 60);
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
                projectionScreenController.loadEmpty();
            } else {
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
            }
            scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
            primaryStage.setOnCloseRequest(we -> {
                System.out.println("Stage is closing");
                Settings settings = Settings.getInstance();
                settings.setMainHeight(primaryStage.getScene().getHeight());
                settings.setMainWidth(primaryStage.getScene().getWidth());
                settings.increaseCurrentBibleUsage();
                settings.save();
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
                    } else if (popup != null) {
                        if (event.getCode() == KeyCode.F2) {
                            popup.show(primaryStage);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something wrong!");
        }
        primaryStage.requestFocus();
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        myController.setProjectionScreenController(projectionScreenController);
    }

    private void setCanvasToSecondScreen() {
        ObservableList<Screen> screen = Screen.getScreens();
        ListIterator<Screen> it = screen.listIterator();
        it.next();
        if (it.hasNext() && popup != null) {
            Screen nextScreen;
            nextScreen = it.next();
            Rectangle2D bounds = nextScreen.getVisualBounds();
            double positionX = bounds.getMinX() + 0;
            double positionY = bounds.getMinY() + 0;
            canvasWidth = bounds.getWidth();
            canvasHeight = bounds.getHeight();
            projectionScreenController.setWidth(canvasWidth);
            projectionScreenController.setHeight(canvasHeight);
            popup.setWidth(canvasWidth);
            popup.setHeight(canvasHeight);
            popup.setX(positionX);
            popup.setY(positionY);
        }
    }
}