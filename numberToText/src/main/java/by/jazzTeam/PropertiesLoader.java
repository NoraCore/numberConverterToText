package by.jazzTeam;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class PropertiesLoader {
    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    private PropertiesLoader() {
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static void loadProperties() {
        try (var inputStream = PropertiesLoader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            PROPERTIES.load(inputStream);
        } catch (IOException ex) {
            log.debug(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
