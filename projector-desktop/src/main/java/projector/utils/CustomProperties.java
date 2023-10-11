package projector.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class CustomProperties {

    private static final Logger LOG = LoggerFactory.getLogger(CustomProperties.class);
    private static CustomProperties instance;

    public static CustomProperties getInstance() {
        if (instance == null) {
            instance = new CustomProperties();
        }
        return instance;
    }

    private static boolean isMacOs() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Mac OS X") || osName.startsWith("Mac OS");
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("app.properties"));
            return properties;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public String vowels() {
        Properties properties = getProperties();
        if (properties != null) {
            String vowels = (String) properties.get("vowels");
            if (vowels != null) {
                return vowels;
            }
        }
        return "aeiou";
    }

    public String getWorkDirectory() {
        Properties properties = getProperties();
        if (properties != null) {
            String workDirectoryName = (String) properties.get("workDirectoryName");
            if (workDirectoryName != null) {
                String workDirectoryPath;
                if (isMacOs()) {
                    workDirectoryPath = System.getProperty("user.home") + "/Library/Application Support/";
                } else {
                    workDirectoryPath = "";
                }
                return workDirectoryPath + workDirectoryName + "/";
            }
        }
        return "./";
    }

}
