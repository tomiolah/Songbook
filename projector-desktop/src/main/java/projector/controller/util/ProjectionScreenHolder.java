package projector.controller.util;

import projector.application.ProjectionScreenSettings;
import projector.controller.ProjectionScreenController;

public class ProjectionScreenHolder {
    private final ProjectionScreenSettings projectionScreenSettings;
    private ProjectionScreenController projectionScreenController;
    private String name;
    private boolean doubleProjectionScreen = false;

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
}
