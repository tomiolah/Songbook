package projector;

import javafx.stage.Stage;
import org.junit.Before;
import org.testfx.framework.junit.ApplicationTest;

public class BaseTest extends ApplicationTest {

    @Before
    public void setUpClass() throws Exception {
        ApplicationTest.launch(MainDesktop.class);
    }

    @Override
    public void start(Stage stage) {
        stage.show();
    }
}