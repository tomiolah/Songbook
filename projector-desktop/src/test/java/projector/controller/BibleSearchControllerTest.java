package projector.controller;

import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.text.TextFlow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import projector.BaseTest;

import static org.loadui.testfx.GuiTest.find;

public class BibleSearchControllerTest extends BaseTest {

    @Before
    public void setUp() {
        final TabPane tabPane = find("#tabPane");
        tabPane.getSelectionModel().select(1);
    }

    @Test
    public void should_click_to_all() {
        searchShouldFind("szeret");
        searchShouldFind("anyasze");
//        verifyThat("#searchListView", (ListView<TextFlow> searchListView) -> {
//            String text = searchListView.getItems().get(0).getAccessibleText();
//        return searchListView.getItems().size() > 0;
//            return text.contains("a");
//        });
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