package projector.controller;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.text.TextFlow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import projector.BaseTest;

import static org.loadui.testfx.GuiTest.find;
import static projector.application.MainTest.createABible;

public class BibleSearchControllerTest extends BaseTest {

    private void openTab(int index) {
        final TabPane tabPane = find("#tabPane");
        Platform.runLater(() -> tabPane.getSelectionModel().select(index));
        sleep(1000);
    }

    @Before
    public void setUp() {
        createABible();
        openTab(1);
    }

    @Test
    public void should_click_to_all() {
        searchShouldFind("verse");
    }

    private void searchShouldFind(String searchText) {
        clickOn("#searchTextField").sleep(100).clickOn("#searchTextField").sleep(50).clickOn("#searchTextField").sleep(10).write(searchText).sleep(500);
        ListView<TextFlow> searchListView = find("#searchListView");
        if (searchListView.getItems().size() == 0) {
            sleep(500);
        }
        Assert.assertNotEquals(searchListView.getItems().size(), 0);
    }
}