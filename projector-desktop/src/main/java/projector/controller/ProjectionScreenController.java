package projector.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import projector.Main;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.song.SongController;
import projector.utils.scene.text.MyTextFlow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProjectionScreenController {

    @FXML
    private MyTextFlow textFlow;
    @FXML
    private MyTextFlow textFlow1;
    @FXML
    private BorderPane mainPane;
    @FXML
    private Pane pane;

    @FXML
    private Line progressLine;

    private Stage stage;
    private boolean isBlank;
    private Settings settings;

    //    private List<Text> textsList;
    private BibleController bibleController;
    private SongController songController;
    private ProjectionType projectionType = ProjectionType.BIBLE;
    private ProjectionScreenController parentProjectionScreenController;
    private ProjectionScreenController doubleProjectionScreenController;
    private ProjectionScreenController previewProjectionScreenController;
    private boolean isLock = false;

    private String activeText = "";
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean bringedToFront;
    private Scene scene;
    private List<ProjectionTextChangeListener> projectionTextChangeListeners;

    public void initialize() {
        settings = Settings.getInstance();
        mainPane.setOnMousePressed(e -> {
            if (isLock) {// ) || !stage.isMaximized()) {
                return;
            }
            int width = (int) mainPane.getWidth();
            if (projectionType == ProjectionType.BIBLE) {
                if (bibleController != null) {
                    if ((double) width / 2 < e.getX()) {
                        bibleController.setNextVerse();
                        // stage.setOpacity(0.5);
                    } else {
                        bibleController.setPreviousVerse();
                        // stage.setOpacity(1);
                    }
                }
            } else if (projectionType == ProjectionType.SONG) {
                if (songController != null) {
                    if ((double) width / 2 < e.getX()) {
                        songController.setNext();
                    } else {
                        songController.setPrevious();
                    }
                }
            }
        });
        progressLine.setVisible(false);
        progressLine.setStroke(settings.getProgressLineColor());
        settings.showProgressLineProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && projectionType == ProjectionType.SONG) {
                progressLine.setVisible(true);
            } else {
                progressLine.setVisible(false);
            }
        });
        settings.progressLinePositionIsTopProperty().addListener((observable, oldValue, newValue) -> {
            double endY;
            if (newValue) {
                endY = 1;
            } else {
                endY = scene.getHeight() - 1;
            }
            progressLine.setStartY(endY);
            progressLine.setEndY(endY);
        });
    }

    void setSongController(SongController songController) {
        this.songController = songController;
    }

    void setBackGroundColor(Color backgroundColor) {
        if (previewProjectionScreenController != null) {
            previewProjectionScreenController.setBackGroundColor(backgroundColor);
        }
        if (isLock) {
            return;
        }
        if (!Settings.getInstance().isBackgroundImage()) {
            BackgroundFill myBF = new BackgroundFill(backgroundColor, new CornerRadii(1),
                    new Insets(0.0, 0.0, 0.0, 0.0));
            mainPane.setBackground(new Background(myBF));
        } else {
            setBackGroundImage();
        }
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.setBackGroundColor(backgroundColor);
        }
    }

    private void setBackGroundImage() {
        if (isLock) {
            return;
        }
        if (Settings.getInstance().isBackgroundImage()) {
            int w = 80;
            int h = 60;
            if (scene != null) {
                w = (int) scene.getWidth();
                h = (int) scene.getHeight();
            }
            mainPane.setBackground(new Background(new BackgroundImage(
                    new Image(settings.getBackgroundImagePath(), w, h, false, true), BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        }
    }

    public void setBlank(boolean isBlank) {
        this.isBlank = isBlank;
        mainPane.setVisible(!isBlank);
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.setBlank(isBlank);
        }
    }

    void reload() {
        if (isLock) {
            return;
        }
        setText(activeText, projectionType);
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.reload();
        }
    }

    public void repaint() {
        if (isLock) {
            return;
        }
        setText(activeText, projectionType);
        setBackGroundColor(Settings.getInstance().getBackgroundColor());
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.repaint();
        }
        if (!settings.isProgressLinePositionIsTop()) {
            double endY = scene.getHeight() - 1;
            progressLine.setStartY(endY);
            progressLine.setEndY(endY);
        }
    }

    public void setText(String newText, ProjectionType projectionType) {
        Platform.runLater(() -> {
            this.projectionType = projectionType;
            activeText = newText;
            if (previewProjectionScreenController != null) {
                previewProjectionScreenController.setText(newText, projectionType);
            }
            if (isLock) {
                return;
            }
            if (doubleProjectionScreenController != null) {
                doubleProjectionScreenController.setText(newText, projectionType);
            }
            if (projectionTextChangeListeners != null) {
                for (ProjectionTextChangeListener projectionTextChangeListener : projectionTextChangeListeners) {
                    projectionTextChangeListener.onSetText(newText, projectionType);
                }
            }
            Scene scene = pane.getScene();
            int width = (int) (scene.getWidth());
            int height = (int) scene.getHeight();
            if (projectionType == ProjectionType.REFERENCE) {
                textFlow1.setText2(newText, width, height);
                double v = settings.getMaxFont() * 0.7;
                if (textFlow1.getSize() < v && newText.length() > 100) {
                    String[] split = splitHalfByNewLine(newText);
                    textFlow.setText2(split[0], width / 2, height);
                    textFlow1.setText2(split[1], width / 2, height);
                    if (textFlow.getSize() > textFlow1.getSize()) {
                        textFlow.setSizeAndAlign(textFlow1.getSize());
                    } else if (textFlow.getSize() < textFlow1.getSize()) {
                        textFlow1.setSizeAndAlign(textFlow.getSize());
                    }
                    return;
                }
            }
            if (projectionType == ProjectionType.SONG) {
                progressLine.setVisible(settings.isShowProgressLine());
            } else {
                progressLine.setVisible(false);
            }
            textFlow.setText2(newText, width, height);
            textFlow1.setText2("", 0, height);
        });
    }

    private String[] splitHalfByNewLine(String newText) {
        String[] split = newText.split("\n");
        int i;
        int half = split.length / 2;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(split[0]);
        for (i = 1; i < split.length; ++i) {
            if (i >= half) {
                break;
            }
            stringBuilder.append("\n").append(split[i]);
        }
        String[] returnValue = new String[2];
        returnValue[0] = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(split[i++]);
        for (; i < split.length; ++i) {
            stringBuilder.append("\n").append(split[i]);
        }
        returnValue[1] = stringBuilder.toString();
        return returnValue;
    }

    void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

    void duplicate() {
        if (doubleProjectionScreenController == null) {
            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(Main.class.getResource("/view/ProjectionScreen.fxml"));
            Pane root2;
            try {
                root2 = loader2.load();

                doubleProjectionScreenController = loader2.getController();
                Scene scene2 = new Scene(root2, 400, 300);
                scene2.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());

                scene2.widthProperty().addListener((observable, oldValue, newValue) -> doubleProjectionScreenController.repaint());
                scene2.heightProperty().addListener((observable, oldValue, newValue) -> doubleProjectionScreenController.repaint());
                Stage stage2 = new Stage();
                stage2.setScene(scene2);

                stage2.setX(0);
                stage2.setY(0);
                doubleProjectionScreenController.setStage(stage2);
                scene2.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.F11) {
                        stage2.setMaximized(!stage2.isMaximized());
                    } else if (event.getCode().equals(KeyCode.ESCAPE)) {
                        stage2.setMaximized(false);
                    }
                });
                stage2.initStyle(StageStyle.UNDECORATED);
                stage2.show();
                stage2.maximizedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        scene2.setCursor(Cursor.NONE);
                    } else {
                        scene2.setCursor(Cursor.DEFAULT);
                    }
                });
                scene2.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                scene2.setOnMouseDragged(event -> {
                    if (!stage2.isMaximized()) {
                        stage2.setX(event.getScreenX() - xOffset);
                        stage2.setY(event.getScreenY() - yOffset);
                    }
                });

                stage2.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        stage2.setMaximized(false);
                    }
                });
                stage2.setOnCloseRequest(we -> {
                    stage2.close();
                    if (doubleProjectionScreenController != null) {
                        doubleProjectionScreenController
                                .setParentProjectionScreenController(parentProjectionScreenController);
                        if (parentProjectionScreenController != null) {
                            parentProjectionScreenController
                                    .setDoubleProjectionScreenController(doubleProjectionScreenController);
                        }
                    }
                });
                doubleProjectionScreenController.setBlank(isBlank);
                doubleProjectionScreenController.setParentProjectionScreenController(doubleProjectionScreenController);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            doubleProjectionScreenController.duplicate();
        }
    }

    void createPreview() {
        if (previewProjectionScreenController == null) {
            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(Main.class.getResource("/view/ProjectionScreen.fxml"));
            Pane root2;
            try {
                root2 = loader2.load();

                previewProjectionScreenController = loader2.getController();
                Scene scene = mainPane.getScene();
                int width = (int) scene.getWidth();
                int height = (int) scene.getHeight();
                int size = 512;
                if (settings.getPreviewWidth() > 0) {
                    size = (int) settings.getPreviewWidth();
                }
                Scene scene2 = new Scene(root2, size, (double) size * height / width);
                scene2.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());

                scene2.widthProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.repaint());
                scene2.heightProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.repaint());
                Stage stage2 = new Stage();
                stage2.setScene(scene2);

                stage2.setX(settings.getPreviewX());
                stage2.setY(settings.getPreviewY());
                previewProjectionScreenController.setStage(stage2);
                scene2.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        stage2.setMaximized(!stage2.isMaximized());
                    }
                });
                stage2.setTitle(Settings.getInstance().getResourceBundle().getString("Preview"));
                stage2.show();
                stage2.maximizedProperty().addListener((observable, oldValue, newValue) -> {
                });
                stage2.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        stage2.setMaximized(false);
                    }
                });
                stage2.setOnCloseRequest(event -> {
                    final double width1 = stage2.getWidth();
                    final double height1 = stage2.getHeight();
                    final double x = stage2.getX();
                    final double y = stage2.getY();
                    if (x + width1 >= 0) {
                        settings.setPreviewX(x);
                        settings.setPreviewWidth(width1);
                    }
                    if (y + height1 >= 0) {
                        settings.setPreviewY(y);
                        settings.setPreviewHeight(height1);
                    }
                    settings.save();
                });
                previewProjectionScreenController.setText(activeText, projectionType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            final Stage stage = previewProjectionScreenController.getStage();
            stage.setX(settings.getPreviewX());
            stage.setY(settings.getPreviewY());
            stage.setWidth(settings.getPreviewWidth());
            stage.setHeight(settings.getPreviewHeight());
            stage.show();
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        setScene(stage.getScene());
        loadEmpty();
    }

    public void loadEmpty() {
        setText("", projectionType);
        setBackGroundColor(Settings.getInstance().getBackgroundColor());
    }

    public void onClose() {
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.getStage().close();
            doubleProjectionScreenController.onClose();
        }
        if (previewProjectionScreenController != null) {
            final Stage stage2 = previewProjectionScreenController.getStage();
            if (stage2 != null) {
                final double width1 = stage2.getWidth();
                final double height1 = stage2.getHeight();
                final double x = stage2.getX();
                final double y = stage2.getY();
                if (x + width1 >= 0) {
                    settings.setPreviewX(x);
                    settings.setPreviewWidth(width1);
                }
                if (y + height1 >= 0) {
                    settings.setPreviewY(y);
                    settings.setPreviewHeight(height1);
                }
                settings.save();
            }
            if (previewProjectionScreenController.getStage() != null) {
                previewProjectionScreenController.getStage().close();
            }
            previewProjectionScreenController.onClose();
        }
    }

    private void setParentProjectionScreenController(ProjectionScreenController parentProjectionScreenController) {
        this.parentProjectionScreenController = parentProjectionScreenController;
    }

    private void setDoubleProjectionScreenController(ProjectionScreenController doubleProjectionScreenController) {
        this.doubleProjectionScreenController = doubleProjectionScreenController;
    }

    void setLock(boolean selected) {
        isLock = selected;
        Stage stage = null;
        if (previewProjectionScreenController != null) {
            stage = previewProjectionScreenController.getStage();
        }
        if (!isLock) {
            repaint();
            if (stage != null) {
                stage.setOpacity(1);
                stage.setTitle(Settings.getInstance().getResourceBundle().getString("Preview"));
            }
        } else {
            if (stage != null) {
                stage.setOpacity(0.77);
                stage.setTitle(Settings.getInstance().getResourceBundle().getString("Preview (MAIN LOCKED)"));
            }
        }
    }

    public void setColor(Color value) {
        if (previewProjectionScreenController != null) {
            previewProjectionScreenController.setColor(value);
        }
        if (isLock) {
            return;
        }
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.setColor(value);
        }
        textFlow.setColor(value);
        textFlow1.setColor(value);
        progressLine.setStroke(settings.getProgressLineColor());
    }

    void setPrimaryStage(Stage primaryStage) {
        bringedToFront = false;
        primaryStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
//                System.out.println(newValue);
            if (newValue) {
                if (bringedToFront) {
                    bringedToFront = false;
                } else {
                    bringedToFront = true;
                    Thread thread = new Thread(() -> {
                        try {
                            Thread.sleep(7);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        Platform.runLater(() -> {
                            if (previewProjectionScreenController != null) {
                                previewProjectionScreenController.getStage().toFront();
                            }
                        });
                    });
                    thread.start();
                }
            }
        });
    }

    public void setHeight(double height) {
        mainPane.setPrefHeight(height);
    }

    public void setWidth(double width) {
        mainPane.setPrefWidth(width);
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        if (!settings.isProgressLinePositionIsTop()) {
            double endY = scene.getHeight() - 1;
            progressLine.setStartY(endY);
            progressLine.setEndY(endY);
        }
    }

    public void setLineSize(double size) {
        if (previewProjectionScreenController != null) {
            previewProjectionScreenController.setLineSize(size);
        }
        if (!isLock) {
            Integer progressLineThickness = settings.getProgressLineThickness();
            progressLine.setStrokeLineCap(StrokeLineCap.BUTT);
            if (!settings.isProgressLinePositionIsTop()) {
                double endY = scene.getHeight() - 1;
                progressLine.setStartY(endY - progressLineThickness / 2);
                progressLine.setEndY(endY - progressLineThickness / 2);
            } else {
                progressLine.setStartY(1 + progressLineThickness / 2);
                progressLine.setEndY(1 + progressLineThickness / 2);
            }
            if (size == 0) {
                progressLine.setStrokeWidth(0);
            } else {
                progressLine.setStrokeWidth(progressLineThickness);
            }
            final double width = scene.getWidth();
            progressLine.setEndX(width * size);
            if (doubleProjectionScreenController != null) {
                doubleProjectionScreenController.setLineSize(size);
            }
        }
    }

    public void progressLineSetVisible(boolean newValue) {
        if (previewProjectionScreenController != null) {
            previewProjectionScreenController.progressLineSetVisible(newValue);
        }
        if (!isLock) {
            progressLine.setVisible(newValue);
            if (doubleProjectionScreenController != null) {
                doubleProjectionScreenController.progressLineSetVisible(newValue);
            }
        }
    }

    public synchronized void addProjectionTextChangeListener(ProjectionTextChangeListener projectionTextChangeListener) {
        if (projectionTextChangeListeners == null) {
            projectionTextChangeListeners = new ArrayList<>();
        }
        projectionTextChangeListeners.add(projectionTextChangeListener);
    }

    public void removeProjectionTextChangeListener(ProjectionTextChangeListener projectionTextChangeListener) {
        if (projectionTextChangeListeners != null) {
            Platform.runLater(() -> projectionTextChangeListeners.remove(projectionTextChangeListener));
        }
    }
}
