package projector.application;

import com.bence.projector.common.dto.ProjectorVersionDTO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.Main;
import projector.api.ProjectorVersionApiBean;
import projector.controller.UpdateController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

public class Updater {

    private static final Logger LOG = LoggerFactory.getLogger(Updater.class);
    private static Updater instance;
    @SuppressWarnings("FieldCanBeLocal")
    private final int projectorVersionNumber = 11;
    private final Settings settings = Settings.getInstance();

    private Updater() {
    }

    public static Updater getInstance() {
        if (instance == null) {
            instance = new Updater();
        }
        return instance;
    }

    public void checkForUpdate() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ProjectorVersionApiBean projectorVersionApiBean = new ProjectorVersionApiBean();
                    List<ProjectorVersionDTO> projectorVersionsAfterNr = projectorVersionApiBean.getProjectorVersionsAfterNr(projectorVersionNumber);
                    if (projectorVersionsAfterNr != null && projectorVersionsAfterNr.size() > 0) {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(Main.class.getResource("/view/UpdateAvailable.fxml"));
                        loader.setResources(settings.getResourceBundle());
                        Pane root = loader.load();
                        UpdateController updateController = loader.getController();
                        updateController.setProjectorVersions(projectorVersionsAfterNr);
                        Scene scene = new Scene(root);
                        scene.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
                        Platform.runLater(() -> {
                            Stage stage = new Stage();
                            stage.setTitle("Update available");
                            stage.setScene(scene);
                            stage.show();
                        });
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }

            }
        });
        thread.start();
    }

    public void updateExe() {
        Thread thread = new Thread() {
            Alert alert2 = null;

            @Override
            public void run() {
                Platform.runLater(() -> {
                    alert2 = new Alert(AlertType.INFORMATION);
                    alert2.setTitle("Update");
                    alert2.setHeaderText("Update will start to download!");
                    alert2.setContentText("You need to wait to complete the download");
                    alert2.show();
                });
                // alert.showAndWait();
                URL website;
                try {
                    website = new URL("http://localhost/projector.exe");
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    File dir = new File("utils");
                    if (!dir.mkdir()) {
                        alert2.close();
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText("Could not create utils directory!");
                        alert.setContentText("If you see this message several times, then you should report it!");
                        alert.showAndWait();
                        return;
                    }
                    FileOutputStream fos = new FileOutputStream("utils\\projector.exe");
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    fos.close();
                    String command = "cmd /c copy /Y utils\\projector.exe projector.exe && del utils\\projector.exe && rmdir utils";
                    // Process child =
                    Runtime.getRuntime().exec(command);
                    // Runtime.getRuntime().exec("cmd /c del
                    // utils\\projector.exe");
                    // Runtime.getRuntime().exec("cmd /c rmdir utils");
                    // OutputStream out = child.getOutputStream();
                    // out.write("copy /Y utils\\projector.exe projector.exe
                    // /r/n".getBytes());
                    // out.flush();
                    // out.close();
                    Platform.runLater(() -> {
                        alert2.close();
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Update file downloaded");
                        alert.setHeaderText("Restart the program!");
                        alert.setContentText("You nead to restart the program to see the differences");
                        alert.showAndWait();
                    });
                } catch (MalformedURLException e) {
                    System.out.println("1");
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    System.out.println("2");
                    e.printStackTrace();
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        alert2.close();
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("No internet");
                        alert.setHeaderText("No internet connection!");
                        alert.setContentText("Try again later!");
                        alert.showAndWait();
                    });
                    System.out.println("3");
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}
