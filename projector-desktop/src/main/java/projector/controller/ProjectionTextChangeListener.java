package projector.controller;

import com.bence.projector.common.dto.ProjectionDTO;
import projector.application.ProjectionType;

public interface ProjectionTextChangeListener {
    void onSetText(String text, ProjectionType projectionType, ProjectionDTO projectionDTO);
}
