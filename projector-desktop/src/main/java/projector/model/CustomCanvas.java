package projector.model;

import com.google.gson.annotations.Expose;
import javafx.stage.Stage;

public class CustomCanvas {

    @Expose
    private String name;
    @Expose
    private Double width;
    @Expose
    private Double height;
    @Expose
    private Double positionX;
    @Expose
    private Double positionY;

    private Stage stage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
    }

    public void apply(CustomCanvas customCanvas) {
        if (customCanvas == null) {
            return;
        }
        setName(customCanvas.getName());
        setWidth(customCanvas.getWidth());
        setHeight(customCanvas.getHeight());
        setPositionX(customCanvas.getPositionX());
        setPositionY(customCanvas.getPositionY());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
