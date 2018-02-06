package projector.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.util.Callback;
import projector.application.ProjectionType;
import projector.controller.song.SongController;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecentController {

    @FXML
    private ListView<String> listView;

    private SongController songController;
    private BibleController bibleController;
    private List<ProjectionType> typeList;
    private List<Integer> bookI;
    private List<Integer> partI;
    private List<Integer> versI;
    private List<String> songTitles;
    private List<String> versNumbersListText;
    private List<List<Integer>> versNumbersList;
    private int searchSelected;
    private boolean isBlank;

    public void initialize() {
        typeList = new LinkedList<>();
        bookI = new LinkedList<>();
        partI = new LinkedList<>();
        versI = new LinkedList<>();
        songTitles = new LinkedList<>();
        versNumbersListText = new LinkedList<>();
        versNumbersList = new ArrayList<>();
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.isEmpty()) {
                    if (typeList.get(listView.getSelectionModel().getSelectedIndex()) == ProjectionType.SONG) {
                        // songController.setTitleTextFieldText(newValue);
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
                            for (Integer i : versNumbersList.get(index)) {
                                bibleController.getVerseListView().getSelectionModel().select(i);
                            }
                            bibleController.setSelecting(false);
                            // bibleController.getFullVersListView().getSelectionModel()
                            // .select(versI.get(index).intValue());
                            bibleController.getVerseListView().scrollTo(versI.get(index));
                            // bibleController.getFullVersListView().scrollTo(versI.get(index));
                        }
                    }
                }
            }

        });
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(final ListView<String> list) {
                return new ListCell<String>() {
                    {
                        Text text = new Text();
                        text.wrappingWidthProperty().bind(list.widthProperty().subtract(50));
                        text.textProperty().bind(itemProperty());

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
                versNumbersListText.add("");
                ArrayList<Integer> tmp = new ArrayList<>();
                tmp.add(-1);
                versNumbersList.add(tmp);
            }
        }
    }

    public void addRecentBibleVers(String text, int book, int part, int vers) {
        if (!isBlank) {
            if (!text.trim().isEmpty() && !isTheLastVers(book, part, vers)) {
                typeList.add(ProjectionType.BIBLE);
                listView.getItems().add(text);
                bookI.add(book);
                partI.add(part);
                versI.add(vers);
                versNumbersListText.add((vers + 1) + "");
                ArrayList<Integer> tmp = new ArrayList<>();
                tmp.add(vers);
                versNumbersList.add(tmp);
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

    public void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

    public void setSearchSelected(int searchSelected) {
        this.searchSelected = searchSelected;
    }

    public boolean isBlank() {
        return isBlank;
    }

    public void setBlank(boolean isBlank) {
        this.isBlank = isBlank;
    }

    public void close() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("recent.txt", true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
            Date date = new Date();
            if (bookI.size() > 0 || songTitles.size() > 0) {
                bw.write(date + System.lineSeparator());
                for (int i = 0; i < bookI.size(); ++i) {
                    if (bookI.get(i) != -1) {
                        bw.write((bookI.get(i) + 1) + " " + (partI.get(i) + 1) + " " + versNumbersListText.get(i)
                                + System.lineSeparator());
                    }
                }
                bw.write(System.lineSeparator());
                for (String title : songTitles) {
                    bw.write(title + System.lineSeparator());
                }
                bw.write(System.lineSeparator());
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addRecentBibleVers(String text, int iBook, int iPart, int iVers, String versNumbers,
                                   ArrayList<Integer> tmpVersNumberList) {
        if (!isBlank) {
            if (!text.trim().isEmpty() && !isTheLastVers(iBook, iPart, iVers)) {
                typeList.add(ProjectionType.BIBLE);
                listView.getItems().add(text);
                bookI.add(iBook);
                partI.add(iPart);
                versI.add(iVers);
                versNumbersListText.add(versNumbers);
                versNumbersList.add(tmpVersNumberList);
            }
        }
    }

    public boolean isTheLastVers(int iBook, int iPart, int iVers) {
        return false;
    }
}
