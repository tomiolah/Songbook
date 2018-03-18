package projector.controller.song;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.controller.song.util.ScheduleSong;
import projector.model.Song;
import projector.service.ServiceManager;
import projector.service.SongService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static projector.controller.song.SongController.setSongCollections;
import static projector.controller.song.SongController.setTextFlowsText;

public class ScheduleController {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleController.class);
    private final String $id$_ = "$id$ ";
    private final String $uuid$_ = "$uuid$ ";

    @FXML
    private ListView<ScheduleSong> listView;

    private SongController songController;
    private KeyCombination keyShiftUp = new KeyCodeCombination(KeyCode.UP, KeyCombination.SHIFT_DOWN);
    private KeyCombination keyShiftDown = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHIFT_DOWN);
    private int selectedIndex;

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public ListView<ScheduleSong> getListView() {
        return listView;
    }

    public void setListView(ListView<ScheduleSong> listView) {
        this.listView = listView;
    }

    public void initialize() {

        listView.setCellFactory(param -> new ListCell<ScheduleSong>() {
            @Override
            protected void updateItem(ScheduleSong item, boolean empty) {
                try {
                    super.updateItem(item, empty);
                    if (item == null) {
                        setGraphic(null);
                    } else if (empty || item.getSong().getTitle() == null) {
                        TextFlow textFlow = setTextFlowsText(item, item.getTextFlow());
                        setGraphic(textFlow);
                        item.setTextFlow(textFlow);
                    } else {
                        Song song = item.getSong();
                        TextFlow textFlow = item.getTextFlow();
                        if (textFlow == null) {
                            textFlow = new TextFlow();
                        } else {
                            textFlow.getChildren().clear();
                        }
                        ObservableList<Node> children = textFlow.getChildren();
                        children.add(new Text(song.getTitle()));
                        setGraphic(textFlow);
                        item.setTextFlow(textFlow);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        listView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                listView.getSelectionModel().clearSelection();
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (listView.getSelectionModel().getSelectedIndex() != -1) {
                selectedIndex = listView.getSelectionModel().getSelectedIndex();
            }
            ScheduleSong selectedItem = listView.getSelectionModel().getSelectedItem();
            if (newValue != null) {
                String text = newValue.getSong().getTitle();
                if (!text.isEmpty()) {
                    setTextColor(selectedItem, Color.rgb(0, 0, 128));
                    songController.selectSong(newValue.getSong());
                }
            }
        });
        final ContextMenu cm = new ContextMenu();
        cm.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                event.consume();
            }
            System.out.println(event.getButton());
        });
        cm.setOnAction(event -> cm.hide());
        MenuItem moveUpMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Move up"));
        MenuItem moveDownMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Move down"));
        MenuItem removeMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Remove"));
        MenuItem saveMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Save"));
        MenuItem loadMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Load"));
        cm.getItems().addAll(moveUpMenuItem, moveDownMenuItem, removeMenuItem, saveMenuItem, loadMenuItem);
        listView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                cm.show(listView, event.getScreenX(), event.getScreenY());
            } else {
                cm.hide();
            }
        });
        moveUpMenuItem.setOnAction(event -> moveUp());
        moveDownMenuItem.setOnAction(event -> moveDown());
        removeMenuItem.setOnAction(event -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                listView.getSelectionModel().clearSelection();
                listView.getItems().remove(selectedIndex);
            }
        });
        saveMenuItem.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(Settings.getInstance().getResourceBundle().getString("Choose a file"));
            fileChooser.getExtensionFilters().add(new ExtensionFilter("text", "*.txt"));
            fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                FileOutputStream ofStream;
                try {
                    ofStream = new FileOutputStream(selectedFile);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, "UTF-8"));
                    for (ScheduleSong i : listView.getItems()) {
                        Song song = i.getSong();
                        if (song.getUuid() == null) {
                            bw.write($id$_ + song.getId() + System.lineSeparator());
                        } else {
                            bw.write($uuid$_ + song.getUuid() + System.lineSeparator());
                        }
                        bw.write(song.getTitle() + System.lineSeparator());
                    }
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        loadMenuItem.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(Settings.getInstance().getResourceBundle().getString("Choose a file"));
            fileChooser.getExtensionFilters().add(new ExtensionFilter("text", "*.txt"));
            fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                FileInputStream ifStream;
                try {
                    ifStream = new FileInputStream(selectedFile);
                    BufferedReader br = new BufferedReader(new InputStreamReader(ifStream, "UTF-8"));
                    listView.getItems().clear();
                    SongService songService = ServiceManager.getSongService();
                    String tmp = br.readLine();
                    List<Song> readSongs = new ArrayList<>();
                    while (tmp != null) {
                        Song byId;
                        if (tmp.startsWith($id$_)) {
                            byId = songService.findById(Long.parseLong(tmp.substring($id$_.length())));
                        } else if (tmp.startsWith($uuid$_)) {
                            byId = songService.findByUuid(tmp.substring($uuid$_.length()));
                        } else {
                            Song byTitle = songService.findByTitle(tmp);
                            if (byTitle != null) {
                                readSongs.add(byTitle);
                            }
                            tmp = br.readLine();
                            continue;
                        }
                        if (byId == null) {
                            Song byTitle = songService.findByTitle(br.readLine());
                            if (byTitle != null) {
                                readSongs.add(byTitle);
                            }
                        } else {
                            br.readLine();
                            readSongs.add(byId);
                        }
                        tmp = br.readLine();
                    }
                    br.close();
                    setSongCollections(readSongs);
                    for (Song song : readSongs) {
                        addSong(song);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listView.setOnKeyPressed(event -> {
            if (keyShiftUp.match(event)) {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex > 0) {
                    ScheduleSong tmp = listView.getSelectionModel().getSelectedItem();
                    listView.getSelectionModel().clearSelection();
                    listView.getSelectionModel().select(selectedIndex - 1);
                    setTextColor(listView.getSelectionModel().getSelectedItem(), Color.rgb(72, 57, 0));
                    listView.getItems().remove(selectedIndex);
                    listView.getItems().add(selectedIndex - 1, tmp);
                }
            } else if (keyShiftDown.match(event)) {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex < listView.getItems().size() - 1) {
                    ScheduleSong tmp = listView.getSelectionModel().getSelectedItem();
                    listView.getSelectionModel().clearSelection();
                    listView.getSelectionModel().select(selectedIndex + 1);
                    setTextColor(listView.getSelectionModel().getSelectedItem(), Color.rgb(72, 57, 0));
                    listView.getItems().remove(selectedIndex);
                    listView.getItems().add(selectedIndex + 1, tmp);
                }
            }
        });

    }

    private void setTextColor(ScheduleSong selectedItem, Color color) {
        TextFlow textFlow = selectedItem.getTextFlow();
        if (textFlow != null) {
            for (Node node : textFlow.getChildren()) {
                Text text1 = (Text) node;
                text1.setFill(color);
            }
        }
    }

    private void moveUp() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            setTextColor(listView.getSelectionModel().getSelectedItem(), Color.rgb(72, 57, 0));
            ScheduleSong tmp = listView.getSelectionModel().getSelectedItem();
            listView.getSelectionModel().clearSelection();
            listView.getItems().remove(selectedIndex);
            listView.getItems().add(selectedIndex - 1, tmp);
        }
    }

    private void moveDown() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < listView.getItems().size() - 1) {
            setTextColor(listView.getSelectionModel().getSelectedItem(), Color.rgb(72, 57, 0));
            ScheduleSong tmp = listView.getSelectionModel().getSelectedItem();
            listView.getSelectionModel().clearSelection();
            listView.getItems().remove(selectedIndex);
            listView.getItems().add(selectedIndex + 1, tmp);
        }
    }

    public void addSong(Song song) {
        if (song != null) {
            listView.getItems().add(new ScheduleSong(song));
        }
    }

    public void setSongController(SongController songController) {
        this.songController = songController;
    }

}
