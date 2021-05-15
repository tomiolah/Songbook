package projector.utils;

import java.io.IOException;
import java.util.Properties;

public class CustomProperties {

    private static CustomProperties instance;

    public static CustomProperties getInstance() {
        if (instance == null) {
            instance = new CustomProperties();
        }
        return instance;
    }

    public String vowels() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("app.properties"));
            String vowels = (String) properties.get("vowels");
            if (vowels != null) {
                return vowels;
            }
        } catch (IOException ignored) {
        }
        return "aeiou";
    }
}
