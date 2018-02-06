package projector.controller.song;

import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import projector.BaseTest;
import projector.controller.song.util.SearchedSong;

import static org.loadui.testfx.GuiTest.find;

public class SongControllerTest extends BaseTest {

    private static final String test_songTitle = "Test song";

    @Before
    public void setUp() {
        final TabPane tabPane = find("#tabPane");
        tabPane.getSelectionModel().select(2);
    }

    @Test
    public void clickNewSongButton() {
        clickOn("#newSongButton");
        clickOn("#titleTextField").write(test_songTitle);
        clickOn("#newVerseButton");
        clickOn("#textArea").write("First verse");
        clickOn("#saveButton");
        ListView<SearchedSong> listView = find("#listView");
        boolean was = false;
        for (SearchedSong song : listView.getItems()) {
            if (song.getSong().getTitle().equals(test_songTitle)) {
                was = true;
                break;
            }
        }
        Assert.assertEquals(true, was);
        editSong();
        deleteASong();
    }

    //	@Test
    private void editSong() {
        clickOn("#searchTextField").write(test_songTitle);
        final ListView<SearchedSong> searchedSongListView = find("#listView");
        Bounds boundsInScene = searchedSongListView.localToScene(searchedSongListView.getBoundsInLocal());
        clickOn("#listView");
        final double x = boundsInScene.getMinX() + 10;
        final double y = boundsInScene.getMinY() + 10;
        rightClickOn(x, y).sleep(100).clickOn(x + 7, y + 7);
        final String edited_text = "Edited text";
        clickOn("#textArea").write(edited_text);
        clickOn("#saveButton");
        ListView<SearchedSong> listView = find("#listView");
        SearchedSong editedSong = null;
        for (SearchedSong song : listView.getItems()) {
            if (song.getSong().getTitle().equals(test_songTitle)) {
                editedSong = song;
                break;
            }
        }
        Assert.assertNotNull(editedSong);
        Assert.assertTrue(editedSong.getSong().getVerses().get(0).getText().contains(edited_text));
    }

    //	@Test
    private void deleteASong() {
        doubleClickOn("#searchTextField").doubleClickOn("#searchTextField").write(test_songTitle);
        final ListView<SearchedSong> searchedSongListView = find("#listView");
        Bounds boundsInScene = searchedSongListView.localToScene(searchedSongListView.getBoundsInLocal());
        clickOn("#listView");
        final double x = boundsInScene.getMinX() + 10;
        final double y = boundsInScene.getMinY() + 10;
        rightClickOn(x, y).sleep(100).clickOn(x + 7, y + 47);
        doubleClickOn("#searchTextField").doubleClickOn("#searchTextField").write(test_songTitle);
        Assert.assertEquals(searchedSongListView.getItems().size(), 0);
    }

    @Test
    public void clickNewSongBookButton() {
        clickOn("#newSongBookButton");
        clickOn("#songBookTitleTextField");
    }
}