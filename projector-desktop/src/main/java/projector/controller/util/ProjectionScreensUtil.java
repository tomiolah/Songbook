package projector.controller.util;

import projector.controller.ProjectionScreenController;
import projector.controller.listener.ProjectionScreenListener;

import java.util.ArrayList;
import java.util.List;

public class ProjectionScreensUtil {
    private static ProjectionScreensUtil instance = null;
    private final List<ProjectionScreenHolder> projectionScreenHolders;
    private final List<ProjectionScreenHolder> screenHolders;
    private final List<ProjectionScreenListener> projectionScreenListeners;

    private ProjectionScreensUtil() {
        projectionScreenHolders = new ArrayList<>();
        projectionScreenListeners = new ArrayList<>();
        screenHolders = new ArrayList<>();
    }

    public static ProjectionScreensUtil getInstance() {
        if (instance == null) {
            instance = new ProjectionScreensUtil();
        }
        return instance;
    }

    public List<ProjectionScreenHolder> getProjectionScreenHolders() {
        return projectionScreenHolders;
    }

    public ProjectionScreenHolder addProjectionScreenController(ProjectionScreenController projectionScreenController, String name) {
        ProjectionScreenHolder projectionScreenHolder = new ProjectionScreenHolder(projectionScreenController, name);
        addProjectionScreenHolder(projectionScreenHolder);
        projectionScreenController.setProjectionScreenSettings(projectionScreenHolder.getProjectionScreenSettings());
        return projectionScreenHolder;
    }

    private void addProjectionScreenHolder(ProjectionScreenHolder projectionScreenHolder) {
        projectionScreenHolders.add(projectionScreenHolder);
        for (ProjectionScreenListener projectionScreenListener : projectionScreenListeners) {
            projectionScreenListener.onNew(projectionScreenHolder);
        }
    }

    public void addDoubleProjectionScreenController(ProjectionScreenController doubleProjectionScreenController) {
        int number = countDoubleProjectionScreens() + 2;
        String name = number + " - screen";
        ProjectionScreenHolder projectionScreenHolder = new ProjectionScreenHolder(doubleProjectionScreenController, name);
        projectionScreenHolder.setDoubleProjectionScreen(true);
        addProjectionScreenHolder(projectionScreenHolder);
        doubleProjectionScreenController.setProjectionScreenSettings(projectionScreenHolder.getProjectionScreenSettings());
        screenHolders.add(projectionScreenHolder);
    }

    public int countDoubleProjectionScreens() {
        int count = 0;
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            if (projectionScreenHolder.isDoubleProjectionScreen()) {
                ++count;
            }
        }
        return count;
    }

    public void addProjectionScreenListener(ProjectionScreenListener projectionScreenListener) {
        projectionScreenListeners.add(projectionScreenListener);
    }

    public void removeProjectionScreenController(ProjectionScreenController projectionScreenController) {
        projectionScreenHolders.remove(projectionScreenController.getProjectionScreenSettings().getProjectionScreenHolder());
    }

    public ProjectionScreenHolder getScreenHolderByIndex(Integer index) {
        if (index < 0 || index >= screenHolders.size()) {
            return null;
        }
        return screenHolders.get(index);
    }
}
