package projector.application;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Updater {

    private static Updater instance;

    private Updater() {
    }

    public static Updater getInstance() {
        if (instance == null) {
            instance = new Updater();
        }
        return instance;
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
                    // website = new
                    // URL("https://sites.google.com/site/vetitoprogram/home/projector.exee?attredirects=0&d=1");
                    website = new URL("http://www.ktgykolozsvar.ro/misc/projector/projector2.exe");
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
