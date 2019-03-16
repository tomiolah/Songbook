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
import javafx.scene.effect.BlendMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
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
import projector.api.SongApiBean;
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
import projector.controller.song.util.ContainsResult;
import projector.controller.song.util.LastSearching;
import projector.controller.song.util.OrderMethod;
import projector.controller.song.util.ScheduleSong;
import projector.controller.song.util.SearchedSong;
import projector.controller.song.util.SongTextFlow;
import projector.model.Language;
import projector.model.Song;
import projector.model.SongCollection;
import projector.model.SongCollectionElement;
import projector.model.SongVerse;
import projector.remote.SongReadRemoteListener;
import projector.remote.SongRemoteListener;
import projector.service.ServiceException;
import projector.service.ServiceManager;
import projector.service.SongCollectionService;
import projector.service.SongService;
import projector.utils.scene.text.MyTextFlow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static projector.utils.StringUtils.stripAccents;

public class SongController {

    private static final Logger LOG = LoggerFactory.getLogger(SongController.class);
    private static final double minOpacity = 0.4;
    private final SongService songService;
    private final Settings settings = Settings.getInstance();
    private final String link = "https://projector-songbook.herokuapp.com/song/";
    private final String prefix = "id:";
    @FXML
    private BorderPane rightBorderPane;
    @FXML
    private Button showVersionsButton;
    @FXML
    private ComboBox<Language> languageComboBox;
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
    private List<Song> songs = new ArrayList<>();
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
    private boolean isBlank = false;
    private LastSearching lastSearching = LastSearching.IN_TITLE;
    private SongCollection selectedSongCollection;
    private List<ProjectionTextChangeListener> projectionTextChangeListeners;
    private Song selectedSong;
    private ArrayList<SongVerse> selectedSongVerseList;
    private int successfullyCreated;
    private SongRemoteListener songRemoteListener;
    private SongReadRemoteListener songReadRemoteListener;
    private boolean initialized = false;

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

    private static void addWordsInCollection(Song song, Collection<String> words) {
        for (SongVerse songVerse : song.getVerses()) {
            String[] split = songVerse.getText().split("[\\s\\t\\n\\r]");
            for (String word : split) {
                word = stripAccents(word.toLowerCase());
                words.add(word);
            }
        }
    }

    public synchronized void lazyInitialize() {
        if (initialized) {
            return;
        }
        initialized = true;
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

                        showVersionsButton.setVisible(false);
                        String versionGroup = selectedSong.getVersionGroup();
                        if (versionGroup == null) {
                            versionGroup = selectedSong.getUuid();
                        }
                        if (versionGroup != null) {
                            List<Song> allByVersionGroup = songService.findAllByVersionGroup(versionGroup);
                            if (allByVersionGroup.size() > 1) {
                                showVersionsButton.setVisible(true);
                            }
                        }

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
                        } else {
                            for (int j = 0; j < times.length && j < songListViewItems.size(); ++j) {
                                String i = songListViewItems.get(j).getRawText();
                                i = i.replaceAll("[^aeiouAEIOUéáőúöüóűíÉÁŰŐÚÜÓÖÍâÂăĂîÎ]", "");
                                double v = i.length() * 0.72782;
                                if (2 * v < times[j]) {
                                    times[j] = 2 * v;
                                } else if (times[j] < v / 2) {
                                    times[j] = v / 2;
                                }
                            }
                        }
                        if (songRemoteListener != null) {
                            songRemoteListener.onSongVerseListViewChanged(songListViewItems);
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
                        if (x < 700) {
                            event.consume();
                            return;
                        }
                    } else if (keyCode == KeyCode.ENTER) {
                        mainController.setBlank(false);
                    } else if (keyCode.isDigitKey()) {
                        verseTextField.setText(event.getCharacter());
                        verseTextField.requestFocus();
                        event.consume();
                    }
                    if (keyCode == KeyCode.PAGE_DOWN) {
                        setNext();
                        event.consume();
                    } else if (keyCode == KeyCode.PAGE_UP) {
                        setPrevious();
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
                        if ((settings.isShareOnNetwork() || settings.isAllowRemote()) && projectionTextChangeListeners != null) {
                            try {
                                String secondText = getSecondText(selectedIndex - 1);
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
                            projectionScreenController.setLineSize(0);
                        } else {
                            projectionScreenController.setLineSize((double) selectedIndex / (songListViewItems.size() - 2));
                        }
                    } else if (ob.size() > 1) {
                        StringBuilder tmpTextBuffer = new StringBuilder();
                        tmpTextBuffer.append(songListViewItems.get(ob.get(0)).getRawText());
                        int lastIndex = 0;
                        for (int i = 1; i < ob.size(); ++i) {
                            Integer index = ob.get(i);
                            if (index != songListViewItems.size() - 1) {
                                tmpTextBuffer.append("\n").append(songListViewItems.get(index).getRawText());
                                if (lastIndex < index) {
                                    lastIndex = index;
                                }
                            }
                        }
                        projectionScreenController.setLineSize((double) lastIndex / (songListViewItems.size() - 2));
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
            initializeLanguageComboBox();
            exportButton.setOnAction(event -> exportButtonOnAction());
            importButton.setOnAction(event -> importButtonOnAction());
            initializeShowVersionsButton();
            initializeDragListeners();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeDragListeners() {
        listView.setOnDragDetected(event -> {
            Dragboard dragboard = listView.startDragAndDrop(TransferMode.LINK, TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            Song song = listView.getSelectionModel().getSelectedItem().getSong();
            String uuid = song.getUuid();
            String s;
            if (uuid == null) {
                s = prefix + song.getId();
                content.putString(s);
            } else {
                s = link + uuid;
                content.putUrl(s);
            }
            dragboard.setContent(content);
            rightBorderPane.setOpacity(0.8);
        });
        listView.setOnDragDone(event -> rightBorderPane.setOpacity(1.0));
        scheduleListView.setOnDragEntered(dragEvent -> {
            if (getSongFromDragBoard(dragEvent) != null) {
                scheduleListView.setBlendMode(BlendMode.DARKEN);
            }
        });

        scheduleListView.setOnDragExited(dragEvent -> {
            if (getSongFromDragBoard(dragEvent) != null) {
                scheduleListView.setBlendMode(null);
            }
        });

        scheduleListView.setOnDragOver(dragEvent -> {
            if (getSongFromDragBoard(dragEvent) != null) {
                dragEvent.acceptTransferModes(TransferMode.COPY, TransferMode.LINK);
            }
        });

        scheduleListView.setOnDragDropped(dragEvent -> {
            Song songFromDragBoard = getSongFromDragBoard(dragEvent);
            if (songFromDragBoard != null) {
                setSongCollection(songFromDragBoard);
                scheduleController.addSong(songFromDragBoard);
                dragEvent.setDropCompleted(true);
            }
        });
    }

    private void setSongCollection(Song song) {
        List<SongCollectionElement> bySong = ServiceManager.getSongCollectionElementService().findBySong(song);
        if (bySong != null && bySong.size() > 0) {
            SongCollectionElement songCollectionElement = bySong.get(0);
            song.setSongCollection(songCollectionElement.getSongCollection());
            song.setSongCollectionElement(songCollectionElement);
        }
    }

    private Song getSongFromDragBoard(DragEvent dragEvent) {
        Dragboard dragboard = dragEvent.getDragboard();
        String url = dragboard.getUrl();
        if (url != null) {
            return songService.findByUuid(url.replace(link, ""));
        }
        String string = dragboard.getString();
        if (string != null && string.startsWith(prefix)) {
            return songService.findById(Long.parseLong(string.replace(prefix, "")));
        }
        return null;
    }

    private void initializeShowVersionsButton() {
        showVersionsButton.setVisible(false);
        showVersionsButton.setOnAction(event -> {
            String versionGroup = selectedSong.getVersionGroup();
            String uuid = selectedSong.getUuid();
            if (versionGroup == null) {
                versionGroup = uuid;
            }
            List<Song> allByVersionGroup = songService.findAllByVersionGroup(versionGroup);
            final List<Song> songs = new ArrayList<>(allByVersionGroup.size());
            HashMap<String, Song> hashMap = new HashMap<>(songs.size());
            for (Song song : allByVersionGroup) {
                hashMap.put(song.getUuid(), song);
            }
            List<SongCollection> songCollections = ServiceManager.getSongCollectionService().findAll();
            for (SongCollection songCollection : songCollections) {
                for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
                    String songUuid = songCollectionElement.getSongUuid();
                    if (hashMap.containsKey(songUuid)) {
                        Song song = hashMap.get(songUuid);
                        song.setSongCollection(songCollection);
                        song.setSongCollectionElement(songCollectionElement);
                        songs.add(song);
                        hashMap.remove(songUuid);
                    }
                }
            }
            songs.addAll(hashMap.values());
            sortSongsByRelevanceOrder(songs);
            listView.getItems().clear();
            for (Song song : songs) {
                SearchedSong searchedSong = new SearchedSong(song);
                listView.getItems().add(searchedSong);
            }
        });
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
        int whitespaceCount = 0;
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            String s1 = stripAccents((c + "").toLowerCase());
            if (!s1.isEmpty()) {
                if (s1.charAt(0) == lastSearch[matchCount]) {
                    if (matchCount == 0) {
                        whitespaceCount = 0;
                    }
                    ++matchCount;
                    if (matchCount == lastSearch.length) {
                        matchCount = 0;
                        s.append("<color=\"0xFFC600FF\">").append(tmp).append(c).append("</color>");
                        tmp = new StringBuilder();
                        continue;
                    }
                } else {
                    if (matchCount > 0) {
                        i -= matchCount + whitespaceCount;
                        s.append(chars[i]);
                        matchCount = 0;
                        tmp = new StringBuilder();
                        continue;
                    }
                }
            } else {
                ++whitespaceCount;
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
            sortComboBox.getItems().addAll(
                    OrderMethod.RELEVANCE,
                    OrderMethod.ASCENDING_BY_TITLE,
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

    public void initializeLanguageComboBox() {
        try {
            List<Language> languages = ServiceManager.getLanguageService().findAll();
            if (countSelectedLanguages(languages) < 2) {
                languageComboBox.setVisible(false);
                languageComboBox.setManaged(false);
            } else {
                languageComboBox.setVisible(true);
                languageComboBox.setManaged(true);
            }
            languages.sort((o1, o2) -> Integer.compare(o2.getSongs().size(), o1.getSongs().size()));
            languageComboBox.getItems().clear();
            for (Language language : languages) {
                if (!language.getSongs().isEmpty()) {
                    languageComboBox.getItems().add(language);
                } else {
                    break;
                }
            }
            if (settings.isCheckLanguages() && languageComboBox.getItems().size() > 1) {
                Language all = new Language();
                all.setEnglishName("All");
                all.setNativeName("All");
                List<Song> songs = ServiceManager.getSongService().findAll();
                List<Song> noLanguageSongs = new ArrayList<>();
                for (Song song : songs) {
                    if (song.getLanguage() == null) {
                        System.out.println("song = " + song.getTitle());
                        noLanguageSongs.add(song);
                    }
                }
                all.setSongs(songs);
                if (noLanguageSongs.size() > 0) {
                    setLanguagesForSongs(noLanguageSongs);
                }
                languageComboBox.getItems().add(0, all);
            }
            SingleSelectionModel<Language> selectionModel = languageComboBox.getSelectionModel();
            Language songSelectedLanguage = settings.getSongSelectedLanguage();
            for (Language language : languages) {
                if (songSelectedLanguage.getUuid().equals(language.getUuid())) {
                    selectionModel.select(language);
                    settings.setSongSelectedLanguage(language);
                    break;
                }
            }
            selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                settings.setSongSelectedLanguage(newValue);
                readSongs();
                addAllSongs();
                addSongCollections();
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setLanguagesForSongs(List<Song> songs) {
        List<Language> languages = ServiceManager.getLanguageService().findAll();
        List<Song> allWithLanguage = songService.findAll();
        HashMap<String, Song> songHashMap = new HashMap<>();
        for (Song song : allWithLanguage) {
            songHashMap.put(song.getUuid(), song);
        }

        Map<Language, Collection<String>> languageMap = new HashMap<>();
        for (Language language : languages) {
            TreeSet<String> value = new TreeSet<>();
            languageMap.put(language, value);
            for (Song song : language.getSongs()) {
                addWordsInCollection(song, value);
            }
        }
        for (Song song : songs) {
            Song song1 = songHashMap.get(song.getUuid());
            if (song1 != null && song1.getLanguage() != null) {
                Language language = song1.getLanguage();
                Collection<String> words = languageMap.get(language);
                addWordsInCollection(song1, words);
            } else {
                if (song.isDeleted()) {
                    continue;
                }
                List<String> words = new ArrayList<>();
                addWordsInCollection(song, words);
                Map<Language, ContainsResult> countMap = new HashMap<>(languages.size());
                for (Language language1 : languages) {
                    Collection<String> wordsByLanguage = languageMap.get(language1);
                    int count = 0;
                    Integer wordCount = 0;
                    for (String word : words) {
                        if (wordsByLanguage.contains(word)) {
                            ++count;
                        }
                        ++wordCount;
                    }
                    ContainsResult containsResult = new ContainsResult();
                    containsResult.setCount(count);
                    containsResult.setWordCount(wordCount);
                    countMap.put(language1, containsResult);
                }
                Set<Map.Entry<Language, ContainsResult>> entries = countMap.entrySet();
                Map.Entry<Language, ContainsResult> max = new AbstractMap.SimpleEntry<>(null, new ContainsResult());
                for (Map.Entry<Language, ContainsResult> entry : entries) {
                    if (entry.getValue().getRatio() > max.getValue().getRatio()) {
                        max = entry;
                    }
                }
                if (max.getKey() != null) {
//                    System.out.println("Language:   " + max.getKey().getEnglishName());
//                    System.out.println("Ratio:  " + max.getValue().getRatio());
//                    System.out.println("Match count:  " + max.getValue().getCount());
//                    System.out.println("Words:  " + max.getValue().getWordCount());
                    song.setLanguage(max.getKey());
                    songService.create(song);
                    addWordsInCollection(song, languageMap.get(song.getLanguage()));
                } else {
                    System.out.println(song.getTitle());
                }
            }
        }
    }

    private int countSelectedLanguages(List<Language> languages) {
        int count = 0;
        for (Language language : languages) {
            if (!language.getSongs().isEmpty()) {
                ++count;
            }
        }
        return count;
    }

    private void initializeVerseTextField() {
        verseTextField.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation());
        verseTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int x = Integer.parseInt(newValue.trim());
                int size = songListView.getItems().size();
                if (x >= 0 && x < size && x * 10 > size - 1 || x == 0) {
                    songListView.getSelectionModel().clearAndSelect(x);
                    songListView.scrollTo(x);
                    verseTextField.setText("");
                } else if (x >= size) {
                    verseTextField.setText("");
                }
            } catch (NumberFormatException ignored) {
            }
        });
        verseTextField.setOnKeyPressed(event -> {
            mainController.globalKeyEventHandler().handle(event);
            if (event.isConsumed()) {
                return;
            }
            KeyCode keyCode = event.getCode();
            if (keyCode.equals(KeyCode.ENTER)) {
                selectByVerseTextFieldNumber();
            } else if (keyCode.isArrowKey()) {
                songListView.requestFocus();
                songListView.fireEvent(event);
            }
        });
    }

    private void selectByVerseTextFieldNumber() {
        try {
            int x = Integer.parseInt(verseTextField.getText().trim());
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
            if (selectedIndex < 0) {
                if (selectedIndex == -1) {
                    return songListView.getItems().get(0).getRawText();
                }
                return "";
            }
            if (selectedIndex >= selectedSongVerseList.size()) {
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
                    scene.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle(Settings.getInstance().getResourceBundle().getString("Download languages"));
                    stage.show();
                    downloadLanguagesController.setSongController(this);
                    downloadLanguagesController.setStage(stage);
                    downloadOldVersionGroups();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void downloadOldVersionGroups() {
        // This will be deleted
        if (getOldVersion() == 0) {
            Thread thread = new Thread(() -> {
                SongApiBean songApi = new SongApiBean();
                List<Language> languages = ServiceManager.getLanguageService().findAll();
                for (Language language : languages) {
                    if (!language.getSongs().isEmpty()) {
                        final List<Song> songApiSongs = songApi.getSongsByLanguageAndAfterModifiedDate(language, 1524234911591L);
                        for (Song song : songApiSongs) {
                            if (song.getVersionGroup() != null) {
                                Song byUuid = songService.findByUuid(song.getUuid());
                                if (byUuid != null) {
                                    byUuid.setVersionGroup(song.getVersionGroup());
                                    byUuid.setVerses(byUuid.getVerses());
                                    songService.update(byUuid);
                                }
                            }
                        }
                    }
                }
            });
            thread.start();
            try (FileOutputStream stream = new FileOutputStream("data/songs.version");
                 BufferedWriter br = new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8))) {
                br.write("1\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getOldVersion() {
        try (FileInputStream stream = new FileInputStream("data/songs.version");
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return Integer.parseInt(br.readLine());
        } catch (FileNotFoundException | NumberFormatException ignored) {
            List<Song> all = songService.findAll();
            if (all.size() == 0) {
                return 1;
            }
            for (Song song : all) {
                if (song.getVersionGroup() != null) {
                    return 1;
                }
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
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
                    scene.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
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
                            remainingText = searchText.substring(firstWord.length() + 1);
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
                ordinalNumber = firstWord.substring(i);
            }
            int ordinalNumberInt = Integer.MIN_VALUE;
            try {
                ordinalNumberInt = Integer.parseInt(ordinalNumber);
            } catch (Exception ignored) {
            }
            String remainingText = "";
            try {
                remainingText = text.substring(firstWord.length() + 1);
            } catch (Exception ignored) {
            }
            remainingText = stripAccents(remainingText);
            text = stripAccents(text);
            List<Song> songs = selectedSongCollection.getSongs();
            boolean wasOrdinalNumber = false;
            for (Song song : songs) {
                SongCollectionElement songCollectionElement = song.getSongCollectionElement();
                boolean contains = false;
                if (songCollectionElement != null) {
                    SongCollection songCollection = song.getSongCollection();
                    String name = songCollection.getStrippedName();
                    boolean contains1 = name.contains(collectionName) || songCollection.getStrippedShortName().contains(collectionName);
                    String number = songCollectionElement.getOrdinalNumber();
                    boolean equals = number.equals(ordinalNumber);
                    boolean contains2 = number.contains(ordinalNumber) || equals || ordinalNumberInt == songCollectionElement.getOrdinalNumberInt();
                    boolean b = remainingText.isEmpty() || song.getStrippedTitle().contains(remainingText);
                    if (contains1 && contains2 && b) {
                        contains = true;
                        if (equals) {
                            wasOrdinalNumber = true;
                        }
                    }
                }
                if (contains || contains(song.getStrippedTitle(), text)) {
                    SearchedSong searchedSong = new SearchedSong(song);
                    listView.getItems().add(searchedSong);
                }
            }
            if (wasOrdinalNumber) {
                listView.getItems().sort((l, r) -> {
                    SongCollectionElement lSongCollectionElement = l.getSong().getSongCollectionElement();
                    SongCollectionElement rSongCollectionElement = r.getSong().getSongCollectionElement();
                    if (lSongCollectionElement != null && rSongCollectionElement != null) {
                        return Integer.compare(lSongCollectionElement.getOrdinalNumberInt(), rSongCollectionElement.getOrdinalNumberInt());
                    } else {
                        return 1;
                    }
                });
            }
            selectIfJustOne();
            if (songRemoteListener != null) {
                songRemoteListener.onSongListViewChanged(listView.getItems());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void titleSearchStartWith(String text) {
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
                    return;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
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
            Language songSelectedLanguage = settings.getSongSelectedLanguage();
            if (songSelectedLanguage != null) {
                songs = songSelectedLanguage.getSongs();
                setSongCollections(songs);
            }
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
            if (selectedItem.equals(OrderMethod.RELEVANCE)) {
                sortSongsByRelevanceOrder(songs);
            } else if (selectedItem.equals(OrderMethod.ASCENDING_BY_TITLE)) {
                songs.sort(Comparator.comparing(l -> l.getStrippedTitle().toLowerCase()));
            } else if (selectedItem.equals(OrderMethod.DESCENDING_BY_TITLE)) {
                songs.sort((l, r) -> r.getStrippedTitle().toLowerCase().compareTo(l.getStrippedTitle().toLowerCase()));
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
                        if (lSongCollection.getName().equals(rSongCollection.getName())) {
                            SongCollectionElement lSongCollectionElement = l.getSongCollectionElement();
                            SongCollectionElement rSongCollectionElement = r.getSongCollectionElement();
                            if (lSongCollectionElement != null && rSongCollectionElement != null) {
                                return Integer.compare(lSongCollectionElement.getOrdinalNumberInt(), rSongCollectionElement.getOrdinalNumberInt());
                            }
                        }
                        return lSongCollection.getStrippedName().compareTo(rSongCollection.getStrippedName());
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

    private void sortSongsByRelevanceOrder(List<Song> songs) {
        songs.sort((lhs, rhs) -> {
            Integer scoreL = lhs.getScore();
            Integer scoreR = rhs.getScore();
            if (scoreL.equals(scoreR)) {
                return rhs.getModifiedDate().compareTo(lhs.getModifiedDate());
            }
            return scoreR.compareTo(scoreL);
        });
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
            MenuItem addToCollectionMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Add to collection"));
            MenuItem removeFromCollectionMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Remove from collection"));
            MenuItem deleteMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Delete"));
            MenuItem addScheduleMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Add to schedule"));
            cm.getItems().addAll(editMenuItem, addToCollectionMenuItem, deleteMenuItem, addScheduleMenuItem);
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
                        scene.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
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
                                .add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
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
            addToCollectionMenuItem.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    try {
                        Song selectedSong = listView.getSelectionModel().getSelectedItem().getSong();
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(Main.class.getResource("/view/song/AddToCollection.fxml"));
                        loader.setResources(Settings.getInstance().getResourceBundle());
                        Pane root = loader.load();
                        AddToCollectionController addToCollectionController = loader.getController();
                        addToCollectionController.setSongController(songController);
                        addToCollectionController.setSelectedSong(selectedSong);
                        Scene scene = new Scene(root);
                        scene.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
                        Stage stage = new Stage();
                        stage.setScene(scene);
                        stage.setTitle(Settings.getInstance().getResourceBundle().getString("Add to collection"));
                        addToCollectionController.setStage(stage);
                        stage.show();
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            removeFromCollectionMenuItem.setOnAction(event -> {
                try {
                    Song selectedSong = listView.getSelectionModel().getSelectedItem().getSong();
                    SongCollectionElement songCollectionElement = selectedSong.getSongCollectionElement();
                    ServiceManager.getSongCollectionElementService().delete(songCollectionElement);
                    selectedSong.setSongCollection(null);
                    selectedSong.setSongCollectionElement(null);
                    addSongCollections();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
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
                        Song selectedSong = listView.getSelectionModel().getSelectedItem().getSong();
                        boolean hasSongCollection = selectedSong.getSongCollectionElement() != null;
                        if (hasSongCollection) {
                            cm.getItems().remove(addToCollectionMenuItem);
                            cm.getItems().add(1, removeFromCollectionMenuItem);
                        } else {
                            cm.getItems().remove(removeFromCollectionMenuItem);
                            cm.getItems().add(1, addToCollectionMenuItem);
                        }
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
                        scene.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
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

    void addSongCollections() {
        try {
            ObservableList<SongCollection> items = songCollectionListView.getItems();
            items.clear();
            SongCollection allSongCollections = new SongCollection(Settings.getInstance().getResourceBundle().getString("All"));
            allSongCollections.setSongs(songs);
            selectedSongCollection = allSongCollections;
            items.add(allSongCollections);
            songCollectionListView.getSelectionModel().selectFirst();
            SongCollectionService songCollectionService = ServiceManager.getSongCollectionService();
            try {
                Date date = new Date();
                List<SongCollection> songCollections = songCollectionService.findAll();
                Date date2 = new Date();
                System.out.println(date2.getTime() - date.getTime());
                songCollections.sort((l, r) -> {
                    if (l.getSongs().size() < r.getSongs().size()) {
                        return 1;
                    } else if (l.getSongs().size() > r.getSongs().size()) {
                        return -1;
                    }
                    return 0;
                });
                Language songSelectedLanguage = settings.getSongSelectedLanguage();
                if (songSelectedLanguage != null) {
                    Long id = songSelectedLanguage.getId();
                    for (SongCollection songCollection : songCollections) {
                        if (songCollection.getLanguage().getId().equals(id)) {
                            items.add(songCollection);
                        }
                    }
                }
                boolean value = items.size() != 1;
                songCollectionListView.setVisible(value);
                songCollectionListView.setManaged(value);
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
            scene.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
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
            scene2.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());

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
            scene.getStylesheets().add(getClass().getResource("/view/" + settings.getSceneStyleFile()).toExternalForm());
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
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
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
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, StandardCharsets.UTF_8));

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
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
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

    void selectSong(Song song) {
        ObservableList<SearchedSong> listViewItems = listView.getItems();
        listViewItems.clear();
        listViewItems.add(new SearchedSong(song));
        listView.getSelectionModel().selectFirst();
    }

    public void setSongRemoteListener(SongRemoteListener songRemoteListener) {
        this.songRemoteListener = songRemoteListener;
    }

    public SongReadRemoteListener getSongReadRemoteListener() {
        if (songReadRemoteListener == null) {
            songReadRemoteListener = new SongReadRemoteListener() {
                @Override
                public void onSongVerseListViewItemClick(int index) {
                    Platform.runLater(() -> {
                        if (songListView.getItems().size() > index) {
                            songListView.getSelectionModel().clearAndSelect(index);
                        }
                    });
                }

                @Override
                public void onSongListViewItemClick(int index) {
                    Platform.runLater(() -> {
                        if (listView.getItems().size() > index) {
                            listView.getSelectionModel().clearAndSelect(index);
                        }
                    });
                }

                @Override
                public void onSearch(String text) {
                    Platform.runLater(() -> titleSearch(text));
                }

                @Override
                public void onSongPrev() {
                    Platform.runLater(() -> setPrevious());
                }

                @Override
                public void onSongNext() {
                    Platform.runLater(() -> {
                        selectNextSongFromScheduleIfLastIndex();
                        setNext();
                    });
                }
            };
        }
        return songReadRemoteListener;
    }
}
