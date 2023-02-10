package projector.controller.util;

import com.goxr3plus.fxborderlessscene.borderless.BorderlessScene;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static projector.utils.ColorUtil.getMainBorderColor;

public class WindowController {
    private static final Logger LOG = LoggerFactory.getLogger(WindowController.class);

    @SuppressWarnings("unused")
    @FXML
    private StackPane mainStackPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Label topLabel;
    @FXML
    private Button minimize;
    @FXML
    private Button maximizeNormalize;
    @FXML
    private Button exit;
    @FXML
    private BorderPane mainBorderPane;

    private BorderlessScene borderlessScene;
    private StackPane root;

    public static WindowController getInstance(Class<?> aClass, Stage stage, Scene scene) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(aClass.getResource("/view/WindowController.fxml"));
        loader.setResources(Settings.getInstance().getResourceBundle());
        try {
            StackPane root = loader.load();
            URL resource = aClass.getResource("/view/" + Settings.getInstance().getSceneStyleFile());
            if (resource != null) {
                ObservableList<String> stylesheets = root.getStylesheets();
                stylesheets.clear();
                stylesheets.add(resource.toExternalForm());
            }
            WindowController windowController = loader.getController();
            windowController.root = root;
            windowController.setup(stage, scene);
            return windowController;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public StackPane getRoot() {
        return root;
    }

    /**
     * Checking the functionality of the Borderless Scene Library
     */
    private void setup(Stage stage, Scene scene) {
        setScene(scene);
        borderlessScene = new BorderlessScene(stage, getRoot());
        // To move the window around by pressing a node:
        borderlessScene.setMoveControl(topLabel);
        topLabel.setText(stage.getTitle());
        stage.titleProperty().addListener((observable, oldValue, newValue) -> topLabel.setText(newValue));
        String titleFocused = "windowTitleFocused";
        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<String> styleClass = topLabel.getStyleClass();
            if (newValue) {
                styleClass.add(titleFocused);
            } else {
                styleClass.remove(titleFocused);
            }
        });

        exit.setOnAction(a -> {
            EventHandler<WindowEvent> onCloseRequest = stage.getOnCloseRequest();
            if (onCloseRequest != null) {
                onCloseRequest.handle(null);
            }
            stage.close();
        });
        minimize.setOnAction(a -> stage.setIconified(true));
        maximizeNormalize.setOnAction(a -> borderlessScene.maximizeStage());
        borderlessScene.getController().maximizedProperty().addListener((observable, oldValue, newValue) -> {
            ImageView imageView = new ImageView();
            imageView.setFitHeight(29);
            imageView.setFitWidth(45);
            imageView.setPickOnBounds(true);
            imageView.setPreserveRatio(true);
            String s;
            if (newValue) {
                s = "maximized";
            } else {
                s = "maximize";
            }
            InputStream resourceAsStream = getClass().getResourceAsStream("../../../icons/" + s + ".png");
            if (resourceAsStream != null) {
                Image image = new Image(resourceAsStream);
                imageView.setImage(image);
            }
            maximizeNormalize.setGraphic(imageView);
        });
    }

    public Scene getScene() {
        return getRoot().getScene();
    }

    private void setScene(Scene scene) {
        mainBorderPane.setCenter(scene.getRoot());
        setBorderWidth();
    }

    private void setBorderWidth() {
        try {
            Screen screen = Screen.getPrimary();
            double scaleX = screen.getOutputScaleX();
            Border border = new Border(new BorderStroke(getMainBorderColor(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1 / scaleX)));
            mainBorderPane.setBorder(border);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public void setStylesheet(String stylesheet) {
        StackPane pane = getRoot();
        if (pane == null) {
            return;
        }
        ObservableList<String> stylesheets = pane.getStylesheets();
        stylesheets.clear();
        stylesheets.add(stylesheet);
    }
}
