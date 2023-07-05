package nostr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Properties;
import java.util.logging.Level;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Data
@Log
public abstract class AbstractBaseConfiguration {

    protected final Properties properties = new Properties();
    protected final ApplicationConfiguration appConfig;

    private static final String PREFIX_FILE = "file.";
    private static final String CONFIG_DIR = "config.folder";

    protected AbstractBaseConfiguration() throws IOException {
        this.appConfig = new ApplicationConfiguration();
    }

    protected AbstractBaseConfiguration(ApplicationConfiguration appConfig) {
        this.appConfig = appConfig;
    }

    protected String getFileLocation(String key) throws FileNotFoundException {

        final String filename = properties.getProperty(key);
        var location = filename;

        String prefix = getPrefix(key);
        if (PREFIX_FILE.equals(prefix)) {
            var configFolder = System.getProperty(CONFIG_DIR);

            if (configFolder != null) {
                configFolder = configFolder.endsWith("/") ? configFolder : configFolder + "/";
                location = (configFolder + filename).replace("//", "/");
            }

            log.log(Level.INFO, "file location ({0}): {1}", new Object[]{key, location});
            return location;
        }

        throw new FileNotFoundException(filename);
    }

    protected String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    protected byte[] readFile(@NonNull String key) throws FileNotFoundException {
        final var filePath = getFileLocation(key);
        final var file = new File(filePath);
        byte[] result;

        try (FileInputStream inputStream = new FileInputStream(file)) {
            result = new byte[(int) file.length()];
            inputStream.read(result);
            return result;
        } catch (IOException ex) {
            log.log(Level.SEVERE, "An exception has occurred. Returning an empty byte array", ex);
            return new byte[]{};
        }
    }

    private String getPrefix(String key) {
        int index = key.indexOf('.');
        if (index > 0) {
            return key.substring(0, index + 1);
        }
        return null;
    }

    protected final void load(@NonNull String filename) throws FileNotFoundException, IOException {

        //var configFolder = System.getProperty(CONFIG_DIR);
        var configFolder = this.appConfig.getDefaultConfigFolder();
        log.log(Level.INFO, "loading configuration file: {0}", filename);
        log.log(Level.INFO, "Configuration folder location: {0}", configFolder);
        if (configFolder != null) {
            final var baseConfigFolder = appConfig.getDefaultBaseConfigFolder();
            final var configLocationFolder = new File(baseConfigFolder, configFolder);
            loadFromConfigDir(filename, configLocationFolder);
            return;
        }

        if (filename.startsWith("/")) {
            loadFromResourceStream(filename);
            return;
        }

        if (new File(filename).exists()) {
            properties.load(new FileInputStream(filename));
            return;
        }

        throw new FileNotFoundException(filename);
    }

    private void loadFromResourceStream(String filename) throws IOException {
        var inputStream = this.getClass().getResourceAsStream(filename);
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            final String fname = filename.substring(1);
            inputStream = this.getClass().getClassLoader().getResourceAsStream(fname);
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new IOException(String.format("Failed to load resource %s", fname));
            }
        }
    }

    private void loadFromConfigDir(String filename, File configFolder) throws IOException {
        log.log(Level.FINER, "loadFromConfigDir({0}, {1})", new Object[]{filename, configFolder});
        final String fname = filename.substring(1);
        var tmpFile = filename.startsWith("/") ? fname : filename;
        final File file = new File(configFolder, tmpFile);
        log.log(Level.INFO, "Configuration file {0}", file.getAbsoluteFile());
        if (file.exists()) {
            var inputStream = new FileInputStream(file);
            log.log(Level.INFO, "Loading configuration file from {0}", file.getParent());
            properties.load(inputStream);
        } else {
            log.log(Level.WARNING, "The file {0} does not exist", file.getAbsoluteFile());
        }
    }

}
