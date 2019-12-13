package projector.application;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import projector.BaseTest;
import projector.controller.song.util.SearchedSong;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.VerseIndex;
import projector.service.BibleService;
import projector.service.ServiceManager;

import java.util.ArrayList;

import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static org.loadui.testfx.GuiTest.find;

public class MainTest extends BaseTest {

    static public void createABible() {
        Bible bible = new Bible();
        bible.setName("Bible");
        ArrayList<Book> books = new ArrayList<>();
        Book book = new Book();
        book.setTitle("book");
        ArrayList<Chapter> chapters = new ArrayList<>();
        Chapter chapter = new Chapter();
        chapter.setNumber((short) 1);
        ArrayList<BibleVerse> verses = new ArrayList<>();
        BibleVerse bibleVerse = new BibleVerse();
        bibleVerse.setText("A verse");
        bibleVerse.setNumber((short) 1);
        ArrayList<VerseIndex> verseIndices = new ArrayList<>();
        VerseIndex verseIndex = new VerseIndex();
        verseIndex.setBibleId((long) 1);
        verseIndex.setIndexNumber((long) 1000);
        verseIndices.add(verseIndex);
        bibleVerse.setVerseIndices(verseIndices);
        verses.add(bibleVerse);
        chapter.setVerses(verses);
        chapters.add(chapter);
        book.setChapters(chapters);
        books.add(book);
        bible.setBooks(books);
        BibleService bibleService = ServiceManager.getBibleService();
        bibleService.delete(bibleService.findAll());
        bibleService.create(bible);
    }

    private void openTab(int index) {
        final TabPane tabPane = find("#tabPane");
        Platform.runLater(() -> tabPane.getSelectionModel().select(index));
        sleep(1000);
    }

    @Test
    public void should_drag_file_into_trashcan() {
        clickOn("#tabPane");
    }

    @Test
    public void should_click_to_all() {
        createABible();
        clickOn("#tabPane");
        openTab(0);
        clickOn("#bible");

        final String bookTitle = "book";
        clickOn("#bookTextField").sleep(50).clickOn("#bookTextField").sleep(50).write(bookTitle).sleep(50);
        final ListView<String> bookListView = find("#bookListView");
        final ObservableList<String> bookListViewItems = bookListView.getItems();
        for (String item : bookListViewItems) {
            Assert.assertThat(item, CoreMatchers.containsString(bookTitle));
        }
        final String partNumber = "1";
        clickOn("#partTextField").write(partNumber);
        final ListView<Integer> partListView = find("#partListView");
        Assert.assertTrue(partListView.getItems().size() > 0);
        final String verseNumber = "1";
        clickOn("#verseTextField").write(verseNumber);
        final ListView<Integer> verseListView = find("#verseListView");
        Assert.assertTrue(verseListView.getItems().size() > 0);
        clickOn("#verseListView").push(ENTER).sleep(100);
    }

    @Test
    public void should_be_none_in_recent() {
        openTab(2);
        clickOn("#searchTextField").write("100");

        final ListView<SearchedSong> searchedSongListView = find("#listView");
        Bounds boundsInScene = searchedSongListView.localToScene(searchedSongListView.getBoundsInLocal());
        clickOn("#listView");
        clickOn(boundsInScene.getMinX() + 10, boundsInScene.getMinY() + 10);
        push(DOWN);
        push(DOWN);
        push(DOWN);
        openTab(3);
        final ListView<String> recentListView = find("#listView");
        Assert.assertEquals(recentListView.getItems().size(), 0);
    }
}