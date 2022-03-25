package projector.application;

import org.junit.Assert;
import org.junit.Test;

public class UpdaterTest {

    @Test
    public void getUrlTest() {
        Assert.assertEquals(Updater.getInstance().getUrl(), "http://localhost:8080/projector.exe");
    }
}