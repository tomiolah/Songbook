package projector.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.song.SongController;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScheduleController {

    @FXML
    private ListView<Text> listView;

    private SongController songController;
    private BibleController bibleController;
    private List<ProjectionType> typeList;
    private List<Integer> bookI;
    private List<Integer> partI;
    private List<Integer> versI;
    private List<List<Integer>> versNumbersList;
    private int searchSelected;
    private KeyCombination keyShiftUp = new KeyCodeCombination(KeyCode.UP, KeyCombination.SHIFT_DOWN);
    private KeyCombination keyShiftDown = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHIFT_DOWN);

    public ListView<Text> getListView() {
        return listView;
    }

    public void setListView(ListView<Text> listView) {
        this.listView = listView;
    }

    public void initialize() {
        typeList = new LinkedList<>();
        bookI = new LinkedList<>();
        partI = new LinkedList<>();
        versI = new LinkedList<>();
        versNumbersList = new ArrayList<>();
        listView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                listView.getSelectionModel().clearSelection();
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            listView.getSelectionModel().getSelectedItem();
            if (newValue != null && !newValue.getText().isEmpty()) {
                listView.getSelectionModel().getSelectedItem().setFill(Color.rgb(0, 0, 128));
                if (typeList.get(listView.getSelectionModel().getSelectedIndex()) == ProjectionType.SONG) {
                    if (!songController.titleSearchStartWith(newValue.getText())) {
                        songController.setTitleTextFieldText(newValue.getText());
                    }
                } else if (typeList.get(listView.getSelectionModel().getSelectedIndex()) == ProjectionType.BIBLE) {
                    int index = listView.getSelectionModel().selectedIndexProperty().get();
                    if (index >= 0) {
                        bibleController.addAllBooks();
                        while (bibleController.isNotAllBooks()) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (bibleController.getBookListView().getSelectionModel().getSelectedIndex() != bookI
                                .get(index)) {
                            searchSelected = 1;
                        } else {
                            searchSelected = 0;
                        }
                        bibleController.getBookListView().getSelectionModel().select(bookI.get(index));
                        while (searchSelected == 1) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        bibleController.getBookListView().scrollTo(bookI.get(index));
                        if (bibleController.getPartListView().getSelectionModel().getSelectedIndex() != partI
                                .get(index)) {
                            searchSelected = 2;
                        } else {
                            searchSelected = 0;
                        }
                        int p = partI.get(index);
                        bibleController.getPartListView().getSelectionModel().select(p);
                        while (searchSelected == 2) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        bibleController.getPartListView().scrollTo(partI.get(index));
                        bibleController.getVerseListView().getSelectionModel().clearSelection();
                        bibleController.setSelecting(true);
                        for (Integer i : versNumbersList.get(index)) {
                            bibleController.getVerseListView().getSelectionModel().select(i);
                        }
                        bibleController.setSelecting(false);
                        bibleController.getVerseListView().scrollTo(versI.get(index));
                    }
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
                    for (Text i : listView.getItems()) {
                        bw.write(i.getText() + System.lineSeparator());
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
                    String tmp = br.readLine();
                    while (tmp != null) {
                        addRecentSong(tmp, ProjectionType.SONG);
                        tmp = br.readLine();
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listView.setOnKeyPressed(event -> {
            if (keyShiftUp.match(event)) {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex > 0) {
                    Text tmp = listView.getSelectionModel().getSelectedItem();
                    listView.getSelectionModel().clearSelection();
                    listView.getSelectionModel().select(selectedIndex - 1);
                    listView.getSelectionModel().getSelectedItem().setFill(Color.rgb(72, 57, 0));
                    listView.getItems().remove(selectedIndex);
                    listView.getItems().add(selectedIndex - 1, tmp);
                }
            } else if (keyShiftDown.match(event)) {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex < listView.getItems().size() - 1) {
                    Text tmp = listView.getSelectionModel().getSelectedItem();
                    listView.getSelectionModel().clearSelection();
                    listView.getSelectionModel().select(selectedIndex + 1);
                    listView.getSelectionModel().getSelectedItem().setFill(Color.rgb(72, 57, 0));
                    listView.getItems().remove(selectedIndex);
                    listView.getItems().add(selectedIndex + 1, tmp);
                }
            }
        });

    }

    private void moveUp() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            listView.getSelectionModel().getSelectedItem().setFill(Color.rgb(72, 57, 0));
            Text tmp = listView.getSelectionModel().getSelectedItem();
            listView.getSelectionModel().clearSelection();
            listView.getItems().remove(selectedIndex);
            listView.getItems().add(selectedIndex - 1, tmp);
        }
    }

    private void moveDown() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < listView.getItems().size() - 1) {
            listView.getSelectionModel().getSelectedItem().setFill(Color.rgb(72, 57, 0));
            Text tmp = listView.getSelectionModel().getSelectedItem();
            listView.getSelectionModel().clearSelection();
            listView.getItems().remove(selectedIndex);
            listView.getItems().add(selectedIndex + 1, tmp);
        }
    }

    public void addRecentSong(String text, ProjectionType type) {
        if (!text.trim().isEmpty()) {
            typeList.add(type);
            Text tmpText = new Text(text);
            listView.getItems().add(tmpText);
            bookI.add(-1);
            partI.add(0);
            versI.add(0);
            ArrayList<Integer> tmp = new ArrayList<>();
            tmp.add(-1);
            versNumbersList.add(tmp);
        }
    }

    public void setPrefHeight(double d) {
        double c = 0.705357143;
        d -= 40;
        listView.setLayoutY(d * c);
        listView.setPrefHeight((d - 14) - d * c);
    }

    public void setPrefWidth(double d) {
        listView.setPrefWidth(listView.getWidth() + d);
    }

    void setSongController(SongController songController) {
        this.songController = songController;
    }

    void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

}
