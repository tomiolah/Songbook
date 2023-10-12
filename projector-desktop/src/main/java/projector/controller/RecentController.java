package projector.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.util.Callback;
import projector.application.ProjectionType;
import projector.controller.song.SongController;
import projector.utils.AppProperties;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static projector.controller.BibleController.setGeneralTextColor;

public class RecentController {

    private final boolean isBlank = false;
    @FXML
    private ListView<String> listView;
    private SongController songController;
    private BibleController bibleController;
    private List<ProjectionType> typeList;
    private List<Integer> bookI;
    private List<Integer> partI;
    private List<Integer> versI;
    private List<String> songTitles;
    private List<String> verseNumbersListText;
    private List<List<Integer>> verseNumbersList;
    private int searchSelected;

    private static void writeLineNoEmptyLine(BufferedWriter bw, String s) throws IOException {
        s = s.replaceAll("\n", "");
        if (s.trim().isEmpty()) {
            return;
        }
        bw.write(s + System.lineSeparator());
    }

    public static String getRecentFilePath() {
        return AppProperties.getInstance().getWorkDirectory() + "recent.txt";
    }

    public void initialize() {
        typeList = new LinkedList<>();
        bookI = new LinkedList<>();
        partI = new LinkedList<>();
        versI = new LinkedList<>();
        songTitles = new LinkedList<>();
        verseNumbersListText = new LinkedList<>();
        verseNumbersList = new ArrayList<>();
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (typeList.get(listView.getSelectionModel().getSelectedIndex()) == ProjectionType.SONG) {
                    songController.titleSearchStartWith(newValue);
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
                        // bibleController.getFullVersListView().getSelectionModel().clearSelection();
                        bibleController.setSelecting(true);
                        for (Integer i : verseNumbersList.get(index)) {
                            bibleController.getVerseListView().getSelectionModel().select(i);
                        }
                        bibleController.setSelecting(false);
                        bibleController.getVerseListView().scrollTo(versI.get(index));
                        // bibleController.getFullVersListView().scrollTo(versI.get(index));
                    }
                }
            }
        });
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(final ListView<String> list) {
                return new ListCell<>() {
                    {
                        Text text = new Text();
                        text.wrappingWidthProperty().bind(list.widthProperty().subtract(50));
                        text.textProperty().bind(itemProperty());
                        setGeneralTextColor(text);
                        super.setPrefWidth(0.0);
                        setGraphic(text);
                    }
                };
            }
        });
    }

    public void addRecentSong(String text, ProjectionType type) {
        if (!isBlank) {
            if (!text.trim().isEmpty()) {
                typeList.add(type);
                listView.getItems().add(text);
                songTitles.add(text);
                bookI.add(-1);
                partI.add(0);
                versI.add(0);
                verseNumbersListText.add("");
                ArrayList<Integer> tmp = new ArrayList<>();
                tmp.add(-1);
                verseNumbersList.add(tmp);
            }
        }
    }

    public String getLastItemText() {
        if (listView.getItems().size() > 0) {
            return listView.getItems().get(listView.getItems().size() - 1);
        } else {
            return "";
        }
    }

    public void setPrefHeight(double d) {
        listView.setPrefHeight(listView.getHeight() + d);
    }

    public void setPrefWidth(double d) {
        listView.setPrefWidth(listView.getWidth() + d);
    }

    public void setSongController(SongController songController) {
        this.songController = songController;
    }

    void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

    void setSearchSelected(int searchSelected) {
        this.searchSelected = searchSelected;
    }

    public void close() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getRecentFilePath(), true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            Date date = new Date();
            if (bookI.size() > 0 || songTitles.size() > 0) {
                bw.write(date + System.lineSeparator());
                for (int i = 0; i < bookI.size(); ++i) {
                    if (bookI.get(i) != -1) {
                        writeLineNoEmptyLine(bw, (bookI.get(i) + 1) + " " + (partI.get(i) + 1) + " " + verseNumbersListText.get(i));
                    }
                }
                bw.write(System.lineSeparator());
                for (String title : songTitles) {
                    writeLineNoEmptyLine(bw, title);
                }
                bw.write(System.lineSeparator());
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addRecentBibleVerse(String text, int iBook, int iPart, int iVerse, String verseNumbers,
                             ArrayList<Integer> tmpVerseNumberList) {
        if (!isBlank) {
            if (!text.trim().isEmpty()) {
                typeList.add(ProjectionType.BIBLE);
                listView.getItems().add(text);
                bookI.add(iBook);
                partI.add(iPart);
                versI.add(iVerse);
                verseNumbersListText.add(verseNumbers);
                verseNumbersList.add(tmpVerseNumberList);
            }
        }
    }
}
