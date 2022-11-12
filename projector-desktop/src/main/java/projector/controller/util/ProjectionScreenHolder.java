package projector.controller.util;

import projector.application.ProjectionScreenSettings;
import projector.controller.ProjectionScreenController;
import projector.controller.listener.OnMainPaneSizeChangeListener;
import projector.controller.listener.PopupCreatedListener;

public class ProjectionScreenHolder {
    private final ProjectionScreenSettings projectionScreenSettings;
    private ProjectionScreenController projectionScreenController;
    private String name;
    private boolean doubleProjectionScreen = false;
    private PopupCreatedListener popupCreatedListener;
    private OnMainPaneSizeChangeListener onMainPaneSizeChangeListener = null;
    private double lastWidth = 0.0;
    private double lastHeight = 0.0;
    private Integer screenIndex = null;

    public ProjectionScreenHolder(ProjectionScreenController projectionScreenController, String name) {
        this.projectionScreenController = projectionScreenController;
        this.name = name;
        this.projectionScreenSettings = new ProjectionScreenSettings(this);
    }

    public ProjectionScreenController getProjectionScreenController() {
        return projectionScreenController;
    }

    public void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        this.projectionScreenController = projectionScreenController;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDoubleProjectionScreen() {
        return doubleProjectionScreen;
    }

    public void setDoubleProjectionScreen(boolean doubleProjectionScreen) {
        this.doubleProjectionScreen = doubleProjectionScreen;
    }

    public ProjectionScreenSettings getProjectionScreenSettings() {
        return projectionScreenSettings;
    }

    public void setOnPopupCreatedListener(PopupCreatedListener popupCreatedListener) {
        this.popupCreatedListener = popupCreatedListener;
    }

    public void popupCreated() {
        if (popupCreatedListener != null) {
            popupCreatedListener.popupCreated();
        }
    }

    public void setOnMainPaneSizeChangeListener(OnMainPaneSizeChangeListener onMainPaneSizeChangeListener) {
        this.onMainPaneSizeChangeListener = onMainPaneSizeChangeListener;
        onMainPaneSizeChangeListener.onMainPaneSizeChange(lastWidth, lastHeight);
    }

    public void onSizeChanged(double width, double height) {
        this.lastWidth = width;
        this.lastHeight = height;
        if (onMainPaneSizeChangeListener != null) {
            onMainPaneSizeChangeListener.onMainPaneSizeChange(width, height);
        }
    }

    public Integer getScreenIndex() {
        return screenIndex;
    }

    public void setScreenIndex(Integer index) {
        this.screenIndex = index;
    }
}
