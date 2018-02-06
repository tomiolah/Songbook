package projector.controller;

import projector.application.ProjectionType;

public interface ProjectionTextChangeListener {
    void onSetText(String text, ProjectionType projectionType);
}
