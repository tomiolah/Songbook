package projector.controller.song;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.Main;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.application.SongVersTime;
import projector.application.SongVersTimes;
import projector.controller.MyController;
import projector.controller.ProjectionScreenController;
import projector.controller.ProjectionTextChangeListener;
import projector.controller.RecentController;
import projector.controller.eventHandler.NextButtonEventHandler;
import projector.controller.language.DownloadLanguagesController;
import projector.controller.song.util.LastSearching;
import projector.controller.song.util.OrderMethod;
import projector.controller.song.util.ScheduleSong;
import projector.controller.song.util.SearchedSong;
import projector.controller.song.util.SongTextFlow;
import projector.model.Song;
import projector.model.SongCollection;
import projector.model.SongCollectionElement;
import projector.model.SongVerse;
import projector.service.ServiceException;
import projector.service.ServiceManager;
import projector.service.SongCollectionService;
import projector.service.SongService;
import projector.utils.scene.text.MyTextFlow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static projector.utils.StringUtils.stripAccents;

public class SongController {

    private static final Logger LOG = LoggerFactory.getLogger(SongController.class);
    private static final double minOpacity = 0.4;
    private final SongService songService;
    private final Settings settings = Settings.getInstance();
    @FXML
    private Button importButton;
    @FXML
    private Button exportButton;
    @FXML
    private ComboBox<OrderMethod> sortComboBox;
    @FXML
    private TextField verseTextField;
    @FXML
    private TextField searchTextField;
    @FXML
    private CheckBox searchInTextCheckBox;
    @FXML
    private ListView<SearchedSong> listView;
    @FXML
    private ListView<MyTextFlow> songListView;
    @FXML
    private Button downloadButton;
    @FXML
    private Button uploadButton;
    @FXML
    private Button newSongButton;
    @FXML
    private ListView<ScheduleSong> scheduleListView;
    @FXML
    private ListView<SongCollection> songCollectionListView;
    @FXML
    private SplitPane horizontalSplitPane;
    @FXML
    private SplitPane verticalSplitPane;
    @FXML
    private BorderPane leftBorderPane;
    @FXML
    private Button nextButton;
    @FXML
    private Slider songHeightSlider;
    @FXML
    private ToggleButton progressLineToggleButton;
    @FXML
    private CheckBox aspectRatioCheckBox;

    private ProjectionScreenController projectionScreenController;
    private ProjectionScreenController previewProjectionScreenController;
    private RecentController recentController;
    private ScheduleController scheduleController;
    private List<Song> songs;
    private String lastSearchText = "";
    private SongController songController = this;
    private SongVersTime activeSongVersTime;
    private long timeStart;
    private List<SongVersTime> previousSongVersTimeList;
    private int previousSelectedVersIndex;
    private ObservableList<MyTextFlow> songSelectedItems;
    private Thread previousLineThread;
    private SongVersTimes songVersTimes;
    private double[] times;
    private MyController mainController;
    private boolean isBlank = true;

    private LastSearching lastSearching = LastSearching.IN_TITLE;
    private SongCollection selectedSongCollection;
    private List<ProjectionTextChangeListener> projectionTextChangeListeners;
    private Song selectedSong;
    private ArrayList<SongVerse> selectedSongVerseList;
    private int successfullyCreated;

    public SongController() {
        songService = ServiceManager.getSongService();
    }

    public static EventHandler<KeyEvent> getKeyEventEventHandler(Logger log) {
        return event -> {
            try {
                if (!event.getCharacter().matches("[0-9]") && event.getCode() != KeyCode.F1) {
                    event.consume();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        };
    }

    static void setSongCollectionForSongsInHashMap(List<SongCollection> songCollections, HashMap<String, Song> hashMap) {
        for (SongCollection songCollection : songCollections) {
            for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
                String songUuid = songCollectionElement.getSongUuid();
                if (hashMap.containsKey(songUuid)) {
                    Song song = hashMap.get(songUuid);
                    song.setSongCollection(songCollection);
                    song.setSongCollectionElement(songCollectionElement);
                }
            }
        }
    }

    static TextFlow setTextFlowsText(SongTextFlow item, TextFlow textFlow) {
        Text text = new Text("");
        if (textFlow == null) {
            textFlow = new TextFlow(text);
            item.setTextFlow(textFlow);
        } else {
            ObservableList<Node> children = textFlow.getChildren();
            children.clear();
            children.add(text);
        }
        return textFlow;
    }

    static void setSongCollections(List<Song> songs) {
        List<SongCollection> songCollections = ServiceManager.getSongCollectionService().findAll();
        HashMap<String, Song> hashMap = new HashMap<>(songs.size());
        for (Song song : songs) {
            hashMap.put(song.getUuid(), song);
        }
        setSongCollectionForSongsInHashMap(songCollections, hashMap);
    }

    public synchronized void initialize() {
        try {
            songVersTimes = SongVersTimes.getInstance();
            previousSongVersTimeList = new LinkedList<>();
            songListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            newSongButton.setFocusTraversable(false);
            searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (searchInTextCheckBox.isSelected()) {
                        search(newValue);
                    } else {
                        titleSearch(newValue);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            searchTextField.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.ENTER) {
                        selectFirstSong();
                    } else if (event.getCode() == KeyCode.F1) {
                        mainController.setBlank();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            searchInTextCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (newValue) {
                        search(lastSearchText);
                    } else {
                        titleSearch(lastSearchText);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            listView.setCellFactory(param -> new ListCell<SearchedSong>() {
                @Override
                protected void updateItem(SearchedSong item, boolean empty) {
                    try {
                        super.updateItem(item, empty);
                        if (item == null) {
                            setGraphic(null);
                        } else if (empty || item.getSong().getTitle() == null) {
                            TextFlow textFlow = setTextFlowsText(item, item.getTextFlow());
                            setGraphic(textFlow);
                        } else {
                            Song song = item.getSong();
                            SongCollection songCollection = song.getSongCollection();
                            TextFlow textFlow = item.getTextFlow();
                            if (textFlow == null) {
                                textFlow = new TextFlow();
                            } else {
                                textFlow.getChildren().clear();
                            }
                            ObservableList<Node> children = textFlow.getChildren();
                            if (songCollection != null) {
                                Text collectionName = new Text(songCollection.getName() + " ");
                                collectionName.setFill(Color.rgb(0, 9, 118));
                                children.add(collectionName);
                                Text ordinalNumber = new Text(song.getSongCollectionElement().getOrdinalNumber() + "\n");
                                ordinalNumber.setFill(Color.rgb(54, 0, 255));
                                children.add(ordinalNumber);
                            }
                            children.add(new Text(song.getTitle()));
                            if (item.getFoundAtVerse() != null) {
                                Text text = new Text(item.getFoundAtVerse());
                                text.setFill(Color.rgb(17, 150, 0));
                                children.add(text);
                            }
                            setGraphic(textFlow);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            ObservableList<MyTextFlow> songListViewItems = songListView.getItems();
            listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    int index = listView.getSelectionModel().selectedIndexProperty().get();
                    if (index >= 0) {
                        if (activeSongVersTime != null && activeSongVersTime.getVersTimes() != null
                                && activeSongVersTime.getVersTimes().length > previousSelectedVersIndex
                                && previousSelectedVersIndex >= 0
                                && activeSongVersTime.getVersTimes()[previousSelectedVersIndex] == 0.0) {
                            double x = System.currentTimeMillis() - timeStart;
                            x /= 1000;
                            activeSongVersTime.getVersTimes()[previousSelectedVersIndex] = x;
                        }
                        songListViewItems.clear();
                        selectedSong = listView.getSelectionModel().getSelectedItem().getSong();
                        final int width = (int) projectionScreenController.getScene().getWidth();
                        int height = (int) projectionScreenController.getScene().getHeight();
                        final int size = (int) songHeightSlider.getValue();
                        selectedSongVerseList = new ArrayList<>(selectedSong.getVerses().size());
                        addAgainChorus(selectedSong, selectedSongVerseList);
                        MyTextFlow myTextFlow = new MyTextFlow();
                        int width1;
                        boolean aspectRatioCheckBoxSelected = aspectRatioCheckBox.isSelected();
                        if (height < 10) {
                            height = 10;
                        }
                        if (aspectRatioCheckBoxSelected) {
                            width1 = (size * width - 30) / height;
                        } else {
                            width1 = (int) songListView.getWidth() - 30;
                        }
                        myTextFlow.setPrefWidth(width);
                        myTextFlow.setPrefHeight(size);
                        myTextFlow.setTextAlignment(TextAlignment.CENTER);
                        myTextFlow.setBackGroundColor();
                        myTextFlow.setOpacity(minOpacity);
                        String selectedSongTitle = "";
                        SongCollection songCollection = selectedSong.getSongCollection();
                        SongCollectionElement songCollectionElement = selectedSong.getSongCollectionElement();
                        if (songCollection != null) {
                            selectedSongTitle += songCollection.getName();
                            if (songCollectionElement != null) {
                                selectedSongTitle += " " + songCollectionElement.getOrdinalNumber();
                            }
                            selectedSongTitle += "\n";
                        }
                        selectedSongTitle += selectedSong.getTitle();
                        myTextFlow.setText2(selectedSongTitle, width1, size);
                        songListViewItems.add(myTextFlow);
                        for (SongVerse songVerse : selectedSongVerseList) {
                            myTextFlow = new MyTextFlow();
                            aspectRatioCheckBoxSelected = aspectRatioCheckBox.isSelected();
                            if (aspectRatioCheckBoxSelected) {
                                width1 = (size * width - 30) / height;
                            } else {
                                width1 = (int) songListView.getWidth() - 30;
                            }
                            myTextFlow.setPrefWidth(width);
                            myTextFlow.setPrefHeight(size);
                            myTextFlow.setTextAlignment(TextAlignment.CENTER);
                            myTextFlow.setBackGroundColor();
                            myTextFlow.setOpacity(minOpacity);
                            String text = songVerse.getText();
                            myTextFlow.setText2(getColorizedStringByLastSearchedText(text), width1, size);
                            myTextFlow.setRawText(text);
                            songListViewItems.add(myTextFlow);
                        }
                        myTextFlow = new MyTextFlow();
                        myTextFlow.setText2("", 100, size / 3);
                        myTextFlow.setPrefHeight(100);
                        myTextFlow.setBackGroundColor();
                        myTextFlow.setOpacity(minOpacity);
                        songListViewItems.add(myTextFlow);
                        songListView.getFocusModel().focus(0);
                        songListView.scrollTo(0);
                        if (activeSongVersTime != null) {
                            previousSongVersTimeList.add(activeSongVersTime);
                        }
                        activeSongVersTime = new SongVersTime(selectedSong.getTitle(), songListViewItems.size() - 1);
                        previousSelectedVersIndex = -1;
                        times = songVersTimes.getAverageTimes(selectedSong.getTitle());
                        if (times == null) {
                            times = new double[songListViewItems.size() - 1];
                            for (int j = 0; j < times.length; ++j) {
                                String i = songListViewItems.get(j).getRawText();
                                i = i.replaceAll("[^aeiouAEIOUéáőúöüóűíÉÁŰŐÚÜÓÖÍâÂăĂîÎ]", "");
                                times[j] = i.length() * 0.72782;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            initListViewMenuItem();
            initSongCollectionListViewMenuItem();

            listView.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                        songListView.requestFocus();
                        songListView.getFocusModel().focus(0);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            songListView.setOnKeyPressed(event -> {
                try {
                    KeyCode keyCode = event.getCode();
                    if (keyCode == KeyCode.DOWN) {
                        selectNextSongFromScheduleIfLastIndex();
                    }
                    if (keyCode == KeyCode.DOWN || keyCode == KeyCode.UP || keyCode == KeyCode.HOME
                            || keyCode == KeyCode.END || keyCode == KeyCode.PAGE_DOWN
                            || keyCode == KeyCode.PAGE_UP) {
                        double x = System.currentTimeMillis() - timeStart;
                        x /= 1000;
                        if (x < 1) {
                            event.consume();
                        }
                    } else if (keyCode == KeyCode.ENTER) {
                        mainController.setBlank(false);
                    } else if (keyCode.isDigitKey()) {
                        verseTextField.setText(event.getCharacter());
                        verseTextField.requestFocus();
                        event.consume();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            songSelectedItems = songListView.getSelectionModel().getSelectedItems();
            songListView.getSelectionModel().getSelectedIndices().addListener((ListChangeListener<Integer>) c -> {
                try {
                    ObservableList<Integer> ob = songListView.getSelectionModel().getSelectedIndices();
                    if (ob.size() == 1) {
                        int selectedIndex = ob.get(0);
                        if (selectedIndex < 0) {
                            return;
                        }
                        if (settings.isShareOnNetwork() && projectionTextChangeListeners != null) {
                            try {
                                String secondText = getSecondText(selectedIndex);
                                for (ProjectionTextChangeListener projectionTextChangeListener : projectionTextChangeListeners) {
                                    projectionTextChangeListener.onSetText(secondText, ProjectionType.SONG);
                                }
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            }
                        }
                        if (timeStart != 0 && previousSelectedVersIndex >= 0
                                && previousSelectedVersIndex < activeSongVersTime.getVersTimes().length) {
                            double x = System.currentTimeMillis() - timeStart;
                            x /= 1000;
                            activeSongVersTime.getVersTimes()[previousSelectedVersIndex] = x;
                        }
                        projectionScreenController.setText(songListViewItems.get(selectedIndex).getRawText(), ProjectionType.SONG);
                        previousSelectedVersIndex = selectedIndex;
                        if (selectedIndex + 1 == songListViewItems.size()) {
                            projectionScreenController.progressLineSetVisible(false);
                        } else {
                            projectionScreenController.setLineSize((double) selectedIndex / (songListViewItems.size() - 2));
                        }
                    } else if (ob.size() > 1) {
                        StringBuilder tmpTextBuffer = new StringBuilder();
                        tmpTextBuffer.append(songListViewItems.get(ob.get(0)).getRawText().replaceAll("\\n", " "));
                        for (int i = 1; i < ob.size(); ++i) {
                            if (ob.get(i) != songListViewItems.size() - 1) {
                                tmpTextBuffer.append("\n").append(songListViewItems.get(ob.get(i)).getRawText().replaceAll("\\n", " "));
                            }
                        }
                        projectionScreenController.setText(tmpTextBuffer.toString(), ProjectionType.SONG);
                    }
                    if (recentController != null && !recentController.getLastItemText().equals(activeSongVersTime.getSongTitle()) &&
                            ob.size() > 0) {
                        recentController.addRecentSong(activeSongVersTime.getSongTitle(), ProjectionType.SONG);
                    }
                    timeStart = System.currentTimeMillis();
                    if (previousLineThread == null) {
                        Thread thread = new Thread() {

                            @Override
                            synchronized public void run() {
                                try {
                                    double x;
                                    //noinspection InfiniteLoopStatement
                                    do {
                                        if (!isBlank && timeStart != 0 && previousSelectedVersIndex >= 0
                                                && previousSelectedVersIndex < activeSongVersTime.getVersTimes().length) {
                                            x = System.currentTimeMillis() - timeStart;
                                            x /= 1000;
                                            double sum = 0.0;
                                            for (int i : songListView.getSelectionModel().getSelectedIndices()) {
                                                if (times.length > i) {
                                                    sum += times[i];
                                                }
                                            }
                                            double z = 1.0 - minOpacity;
                                            final double v = z * x / sum;
                                            for (MyTextFlow songListViewItem : songSelectedItems) {
                                                double opacity = minOpacity + v;
                                                if (opacity > 1) {
                                                    opacity = 1;
                                                }
                                                songListViewItem.setOpacity(opacity);
                                            }
                                        }
                                        wait(39);
                                    } while (true);
                                } catch (InterruptedException ignored) {
                                } catch (Exception e) {
                                    LOG.error(e.getMessage(), e);
                                    this.start();
                                }
                            }
                        };
                        thread.start();
                        previousLineThread = thread;
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            songListView.setOnMouseClicked(event -> {
                try {
                    if (event.getClickCount() == 2) {
                        projectionScreenController.setText(
                                songListViewItems.get(songListView.getSelectionModel().getSelectedIndex()).getRawText(),
                                ProjectionType.SONG);
                        timeStart = System.currentTimeMillis();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            songCollectionListView.orientationProperty().set(Orientation.HORIZONTAL);
            songCollectionListView.setCellFactory(param -> new ListCell<SongCollection>() {
                @Override
                protected void updateItem(SongCollection item, boolean empty) {
                    try {
                        super.updateItem(item, empty);
                        if (empty || item == null || item.getName() == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            songCollectionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (newValue != null) {
                        selectedSongCollection = newValue;
                        sortSongs(selectedSongCollection.getSongs());
                    }
                    switch (lastSearching) {
                        case IN_SONG:
                            search(lastSearchText);
                            break;
                        case IN_TITLE:
                            titleSearch(lastSearchText);
                            break;
                        case IN_TITLE_START_WITH:
                            titleSearchStartWith(lastSearchText);
                            break;
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            Settings settings = Settings.getInstance();
            SplitPane.setResizableWithParent(leftBorderPane, false);
            horizontalSplitPane.getDividers().get(0).setPosition(settings.getSongTabHorizontalSplitPaneDividerPosition());
            verticalSplitPane.setDividerPositions(settings.getSongTabVerticalSplitPaneDividerPosition());
            songHeightSlider.setValue(settings.getSongHeightSliderValue());
            songHeightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    final int size = newValue.intValue();
                    resizeSongList(size);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            aspectRatioCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    final int size = (int) songHeightSlider.getValue();
                    resizeSongList(size);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            initializeNextButton();
            initializeProgressLineButton();
            initializeDownloadButton();
            initializeUploadButton();
            initializeVerseTextField();
            initializeSortComboBox();
            exportButton.setOnAction(event -> exportButtonOnAction());
            importButton.setOnAction(event -> importButtonOnAction());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void selectNextSongFromScheduleIfLastIndex() {
        if (songListView.getSelectionModel().getSelectedIndex() == songListView.getItems().size() - 1) {
            int nextIndex = scheduleController.getSelectedIndex() + 1;
            scheduleListView.getSelectionModel().select(nextIndex);
        }
    }

    private String getColorizedStringByLastSearchedText(String text) {
        StringBuilder s = new StringBuilder();
        char[] lastSearch = stripAccents(lastSearchText.toLowerCase()).toCharArray();
        if (lastSearch.length == 0) {
            return text;
        }
        int matchCount = 0;
        char[] chars = text.toCharArray();
        StringBuilder tmp = new StringBuilder();
        for (char c : chars) {
            String s1 = stripAccents((c + "").toLowerCase());
            if (!s1.isEmpty()) {
                if (s1.charAt(0) == lastSearch[matchCount]) {
                    ++matchCount;
                    if (matchCount == lastSearch.length) {
                        matchCount = 0;
                        s.append("<color=\"0xFFC600FF\">").append(tmp).append(c).append("</color>");
                        tmp = new StringBuilder();
                        continue;
                    }
                } else {
                    matchCount = 0;
                    s.append(tmp);
                    tmp = new StringBuilder();
                }
            }
            if (matchCount == 0) {
                s.append(c);
            } else {
                tmp.append(c);
            }
        }
        return s.toString();
    }

    private void initializeSortComboBox() {
        try {
            OrderMethod ascendingByTitle = OrderMethod.ASCENDING_BY_TITLE;

            sortComboBox.getItems().addAll(ascendingByTitle,
                    OrderMethod.DESCENDING_BY_TITLE,
                    OrderMethod.BY_MODIFIED_DATE,
                    OrderMethod.BY_PUBLISHED,
                    OrderMethod.BY_COLLECTION);
            SingleSelectionModel<OrderMethod> selectionModel = sortComboBox.getSelectionModel();
            selectionModel.selectFirst();
            selectionModel.select(settings.getSongOrderMethod());
            selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                sortSongs(songs);
                addAllSongs();
                addSongCollections();
                settings.setSongOrderMethod(newValue);
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeVerseTextField() {
        verseTextField.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation());
        verseTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int x = Integer.parseInt(newValue.trim());
                int size = songListView.getItems().size();
                if (x >= 0 && x < size && (x + 1) * 10 > size) {
                    songListView.getSelectionModel().clearAndSelect(x);
                    songListView.scrollTo(x);
                    verseTextField.setText("");
                } else if ((x + 1) > size * 10) {
                    verseTextField.setText("");
                }
            } catch (NumberFormatException ignored) {
            }
        });
        verseTextField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                selectByVerseTextFieldNumber();
            }
        });
    }

    private void selectByVerseTextFieldNumber() {
        try {
            int x = Integer.parseInt(verseTextField.getText().trim()) - 1;
            int size = songListView.getItems().size();
            if (x >= 0 && x < size) {
                songListView.getSelectionModel().clearAndSelect(x);
                songListView.scrollTo(x);
                verseTextField.setText("");
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private EventHandler<KeyEvent> numeric_Validation() {
        return getKeyEventEventHandler(LOG);
    }

    private String getSecondText(int selectedIndex) {
        try {
            if (selectedIndex == selectedSongVerseList.size()) {
                return "";
            }
            SongVerse songVerse = selectedSongVerseList.get(selectedIndex);
            String secondText = songVerse.getSecondText();
            if (secondText == null || secondText.trim().isEmpty()) {
                secondText = songVerse.getText();
            }
            return secondText;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return "";
        }
    }

    private void initializeDownloadButton() {
        try {
            downloadButton.setOnAction(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(Main.class.getResource("/view/language/DownloadLanguages.fxml"));
                    loader.setResources(Settings.getInstance().getResourceBundle());
                    Pane root = loader.load();
                    DownloadLanguagesController downloadLanguagesController = loader.getController();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle(Settings.getInstance().getResourceBundle().getString("Download languages"));
                    stage.show();
                    downloadLanguagesController.setSongController(this);
                    downloadLanguagesController.setStage(stage);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeUploadButton() {
        try {
            uploadButton.setOnAction(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(Main.class.getResource("/view/song/UploadSongs.fxml"));
                    loader.setResources(Settings.getInstance().getResourceBundle());
                    Pane root = loader.load();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle(Settings.getInstance().getResourceBundle().getString("Upload songs"));
                    stage.show();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void addAgainChorus(Song selectedSong, List<SongVerse> verseList) {
        try {
            final List<SongVerse> verses = selectedSong.getVerses();
            SongVerse chorus = null;
            int size = verses.size();
            for (int i = 0; i < size; ++i) {
                SongVerse songVerse = verses.get(i);
                verseList.add(songVerse);
                if (songVerse.isChorus()) {
                    chorus = songVerse;
                } else if (chorus != null) {
                    if (i + 1 < size) {
                        if (!verses.get(i + 1).isChorus()) {
                            verseList.add(chorus);
                        }
                    } else {
                        verseList.add(chorus);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeProgressLineButton() {
        try {
            progressLineToggleButton.setOnAction(event -> settings.setShowProgressLine(progressLineToggleButton.isSelected()));
            progressLineToggleButton.setSelected(true);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeNextButton() {
        try {
            nextButton.setOnAction(event -> {
                try {
                    final MultipleSelectionModel<MyTextFlow> selectionModel = songListView.getSelectionModel();
                    final int selectedIndex = selectionModel.getSelectedIndex();
                    if (selectedIndex >= 0) {
                        final int index = selectedIndex + 1;
                        if (songListView.getItems().size() > index) {
                            selectionModel.clearAndSelect(index);
                            songListView.scrollTo(index);
                        }
                    } else {
                        if (songListView.getItems().size() > 0) {
                            selectionModel.clearAndSelect(0);
                            songListView.scrollTo(0);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            nextButton.addEventHandler(KeyEvent.KEY_PRESSED, new NextButtonEventHandler(nextButton, LOG) {
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void resizeSongList(int size) {
        try {
            int width1;
            final int width = (int) projectionScreenController.getScene().getWidth();
            int height = (int) projectionScreenController.getScene().getHeight();
            if (height < 10) {
                height = 10;
            }
            if (aspectRatioCheckBox.isSelected()) {
                width1 = (size * width - 30) / height;
            } else {
                width1 = (int) songListView.getWidth() - 30;
            }
            for (MyTextFlow myTextFlow : songListView.getItems()) {
                myTextFlow.setSize(width1, size);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        try {
            this.projectionScreenController = projectionScreenController;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setRecentController(RecentController recentController) {
        try {
            this.recentController = recentController;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setScheduleController(ScheduleController scheduleController) {
        try {
            this.scheduleController = scheduleController;
            scheduleController.setListView(scheduleListView);
            scheduleController.initialize();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public boolean contains(String a, String b) {
        try {
            a = stripAccents(a);
            b = stripAccents(b);
            a = a.replace("[", "");
            a = a.replace("]", "");
            b = b.replace("[", "");
            b = b.replace("]", "");
            return a.toLowerCase().contains(b.toLowerCase().trim());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    private void search(String text) {
        try {
            lastSearching = LastSearching.IN_SONG;
            List<Song> songs = selectedSongCollection.getSongs();
            if (text.trim().isEmpty()) {
                listView.getItems().clear();
                for (Song song : songs) {
                    SearchedSong searchedSong = new SearchedSong(song);
                    listView.getItems().add(searchedSong);
                }
                lastSearchText = text;
            } else {
                Thread thread = new Thread(() -> {
                    try {
                        String searchText = text;
                        String[] split = searchText.split(" ");
                        String firstWord = split[0];
                        String remainingText = "";
                        try {
                            remainingText = searchText.substring(firstWord.length() + 1, searchText.length());
                        } catch (Exception ignored) {
                        }
                        remainingText = stripAccents(remainingText);
                        searchText = stripAccents(searchText);
                        searchText = searchText.toLowerCase();
                        ArrayList<Integer> tmpSearchISong = new ArrayList<>();
                        ArrayList<String> tmpSearchIFoundAtLine = new ArrayList<>();
                        for (int i = 0; i < songs.size(); ++i) {
                            boolean contains = false;
                            String line = "";
                            Song song = songs.get(i);
                            SongCollectionElement songCollectionElement = song.getSongCollectionElement();
                            if (songCollectionElement != null) {
                                if (songCollectionElement.getOrdinalNumber().contains(firstWord) && !(remainingText.isEmpty() || song.getStrippedTitle().contains(remainingText))) {
                                    System.out.println("remainingText = " + remainingText);
                                }
                                if (songCollectionElement.getOrdinalNumber().contains(firstWord) && (remainingText.isEmpty() || song.getStrippedTitle().contains(remainingText))) {
                                    contains = true;
                                }
                            }
                            if (song.getStrippedTitle().contains(searchText)) {
                                contains = true;
                            } else {
                                final List<SongVerse> verses = song.getVerses();
                                for (SongVerse verse : verses) {
                                    if (verse.getStrippedText().contains(searchText)) {
                                        contains = true;
                                        line = "\n";
                                        final int k = 35;
                                        final String text1 = verse.getText();
                                        if (text1.length() > k) {
                                            line += text1.substring(0, k).replaceAll("\\n", " ") + "...";
                                        } else {
                                            line += text1;
                                        }
                                        break;
                                    }
                                }
                            }
                            if (contains) {
                                tmpSearchISong.add(i);
                                tmpSearchIFoundAtLine.add(line);
                            }
                        }
                        Platform.runLater(() -> {
                            try {
                                listView.getItems().clear();
                                for (int i = 0; i < tmpSearchISong.size(); ++i) {
                                    SearchedSong searchedSong = new SearchedSong(songs.get(tmpSearchISong.get(i)));
                                    searchedSong.setFoundAtVerse(tmpSearchIFoundAtLine.get(i));
                                    listView.getItems().add(searchedSong);
                                }
                                selectIfJustOne();
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            } finally {
                                lastSearchText = text;
                            }
                        });
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
                thread.start();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void titleSearch(String text) {
        try {
            text = text.trim();
            lastSearching = LastSearching.IN_TITLE;
            lastSearchText = text;
            listView.getItems().clear();
            String[] split = text.split(" ");
            String firstWord = split[0];
            String ordinalNumber = firstWord;
            String collectionName = "";
            if (!firstWord.matches("^[0-9]+.*")) {
                char[] chars = firstWord.toCharArray();
                int i;
                for (i = 0; i < chars.length; ++i) {
                    if (chars[i] >= '0' && chars[i] <= '9') {
                        break;
                    }
                }
                collectionName = stripAccents(firstWord.substring(0, i).toLowerCase());
                ordinalNumber = firstWord.substring(i, firstWord.length());
            }
            String remainingText = "";
            try {
                remainingText = text.substring(firstWord.length() + 1, text.length());
            } catch (Exception ignored) {
            }
            remainingText = stripAccents(remainingText);
            text = stripAccents(text);
            List<Song> songs = selectedSongCollection.getSongs();
            for (Song song : songs) {
                SongCollectionElement songCollectionElement = song.getSongCollectionElement();
                boolean contains = false;
                if (songCollectionElement != null) {
                    String name = song.getSongCollection().getStrippedName();
                    if ((name.contains(collectionName)) && songCollectionElement.getOrdinalNumber().contains(ordinalNumber) && (remainingText.isEmpty() || song.getStrippedTitle().contains(remainingText))) {
                        contains = true;
                    }
                }
                if (contains || contains(song.getStrippedTitle(), text)) {
                    SearchedSong searchedSong = new SearchedSong(song);
                    listView.getItems().add(searchedSong);
                }
            }
            selectIfJustOne();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public boolean titleSearchStartWith(String text) {
        try {
            lastSearching = LastSearching.IN_TITLE_START_WITH;
            lastSearchText = text;
            listView.getItems().clear();
            List<Song> songs = selectedSongCollection.getSongs();
            for (Song song : songs) {
                if (song.getTitle().equals(text)) {
                    SearchedSong searchedSong = new SearchedSong(song);
                    listView.getItems().add(searchedSong);
                    listView.getSelectionModel().select(0);
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    private void selectIfJustOne() {
        try {
            if (listView.getItems().size() == 1) {
                listView.getSelectionModel().clearAndSelect(0);
                listView.requestFocus();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void initializeSongs() {
        try {
            Date date = new Date();
            readSongs();
            sortSongs(songs);
            addAllSongs();
            addSongCollections();
            Date date2 = new Date();
            final String time = date2.getTime() - date.getTime() + "";
            LOG.info(time);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void readSongs() {
        try {
            songs = songService.findAll();
            setSongCollections(songs);
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void addAllSongs() {
        try {
            listView.getItems().clear();
            for (Song song : songs) {
                SearchedSong searchedSong = new SearchedSong(song);
                listView.getItems().add(searchedSong);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void sortSongs(List<Song> songs) {
        try {
            OrderMethod selectedItem = sortComboBox.getSelectionModel().getSelectedItem();
            if (selectedItem.equals(OrderMethod.ASCENDING_BY_TITLE)) {
                songs.sort(Comparator.comparing(l -> l.getTitle().toLowerCase()));
            } else if (selectedItem.equals(OrderMethod.DESCENDING_BY_TITLE)) {
                songs.sort((l, r) -> r.getTitle().toLowerCase().compareTo(l.getTitle().toLowerCase()));
            } else if (selectedItem.equals(OrderMethod.BY_MODIFIED_DATE)) {
                songs.sort((l, r) -> r.getModifiedDate().compareTo(l.getModifiedDate()));
            } else if (selectedItem.equals(OrderMethod.BY_PUBLISHED)) {
                songs.sort((l, r) -> {
                    if (l.isPublished() && !r.isPublished()) {
                        return 1;
                    } else if (!l.isPublished() && r.isPublished()) {
                        return -1;
                    }
                    return 0;
                });
            } else if (selectedItem.equals(OrderMethod.BY_COLLECTION)) {
                songs.sort((l, r) -> {
                    SongCollection rSongCollection = r.getSongCollection();
                    SongCollection lSongCollection = l.getSongCollection();
                    if (lSongCollection != null && rSongCollection != null) {
                        return lSongCollection.getName().compareTo(rSongCollection.getName());
                    } else if (lSongCollection != null) {
                        return -1;
                    } else if (rSongCollection != null) {
                        return 1;
                    }
                    return 0;
                });
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
    //        }
    //            }
    //                }
    ////                    }
    //                    // + " " + songs.get(j).getTitle());
    //                    // s2.length() + " " + songs.get(i).getTitle()
    //                    // System.out.println(s.length() + " " + x + " " +
    ////                    if (((double) x) / (double) s.length() > 0.9 || ((double) x) / (double) s2.length() > 0.9) {
    ////                    int x = StringUtils.highestCommonSubStringInt(s.toString(), s2.toString());
    //                    }
    //                        s2.append(k);
    //                    for (String k : songs.get(j).getVerses()) {
    //                    StringBuilder s2 = new StringBuilder();
    //                if (!songs.get(j).getTitle().equals(tmp.getTitle())) {
    //            for (int j = i + 1; j < songs.size(); ++j) {
    //            }
    //                s.append(k);
    //            for (String k : tmp.getVerses()) {
    //            Song tmp = songs.get(i);
    //            StringBuilder s = new StringBuilder();
    //        for (int i = 900; i < songs.size(); ++i) {
//    public void similars() {

//    }

    private void initListViewMenuItem() {
        try {
            final ContextMenu cm = new ContextMenu();
            consumeContextMenuOnSecondaryButton(cm);
            cm.setOnAction(event -> {
                try {
                    cm.hide();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            MenuItem editMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Edit"));
            MenuItem deleteMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Delete"));
            MenuItem addScheduleMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Add to schedule"));
            cm.getItems().addAll(editMenuItem, deleteMenuItem, addScheduleMenuItem);
            editMenuItem.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    try {
                        Song selectedSong = listView.getSelectionModel().getSelectedItem().getSong();
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(Main.class.getResource("/view/song/NewSong.fxml"));
                        loader.setResources(Settings.getInstance().getResourceBundle());
                        Pane root = loader.load();
                        NewSongController newSongController = loader.getController();
                        newSongController.setSongController(songController);
                        newSongController.setEdit();
                        newSongController.setSelectedSong(listView.getSelectionModel().getSelectedItem());
                        newSongController.setTitleTextFieldText(selectedSong.getTitle());
                        Scene scene = new Scene(root);
                        scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
                        Stage stage = new Stage();
                        stage.setScene(scene);
                        stage.setTitle(Settings.getInstance().getResourceBundle().getString("Song Edit"));
                        stage.show();

                        FXMLLoader loader2 = new FXMLLoader();
                        loader2.setLocation(Main.class.getResource("/view/ProjectionScreen.fxml"));
                        loader2.setResources(Settings.getInstance().getResourceBundle());
                        Pane root2 = loader2.load();
                        previewProjectionScreenController = loader2.getController();
                        newSongController.setPreviewProjectionScreenController(previewProjectionScreenController);
                        Scene scene2 = new Scene(root2, 400, 300);
                        scene2.getStylesheets()
                                .add(getClass().getResource("/view/application.css").toExternalForm());
                        scene2.widthProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.repaint());
                        scene2.heightProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.repaint());
                        Stage stage2 = new Stage();
                        stage2.setScene(scene2);

                        stage2.setX(0);
                        stage2.setY(0);
                        stage2.setTitle(Settings.getInstance().getResourceBundle().getString("Preview"));
                        stage2.show();
                        previewProjectionScreenController.setStage(stage2);

                        stage.setOnCloseRequest(we -> stage2.close());
                        newSongController.setEditingSong(selectedSong);
                        newSongController.setStage(stage, stage2);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            deleteMenuItem.setOnAction(event -> {
                try {
                    deleteSong(listView.getSelectionModel().getSelectedItem());
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            addScheduleMenuItem.setOnAction(event -> {
                try {
                    Song tmp = listView.getSelectionModel().getSelectedItem().getSong();
                    scheduleController.addSong(tmp);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            listView.setOnMouseClicked(event -> {
                try {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        cm.show(listView, event.getScreenX(), event.getScreenY());
                    } else {
                        cm.hide();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initSongCollectionListViewMenuItem() {
        try {
            final ContextMenu cm = new ContextMenu();
            consumeContextMenuOnSecondaryButton(cm);
            cm.setOnAction(event -> {
                try {
                    cm.hide();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            MenuItem editMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Edit"));
            MenuItem deleteMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Delete"));
//            cm.getItems().addAll(editMenuItem, deleteMenuItem);
            cm.getItems().addAll(deleteMenuItem);
            editMenuItem.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(Main.class.getResource("/view/song/SongBook.fxml"));
                        loader.setResources(Settings.getInstance().getResourceBundle());
                        Pane root = loader.load();
                        NewSongCollectionController newSongCollectionController = loader.getController();
                        newSongCollectionController.setSongController(songController);
                        newSongCollectionController.setEditing(true, songCollectionListView.getSelectionModel().getSelectedItem());
                        newSongCollectionController.setSongs(songs);
                        Scene scene = new Scene(root);
                        scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
                        Stage stage = new Stage();
                        stage.setScene(scene);
                        stage.setTitle(Settings.getInstance().getResourceBundle().getString("SongBook Edit"));
                        stage.show();
                        newSongCollectionController.setStage(stage);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            deleteMenuItem.setOnAction(event -> {
                try {
                    SongCollectionService SongCollectionService = ServiceManager.getSongCollectionService();
                    SongCollection selectedItem = songCollectionListView.getSelectionModel().getSelectedItem();
                    SongCollectionService.delete(selectedItem);
                    songCollectionListView.getItems().remove(selectedItem);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });

            songCollectionListView.setOnMouseClicked(event -> {
                try {
                    if (event.getButton() == MouseButton.SECONDARY && !songCollectionListView.getSelectionModel().getSelectedIndices().get(0).equals(0)) {
                        cm.show(songCollectionListView, event.getScreenX(), event.getScreenY());
                    } else {
                        cm.hide();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void consumeContextMenuOnSecondaryButton(ContextMenu cm) {
        cm.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            try {
                if (event.getButton() == MouseButton.SECONDARY) {
                    event.consume();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    public void addSongCollections() {
        try {
            songCollectionListView.getItems().clear();
            SongCollection allSongs = new SongCollection(Settings.getInstance().getResourceBundle().getString("All"));
            allSongs.setSongs(songs);
            selectedSongCollection = allSongs;
            songCollectionListView.getItems().add(allSongs);
            songCollectionListView.getSelectionModel().selectFirst();
            SongCollectionService SongCollectionService = ServiceManager.getSongCollectionService();
            try {
                Date date = new Date();
                List<SongCollection> SongCollections = SongCollectionService.findAll();
                Date date2 = new Date();
                System.out.println(date2.getTime() - date.getTime());
                for (SongCollection SongCollection : SongCollections) {
                    songCollectionListView.getItems().add(SongCollection);
                }
            } catch (ServiceException e) {
                LOG.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public ListView<MyTextFlow> getSongListView() {
        return songListView;
    }

    List<Song> getSongs() {
        return songs;
    }

    private void deleteSong(SearchedSong selectedSong) {
        try {
            final Song song = removeSongFromList(selectedSong);
            listView.getItems().remove(selectedSong);
            try {
                songService.delete(song);
            } catch (ServiceException e) {
                LOG.error(e.getMessage(), e);
            }
            addAllSongs();
            addSongCollections();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    Song removeSongFromList(SearchedSong searchedSong) {
        try {
            listView.getItems().remove(searchedSong);
            final Song song = searchedSong.getSong();
            for (int i = 0; i < songs.size(); ++i) {
                final Song song1 = songs.get(i);
                if (song.getId().equals(song1.getId())) {
                    songs.remove(i);
                    return song1;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public void newSongButtonOnAction() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/view/song/NewSong.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            NewSongController newSongController = loader.getController();
            newSongController.setSongController(songController);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(Settings.getInstance().getResourceBundle().getString("Song Edit"));
            stage.show();

            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(Main.class.getResource("/view/ProjectionScreen.fxml"));
            loader2.setResources(Settings.getInstance().getResourceBundle());
            Pane root2 = loader2.load();
            previewProjectionScreenController = loader2.getController();
            newSongController.setPreviewProjectionScreenController(previewProjectionScreenController);
            Scene scene2 = new Scene(root2, 400, 300);
            scene2.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());

            scene2.widthProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    previewProjectionScreenController.repaint();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            scene2.heightProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    previewProjectionScreenController.repaint();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            Stage stage2 = new Stage();
            stage2.setScene(scene2);

            stage2.setX(0);
            stage2.setY(0);
            stage2.show();
            previewProjectionScreenController.setStage(stage2);

            stage.setOnCloseRequest(we -> stage2.close());
            newSongController.setStage(stage, stage2);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void newSongCollectionButtonOnAction() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/view/song/SongBook.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            NewSongCollectionController newSongCollectionController = loader.getController();
            newSongCollectionController.setSongs(songs);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(Settings.getInstance().getResourceBundle().getString("New song book"));
            newSongCollectionController.setStage(stage);
            newSongCollectionController.setSongController(this);
            stage.show();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void onClose() {
        try {
            if (previousLineThread != null) {
                previousLineThread.interrupt();
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream("songVersTimes", true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
                Date date = new Date();
                boolean wasSong = false;
                if (activeSongVersTime != null) {
                    previousSongVersTimeList.add(activeSongVersTime);
                }
                int minSec = 10;
                for (SongVersTime tmp : previousSongVersTimeList) {
                    double x = 0;
                    for (double j : tmp.getVersTimes()) {
                        x += j;
                    }
                    if (x > minSec) {
                        wasSong = true;
                        break;
                    }
                }
                if (wasSong) {
                    bw.write(date + System.lineSeparator());
                    for (SongVersTime tmp : previousSongVersTimeList) {
                        double x = 0;
                        for (double j : tmp.getVersTimes()) {
                            x += j;
                        }
                        if (x > minSec) {
                            bw.write(tmp.getSongTitle() + System.lineSeparator());
                            for (double j : tmp.getVersTimes()) {
                                bw.write(j + " ");
                            }
                            bw.write(System.lineSeparator());
                        }
                    }
                    bw.write(System.lineSeparator());
                    bw.write(System.lineSeparator());
                }
                bw.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            Settings settings = Settings.getInstance();
            settings.setSongTabHorizontalSplitPaneDividerPosition(horizontalSplitPane.getDividerPositions()[0]);
            settings.setSongTabVerticalSplitPaneDividerPosition(verticalSplitPane.getDividerPositions()[0]);
            settings.setSongHeightSliderValue(songHeightSlider.getValue());
            settings.save();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setNext() {
        try {
            if (songListView.getSelectionModel().getSelectedIndex() + 1 < songListView.getItems().size()) {
                songListView.getSelectionModel().clearAndSelect(songListView.getSelectionModel().getSelectedIndex() + 1);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setPrevious() {
        try {
            if (songListView.getSelectionModel().getSelectedIndex() - 1 >= 0) {
                songListView.getSelectionModel().clearAndSelect(songListView.getSelectionModel().getSelectedIndex() - 1);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void onBlankButtonSelected(boolean isSelected) {
        try {
            if (isSelected) {
                if (activeSongVersTime != null && activeSongVersTime.getVersTimes() != null
                        && activeSongVersTime.getVersTimes().length > previousSelectedVersIndex
                        && previousSelectedVersIndex >= 0
                        && activeSongVersTime.getVersTimes()[previousSelectedVersIndex] == 0.0) {
                    double x = System.currentTimeMillis() - timeStart;
                    x /= 1000;
                    activeSongVersTime.getVersTimes()[previousSelectedVersIndex] = x;
                }
            } else {
                timeStart = System.currentTimeMillis();
            }
            isBlank = isSelected;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setMainController(MyController mainController) {
        try {
            this.mainController = mainController;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void selectFirstSong() {
        try {
            if (listView.getItems().size() > 0) {
                listView.getSelectionModel().clearAndSelect(0);
                listView.requestFocus();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public ListView<SearchedSong> getListView() {
        return listView;
    }

    void addSong(Song song) {
        try {
            songs.add(song);
            sortSongs(songs);
            addAllSongs();
            int scrollToIndex = 0;
            for (Song song1 : songs) {
                if (song1.equals(song)) {
                    break;
                }
                ++scrollToIndex;
            }
            addSongCollections();
            songController.getListView().scrollTo(scrollToIndex);
            songController.getListView().getSelectionModel().select(scrollToIndex);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public synchronized void addProjectionTextChangeListener(ProjectionTextChangeListener projectionTextChangeListener) {
        if (projectionTextChangeListeners == null) {
            projectionTextChangeListeners = new ArrayList<>();
        }
        projectionTextChangeListeners.add(projectionTextChangeListener);
    }

    public synchronized void removeProjectionTextChangeListener(ProjectionTextChangeListener projectionTextChangeListener) {
        if (projectionTextChangeListeners != null) {
            Platform.runLater(() -> projectionTextChangeListeners.remove(projectionTextChangeListener));
        }
    }

    private void exportButtonOnAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Settings.getInstance().getResourceBundle().getString("Choose a file"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("text", "*.txt"));
        fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
        File selectedFile = fileChooser.showSaveDialog(null);
        Thread thread = new Thread(() -> {
            if (selectedFile != null) {
                FileOutputStream ofStream;
                try {
                    ofStream = new FileOutputStream(selectedFile);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, "UTF-8"));

                    Gson gson = new GsonBuilder().serializeNulls()
                            .excludeFieldsWithoutExposeAnnotation().create();
                    for (Song song : songs) {
                        song.getVerses();
                    }
                    String json = gson.toJson(songs);
                    bw.write(json);
                    bw.close();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Completed");
                        alert.setContentText("Successfully exported!");
                        alert.showAndWait();
                    });
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        thread.start();
    }

    private void importButtonOnAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Settings.getInstance().getResourceBundle().getString("Choose a file"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("text", "*.txt"));
        fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
        File selectedFile = fileChooser.showOpenDialog(null);
        Thread thread = new Thread(() -> {
            if (selectedFile != null) {
                FileInputStream inputStream;
                try {
                    inputStream = new FileInputStream(selectedFile);
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    StringBuilder s = new StringBuilder();
                    String readLine = br.readLine();
                    while (readLine != null) {
                        s.append(readLine);
                        readLine = br.readLine();
                    }
                    Gson gson = new GsonBuilder().serializeNulls()
                            .excludeFieldsWithoutExposeAnnotation().create();
                    ArrayList<Song> songArrayList;
                    Type listType = new TypeToken<ArrayList<Song>>() {
                    }.getType();
                    songArrayList = gson.fromJson(s.toString(), listType);
                    successfullyCreated = 0;
                    for (Song song : songArrayList) {
                        Song byUuid = null;
                        String uuid = song.getUuid();
                        if (uuid != null) {
                            byUuid = songService.findByUuid(uuid);
                        }
                        if (byUuid == null) {
                            try {
                                song.stripTitle();
                                song.setVerses(song.getVerses());
                                songService.create(song);
                                ++successfullyCreated;
                            } catch (ServiceException ignored) {
                            }
                        }
                    }
                    br.close();
                    Platform.runLater(() -> {
                        initializeSongs();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Completed");
                        String contentText = "Successfully imported: " + successfullyCreated;
                        int conflicts = songArrayList.size() - successfullyCreated;
                        if (conflicts > 0) {
                            contentText += "\nConflicts: " + conflicts + "\nIf you have trouble please contact us!";
                        }
                        alert.setContentText(contentText);
                        alert.showAndWait();
                    });
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        thread.start();
    }

    public void onKeyPressed(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        if (keyCode.isDigitKey()) {
            verseTextField.setText(verseTextField.getText() + event.getText());
            verseTextField.requestFocus();
            event.consume();
        } else if (keyCode.equals(KeyCode.ENTER)) {
            selectByVerseTextFieldNumber();
        }
    }

    public void selectSong(Song song) {
        ObservableList<SearchedSong> listViewItems = listView.getItems();
        listViewItems.clear();
        listViewItems.add(new SearchedSong(song));
        listView.getSelectionModel().selectFirst();
    }
}
