package projector.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static projector.utils.AlertUtil.getAppAlert;

public class FirstSetupController {

    private static final Logger LOG = LoggerFactory.getLogger(FirstSetupController.class);
    public Button copyDataButton;
    public Button startAsNewButton;
    private Listener listener;

    private static String replaceDirectorySeparator(String s) {
        return s.replace("/", "\\");
    }

    public void onStartAsNew() {
        disableButtons();
        if (listener != null) {
            listener.onStartAsNew();
        }
    }

    private void disableButtons_(boolean disabled) {
        startAsNewButton.setDisable(disabled);
        copyDataButton.setDisable(disabled);
    }

    private void disableButtons() {
        disableButtons_(true);
    }

    private void enableButtons() {
        disableButtons_(false);
    }

    public void onCopyDataFromPreviousVersion() {
        try {
            disableButtons();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select previously installed directory!");
            File file = directoryChooser.showDialog(null);
            if (file == null) {
                return;
            }
            System.out.println(file.getAbsolutePath());
            List<WantedFile> wantedFiles = new ArrayList<>();
            addWantedFile(wantedFiles, "data/projector.mv.db");
            addWantedFile(wantedFiles, "data/projector.trace.db");
            addWantedFile(wantedFiles, "data/database.version");
            addWantedFile(wantedFiles, "settings.ini");
            addWantedFile(wantedFiles, "recent.txt");
            addWantedFile(wantedFiles, "projector.log");
            addWantedFile(wantedFiles, "songVersTimes");
            tryToFindWantedFiles(wantedFiles, file.getAbsolutePath());
        } finally {
            enableButtons();
        }
    }

    private void tryToFindWantedFiles(List<WantedFile> wantedFiles, String directory) {
        for (WantedFile wantedFile : wantedFiles) {
            File file = new File(directory + "/" + wantedFile.getFilePath());
            wantedFile.setFound(file.exists());
        }
        if (allFilesFound(wantedFiles)) {
            copyFoundFiles(wantedFiles, directory);
        } else if (someFilesFound(wantedFiles)) {
            String notFoundFiles = gatherNotFoundFiles(wantedFiles);
            Platform.runLater(() -> {
                Alert alert = getAppAlert(Alert.AlertType.WARNING, getClass());
                alert.setTitle("Could not find all files!");
                alert.setHeaderText("The following files were not found:");
                alert.setContentText(notFoundFiles);
                ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getDialogPane().getButtonTypes().add(cancelButtonType);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    copyFoundFiles(wantedFiles, directory);
                }
            });
        } else {
            Platform.runLater(() -> {
                Alert alert = getAppAlert(Alert.AlertType.WARNING, getClass());
                alert.setTitle("Wrong folder!");
                alert.setHeaderText("Could not find files from previous installation!");
                alert.setContentText("Select a different folder");
                alert.show();
            });
        }
    }


    private String gatherNotFoundFiles(List<WantedFile> wantedFiles) {
        StringBuilder s = new StringBuilder();
        for (WantedFile wantedFile : wantedFiles) {
            if (!wantedFile.isFound()) {
                s.append(wantedFile.getFilePath()).append("\n");
            }
        }
        return s.toString();
    }

    private void copyFoundFiles(List<WantedFile> wantedFiles, String directory) {
        for (WantedFile wantedFile : wantedFiles) {
            if (!wantedFile.isFound()) {
                continue;
            }
            String fromPath = directory + "/" + wantedFile.getFilePath();
            String toPath = wantedFile.getFilePath();
            fromPath = replaceDirectorySeparator(fromPath);
            toPath = replaceDirectorySeparator(toPath);
            String command = "cmd /c copy /Y " + fromPath + " " + toPath;
            try {
                Process process = Runtime.getRuntime().exec(command);
                int result = process.waitFor();
                wantedFile.setCopiedSuccessFully(result == 0);
            } catch (IOException | InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (!warnIfNotAllFilesWasCopied(wantedFiles)) {
            onStartAsNew();
        }
    }

    private boolean warnIfNotAllFilesWasCopied(List<WantedFile> wantedFiles) {
        StringBuilder message = new StringBuilder();
        for (WantedFile wantedFile : wantedFiles) {
            if (wantedFile.isFound() && !wantedFile.isCopiedSuccessFully()) {
                message.append(wantedFile.getFilePath()).append("\n");
            }
        }
        String s = message.toString();
        if (s.equals("")) {
            return false;
        }
        Platform.runLater(() -> {
            Alert alert = getAppAlert(Alert.AlertType.ERROR, getClass());
            alert.setTitle("Could not copy all files!");
            alert.setHeaderText("The following files were not copied:");
            alert.setContentText(s);
            alert.show();
        });
        return true;
    }

    private boolean someFilesFound(List<WantedFile> wantedFiles) {
        for (WantedFile wantedFile : wantedFiles) {
            if (wantedFile.isFound()) {
                return true;
            }
        }
        return false;
    }

    private boolean allFilesFound(List<WantedFile> wantedFiles) {
        for (WantedFile wantedFile : wantedFiles) {
            if (!wantedFile.isFound()) {
                return false;
            }
        }
        return true;
    }

    private void addWantedFile(List<WantedFile> wantedFiles, String filePath) {
        wantedFiles.add(new WantedFile(filePath));
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onStartAsNew();
    }

    private static class WantedFile {
        private final String filePath;
        private boolean found = false;
        private boolean copiedSuccessFully = false;

        public WantedFile(String filePath) {
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }

        public boolean isFound() {
            return found;
        }

        public void setFound(boolean found) {
            this.found = found;
        }

        public boolean isCopiedSuccessFully() {
            return copiedSuccessFully;
        }

        public void setCopiedSuccessFully(boolean copiedSuccessFully) {
            this.copiedSuccessFully = copiedSuccessFully;
        }
    }
}
