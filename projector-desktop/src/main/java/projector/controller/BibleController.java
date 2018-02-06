package projector.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.application.Reader;
import projector.application.Settings;
import projector.controller.eventHandler.NextButtonEventHandler;
import projector.model.Bible;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.Reference;
import projector.model.ReferenceBook;
import projector.model.ReferenceChapter;
import projector.utils.MarkTextFlow;
import projector.utils.StringUtils;
import projector.utils.Triplet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static projector.controller.song.SongController.getKeyEventEventHandler;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BibleController {

    private static final Logger LOG = LoggerFactory.getLogger(BibleController.class);
    private static final int verseRightMargin = 37;
    private MyController mainController;
    private ProjectionScreenController projectionScreenController;
    private BibleSearchController bibleSearchController;
    private RecentController recentController;
    private HistoryController historyController;
    @FXML
    private ListView<String> bookListView;
    @FXML
    private ListView<Integer> partListView;
    @FXML
    private Label partLabel;
    @FXML
    private ListView<MarkTextFlow> verseListView;
    @FXML
    private TextField bookTextField;
    @FXML
    private TextField partTextField;
    @FXML
    private TextField verseTextField;
    @FXML
    private ListView<String> bibleListView;
    @FXML
    private TextArea referenceTextArea;
    @FXML
    private Button sendProjectionScreenText;
    @FXML
    private Button referenceResetText;
    @FXML
    private Button referenceEditText;
    @FXML
    private ListView<String> referenceListView;
    @FXML
    private SplitPane horizontalSplitPane;
    @FXML
    private SplitPane verticalSplitPane;
    @FXML
    private TextField searchTextField;
    @FXML
    private Label foundLabel;
    @FXML
    private Button decreaseButton;
    @FXML
    private Button increaseButton;
    @FXML
    private Button nextButton;

    private Bible bible;
    private Bible parallelBible;
    private List<Integer> searchIBook;
    private Integer searchSelected = 0;
    private boolean isAllBooks;
    private boolean isSelecting;
    private boolean firstDrag = false;
    private int partListBookI = -1;
    private int selectedBook = -1;
    private int selectedPart = -1;
    private int selectedVerse = -1;
    private boolean isLastVerse = false;
    private List<String> shiftParallel;
    private List<String> multipleShiftParallel;
    private List<Integer> shiftParallelI;

    private Reference ref;
    private List<Reference> references;
    private boolean oldReplace = false;
    private boolean newReferenceAdded = false;
    private Settings settings = Settings.getInstance();
    private Font verseFont;

    private static String strip(String s) {
        try {
            s = stripAccents(s).replaceAll("[^a-zA-Z]", "").toLowerCase(Locale.US).trim();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return s;
    }

    private static String stripAccents(String s) {
        try {
            s = Normalizer.normalize(s, Normalizer.Form.NFD);
            s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return s;
    }

    private static void oldReplaceScrollTo(int tmp, ListView listView) {
        try {
            if (tmp > listView.getItems().size()) {
                listView.scrollTo(listView.getItems().size() - 2);
                listView.getFocusModel().focus(listView.getItems().size() - 1);
            } else {
                listView.scrollTo(0);
                listView.getFocusModel().focus(0);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static void resetTextFieldValue(String oldValue, TextField partTextField) {
        try {
            if (!oldValue.trim().isEmpty()) {
                try {
                    Integer.parseInt(oldValue.trim());
                    partTextField.setText(oldValue.trim());
                } catch (NumberFormatException ne) {
                    partTextField.setText("");
                }
            } else {
                partTextField.setText("");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void initialize() {
        try {
            // double x = System.currentTimeMillis();
            // System.out.println("Ido4: " + (System.currentTimeMillis() - x));
            // double x = System.currentTimeMillis();
            bibleListView.orientationProperty().set(Orientation.HORIZONTAL);
            for (String bibleTitle : settings.getBibleTitles()) {
                bibleListView.getItems().add(bibleTitle);
            }
            bibleListView.getSelectionModel().select(0);
            bibleListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (settings.isParallel()) {
                        readShiftsParallel();
                    }
                    settings.setCurrentBible(newValue.intValue());

                    int bookI = selectedBook;// bookListView.getSelectionModel().getSelectedIndex();
                    int partI = selectedPart;// partListView.getSelectionModel().getSelectedIndex();
                    ObservableList<Integer> obVerseI = verseListView.getSelectionModel().getSelectedIndices();
                    Vector<Integer> verseI = new Vector<>(obVerseI.size());
                    verseI.addAll(obVerseI);
                    Reader.setBooksRead(false);
                    bible.setBooks(Reader.getBooks(settings.getBiblePaths().get(newValue.intValue())));
                    addAllBooks();
                    bibleSearchController.setBooks(bible.getBooks());
                    historyController.setBible(bible);

                    if (bookI > bible.getBooks().length - 1) {
                        bookI = bible.getBooks().length - 1;
                    }
                    if (bookI >= 0) {
                        bookListView.getSelectionModel().select(bookI);
                        bookListView.scrollTo(bookI);
                        if (partI > bible.getBooks()[bookI].getChapters().length - 1) {
                            partI = bible.getBooks()[bookI].getChapters().length - 1;
                        }
                        if (partI >= 0) {
                            partListView.getSelectionModel().select(partI);
                            partListView.scrollTo(partI);
                            if (verseI.size() > 0) {
                                setSelecting(true);
                                for (Integer aVerseI : verseI) {
                                    if (aVerseI > bible.getBooks()[bookI].getChapters()[partI].getVerses().length
                                            - 1) {
                                        verseListView.getSelectionModel().select(
                                                bible.getBooks()[bookI].getChapters()[partI].getVerses().length - 1);
                                        break;
                                    } else {
                                        verseListView.getSelectionModel().select(aVerseI);
                                    }
                                }
                                verseListView.scrollTo(verseI.get(0));
                                setSelecting(false);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            bookTextField.textProperty().addListener((observable, oldValue, newValue) -> titleSearch(newValue));
            bookTextField.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.RIGHT) {
                        if (event.isAltDown() || bookTextField.getCaretPosition() == bookTextField.getText().length()) {
                            partTextField.requestFocus();
                        }
                    } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                        if (bookListView.getItems().size() > 0) {
                            bookListView.getSelectionModel().select(0);
                            if (!partTextField.getText().equals("")) {
                                oldReplace = true;
                                partTextField.setText("");
                            }
                            partTextField.requestFocus();
                        }
                    } else if (event.getCode() == KeyCode.DOWN) {
                        bookListView.requestFocus();
                    } else if (event.getCode() == KeyCode.F1) {
                        mainController.setBlank();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            partTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (oldReplace) {
                        oldReplace = false;
                        partTextField.setText(partTextField.getText().trim());
                        return;
                    }
                    if (!newValue.isEmpty()) {
                        try {
                            int tmp = Integer.parseInt(newValue);
                            if (tmp <= partListView.getItems().size() && tmp > 0) {
                                partListView.scrollTo(tmp - 2);
                                partListView.getSelectionModel().select(tmp - 1);
                                if (tmp * 10 > partListView.getItems().size()) {
                                    // oldReplace = true;
                                    // verseTextField.setText("1");
                                    if (!verseTextField.getText().equals("")) {
                                        oldReplace = true;
                                        verseTextField.setText("");
                                    }
                                    verseTextField.requestFocus();
                                }
                            } else {
                                oldReplace = true;
                                if (!oldValue.isEmpty()) {
                                    partTextField.setText(oldValue.trim());
                                } else {
                                    partTextField.setText("");
                                }
                                oldReplaceScrollTo(tmp, partListView);
                            }
                        } catch (NumberFormatException e) {
                            oldReplace = true;
                            resetTextFieldValue(oldValue, partTextField);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            partTextField.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.RIGHT) {
                        if (event.isAltDown() || partTextField.getCaretPosition() == partTextField.getText().length()) {
                            verseTextField.requestFocus();
                        }
                    } else if (event.getCode() == KeyCode.LEFT) {
                        if (event.isAltDown() || partTextField.getCaretPosition() == 0) {
                            bookTextField.requestFocus();
                        }
                    } else if (!partTextField.getText().trim().isEmpty()
                            && (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER
                            || event.getCode() == KeyCode.PERIOD || event.getCode() == KeyCode.DECIMAL)) {
                        try {
                            int tmp = Integer.parseInt(partTextField.getText());
                            if (tmp <= partListView.getItems().size() && tmp > 0) {
                                partListView.scrollTo(tmp - 2);
                                partListView.getSelectionModel().select(tmp - 1);
                                if (!verseTextField.getText().equals("")) {
                                    oldReplace = true;
                                    verseTextField.setText("");
                                }
                                verseTextField.requestFocus();
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (event.getCode() == KeyCode.DOWN) {
                        // &&
                        // event.isAltDown())
                        // {
                        partListView.requestFocus();
                    } else if (event.getCode() == KeyCode.F1) {
                        mainController.setBlank();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            partTextField.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation());
            verseTextField.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation());
            verseTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (oldReplace) {
                        oldReplace = false;
                        return;
                    }
                    if (!newValue.isEmpty()) {
                        try {
                            int tmp = Integer.parseInt(newValue);
                            if (tmp <= verseListView.getItems().size() && tmp > 0) {
                                verseListView.scrollTo(tmp - 2);
                                verseListView.getFocusModel().focus(tmp - 1);
                                if (tmp * 10 > verseListView.getItems().size()) {
                                    verseListView.getSelectionModel().clearSelection();
                                    if (settings.isFastMode()) {
                                        verseListView.getSelectionModel().select(tmp - 1);
                                    } else {
                                        verseListView.getFocusModel().focus(tmp - 1);
                                    }
                                    verseListView.requestFocus();
                                }
                            } else {
                                oldReplace = true;
                                if (!oldValue.isEmpty()) {
                                    verseTextField.setText(oldValue.trim());
                                } else {
                                    verseTextField.setText("");
                                }
                                oldReplaceScrollTo(tmp, verseListView);
                            }
                        } catch (NumberFormatException e) {
                            oldReplace = true;
                            resetTextFieldValue(oldValue, verseTextField);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseTextField.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.ENTER) {
                        try {
                            Integer tmp = Integer.parseInt(verseTextField.getText());
                            verseListView.scrollTo(tmp - 2);
                            if (settings.isFastMode()) {
                                verseListView.getSelectionModel().clearAndSelect(tmp - 1);
                            } else {
                                verseListView.getFocusModel().focus(tmp - 1);
                            }
                            verseListView.requestFocus();
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (event.getCode() == KeyCode.LEFT) {
                        if (event.isAltDown() || verseTextField.getCaretPosition() == 0) {
                            partTextField.requestFocus();
                        }
                    } else if (event.getCode() == KeyCode.DOWN) {
                        // &&event.isAltDown()) {
                        verseListView.requestFocus();
                    } else if (event.getCode() == KeyCode.F1) {
                        mainController.setBlank();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            bookListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public synchronized void changed(ObservableValue<? extends String> selected, String oldBook,
                                                 String newBook) {
                    try {
                        if (!bookListView.getSelectionModel().isEmpty()) {
                            partListBookI = searchIBook.get(bookListView.getSelectionModel().getSelectedIndex());
                            partListView.getItems().clear();
                            for (int i = 0; i < bible.getBooks()[partListBookI].getChapters().length; ++i) {
                                partListView.getItems().add(i + 1);
                            }
                            searchSelected = 2;
                            bibleSearchController.setSearchSelected(searchSelected);
                            recentController.setSearchSelected(searchSelected);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            bookListView.setOnKeyPressed(event -> {
                try {
                    final KeyCode keyCode = event.getCode();
                    if (keyCode == KeyCode.UP && event.isAltDown()) {
                        bookTextField.requestFocus();
                    } else if (keyCode == KeyCode.RIGHT) {
                        partListView.requestFocus();
                    } else if (keyCode.isDigitKey() || keyCode.isKeypadKey() || keyCode.isLetterKey()) {
                        bookTextField.setText(event.getCharacter());
                        bookTextField.requestFocus();
                        event.consume();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            partListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
                @Override
                public synchronized void changed(ObservableValue<? extends Integer> observable, Integer oldPart,
                                                 Integer newPart) {
                    try {
                        final int selectedPartIndex = partListView.getSelectionModel().getSelectedIndex();
                        if (selectedPartIndex >= 0) {
                            partLabel.setText(bible.getBooks()[partListBookI].getTitle().trim() + " " + (selectedPartIndex + 1));
                            selectedBook = partListBookI;
                            selectedPart = selectedPartIndex;
                            addAllVerse();
                            if (isLastVerse) {
                                isLastVerse = false;
                                selectedVerse = verseListView.getItems().size() - 1;
                                verseListView.getSelectionModel().select(selectedVerse);
                                verseListView.scrollTo(selectedVerse);
                            }
                            searchSelected = 3;
                            bibleSearchController.setSearchSelected(searchSelected);
                            recentController.setSearchSelected(searchSelected);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            partListView.setOnKeyPressed(event -> {
                try {
                    final KeyCode keyCode = event.getCode();
                    if (keyCode == KeyCode.UP && event.isAltDown()) {
                        partTextField.requestFocus();
                    } else if (keyCode == KeyCode.LEFT) {
                        bookListView.requestFocus();
                    } else if (keyCode == KeyCode.RIGHT) {
                        verseListView.requestFocus();
                    } else {
                        if (keyCode.isDigitKey() || keyCode.isKeypadKey()) {
                            partTextField.setText(event.getCharacter());
                            partTextField.requestFocus();
                            event.consume();
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    selectedVerse = newValue.intValue();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListView.setOnKeyPressed(event -> {
                try {
                    final KeyCode keyCode = event.getCode();
                    if (keyCode == KeyCode.ENTER) {
                        if (settings.isFastMode()) {
                            projectionScreenController.setBlank(false);
                            mainController.getBlankButton().setSelected(false);
                        }
                    } else if (keyCode == KeyCode.UP && event.isAltDown()) {
                        verseTextField.requestFocus();
                    } else if (keyCode == KeyCode.LEFT) {
                        partListView.requestFocus();
                    } else if (keyCode == KeyCode.DOWN) {
                        if (setNextPart()) {
                            event.consume();
                        }
                    } else if (keyCode == KeyCode.UP) {
                        if (setPreviousPart()) {
                            event.consume();
                        }
                    } else if ((keyCode.equals(KeyCode.ADD) || keyCode.equals(KeyCode.EQUALS)) && event.isControlDown()) {
                        enlargeVerseListViewText();
                    } else if ((keyCode.equals(KeyCode.MINUS) || keyCode.equals(KeyCode.SUBTRACT)) && event.isControlDown()) {
                        shrinkVerseListViewText();
                    } else {
                        if (keyCode.isDigitKey() || keyCode.isKeypadKey()) {
                            verseTextField.setText(event.getCharacter());
                            verseTextField.requestFocus();
                            event.consume();
                        } else if (keyCode.isLetterKey()) {
                            searchTextField.setText(event.getCharacter());
                            searchTextField.requestFocus();
                            event.consume();
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListView.setCellFactory((ListView<MarkTextFlow> list) -> {
                final ListCell<MarkTextFlow> cell = new ListCell<MarkTextFlow>() {
                    @Override
                    protected void updateItem(MarkTextFlow textFlow, boolean empty) {
                        try {
                            super.updateItem(textFlow, empty);
                            setText(null);
                            if (empty || textFlow == null) {
                                setGraphic(null);
                            } else {
                                setGraphic(textFlow);
                            }
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                };
                cell.setOnMouseDragEntered(t -> {
                    try {
                        if (isSelecting() && !firstDrag) {
                            setSelection(cell);
                        }
                        firstDrag = false;
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
                cell.setOnDragDetected(t -> firstDrag = true);
                return cell;
            });
            bookTextField.requestFocus();
            sendProjectionScreenText.setOnAction(event -> {
                try {
                    projectionScreenController.setText(referenceTextArea.getText(), ProjectionType.REFERENCE);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListView.setOnMousePressed(event -> {
                if (event.getClickCount() == 2) {
                    verseSelected();
                }
            });
            referenceResetText.setOnAction(event -> {
                try {
                    ref.clear();
                    referenceTextArea.setText("");
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            referenceEditText.setOnAction(event -> {
                try {
                    Stage dialog = new Stage(); // new stage
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    // Defines a modal window that blocks events from being
                    // delivered to any other application window.
                    // dialog.initOwner(primaryStage);
                    VBox vb = new VBox(15);
                    List<CheckBox> list = new LinkedList<>();
                    for (ReferenceBook rb : ref.getBookList()) {
                        for (ReferenceChapter ch : rb.getChapters()) {
                            for (int i : ch.getVerses()) {
                                CheckBox tmp = new CheckBox(bible.getBooks()[rb.getBookNumber()].getTitle() + " "
                                        + ch.getChapterNumber() + ":" + i);
                                tmp.selectedProperty().set(true);
                                list.add(tmp);
                            }
                        }
                    }
                    vb.getChildren().addAll(list);
                    Group root = new Group();
                    int height = list.size() * 35;
                    if (height > 700) {
                        height = 700;
                    }
                    Scene dialogScene = new Scene(root, 300, height);
                    dialog.setScene(dialogScene);
                    ScrollPane scrollPane = new ScrollPane();
                    dialogScene.heightProperty().addListener((observable, oldValue, newValue) -> {
                        try {
                            double delta = newValue.doubleValue() - oldValue.doubleValue();
                            scrollPane.setPrefHeight(scrollPane.getPrefHeight() + delta);
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    });
                    dialogScene.widthProperty().addListener((observable, oldValue, newValue) -> {
                        try {
                            double delta = newValue.doubleValue() - oldValue.doubleValue();
                            scrollPane.setPrefWidth(scrollPane.getPrefWidth() + delta);
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    });
                    scrollPane.setPrefSize(300, height);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setContent(vb);
                    root.getChildren().add(scrollPane);
                    Double first = 0.9999789;
                    scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                        try {
                            if (oldValue.equals(first) && newValue.equals(0.0)) {
                                scrollPane.setVvalue(1.0);
                            }
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    });
                    scrollPane.setVvalue(first);
                    dialog.showAndWait();
                    int k = 0;
                    List<Triplet<Integer, Integer, Integer>> waitToRemove = new LinkedList<>();
                    for (ReferenceBook rb : ref.getBookList()) {
                        for (ReferenceChapter ch : rb.getChapters()) {
                            for (int i : ch.getVerses()) {
                                CheckBox tmp = list.get(k++);
                                if (!tmp.isSelected()) {
                                    waitToRemove.add(new Triplet<>(rb.getBookNumber(),
                                            ch.getChapterNumber(), i));
                                }
                            }
                        }
                    }
                    for (Triplet<Integer, Integer, Integer> i : waitToRemove) {
                        ref.removeVers(i.getFirst(), i.getSecond(), i.getThird());
                    }
                    referenceTextArea.setText(ref.getReference());
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            ref = new Reference();
            ref.setBible(bible);
            referenceTextArea.selectionProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    int startIndex = referenceTextArea.getSelection().getStart();
                    int endIndex = referenceTextArea.getSelection().getEnd();
                    String textAreaText = referenceTextArea.getText();
                    // if (endIndex - startIndex < textAreaText.length() &&
                    if (startIndex != endIndex) {
                        // System.out.println(startIndex + " " + endIndex);
                        // System.out.println(referenceTextArea.getSelectedText());
                        String[] lines = textAreaText.split("\n");
                        String bookName;
                        int bookIndex = -1;
                        int part = -1;
                        for (String line : lines) {
                            bookName = line.split(":")[0];
                            int lastSpace = bookName.lastIndexOf(" ");
                            if (lastSpace > 0) {
                                try {
                                    part = Integer.parseInt(bookName.substring(lastSpace + 1, bookName.length()).trim());
                                } catch (NumberFormatException ignored) {
                                }
                                bookName = bookName.substring(0, lastSpace);
                                bookIndex = bible.getBookIndex(bookName);
                                // System.out.println(bookName + " " + bookIndex);
                            } else {
                                part = Integer.parseInt(bookName.trim());
                            }
                            if (startIndex >= line.length()) {
                                startIndex -= line.length() + 1;
                                endIndex -= line.length() + 1;
                            } else {
                                String[] split = line.split(":");
                                startIndex -= split[0].length() + 1;
                                endIndex -= split[0].length() + 1;
                                if (startIndex < 0) {
                                    startIndex = 0;
                                }
                                if (startIndex < endIndex) {
                                    // System.out.println(split[1].charAt(startIndex));
                                    String verseNumber = split[1].substring(startIndex, startIndex + 1);
                                    boolean number = false;
                                    do {
                                        try {
                                            Integer.parseInt(verseNumber);
                                            number = true;
                                        } catch (NumberFormatException e) {
                                            ++startIndex;
                                            if (startIndex < split[1].length()) {
                                                verseNumber = split[1].substring(startIndex, startIndex + 1);
                                            } else {
                                                break;
                                            }
                                        }
                                    } while (!number);
                                    if (number && startIndex < endIndex) {
                                        ++startIndex;
                                        StringBuilder verseNumberBuffer = new StringBuilder();
                                        verseNumberBuffer.append(verseNumber);
                                        while (startIndex < split[1].length() && startIndex < endIndex) {
                                            String character = split[1].substring(startIndex, startIndex + 1);
                                            try {
                                                Integer.parseUnsignedInt(character);
                                                verseNumberBuffer.append(character);
//											verseNumber += character;
                                                ++startIndex;
                                            } catch (NumberFormatException e) {
                                                break;
                                            }
                                        }
                                        int verse = Integer.parseInt(verseNumberBuffer.toString());
                                        if (bookIndex >= 0 && part >= 0 && verse >= 0) {
                                            // System.out.println(bookIndex + " " +
                                            // part + " " + verse);
                                            setBookPartVerse(bookIndex, part - 1, verse - 1);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            referenceTextArea.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.F1) {
                        mainController.setBlank();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListView.getSelectionModel().getSelectedIndices().addListener((ListChangeListener<Integer>) c -> {
                try {
                    if (!isSelecting()) {
                        verseSelected();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            referenceListView.orientationProperty().set(Orientation.HORIZONTAL);
            referenceListView.getItems().add("1");
            referenceListView.getItems().add(settings.getResourceBundle().getString("New"));
            references = new LinkedList<>();
            references.add(ref);
            referenceListView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {
                @Override
                synchronized public void onChanged(Change<? extends Integer> c) {
                    try {
                        int selectedIndex = referenceListView.getSelectionModel().getSelectedIndex();
                        int lastIndex = referenceListView.getItems().size() - 1;
                        if (!newReferenceAdded) {
                            if (selectedIndex == lastIndex) {
                                newReferenceAdded = true;
                                Reference reference = new Reference();
                                reference.setBible(bible);
                                references.add(reference);
                                referenceListView.getItems().add(lastIndex, (lastIndex + 1) + "");
                            }
                            ref = references.get(selectedIndex);
                            refreshReferenceTextArea();
                        } else {
                            newReferenceAdded = false;
                            referenceListView.getSelectionModel().select(lastIndex - 1);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            horizontalSplitPane.setDividerPositions(settings.getBibleTabHorizontalSplitPaneDividerPosition());
            verticalSplitPane.setDividerPositions(settings.getBibleTabVerticalSplitPaneDividerPosition());
            SplitPane.setResizableWithParent(verticalSplitPane, false);
            nextButton.setOnAction(event -> setNextVerse());
            nextButton.addEventHandler(KeyEvent.KEY_PRESSED, new NextButtonEventHandler(nextButton, LOG) {
            });
            partLabel.setText("");
            foundLabel.setText("");
            searchTextField.textProperty().addListener((observable, oldValue, newValue) -> search(newValue));
            verseFont = Font.font(settings.getVerseListViewFontSize());
            initializeDecreaseIncreaseButtons();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeDecreaseIncreaseButtons() {
        try {
            decreaseButton.setOnAction(event -> shrinkVerseListViewText());
            increaseButton.setOnAction(event -> enlargeVerseListViewText());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void shrinkVerseListViewText() {
        try {
            verseFont = Font.font(verseFont.getSize() - 1);
            setVerseListViewTextFont(verseFont);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void enlargeVerseListViewText() {
        try {
            verseFont = Font.font(verseFont.getSize() + 1);
            setVerseListViewTextFont(verseFont);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setVerseListViewTextFont(Font verseFont) {
        try {
            settings.setVerseListViewFontSize(verseFont.getSize());
            for (MarkTextFlow textFlow : verseListView.getItems()) {
                for (Node node : textFlow.getChildren()) {
                    if (node instanceof Text) {
                        final Text node1 = (Text) node;
                        node1.setFont(verseFont);
                    }
                }
            }
            addAllVerse();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void addAllVerse() {
        try {
            verseListView.getItems().clear();
            if (selectedPart >= 0 && selectedBook >= 0) {
                for (int i = 0; i < bible.getBooks()[selectedBook].getChapters()[selectedPart].getLength(); ++i) {
                    final String text = (i + 1) + ".	" + bible.getBooks()[selectedBook].getChapters()[selectedPart].getVerses()[i];
                    TextFlow textFlow = new TextFlow();
                    final Text e = new Text(text);
                    e.setFont(verseFont);
                    textFlow.getChildren().add(e);
                    textFlow.setTextAlignment(TextAlignment.JUSTIFY);
                    textFlow.setPrefWidth(verseListView.getWidth() - verseRightMargin);
                    MarkTextFlow markTextFlow = new MarkTextFlow(textFlow);
                    verseListView.getItems().add(markTextFlow);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void search(String text) {
        try {
            if (text.trim().isEmpty()) {
                Platform.runLater(() -> {
                    try {
                        addAllVerse();
                        foundLabel.setText("");
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
                return;
            }
            Thread thread = new Thread(() -> {
                try {
                    if (selectedPart >= 0 && selectedBook >= 0) {
                        String text3 = text;
                        if (!Settings.getInstance().isWithAccents()) {
                            text3 = strip(text);
                        }
                        text3 = text3.replace("]", "").replace("[", "");
                        List<TextFlow> tmpSearchListView = new ArrayList<>();
                        final Chapter chapter = bible.getBooks()[selectedBook].getChapters()[selectedPart];
                        int found = 0;
                        for (int i = 0; i < chapter.getLength(); ++i) {
//							String text2;
                            String verse = chapter.getVerses()[i];
//							if (Settings.getInstance().isWithAccents()) {
//								text2 = chapter.getVerses()[i];
//							} else {
//								text2 = strip(chapter.getVerses()[i]);
//							}
//                    if (contains(text2, text3)) {
//                    }
                            TextFlow textFlow = new TextFlow();
                            Text reference = new Text((i + 1) + ".	");
                            reference.setFont(verseFont);
                            reference.setFill(Color.rgb(5, 30, 70));
                            textFlow.getChildren().add(reference);
                            char[] chars = stripAccents(verse).toLowerCase().toCharArray();
                            char[] searchTextChars = text3.toCharArray();
                            int verseIndex = 0;
                            int fromIndex = 0;
                            int lastAddedIndex = 0;
                            for (int j = 0; j < chars.length; ++j) {
                                if ('a' <= chars[j] && chars[j] <= 'z') {
                                    if (chars[j] == searchTextChars[verseIndex]) {
                                        if (verseIndex == 0) {
                                            fromIndex = j;
                                        }
                                        ++verseIndex;
                                        if (verseIndex == searchTextChars.length) {
                                            if (lastAddedIndex != fromIndex) {
                                                Text text1 = new Text(verse.substring(lastAddedIndex, fromIndex));
                                                text1.setFont(verseFont);
                                                textFlow.getChildren().add(text1);
                                            }
                                            Text foundText = new Text(verse.substring(fromIndex, j + 1));
                                            foundText.setFill(Color.rgb(164, 0, 17));
                                            foundText.setFont(Font.font(verseFont.getFamily(), FontWeight.BOLD, verseFont.getSize() + 1));
                                            foundText.getStyleClass().add("found");
                                            textFlow.getChildren().add(foundText);
                                            lastAddedIndex = j + 1;
                                            verseIndex = 0;
                                            ++found;
                                        }
                                    } else {
                                        if (verseIndex != 0) {
                                            --j;
                                            verseIndex = 0;
                                        }
                                    }
                                }
                            }
                            if (lastAddedIndex < verse.length()) {
                                Text text1 = new Text(verse.substring(lastAddedIndex));
                                text1.setFont(verseFont);
                                textFlow.getChildren().add(text1);
                            }
                            textFlow.setTextAlignment(TextAlignment.JUSTIFY);
                            textFlow.setPrefWidth(verseListView.getWidth() - verseRightMargin);
                            tmpSearchListView.add(textFlow);
//                    }
                        }
                        int finalFound = found;
                        Platform.runLater(() -> {
                            try {
                                final String found1 = settings.getResourceBundle().getString("Found");
                                foundLabel.setText(found1 + ": " + finalFound);
                                //TODO try not to clear
                                verseListView.getItems().clear();
                                for (TextFlow i : tmpSearchListView) {
                                    verseListView.getItems().add(new MarkTextFlow(i));
                                }
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            }
                        });
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            thread.start();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void initializeBibles() {
        try {
            if (bible == null) {
                bible = new Bible();
                Reader.setBooksRead(false);
                bible.setBooks(Reader.getBooks(settings.getBiblePaths().get(0)));
                // countWords();
                // System.out.println("Ido1: " + (System.currentTimeMillis() - x));
                parallelBible = new Bible();
                Reader.setBooksRead(false);
                parallelBible.setBooks(Reader.getBooks(settings.getParallelBiblePath()));
                // System.out.println("Ido2: " + (System.currentTimeMillis() - x));
                // System.out.println(settings.getParallelBiblePath());
                addAllBooks();
                // System.out.println("Ido3: " + (System.currentTimeMillis() - x));
                shiftParallel = new LinkedList<>();
                if (settings.isParallel()) {
                    readShiftsParallel();
                }
                bibleSearchController.setBooks(bible.getBooks());
                historyController.setBible(bible);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    boolean isNotAllBooks() {
        return !isAllBooks;
    }

    void readParallelBible() {
        try {
            Reader.setBooksRead(false);
            parallelBible.setBooks(Reader.getBooks(settings.getParallelBiblePath()));
            if (settings.isParallel()) {
                readShiftsParallel();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private String getPrevVerse(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift >= 0) {
                if (verse - shift >= 0) {
                    return bible.getBooks()[book].getChapters()[chapter].getVerses()[verse - shift];
                }
                if (chapter > 0) {
                    if (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift >= 0) {
                        return bible.getBooks()[book].getChapters()[chapter - 1]
                                .getVerses()[bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse
                                - shift];
                    } else {
                        // return "";
                        shift -= bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length;
                        chapter--;
                        if (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift >= 0) {
                            return bible.getBooks()[book].getChapters()[chapter - 1]
                                    .getVerses()[bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse
                                    - shift];
                        } else {
                            return "";
                        }
                    }
                }
                if (book > 0) {
                    Book tmpBook = bible.getBooks()[book - 1];
                    if (tmpBook.getChapters()[tmpBook.getChapters().length - 1].getVerses().length - shift >= 0) {
                        return tmpBook.getChapters()[tmpBook.getChapters().length - 1]
                                .getVerses()[tmpBook.getChapters()[tmpBook.getChapters().length - 1].getVerses().length
                                - shift];
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private int getPrevVerseNumber(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift >= 0) {
                if (verse - shift >= 0) {
                    return verse - shift;
                }
                if (chapter > 0) {
                    if (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift >= 0) {
                        return bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift;
                    } else {
                        // return "";
                        shift -= bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length;
                        chapter--;
                        if (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift >= 0) {
                            return bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift;
                        } else {
                            return -1;
                        }
                    }
                }
                if (book > 0) {
                    Book tmpBook = bible.getBooks()[book - 1];
                    if (tmpBook.getChapters()[tmpBook.getChapters().length - 1].getVerses().length - shift >= 0) {
                        return tmpBook.getChapters()[tmpBook.getChapters().length - 1].getVerses().length - shift;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return -1;
    }

    private String getNextVerse(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift >= 0) {
                if (verse + shift < bible.getBooks()[book].getChapters()[chapter].getVerses().length) {
                    return bible.getBooks()[book].getChapters()[chapter].getVerses()[verse + shift];
                }
                if (verse >= bible.getBooks()[book].getChapters()[chapter].getVerses().length) {
                    shift -= 1;
                } else {
                    shift -= bible.getBooks()[book].getChapters()[chapter].getVerses().length - verse + 1;
                }
                if (chapter + 1 < bible.getBooks()[book].getChapters().length) {
                    if (bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length > shift) {
                        return bible.getBooks()[book].getChapters()[chapter + 1].getVerses()[shift];
                    } else {
                        // return "";
                        shift -= bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length;
                        chapter++;
                        if (chapter + 1 < bible.getBooks()[book].getChapters().length) {
                            if (bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length > shift) {
                                return bible.getBooks()[book].getChapters()[chapter + 1].getVerses()[shift];
                            } else {
                                return "";
                            }
                        }
                    }
                }
                if (book < bible.getBooks().length - 1) {
                    Book tmpBook = bible.getBooks()[book + 1];
                    if (tmpBook.getChapters()[0].getVerses().length > shift) {
                        return tmpBook.getChapters()[0].getVerses()[shift];
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private int getNextVerseNumber(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift >= 0) {
                if (verse + shift < bible.getBooks()[book].getChapters()[chapter].getVerses().length) {
                    return verse + shift;
                }
                if (verse >= bible.getBooks()[book].getChapters()[chapter].getVerses().length) {
                    shift -= 1;
                } else {
                    shift -= bible.getBooks()[book].getChapters()[chapter].getVerses().length - verse + 1;
                }
                if (chapter + 1 < bible.getBooks()[book].getChapters().length) {
                    if (bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length > shift) {
                        return shift;
                    } else {
                        // return "";
                        shift -= bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length;
                        chapter++;
                        if (chapter + 1 < bible.getBooks()[book].getChapters().length) {
                            if (bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length > shift) {
                                return shift;
                            } else {
                                return -1;
                            }
                        }
                    }
                }
                if (book < bible.getBooks().length - 1) {
                    Book tmpBook = bible.getBooks()[book + 1];
                    if (tmpBook.getChapters()[0].getVerses().length > shift) {
                        return shift;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return -1;
    }

    private String getPrevVerseReference(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift >= 0) {
                if (verse - shift >= 0) {
                    return bible.getBooks()[book].getTitle().trim() + " " + (chapter + 1) + ":" + (verse - shift + 1);
                }
                if (chapter > 0) {
                    if (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift >= 0) {
                        return bible.getBooks()[book].getTitle().trim() + " " + (chapter) + ":"
                                + (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift + 1);
                    } else {
                        // return "";
                        shift -= bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length;
                        chapter--;
                        if (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift >= 0) {
                            return bible.getBooks()[book].getTitle().trim() + " " + (chapter) + ":"
                                    + (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift
                                    + 1);
                        } else {
                            return "";
                        }
                    }
                }
                if (book > 0) {
                    Book tmpBook = bible.getBooks()[book - 1];
                    if (tmpBook.getChapters()[tmpBook.getChapters().length - 1].getVerses().length - shift >= 0) {
                        return tmpBook.getTitle().trim() + " " + (tmpBook.getChapters().length) + ":"
                                + (tmpBook.getChapters()[tmpBook.getChapters().length - 1].getVerses().length - shift + 1);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private String getPrevVerseReferenceNumbers(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift >= 0) {
                if (verse - shift >= 0) {
                    return book + " " + (chapter + 1) + " " + (verse - shift + 1);
                }
                if (chapter > 0) {
                    if (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift >= 0) {
                        return book + " " + (chapter) + " "
                                + (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift + 1);
                    } else {
                        shift -= bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length;
                        chapter--;
                        if (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift >= 0) {
                            return book + " " + (chapter) + " "
                                    + (bible.getBooks()[book].getChapters()[chapter - 1].getVerses().length + verse - shift
                                    + 1);
                        } else {
                            return "";
                        }
                    }
                }
                if (book > 0) {
                    Book tmpBook = bible.getBooks()[book - 1];
                    if (tmpBook.getChapters()[tmpBook.getChapters().length - 1].getVerses().length - shift >= 0) {
                        return (book - 1) + " " + (tmpBook.getChapters().length) + " "
                                + (tmpBook.getChapters()[tmpBook.getChapters().length - 1].getVerses().length - shift + 1);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private String getNextVerseReference(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift >= 0) {
                if (verse + shift < bible.getBooks()[book].getChapters()[chapter].getVerses().length) {
                    return bible.getBooks()[book].getTitle().trim() + " " + (chapter + 1) + ":" + (verse + shift + 1);
                }
                if (verse >= bible.getBooks()[book].getChapters()[chapter].getVerses().length) {
                    shift -= 1;
                } else {
                    shift -= bible.getBooks()[book].getChapters()[chapter].getVerses().length - verse + 1;
                }
                if (chapter + 1 < bible.getBooks()[book].getChapters().length) {
                    if (bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length > shift) {
                        return bible.getBooks()[book].getTitle().trim() + " " + (chapter + 2) + ":" + (shift + 1);
                    } else {
                        // return "";
                        shift -= bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length;
                        chapter++;
                        if (chapter + 1 < bible.getBooks()[book].getChapters().length) {
                            if (bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length > shift) {
                                return bible.getBooks()[book].getTitle().trim() + " " + (chapter + 2) + ":" + (shift + 1);
                            } else {
                                return "";
                            }
                        }
                    }
                }
                if (book < bible.getBooks().length - 1) {
                    Book tmpBook = bible.getBooks()[book + 1];
                    if (tmpBook.getChapters()[0].getVerses().length > shift) {
                        return tmpBook.getTitle().trim() + " 1:" + (shift + 1);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private String getNextVerseReferenceNumbers(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift >= 0) {
                if (verse + shift < bible.getBooks()[book].getChapters()[chapter].getVerses().length) {
                    return book + " " + (chapter + 1) + " " + (verse + shift + 1);
                }
                if (verse >= bible.getBooks()[book].getChapters()[chapter].getVerses().length) {
                    shift -= 1;
                } else {
                    shift -= bible.getBooks()[book].getChapters()[chapter].getVerses().length - verse + 1;
                }
                if (chapter + 1 < bible.getBooks()[book].getChapters().length) {
                    if (bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length > shift) {
                        return book + " " + (chapter + 2) + " " + (shift + 1);
                    } else {
                        shift -= bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length;
                        chapter++;
                        if (chapter + 1 < bible.getBooks()[book].getChapters().length) {
                            if (bible.getBooks()[book].getChapters()[chapter + 1].getVerses().length > shift) {
                                return book + " " + (chapter + 2) + " " + (shift + 1);
                            } else {
                                return "";
                            }
                        }
                    }
                }
                if (book < bible.getBooks().length - 1) {
                    Book tmpBook = bible.getBooks()[book + 1];
                    if (tmpBook.getChapters()[0].getVerses().length > shift) {
                        return (book + 1) + " 1 " + (shift + 1);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private void readShiftsParallel() {
        try {
            Settings settings = this.settings;
            int index = 0;
            if (bibleListView != null) {
                index = bibleListView.getSelectionModel().getSelectedIndex();
                if (index < 0) {
                    index = 0;
                }
            }
            String fileName = settings.getBiblePaths().get(index) + "-" + settings.getParallelBiblePath();
            // System.out.println(fileName);
            shiftParallel = new LinkedList<>();
            shiftParallelI = new LinkedList<>();
            multipleShiftParallel = new LinkedList<>();
            try {
                FileInputStream f = new FileInputStream(fileName);
                BufferedReader br = new BufferedReader(new InputStreamReader(f, "UTF-8"));
                while (br.ready()) {
                    String tmp = br.readLine();
                    if (tmp == null) {
                        break;
                    }
                    String[] split = tmp.split(" ");
                    shiftParallel.add(split[0] + " " + split[1] + " " + split[2]);
                    if (split[3].equals("x")) {
                        shiftParallelI.add(null);
                        multipleShiftParallel.add("");
                        continue;
                    }
                    if (!tmp.contains(":")) {
                        // shiftParallel.add(split[0] + " " + split[1] + " " +
                        // split[2]);
                        shiftParallelI.add(Integer.parseInt(split[3]));
                        multipleShiftParallel.add("");
                    } else {
                        // 18 13 6 : 18 13 5 18 13 6
                        // shiftParallel.add(split[0] + " " + split[1] + " " +
                        // split[2]);
                        multipleShiftParallel.add(split[4] + " " + split[5] + " " + split[6] + " " + split[7] + " "
                                + split[8] + " " + split[9]);
                        shiftParallelI.add(0);
                    }
                }
                br.close();
            } catch (FileNotFoundException ignored) {
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void setMainController(MyController mainController) {
        try {
            this.mainController = mainController;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        try {
            this.projectionScreenController = projectionScreenController;
            projectionScreenController.setBibleController(this);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void setBibleSearchController(BibleSearchController bibleSearchController) {
        this.bibleSearchController = bibleSearchController;
    }

    void setRecentController(RecentController recentController) {
        this.recentController = recentController;
    }

    void setHistoryController(HistoryController historyController) {
        this.historyController = historyController;
    }

    public boolean contains(String a, String b) {
        try {
            a = StringUtils.stripAccents(a);
            b = StringUtils.stripAccents(b);
            return a.toLowerCase().contains(b.toLowerCase().trim());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    void addAllBooks() {
        try {
            isAllBooks = false;
            if (searchIBook == null) {
                searchIBook = new ArrayList<>();
            } else {
                searchIBook.clear();
            }
            bookListView.getItems().clear();
            for (int iBook = 0; iBook < bible.getBooks().length; ++iBook) {
                bookListView.getItems().add(bible.getBooks()[iBook].getTitle());
                searchIBook.add(iBook);
            }
            isAllBooks = true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void titleSearch(String text) {
        try {
            if (!text.trim().isEmpty()) {
                if (searchIBook == null) {
                    searchIBook = new ArrayList<>();
                } else {
                    searchIBook.clear();
                }
                bookListView.getItems().clear();
                for (int iBook = 0; iBook < bible.getBooks().length; ++iBook) {
                    if (contains(bible.getBooks()[iBook].getTitle(), text)) {
                        bookListView.getItems().add(bible.getBooks()[iBook].getTitle());
                        searchIBook.add(iBook);
                    }
                }
                if (bookListView.getItems().size() == 1) {
                    bookListView.getSelectionModel().select(0);
                    oldReplace = true;
                    partTextField.setText("1");
                    oldReplace = true;
                    partTextField.setText("");
                    partTextField.requestFocus();
                }
            } else {
                addAllBooks();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private boolean setNextPart() {
        try {
            if (selectedVerse + 1 >= verseListView.getItems().size() && selectedPart + 1 < partListView.getItems().size()) {
                ++selectedPart;
                partListView.getSelectionModel().select(selectedPart);
                partListView.scrollTo(selectedPart);
                verseListView.getSelectionModel().select(0);
                verseListView.scrollTo(0);
                return true;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    private boolean setPreviousPart() {
        try {
            if (selectedVerse == 0 && selectedPart - 1 >= 0) {
                --selectedPart;
                isLastVerse = true;
                partListView.scrollTo(selectedPart);
                partListView.getSelectionModel().select(selectedPart);
                return true;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    void setNextVerse() {
        try {
            if (selectedVerse + 1 < verseListView.getItems().size()) {
                ++selectedVerse;
                verseListView.getSelectionModel().clearAndSelect(selectedVerse);
                verseListView.scrollTo(selectedVerse - 1);
            } else {
                setNextPart();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void setPreviousVerse() {
        try {
            if (selectedVerse > 0) {
                --selectedVerse;
                verseListView.getSelectionModel().clearAndSelect(selectedVerse);
                verseListView.scrollTo(selectedVerse);
            } else {
                setPreviousPart();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setSelection(ListCell<MarkTextFlow> cell) {
        try {
            if (cell.isSelected()) {
                verseListView.getSelectionModel().clearSelection(cell.getIndex());
            } else {
                verseListView.getSelectionModel().select(cell.getIndex());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    ListView<String> getBookListView() {
        return bookListView;
    }

    ListView<Integer> getPartListView() {
        return partListView;
    }

    ListView<MarkTextFlow> getVerseListView() {
        return verseListView;
    }

    private boolean isSelecting() {
        return isSelecting;
    }

    void setSelecting(boolean isSelecting) {
        try {
            if (this.isSelecting && !isSelecting) {
                verseSelected();
            }
            if (!this.isSelecting && isSelecting) {
                verseListView.getSelectionModel().clearSelection();
            }
            this.isSelecting = isSelecting;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private String getVerse(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift < 0) {
                return getPrevVerse(-shift, bible, book, chapter, verse);
            } else {
                return getNextVerse(shift, bible, book, chapter, verse);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private int getVerseNumber(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift < 0) {
                return getPrevVerseNumber(-shift, bible, book, chapter, verse);
            } else {
                return getNextVerseNumber(shift, bible, book, chapter, verse);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return 0;
    }

    private String getVerseReference(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift < 0) {
                return getPrevVerseReference(-shift, bible, book, chapter, verse);
            } else {
                return getNextVerseReference(shift, bible, book, chapter, verse);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private String getVerseReferenceNumbers(int shift, Bible bible, int book, int chapter, int verse) {
        try {
            if (shift < 0) {
                return getPrevVerseReferenceNumbers(-shift, bible, book, chapter, verse);
            } else {
                return getNextVerseReferenceNumbers(shift, bible, book, chapter, verse);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private String getParallelVerse(int book, int part, int verse) {
        try {
            String result = "";
            if (shiftParallel.contains(book + " " + (part + 1) + " " + (verse + 1))) {
                int index = shiftParallel.indexOf(book + " " + (part + 1) + " " + (verse + 1));
                if (shiftParallelI.get(index) != null) {
                    int shift = shiftParallelI.get(index);
                    if (shift != 0) {
                        int x = getVerseNumber(shift, parallelBible, book, part, verse);
                        if (x != -1) {
                            result += (x + 1) + ". ";
                        }
                        result += getVerse(shift, parallelBible, book, part, verse);
                    } else {
                        String[] split = multipleShiftParallel.get(index).split(" ");
                        int book1 = Integer.parseInt(split[0]);
                        int part1 = Integer.parseInt(split[1]) - 1;
                        int verse1 = Integer.parseInt(split[2]) - 1;
                        int book2 = Integer.parseInt(split[3]);
                        int part2 = Integer.parseInt(split[4]) - 1;
                        int verse2 = Integer.parseInt(split[5]) - 1;
                        if (verse1 < verse2) {
                            result += (verse1 + 1) + ". "
                                    + parallelBible.getBooks()[book1].getChapters()[part1].getVerses()[verse1] + "\n"
                                    + (verse2 + 1) + ". "
                                    + parallelBible.getBooks()[book2].getChapters()[part2].getVerses()[verse2];
                        }
                    }
                }
            } else {
                if (parallelBible.getBooks()[book].getChapters().length > part) {
                    if (parallelBible.getBooks()[book].getChapters()[part].getVerses().length > verse) {
                        result += (verse + 1) + ". ";
                        result += parallelBible.getBooks()[book].getChapters()[part].getVerses()[verse];
                    } else {
                        result += (parallelBible.getBooks()[book].getChapters()[part].getVerses().length) + ". ";
                        result += parallelBible.getBooks()[book].getChapters()[part]
                                .getVerses()[parallelBible.getBooks()[book].getChapters()[part].getVerses().length - 1];
                    }
                }
            }
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private String getParallelReference(int book, int part, int verse) {
        try {
            String result = "";
            if (shiftParallel.contains(book + " " + (part + 1) + " " + (verse + 1))) {
                int index = shiftParallel.indexOf(book + " " + (part + 1) + " " + (verse + 1));
                if (shiftParallelI.get(index) != null) {
                    int shift = shiftParallelI.get(index);
                    if (shift != 0) {
                        result += getVerseReferenceNumbers(shift, parallelBible, book, part, verse);
                    } else {
                        return multipleShiftParallel.get(index);
                    }
                }
            } else {
                if (parallelBible.getBooks()[book].getChapters().length > part) {
                    if (parallelBible.getBooks()[book].getChapters()[part].getVerses().length > verse) {
                        result = book + " " + (part + 1) + " " + (verse + 1);
                    } else {
                        result = book + " " + (part + 1) + " "
                                + (parallelBible.getBooks()[book].getChapters()[part].getVerses().length);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private String getParallelVerseAndReference(int book, int part, int verse) {
        try {
            String result = "";
            if (shiftParallel.contains(book + " " + (part + 1) + " " + (verse + 1))) {
                int index = shiftParallel.indexOf(book + " " + (part + 1) + " " + (verse + 1));
                if (shiftParallelI.get(index) != null) {
                    int shift = shiftParallelI.get(index);
                    if (shift != 0) {
                        result += "\n" + getVerse(shift, parallelBible, book, part, verse);
                        if (settings.isReferenceItalic()) {
                            result += "[";
                        }
                        result += "\n" + getVerseReference(shift, parallelBible, book, part, verse);
                        if (settings.isReferenceItalic()) {
                            result += "]";
                        }
                    } else {
                        String[] split = multipleShiftParallel.get(index).split(" ");
                        int book1 = Integer.parseInt(split[0]);
                        int part1 = Integer.parseInt(split[1]) - 1;
                        int verse1 = Integer.parseInt(split[2]) - 1;
                        int book2 = Integer.parseInt(split[3]);
                        int part2 = Integer.parseInt(split[4]) - 1;
                        int verse2 = Integer.parseInt(split[5]) - 1;
                        if (verse1 < verse2) {
                            result += "\n" + (verse1 + 1) + ". "
                                    + parallelBible.getBooks()[book1].getChapters()[part1].getVerses()[verse1] + "\n"
                                    + (verse2 + 1) + ". "
                                    + parallelBible.getBooks()[book2].getChapters()[part2].getVerses()[verse2];
                            result += "\n";
                            if (settings.isReferenceItalic()) {
                                result += "[";
                            }
                            result += parallelBible.getBooks()[book1].getTitle().trim() + " ";
                            result += (part1 + 1) + ":" + (verse1 + 1);
                            if (book1 == book2) {
                                if (part1 == part2) {
                                    result += "," + (verse2 + 1);
                                } else {
                                    result += "; " + (part2 + 1) + ":" + (verse2 + 1);
                                }
                            } else {
                                result += "; " + parallelBible.getBooks()[book2].getTitle().trim() + " " + (part2 + 1) + ":"
                                        + (verse2 + 1);
                            }
                            if (settings.isReferenceItalic()) {
                                result += "]";
                            }
                        }
                    }
                }
            } else {
                if (parallelBible.getBooks()[book].getChapters().length > part) {
                    if (parallelBible.getBooks()[book].getChapters()[part].getVerses().length > verse) {
                        // ITT VANN TODO
                        // int p =
                        // StringUtils.highestCommonSubStringInt(
                        // bible.getBooks()[selectedBook].getChapters()[selectedPart].getVerses()[iVers],
                        // parallelBible.getBooks()[selectedBook].getChapters()[selectedPart]
                        // .getVerses()[iVers]);
                        // double x = p;
                        // if
                        // (bible.getBooks()[selectedBook].getChapters()[selectedPart].getVerses()[iVers]
                        // .length() >
                        // parallelBible.getBooks()[selectedBook].getChapters()[selectedPart]
                        // .getVerses()[iVers].length()) {
                        //// x /=
                        // bible.getBooks()[selectedBook].getChapters()[selectedPart].getVerses()[iVers]
                        //// .length();
                        // } else {
                        //// x /=
                        // parallelBible.getBooks()[selectedBook].getChapters()[selectedPart]
                        //// .getVerses()[iVers].length();
                        // }
                        // System.out.println(p + " " + x);
                        //
                        // System.out.println(getPrevVerse(1, bible,
                        // selectedBook, selectedPart, iVers));
                        // System.out.println(getPrevVerse(2, bible,
                        // selectedBook, selectedPart, iVers));
                        // System.out.println(getPrevVerse(3, bible,
                        // selectedBook, selectedPart, iVers));
                        // System.out.println(getNextVerse(1, bible,
                        // selectedBook, selectedPart, iVers));
                        // System.out.println(getNextVerse(2, bible,
                        // selectedBook, selectedPart, iVers));
                        // System.out.println(getNextVerse(3, bible,
                        // selectedBook, selectedPart, iVers));
                        // TODO END
                        if (shiftParallel.contains(book + " " + (part + 1) + " " + (verse + 1))) {
                            int shift = shiftParallelI
                                    .get(shiftParallel.indexOf(book + " " + (part + 1) + " " + (verse + 1)));
                            result += "\n" + getVerse(shift, parallelBible, book, part, verse);
                            if (settings.isReferenceItalic()) {
                                result += "[";
                            }
                            result += "\n" + getVerseReference(shift, parallelBible, book, part, verse);
                            if (settings.isReferenceItalic()) {
                                result += "]";
                            }
                        } else {
                            result += "\n" + parallelBible.getBooks()[book].getChapters()[part].getVerses()[verse];
                            if (settings.isReferenceItalic()) {
                                result += "[";
                            }
                            result += "\n" + parallelBible.getBooks()[book].getTitle().trim() + " " + (part + 1) + ":"
                                    + (verse + 1);
                            if (settings.isReferenceItalic()) {
                                result += "]";
                            }
                        }
                    } else {
                        result += "\n" + parallelBible.getBooks()[book].getChapters()[part]
                                .getVerses()[parallelBible.getBooks()[book].getChapters()[part].getVerses().length - 1];
                        if (settings.isReferenceItalic()) {
                            result += "[";
                        }
                        result += "\n" + parallelBible.getBooks()[book].getTitle().trim() + " " + (part + 1) + ":"
                                + (parallelBible.getBooks()[book].getChapters()[part].getVerses().length);
                        if (settings.isReferenceItalic()) {
                            result += "]";
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    private void verseSelected() {
        try {
            // System.out.println(selectedBook + " " + selectedPart);
            ObservableList<Integer> ob = verseListView.getSelectionModel().getSelectedIndices();
            StringBuilder string = new StringBuilder();
            int iVerse;
            String reference;
            if (ob.size() == 1) {
                iVerse = verseListView.getSelectionModel().getSelectedIndex();
                if (selectedBook >= 0 && selectedPart >= 0 && iVerse >= 0) {
                    // List<Text> tmpTextList = new LinkedList<>();
                    string = new StringBuilder(bible.getBooks()[selectedBook].getChapters()[selectedPart].getVerses()[iVerse]);
                    // Text tmp = new Text(string);
                    // tmpTextList.add(tmp);
                    reference = getVerseReference(0, bible, selectedBook, selectedPart, iVerse);
                    // tmp = new Text("\n" + reference);
                    // tmp.setTextAlignment(TextAlignment.RIGHT);
                    // tmp.setFill(Color.GREEN);
                    // tmpTextList.add(tmp);
                    // projectionScreenController.setTextsList(tmpTextList);
                    if (settings.isReferenceItalic()) {
                        reference = "[" + reference + "]";
                    }
                    string.append("\n").append(reference);
                    if (settings.isParallel()) {
                        string.append("<color=\"").append(settings.getParallelBibleColor().toString()).append("\">");
                        string.append(getParallelVerseAndReference(selectedBook, selectedPart, iVerse));
                        string.append("</color>");
                    }
                }
                recentController.addRecentBibleVers(string.toString(), selectedBook, selectedPart, iVerse);
            } else if (ob.size() > 1) {
                iVerse = ob.get(0);
                StringBuilder verseNumbers = new StringBuilder();
                StringBuilder tmpParallelVerses = new StringBuilder();
                Reference parallelRef = new Reference();
                parallelRef.setBible(parallelBible);
                if (selectedBook >= 0 && selectedPart >= 0 && iVerse >= 0) {
                    System.out.println(ob.get(0) + " " + iVerse);
                    string = new StringBuilder((ob.get(0) + 1) + ". "
                            + bible.getBooks()[selectedBook].getChapters()[selectedPart].getVerses()[iVerse]);
                    verseNumbers.append(ob.get(0) + 1);
                    if (settings.isParallel()) {
                        String tmp = getParallelVerse(selectedBook, selectedPart, iVerse);
                        if (!tmp.equals("")) {
                            tmpParallelVerses.append("\n").append(tmp);
                            String[] split = getParallelReference(selectedBook, selectedPart, iVerse).split(" ");
                            getReferenceFromSplit(parallelRef, split);
                        }
                    }
                }
                if (selectedBook >= 0 && selectedPart >= 0 && iVerse >= 0) {
                    for (int i = 1; i < ob.size(); ++i) {
                        string.append("\n").append(ob.get(i) + 1).append(". ").append(bible.getBooks()[selectedBook].getChapters()[selectedPart].getVerses()[ob.get(i)]);

                        if (settings.isParallel()) {
                            String tmp = getParallelVerse(selectedBook, selectedPart, ob.get(i));
                            if (!tmp.equals("")) {
                                tmpParallelVerses.append("\n").append(tmp);
                                String[] split = getParallelReference(selectedBook, selectedPart, ob.get(i)).split(" ");
                                getReferenceFromSplit(parallelRef, split);
                            }
                        }
                        if (i == ob.size() - 1) {
                            if (ob.get(i - 1) + 1 == ob.get(i)) {
                                verseNumbers.append("-").append(ob.get(i) + 1);
                            } else {
                                verseNumbers.append(",").append(ob.get(i) + 1);
                            }
                        } else {
                            if (ob.get(i - 1) + 1 == ob.get(i)) {
                                if (ob.get(i) + 1 != ob.get(i + 1)) {
                                    verseNumbers.append("-").append(ob.get(i) + 1);
                                }
                            } else {
                                verseNumbers.append(",").append(ob.get(i) + 1);
                            }
                        }
                    }
                }
                // string = string + "\n" +
                // bookListView.getSelectionModel().getSelectedItem().trim()
                // + " "
                // + (iPart + 1) + ":" + ob.get(0) + "-" +
                // ob.get(ob.size() - 1);
                reference = bible.getBooks()[selectedBook].getTitle().trim() + " " + (selectedPart + 1) + ":" + verseNumbers;
                if (settings.isReferenceItalic()) {
                    reference = "[" + reference + "]";
                }
                string.append("\n").append(reference);
                if (settings.isParallel()) {
                    if (!tmpParallelVerses.toString().equals("")) {
                        if (!tmpParallelVerses.toString().trim().isEmpty()) {
                            string.append("<color=\"").append(settings.getParallelBibleColor().toString()).append("\">");
                            string.append(tmpParallelVerses).append("\n");
                            if (settings.isReferenceItalic()) {
                                string.append("[");
                            }
                            string.append(parallelRef.getReference());
                            if (settings.isReferenceItalic()) {
                                string.append("]");
                            }
                            string.append("</color>");
                        }
                    }
                }
                ArrayList<Integer> tmp = new ArrayList<>(ob);
                recentController.addRecentBibleVers(string.toString(), selectedBook, selectedPart, iVerse, verseNumbers.toString(), tmp);
            }
            if (string.length() > 0) {
                if (!settings.isShowReferenceOnly()) {
                    projectionScreenController.setText(string.toString(), ProjectionType.BIBLE);
                }
                // if (!referenceTextArea.getText().trim().isEmpty()) {
                // reference = "\n" + reference;
                // }
                // ref.setReference(referenceTextArea.getText() + reference);
                for (int i : ob) {
                    if (i == -1) {
                        i = verseListView.getSelectionModel().getSelectedIndex();
                    }
                    ref.addVers(selectedBook, selectedPart + 1, i + 1);
                }
                refreshReferenceTextArea();
                if (settings.isShowReferenceOnly()) {
                    projectionScreenController.setText(referenceTextArea.getText(), ProjectionType.REFERENCE);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void getReferenceFromSplit(Reference reference, String[] split) {
        try {
            if (split.length > 2) {
                int book1 = Integer.parseInt(split[0]);
                int part1 = Integer.parseInt(split[1]);
                int verse1 = Integer.parseInt(split[2]);
                reference.addVers(book1, part1, verse1);
                if (split.length > 5) {
                    book1 = Integer.parseInt(split[3]);
                    part1 = Integer.parseInt(split[4]);
                    verse1 = Integer.parseInt(split[5]);
                    reference.addVers(book1, part1, verse1);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void refreshReferenceTextArea() {
        try {
            ref.setBible(bible);
            referenceTextArea.setText(ref.getReference());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private EventHandler<KeyEvent> numeric_Validation() {
        return getKeyEventEventHandler(LOG);
    }

    private void setBookPartVerse(int book, int part, int verse) {
        try {
            addAllBooks();
            verseListView.getSelectionModel().clearSelection();
            verseListView.scrollTo(0);
            verseListView.refresh();
            if (book >= 0 && book < bookListView.getItems().size()) {
                if (bookListView.getSelectionModel().getSelectedIndex() != book) {
                    searchSelected = 1;
                } else {
                    searchSelected = 0;
                }
                bookListView.getSelectionModel().select(book);
                while (searchSelected == 1) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                bookListView.scrollTo(book);
                if (part >= 0 && part < partListView.getItems().size()) {
                    if (partListView.getSelectionModel().getSelectedIndex() != part) {
                        searchSelected = 2;
                    } else {
                        searchSelected = 0;
                    }
                    partListView.getSelectionModel().select(part);
                    while (searchSelected == 2) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    partListView.scrollTo(part);
                    if (verse >= 0 && verse < verseListView.getItems().size()) {
                        verseListView.scrollTo(verse);
                        verseListView.getFocusModel().focus(verse);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void onClose() {
        try {
            Settings settings = this.settings;
            settings.setBibleTabHorizontalSplitPaneDividerPosition(horizontalSplitPane.getDividerPositions()[0]);
            settings.setBibleTabVerticalSplitPaneDividerPosition(verticalSplitPane.getDividerPositions()[0]);
            settings.save();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
