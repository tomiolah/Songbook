package projector.controller;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import projector.api.BibleApiBean;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.VerseIndex;
import projector.service.BibleService;
import projector.service.ServiceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndicesForBibleController {
    public TextField textField;
    public ListView<BibleVerse> leftListView;
    public ListView<BibleVerse> otherListView;
    public ListView<Book> bookListView;
    public ListView<Chapter> chapterListView;
    private Bible otherBible;
    private HashMap<Long, List<BibleVerse>> verseHashMap;
    private Bible bible;
    private MultipleSelectionModel<BibleVerse> leftListViewSelectionModel;
    private MultipleSelectionModel<BibleVerse> otherListViewSelectionModel;

    public void initialize() {
        leftListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        otherListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        leftListViewSelectionModel = leftListView.getSelectionModel();
        otherListViewSelectionModel = otherListView.getSelectionModel();
    }

    void setLeftBible(Bible bible) {
        this.bible = bible;
        bookListView.getItems().addAll(bible.getBooks());
        MultipleSelectionModel<Book> bookSelectionModel = bookListView.getSelectionModel();
        MultipleSelectionModel<Chapter> chapterSelectionModel = chapterListView.getSelectionModel();
        bookSelectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ObservableList<Chapter> items = chapterListView.getItems();
                items.clear();
                items.addAll(newValue.getChapters());
            }
        });
        bookSelectionModel.selectFirst();
        chapterSelectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ObservableList<BibleVerse> items = leftListView.getItems();
                items.clear();
                items.addAll(newValue.getVerses());
                if (otherBible != null) {
                    ObservableList<BibleVerse> items1 = otherListView.getItems();
                    items1.clear();
                    for (BibleVerse verse : newValue.getVerses()) {
                        try {
                            List<VerseIndex> verseIndices = verse.getVerseIndices();
                            if (verseIndices != null && verseIndices.size() > 0) {
                                List<BibleVerse> bibleVerses = new ArrayList<>();
                                for (VerseIndex verseIndex : verseIndices) {
                                    List<BibleVerse> c = verseHashMap.get(verseIndex.getIndexNumber());
                                    if (c != null) {
                                        bibleVerses.addAll(c);
                                    }
                                }
                                if (bibleVerses.size() == 0) {
                                    items1.add(new BibleVerse());
                                } else if (bibleVerses.size() == 1) {
                                    items1.add(bibleVerses.get(0));
                                } else {
                                    BibleVerse bibleVerse = bibleVerses.get(0);
                                    BibleVerse verse1 = new BibleVerse();
                                    verse1.setVerseIndices(bibleVerse.getVerseIndices());
                                    verse1.setNumber(bibleVerse.getNumber());
                                    verse1.setChapter(bibleVerse.getChapter());
                                    StringBuilder text = new StringBuilder(bibleVerse.getText());
                                    for (int i = 1; i < bibleVerses.size(); ++i) {
                                        text.append("\n").append(bibleVerses.get(i).getText());
                                    }
                                    verse1.setText(text.toString());
                                    items1.add(verse1);
                                }
                            } else {
                                System.out.println();
                            }
                        } catch (IndexOutOfBoundsException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    void setOtherBible(Bible otherBible) {
        this.otherBible = otherBible;
        verseHashMap = new HashMap<>();
        fillVerseMap();
        chapterListView.getSelectionModel().selectFirst();
    }

    private void fillVerseMap() {
        verseHashMap.clear();
        for (Book book : otherBible.getBooks()) {
            for (Chapter chapter : book.getChapters()) {
                for (BibleVerse verse : chapter.getVerses()) {
                    if (verse.getText().isEmpty()) {
                        System.out.println(verse.getId());
                    }
                    for (VerseIndex index : verse.getVerseIndices()) {
                        if (verseHashMap.containsKey(index.getIndexNumber())) {
                            List<BibleVerse> bibleVerses = verseHashMap.get(index.getIndexNumber());
//                            BibleVerse bibleVerse = bibleVerses.get(0);
//                            bibleVerse.setText(bibleVerse.getText().replaceAll("\n" + verse.getText(), ""));
                            bibleVerses.add(verse);
                        } else {
                            ArrayList<BibleVerse> list = new ArrayList<>();
                            list.add(verse);
                            verseHashMap.put(index.getIndexNumber(), list);
                        }
                    }
                }
            }
        }
    }

    public void merge() {
        ObservableList<BibleVerse> selectedItems = leftListViewSelectionModel.getSelectedItems();
        BibleVerse selectedItem = otherListViewSelectionModel.getSelectedItem();
        List<VerseIndex> verseIndices = selectedItem.getVerseIndices();
        verseIndices.clear();
        for (BibleVerse verse : selectedItems) {
            for (VerseIndex index : verse.getVerseIndices()) {
                VerseIndex verseIndex = new VerseIndex();
                verseIndex.setIndexNumber(index.getIndexNumber());
                verseIndices.add(verseIndex);
            }
        }
        selectedItem.setVerseIndices(verseIndices);
        fillVerseMap();
    }

    public void decreaseIndex() {
        BibleVerse selectedItem = otherListViewSelectionModel.getSelectedItem();
        Long indexNumber = selectedItem.getVerseIndices().get(0).getIndexNumber();
        changeIndex(indexNumber, -1000);
        fillVerseMap();
    }

    public void increaseIndex() {
        BibleVerse selectedItem = otherListViewSelectionModel.getSelectedItem();
        Long indexNumber = selectedItem.getVerseIndices().get(0).getIndexNumber();
        changeIndex(indexNumber, 1000);
        fillVerseMap();
    }

    private void changeIndex(Long indexNumber, int shift) {
        for (Map.Entry<Long, List<BibleVerse>> next : verseHashMap.entrySet()) {
            if (next.getKey() >= indexNumber) {
                for (BibleVerse bibleVerse : next.getValue()) {
                    for (VerseIndex verseIndex : bibleVerse.getVerseIndices()) {
                        verseIndex.setIndexNumber(verseIndex.getIndexNumber() + shift);
                    }
                }
            }
        }
    }

    public void save() {
        BibleService bibleService = ServiceManager.getBibleService();
//        bibleService.delete(bible);
        bibleService.delete(otherBible);
//        bibleService.create(bible);
        bibleService.create(otherBible);
    }

    public void task() {
        for (Book book : bible.getBooks()) {
            for (Chapter chapter : book.getChapters()) {
                for (BibleVerse bibleVerse : chapter.getVerses()) {
                    Long indexNumber = bibleVerse.getVerseIndices().get(0).getIndexNumber();
                    if (!verseHashMap.containsKey(indexNumber)) {
                        System.out.println(indexNumber + " " + book.getTitle() + " " + chapter.getNumber() + ":" + bibleVerse.getNumber() + "     " + bibleVerse.getText());
                    }
                }
            }
        }
        BibleApiBean bibleApiBean = new BibleApiBean();
        Bible uploadedBible = bibleApiBean.uploadBible(otherBible);
        System.out.println("accomplished");
    }

    public void merge1N() {
        ObservableList<BibleVerse> selectedItems = otherListViewSelectionModel.getSelectedItems();
        BibleVerse selectedItem = leftListViewSelectionModel.getSelectedItem();
        List<VerseIndex> verseIndices = selectedItem.getVerseIndices();
        VerseIndex first = verseIndices.get(0);
        verseIndices.clear();
        verseIndices.add(first);
        List<VerseIndex> otherSelectedVerseIndexList = selectedItems.get(0).getVerseIndices();
        long l = otherSelectedVerseIndexList.get(0).getIndexNumber() - first.getIndexNumber();
        if (l != 0) {
            changeIndex(otherSelectedVerseIndexList.get(0).getIndexNumber(), (int) -l);
        }
        for (int i = 1; i < selectedItems.size() && i < 10; ++i) {
            VerseIndex verseIndex = new VerseIndex();
            long newIndex = first.getIndexNumber() + 10 * i;
            verseIndex.setIndexNumber(newIndex);
            verseIndices.add(verseIndex);
            selectedItems.get(i).getVerseIndices().get(0).setIndexNumber(newIndex);
        }
        selectedItem.setVerseIndices(verseIndices);
        fillVerseMap();
    }
}
