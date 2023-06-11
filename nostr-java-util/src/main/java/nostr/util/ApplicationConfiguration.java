package nostr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.Properties;
import lombok.extern.java.Log;

/**
 *
 * @author eric
 */
@Log
public class ApplicationConfiguration {

    private final String DEFAULT_APP_CONFIG = "config";
    private final String DEFAULT_ID_CONFIG = "/profile.properties";
    private final String DEFAULT_RELAYS_CONFIG = "/relays.properties";
    //private final String DEFAULT_CLIENT_CONFIG = "/client.properties";
    private final String DEFAULT_CONFIG_FOLDER = ".nostr-java";

    private final Properties properties;

    public ApplicationConfiguration() throws IOException {
        InputStream appConfig = getAppConfig();
        this.properties = new Properties();
        
        log.log(Level.INFO, "Loading the application configuration file...");
        this.properties.load(appConfig);
    }

    public String getIdentityProperties() {
        var id = getProperty("profile");
        return id == null ? DEFAULT_ID_CONFIG : id;
    }

    public String getRelaysProperties() {
        var client = getProperty("relays");
        return client == null ? DEFAULT_RELAYS_CONFIG : client;
    }

    public String getDefaultConfigFolder() {
        return getProperty("config.folder");
    }

    public String getDefaultBaseConfigFolder() {
        var dcf = getProperty("config.base");
        return dcf == null ? System.getProperty("user.home") : dcf;
    }

    private InputStream getAppConfig() throws FileNotFoundException {
        var configFile = System.getProperty(DEFAULT_APP_CONFIG);
        return configFile == null ? getAppProperties() : new FileInputStream(configFile);
    }

    protected String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    private InputStream getAppProperties() throws FileNotFoundException {

        if (new File("app.properties").exists()) {
            return new FileInputStream("app.properties");
        }

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resource = classLoader.getResourceAsStream("/app.properties");
        resource = resource == null ? classLoader.getResourceAsStream("app.properties") : resource;

        if (resource == null) {
            throw new IllegalStateException("app.properties file not found in resources folder.");
        }

        return resource;
    }

}
