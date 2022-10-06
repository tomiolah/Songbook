package projector.application;


import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ApplicationUtil {

    private static ApplicationUtil instance;
    private final List<Stage> stages = new ArrayList<>();
    private Listener listener;
    private Stage primaryStage;

    private ApplicationUtil() {

    }

    public static ApplicationUtil getInstance() {
        if (instance == null) {
            instance = new ApplicationUtil();
        }
        return instance;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void closeApplication() {
        Platform.runLater(() -> {
            if (listener != null) {
                listener.onApplicationClose();
            }
            closeStages();
        });
    }

    private void closeStages() {
        primaryStage.close();
        for (Stage stage : stages) {
            stage.close();
        }
    }

    public void addCloseNeededStage(Stage stage) {
        stages.add(stage);
        stage.setOnCloseRequest(event -> stages.remove(stage));
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public interface Listener {
        void onApplicationClose();
    }
}
