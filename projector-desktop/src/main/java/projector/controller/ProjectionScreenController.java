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
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import projector.MainDesktop;
import projector.application.ProjectionScreenSettings;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.listener.OnBlankListener;
import projector.controller.listener.ViewChangedListener;
import projector.controller.song.SongController;
import projector.controller.util.ProjectionScreenHolder;
import projector.controller.util.ProjectionScreensUtil;
import projector.utils.scene.text.MyTextFlow;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static projector.controller.MyController.calculateSizeByScale;
import static projector.utils.CountDownTimerUtil.getRemainedDate;
import static projector.utils.CountDownTimerUtil.getTimeTextFromDate;
import static projector.utils.SceneUtils.getAStage;

public class ProjectionScreenController {

    private final List<ViewChangedListener> viewChangedListeners = new ArrayList<>();
    private final List<OnBlankListener> onBlankListeners = new ArrayList<>();
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
    private ProjectionScreenController customStageController;
    private boolean countDownTimerRunning = false;
    private Thread countDownTimerThread = null;
    private ProjectionScreenSettings projectionScreenSettings = new ProjectionScreenSettings();
    private Popup popup;
    private Pane root;
    private Screen screen;
    private Stage primaryStage;
    private MainDesktop mainDesktop;

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
    }

    private void initializeFromSettings() {
        progressLine.setStroke(projectionScreenSettings.getProgressLineColor());
        settings.showProgressLineProperty().addListener((observable, oldValue, newValue) -> progressLine.setVisible(newValue && projectionType == ProjectionType.SONG));
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
        this.textFlow.setProjectionScreenSettings(projectionScreenSettings);
        this.textFlow1.setProjectionScreenSettings(projectionScreenSettings);
    }

    void setSongController(SongController songController) {
        this.songController = songController;
    }

    void setBackGroundColor() {
        if (previewProjectionScreenController != null) {
            previewProjectionScreenController.setBackGroundColor();
        }
        if (isLock) {
            return;
        }
        if (!projectionScreenSettings.isBackgroundImage()) {
            Color backgroundColor = projectionScreenSettings.getBackgroundColor();
            BackgroundFill myBF = new BackgroundFill(backgroundColor, new CornerRadii(1),
                    new Insets(0.0, 0.0, 0.0, 0.0));
            mainPane.setBackground(new Background(myBF));
        } else {
            setBackGroundImage();
        }
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.setBackGroundColor();
        }
    }

    private void setBackGroundImage() {
        if (isLock) {
            return;
        }
        if (projectionScreenSettings.isBackgroundImage()) {
            int w = 80;
            int h = 60;
            if (scene != null) {
                w = (int) scene.getWidth();
                h = (int) scene.getHeight();
            }
            mainPane.setBackground(new Background(new BackgroundImage(
                    new Image(projectionScreenSettings.getBackgroundImagePath(), w, h, false, true), BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        }
    }

    public void setBlank(boolean isBlank) {
        setBlankLocally(isBlank);
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.setBlank(isBlank);
        }
    }

    private void setBlankLocally(boolean isBlank) {
        this.isBlank = isBlank;
        mainPane.setVisible(!isBlank);
        onViewChanged();
        onBlankChanged();
    }

    private void onBlankChanged() {
        for (OnBlankListener onBlankListener : onBlankListeners) {
            onBlankListener.onBlankChanged(isBlank);
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
        setBackGroundColor();
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.repaint();
        }
        if (!projectionScreenSettings.isProgressLinePositionIsTop()) {
            double endY = scene.getHeight() - 1;
            progressLine.setStartY(endY);
            progressLine.setEndY(endY);
        }
    }

    public void setCountDownTimer(Date finishedDate) {
        countDownTimerRunning = false;
        if (countDownTimerThread != null) {
            try {
                countDownTimerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        countDownTimerThread = new Thread(() -> {
            while (countDownTimerRunning) {
                try {
                    String timeTextFromDate = getTimeTextFromDate(getRemainedDate(finishedDate));
                    if (!timeTextFromDate.isEmpty() && !activeText.equals(timeTextFromDate)) {
                        Platform.runLater(() -> setText(timeTextFromDate, ProjectionType.COUNTDOWN_TIMER));
                    }
                    //noinspection BusyWait
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        countDownTimerRunning = true;
        countDownTimerThread.start();
    }

    public void setText(String newText, ProjectionType projectionType) {
        if (projectionType != ProjectionType.COUNTDOWN_TIMER) {
            countDownTimerRunning = false;
        }
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
            if (scene == null) {
                return;
            }
            int width = (int) (scene.getWidth());
            int height = (int) scene.getHeight();
            if (projectionType == ProjectionType.REFERENCE) {
                textFlow1.setText2(newText, width, height);
                double v = projectionScreenSettings.getMaxFont() * 0.7;
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
            onViewChanged();
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

    private void setStyleFile(Scene scene) {
        URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    void duplicate() {
        if (doubleProjectionScreenController == null) {
            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
            Pane root2;
            try {
                root2 = loader2.load();

                doubleProjectionScreenController = loader2.getController();
                ProjectionScreensUtil.getInstance().addDoubleProjectionScreenController(doubleProjectionScreenController);
                Scene scene2 = new Scene(root2, 400, 300);
                setStyleFile(scene2);

                scene2.widthProperty().addListener((observable, oldValue, newValue) -> doubleProjectionScreenController.repaint());
                scene2.heightProperty().addListener((observable, oldValue, newValue) -> doubleProjectionScreenController.repaint());
                Stage stage2 = getAStage(getClass());
                stage2.setScene(scene2);
                stage2.setTitle(doubleProjectionScreenController.getProjectionScreenSettings().getProjectionScreenHolder().getName());
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
                        ProjectionScreensUtil.getInstance().removeProjectionScreenController(doubleProjectionScreenController);
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

    public ProjectionScreenController duplicate2() {
        if (doubleProjectionScreenController == null) {
            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
            try {
                Pane root = loader2.load();
                doubleProjectionScreenController = loader2.getController();
                doubleProjectionScreenController.setRoot(root);
                ProjectionScreensUtil.getInstance().addDoubleProjectionScreenController(doubleProjectionScreenController);
                doubleProjectionScreenController.setBlank(isBlank);
                doubleProjectionScreenController.setParentProjectionScreenController(doubleProjectionScreenController);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return doubleProjectionScreenController;
        } else {
            return doubleProjectionScreenController.duplicate2();
        }
    }

    void createCustomStage(int width, int height) {
        if (customStageController == null) {
            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
            Pane root2;
            try {
                root2 = loader2.load();

                customStageController = loader2.getController();
                Scene scene2 = new Scene(root2, calculateSizeByScale(width), calculateSizeByScale(height));
                setStyleFile(scene2);

                scene2.widthProperty().addListener((observable, oldValue, newValue) -> customStageController.repaint());
                scene2.heightProperty().addListener((observable, oldValue, newValue) -> customStageController.repaint());
                Stage stage2 = getAStage(getClass());
                stage2.setTitle("Custom Canvas");
                ProjectionScreensUtil.getInstance().addProjectionScreenController(customStageController, stage2.getTitle());
                stage2.initStyle(StageStyle.TRANSPARENT);
                scene2.setFill(Color.TRANSPARENT);
                stage2.setScene(scene2);

                stage2.setX(0);
                stage2.setY(0);
                customStageController.setStage(stage2);
                scene2.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.F11) {
                        stage2.setMaximized(!stage2.isMaximized());
                    } else if (event.getCode().equals(KeyCode.ESCAPE)) {
                        stage2.setMaximized(false);
                    }
                });
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
                    customStageController = null;
                    doubleProjectionScreenController = null;
                });
                customStageController.setBlank(isBlank);
                doubleProjectionScreenController = customStageController;
                doubleProjectionScreenController.setParentProjectionScreenController(doubleProjectionScreenController);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Stage stage = customStageController.getStage();
            stage.setWidth(calculateSizeByScale(width));
            stage.setHeight(calculateSizeByScale(height));
        }
    }

    public void createPreview() {
        if (previewProjectionScreenController == null) {
            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
            Pane root2;
            try {
                root2 = loader2.load();

                previewProjectionScreenController = loader2.getController();
                String title = Settings.getInstance().getResourceBundle().getString("Preview");
                ProjectionScreenHolder projectionScreenHolder =
                        ProjectionScreensUtil.getInstance().addProjectionScreenController(previewProjectionScreenController, title);
                previewProjectionScreenController.setScreen(Screen.getPrimary());
                projectionScreenHolder.setScreenIndex(0);
                double ratio = getSceneAspectRatio(mainPane.getScene());
                double size = 512;
                if (settings.getPreviewWidth() > 0) {
                    size = settings.getPreviewWidth();
                }
                double width = size;
                double height = size * ratio;
                Scene scene2 = new Scene(root2, width, height);
                setStyleFile(scene2);

                scene2.widthProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.repaint());
                scene2.heightProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.repaint());
                Stage stage2 = getAStage(getClass());
                stage2.setScene(scene2);
                stage2.setWidth(width);
                stage2.setHeight(height);

                stage2.setX(settings.getPreviewX());
                stage2.setY(settings.getPreviewY());
                previewProjectionScreenController.setStage(stage2);
                scene2.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        stage2.setMaximized(!stage2.isMaximized());
                    }
                });
                stage2.setTitle(title);
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

    private double getSceneAspectRatio(Scene scene) {
        if (scene != null) {
            double width = Math.max(scene.getWidth(), 16);
            int height = Math.max((int) scene.getHeight(), 9);
            return height / width;
        }
        double x = 9;
        return x / 16;
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
        setBackGroundColor();
    }

    public void onClose() {
        if (doubleProjectionScreenController != null) {
            Stage stage = doubleProjectionScreenController.getStage();
            if (stage != null) {
                stage.close();
            }
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
        countDownTimerRunning = false;
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
        progressLine.setStroke(projectionScreenSettings.getProgressLineColor());
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
        if (!projectionScreenSettings.isProgressLinePositionIsTop()) {
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
            double progressLineThickness = projectionScreenSettings.getProgressLineThickness();
            progressLine.setStrokeLineCap(StrokeLineCap.BUTT);
            if (!projectionScreenSettings.isProgressLinePositionIsTop()) {
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

    public ProjectionScreenSettings getProjectionScreenSettings() {
        return projectionScreenSettings;
    }

    public void setProjectionScreenSettings(ProjectionScreenSettings projectionScreenSettings) {
        this.projectionScreenSettings = projectionScreenSettings;
        initializeFromSettings();
    }

    public void onSettingsChanged() {
        repaint();
    }

    public Popup getPopup() {
        return popup;
    }

    public void setPopup(Popup popup) {
        this.popup = popup;
    }

    public Pane getRoot() {
        return root;
    }

    public void setRoot(Pane root) {
        this.root = root;
    }

    public void hidePopups() {
        hidePopup();
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.hidePopups();
        }
    }

    public void hidePopup() {
        Popup popup = getPopup();
        if (popup != null) {
            popup.hide();
        }
    }

    public void toggleBlank() {
        setBlankLocally(!isBlank);
    }

    public void toggleShowHidePopup() {
        Popup popup = getPopup();
        if (popup == null) {
            return;
        }
        if (popup.isShowing()) {
            popup.hide();
        } else {
            mainDesktop.createPopupForNextScreen(screen, this);
        }
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public Stage getPrimaryStageVariable() {
        return primaryStage;
    }

    public void setPrimaryStageVariable(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setMainDesktop(MainDesktop mainDesktop) {
        this.mainDesktop = mainDesktop;
    }

    public void addViewChangedListener(ViewChangedListener viewChangedListener) {
        viewChangedListeners.add(viewChangedListener);
    }

    private void onViewChanged() {
        for (ViewChangedListener viewChangedListener : viewChangedListeners) {
            viewChangedListener.viewChanged();
        }
    }

    public BorderPane getMainPane() {
        return mainPane;
    }

    public void addOnBlankListener(OnBlankListener onBlankListener) {
        onBlankListeners.add(onBlankListener);
    }

}
