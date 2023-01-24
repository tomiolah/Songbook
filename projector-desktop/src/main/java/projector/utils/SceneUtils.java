package projector.utils;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import projector.application.Settings;
import projector.controller.util.WindowController;

import java.io.InputStream;
import java.net.URL;

public class SceneUtils {

    public static void addIconToStage(Stage stage, Class<?> aClass) {
        addOneIcon(stage, aClass, "/icons/icon32.png");
        addOneIcon(stage, aClass, "/icons/icon24.png");
        addOneIcon(stage, aClass, "/icons/icon16.png");
    }

    private static void addOneIcon(Stage stage, Class<?> aClass, String name) {
        InputStream resourceAsStream = aClass.getResourceAsStream(name);
        if (resourceAsStream != null) {
            stage.getIcons().add(new Image(resourceAsStream));
        }
    }

    public static void addStylesheetToScene(Scene scene, Class<?> aClass, String s) {
        URL resource = aClass.getResource(s);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    public static void addStylesheetToSceneBySettings(Scene scene, Class<?> aClass) {
        addStylesheetToScene(scene, aClass, "/view/" + Settings.getInstance().getSceneStyleFile());
    }

    public static Stage getAStage(Class<?> aClass) {
        Stage stage = new Stage();
        addIconToStage(stage, aClass);
        return stage;
    }

    public static Stage getCustomStage(Class<?> aClass, Scene scene) {
        Stage stage = getAStage(aClass);
        createWindowController(aClass, scene, stage);
        return stage;
    }

    public static WindowController createWindowController(Class<?> aClass, Scene scene, Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        WindowController windowController = WindowController.getInstance(aClass, stage, scene);
        if (windowController == null) {
            return null;
        }
        stage.setScene(windowController.getScene());
        return windowController;
    }
}
