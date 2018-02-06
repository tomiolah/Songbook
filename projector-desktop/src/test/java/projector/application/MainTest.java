package projector.application;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import projector.BaseTest;
import projector.controller.song.util.SearchedSong;

import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static org.loadui.testfx.GuiTest.find;

public class MainTest extends BaseTest {

    @Test
    public void should_drag_file_into_trashcan() {
        // given:
        clickOn("#tabPane");

//        clickOn("#myButton2");
//        clickOn("#myButton3");
//        clickOn("#myButton4");
//        rightClickOn("#desktop").moveTo("New").clickOn("Text Document");
//        write("myTextfile.txt").push(ENTER);

        // when:
//        drag(".file").dropTo("#trash-can");

        // then:
//        verifyThat("#desktop", hasChildren(0, ".file"));
    }

    @Test
    public void should_click_to_all() {
        // given:
        clickOn("#tabPane");
        final TabPane tabPane = find("#tabPane");
        tabPane.getSelectionModel().select(0);
        clickOn("#bible");
//        press(KeyCode.M);
//        write("4").sleep(100);
//        write("4").sleep(100);
//        write("4").sleep(100);
//        sleep(1000);

        final String bookTitle = "4";
        clickOn("#bookTextField").sleep(50).clickOn("#bookTextField").sleep(50).write(bookTitle).sleep(50);
        final ListView<String> bookListView = find("#bookListView");
        final ObservableList<String> bookListViewItems = bookListView.getItems();
        for (String item : bookListViewItems) {
            Assert.assertThat(item, CoreMatchers.containsString(bookTitle));
        }
//        verifyThat(, "4");
//        verifyThat("#bookListView", hasChildren(1,"4"));
        final String partNumber = "4";
        clickOn("#partTextField").write(partNumber);
        final ListView<Integer> partListView = find("#partListView");
        Assert.assertTrue(partListView.getItems().size() > 0);
//        final ObservableList<Integer> partListViewItems = partListView.getItems();

//        for (Integer item : partListViewItems) {
//            Assert.assertThat(item.toString(), CoreMatchers.containsString(partNumber));
//        }
        final String verseNumber = "4";
        clickOn("#versTextField").write(verseNumber);
        final ListView<Integer> verseListView = find("#versListView");
        Assert.assertTrue(verseListView.getItems().size() > 0);
//        final ObservableList<Integer> verseListViewItems = verseListView.getItems();
//        for (Integer item : verseListViewItems) {
//            Assert.assertThat(item.toString(), CoreMatchers.containsString(verseNumber));
//        }
        clickOn("#versListView").push(ENTER).sleep(100);
//        clickOn("#versListView").push(ENTER).sleep(100);
//        clickOn("#versListView").push(ENTER).sleep(100);
//        clickOn("#bibleSearch");
//        clickOn("#song");
//        clickOn("#recent");
//        clickOn("#history");
//        clickOn("#myButton2");
//        clickOn("#myButton3");
//        clickOn("#myButton4");
//        rightClickOn("#desktop").moveTo("New").clickOn("Text Document");
//        write("myTextfile.txt").push(ENTER);

        // when:
//        drag(".file").dropTo("#trash-can");

        // then:
//        verifyThat("#desktop", hasChildren(0, ".file"));
    }

    @Test
    public void should_be_none_in_recent() {
        final TabPane tabPane = find("#tabPane");
        tabPane.getSelectionModel().select(2);
        clickOn("#searchTextField").write("100");

        final ListView<SearchedSong> searchedSongListView = find("#listView");
        Bounds boundsInScene = searchedSongListView.localToScene(searchedSongListView.getBoundsInLocal());
        clickOn("#listView");
        clickOn(boundsInScene.getMinX() + 10, boundsInScene.getMinY() + 10);
        push(DOWN);
        push(DOWN);
        push(DOWN);
        tabPane.getSelectionModel().select(3);
        final ListView<String> recentListView = find("#listView");
        Assert.assertEquals(recentListView.getItems().size(), 0);
    }
}