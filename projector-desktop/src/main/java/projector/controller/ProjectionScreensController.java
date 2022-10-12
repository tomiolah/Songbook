package projector.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import projector.application.Settings;
import projector.controller.util.ProjectionScreenHolder;
import projector.controller.util.ProjectionScreensUtil;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ResourceBundle;

import static projector.controller.BibleController.setSceneStyleFile;
import static projector.controller.MyController.calculateSizeByScale;
import static projector.utils.SceneUtils.getAStage;

public class ProjectionScreensController {

    @FXML
    private VBox vBox;
    private boolean initialized = false;

    public void lazyInitialize() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
        List<ProjectionScreenHolder> projectionScreenHolders = projectionScreensUtil.getProjectionScreenHolders();
        vBox.getChildren().clear();
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            addProjectionScreenHolderToVBox(projectionScreenHolder);
        }
        projectionScreensUtil.addProjectionScreenListener(this::addProjectionScreenHolderToVBox);
    }

    private void addProjectionScreenHolderToVBox(ProjectionScreenHolder projectionScreenHolder) {
        ObservableList<Node> vBoxChildren = vBox.getChildren();
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setPrefHeight(50.0);
        hBox.setPrefWidth(300.0);
        hBox.setSpacing(10.0);
        Label label = new Label();
        label.setText(projectionScreenHolder.getName());
        ObservableList<Node> hBoxChildren = hBox.getChildren();
        hBoxChildren.add(label);
        Button settingsButton = new Button();
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        settingsButton.setText(resourceBundle.getString("Settings"));
        settingsButton.setOnAction(onSettingsAction(projectionScreenHolder));
        hBoxChildren.add(settingsButton);
        ToggleButton blankButton = new ToggleButton();
        blankButton.setMnemonicParsing(false);
        blankButton.setText(resourceBundle.getString("Blank"));
        blankButton.setOnAction(event -> projectionScreenHolder.getProjectionScreenController().toggleBlank());
        hBoxChildren.add(blankButton);
        if (projectionScreenHolder.getProjectionScreenController().getPopup() != null) {
            hBoxChildren.add(getShowProjectionScreenToggleButton(projectionScreenHolder));
        }
        vBoxChildren.add(hBox);
    }

    private ToggleButton getShowProjectionScreenToggleButton(ProjectionScreenHolder projectionScreenHolder) {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setContentDisplay(ContentDisplay.CENTER);
        toggleButton.setFocusTraversable(false);
        toggleButton.setGraphicTextGap(0.0);
        double size = 19.0;
        toggleButton.setPrefHeight(size);
        toggleButton.setPrefWidth(size);
        toggleButton.setSelected(true);
        toggleButton.setTextAlignment(TextAlignment.CENTER);
        toggleButton.setPadding(new Insets(4.0));
        toggleButton.setOnAction(event -> projectionScreenHolder.getProjectionScreenController().toggleShowHidePopup());
        ImageView imageView = new ImageView();
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        InputStream resourceAsStream = getClass().getResourceAsStream("../../icons/monitor.png");
        if (resourceAsStream != null) {
            imageView.setImage(new Image(resourceAsStream));
            toggleButton.setGraphic(imageView);
        }
        return toggleButton;
    }

    private EventHandler<ActionEvent> onSettingsAction(ProjectionScreenHolder projectionScreenHolder) {
        return new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
                String title = resourceBundle.getString("Settings") + " - " + projectionScreenHolder.getName();
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/view/ProjectionScreenSettings.fxml"));
                loader.setResources(resourceBundle);
                try {
                    Pane root = loader.load();
                    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                    int height = gd.getDisplayMode().getHeight();
                    Scene scene = new Scene(root, 850, calculateSizeByScale(height - 100));
                    setSceneStyleFile(scene);
                    Stage settingsStage = getAStage(getClass());
                    settingsStage.setScene(scene);
                    settingsStage.setTitle(title);
                    ProjectionScreenSettingsController settingsController = loader.getController();
                    settingsController.setProjectionScreenHolder(projectionScreenHolder);
                    settingsController.setStage(settingsStage);
                    settingsStage.show();
                } catch (IOException ignored) {
                }
            }
        };
    }
}
