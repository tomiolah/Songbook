package projector.utils;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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

    public static Stage getCustomStage2(Class<?> aClass, Scene scene, double width, double height) {
        Stage stage = getCustomStage(aClass, scene);
        stage.setWidth(width);
        stage.setHeight(height);
        return stage;
    }

    public static Stage getCustomStage3(Class<?> aClass, Pane root) {
        return getCustomStage2(aClass, new Scene(root), root.getPrefWidth(), root.getPrefHeight());
    }

    public static WindowController createWindowController(Class<?> aClass, Scene scene, Stage stage) {
        try {
            if (stage.getStyle() != StageStyle.TRANSPARENT) {
                stage.initStyle(StageStyle.TRANSPARENT);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        WindowController windowController = WindowController.getInstance(aClass, stage, scene);
        if (windowController == null) {
            return null;
        }
        Scene windowControllerScene = windowController.getScene();
        windowControllerScene.setFill(Color.TRANSPARENT);
        stage.setScene(windowControllerScene);
        return windowController;
    }
}
