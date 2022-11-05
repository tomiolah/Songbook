package projector.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.BibleApiBean;
import projector.application.Settings;
import projector.model.Bible;
import projector.service.BibleService;
import projector.service.ServiceManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import static projector.utils.BibleImport.bibleImportFromSQLite;

public class DownloadBiblesController {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadBiblesController.class);
    @FXML
    private Label label;
    @FXML
    private Button selectButton;
    @FXML
    private VBox listView;
    private List<Bible> bibles;
    private List<CheckBox> checkBoxes;
    private BibleService bibleService;
    private Stage stage;
    private BibleController bibleController;

    public void initialize() {
        bibleService = ServiceManager.getBibleService();
        bibles = bibleService.findAll();
        checkBoxes = new ArrayList<>(bibles.size());
        for (Bible bible : bibles) {
            addBibleToVBox(bible);
        }
        BibleApiBean bibleApiBean = new BibleApiBean();
        Thread thread = new Thread(() -> {
            List<Bible> onlineBibles = bibleApiBean.getBibleTitles();
            if (onlineBibles == null) {
                noInternetMessage();
                return;
            }
            HashMap<String, Boolean> hashMap = new HashMap<>();
            for (Bible bible : bibles) {
                hashMap.put(bible.getUuid(), true);
            }
            for (Bible bible : onlineBibles) {
                if (!hashMap.containsKey(bible.getUuid())) {
                    addBibleToVBox(bible);
                    bibles.add(bible);
                }
            }
        });
        thread.start();
        selectButton.setOnAction(event -> {
            try {
                selectButton.setDisable(true);
                Thread thread2 = new Thread(() -> {
                    Thread thread1 = null;
                    for (int i = 0; i < bibles.size(); ++i) {
                        CheckBox checkBox = checkBoxes.get(i);
                        if (checkBox.isSelected() && !checkBox.isDisabled()) {
                            Bible bible = bibleApiBean.getBible(bibles.get(i).getUuid());
                            thread1 = saveBibleInThread(thread1, bible);
                        }
                    }
                    if (thread1 != null) {
                        try {
                            thread1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    closeDownloadBibleStage();
                });
                thread2.start();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    private void closeDownloadBibleStage() {
        if (stage != null) {
            Platform.runLater(() -> {
                bibleController.initializeBibles();
                this.stage.close();
            });
        }
    }

    private Thread saveBibleInThread(Thread thread, Bible bible) {
        if (bible == null) {
            return null;
        }
        try {
            if (thread != null) {
                thread.join();
            }
            thread = new Thread(() -> bibleService.create(bible));
            thread.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return thread;
    }

    private void addBibleToVBox(Bible bible) {
        CheckBox checkBox = new CheckBox(bible.getName() + " - " + bible.getShortName());
        boolean b = bible.getId() != null;
        checkBox.setSelected(b);
        checkBox.setDisable(b);
        Platform.runLater(() -> listView.getChildren().add(checkBox));
        checkBoxes.add(checkBox);
    }

    private void noInternetMessage() {
        final ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        final String no_internet_connection = resourceBundle.getString("No internet connection");
        final String try_again_later = resourceBundle.getString("Try again later");
        Platform.runLater(() -> label.setText(no_internet_connection + "! " + try_again_later + "!"));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

    public void openMyBibleModuleDownloadSite() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://www.ph4.org/b4_index.php"));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void importMyBibleModule() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose the MyBible module!");
        fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MyBible", "*.SQLite3"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            Bible bible = bibleImportFromSQLite(selectedFile.getAbsolutePath());
            ServiceManager.getBibleService().create(bible);
            closeDownloadBibleStage();
        }
    }
}
