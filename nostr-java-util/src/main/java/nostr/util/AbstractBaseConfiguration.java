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
 * @author squirrel
 */
@Data
@Log
public abstract class AbstractBaseConfiguration {

    protected final Properties properties = new Properties();
    protected final ApplicationConfiguration appConfig;

    private static final String PREFIX_FILE = "file.";
    private static final String CONFIG_DIR = "config.folder";

    public static final String CONFIG_TYPE_RELAY = "RELAY";
    public static final String CONFIG_TYPE_IDENTITY = "IDENTITY";

    protected AbstractBaseConfiguration(@NonNull String name, @NonNull String configType) throws IOException {
        this.appConfig = new ApplicationConfiguration();
        loadConfigFile(name, configType);
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

            log.log(Level.FINE, "file location ({0}): {1}", new Object[]{key, location});
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

    protected final void load(@NonNull String filename) throws IOException {

        log.log(Level.INFO, "Loading configuration file {0}...", filename);

        var configFolder = this.appConfig.getDefaultConfigFolder();

        log.log(Level.FINER, "loading configuration file: {0}", filename);
        log.log(Level.FINER, "Configuration folder location: {0}", configFolder);

        if (configFolder != null) {
            final var baseConfigFolder = appConfig.getDefaultBaseConfigFolder();
            final var configLocationFolder = new File(baseConfigFolder, configFolder);

            if (loadFromConfigDir(filename, configLocationFolder)) {
                return;
            }
        }

        if (filename.startsWith("/")) {
            if (loadFromResourceStream(filename)) {
                return;
            }
        }

        if (new File(filename).exists()) {
            properties.load(new FileInputStream(filename));
            log.log(Level.FINE, "Loaded {0}", filename);
            return;
        }

        throw new FileNotFoundException(filename);
    }

    private void loadConfigFile(@NonNull String name, @NonNull String config) throws IOException {
        String configFile = switch (config) {
            case CONFIG_TYPE_IDENTITY -> {
                yield name.isEmpty() ?
                        appConfig.getIdentityProperties() :
                        appConfig.getIdentityFolderProperties() + "/" + name + ".properties";
            }
            case CONFIG_TYPE_RELAY -> {
                // We assume the name is always empty!
                yield appConfig.getRelaysProperties();
            }
            default -> throw new RuntimeException("Invalid configuration type");
        };

        configFile = configFile.startsWith("/") ? configFile : "/" + configFile;

        load(configFile);
    }

    private boolean loadFromResourceStream(String filename) throws IOException {

        log.log(Level.FINE, "Attempting to load resource configuration file {0}...", new Object[]{filename});

        var inputStream = this.getClass().getResourceAsStream(filename);

        if (inputStream == null) {
            final String fname = filename.startsWith("/") ? filename.substring(1) : filename;
            inputStream = this.getClass().getClassLoader().getResourceAsStream(fname);
        }

        if (inputStream != null) {
            properties.load(inputStream);
            log.log(Level.FINE, "Resource configuration file {0} loaded!", new Object[]{filename});
            return true;
        } else {
            log.log(Level.WARNING, "Failed to load resource {0}", filename);
            return false;
        }
    }

/*
    private boolean loadFromResourceStream(String filename) throws IOException {

        log.log(Level.FINE, "Attempting to load resource configuration file {0}...", new Object[]{filename});

        var inputStream = this.getClass().getResourceAsStream(filename);

        if (inputStream != null) {
            properties.load(inputStream);
            log.log(Level.FINE, "Resource configuration file {0} loaded!", new Object[]{filename});
            return true;
        } else {
            final String fname = filename.substring(1);
            inputStream = this.getClass().getClassLoader().getResourceAsStream(fname);
            if (inputStream != null) {
                properties.load(inputStream);
                log.log(Level.FINE, "Resource configuration file {0} loaded!", new Object[]{filename});
                return true;
            } else {
                log.log(Level.WARNING, "Failed to load resource {0}", fname);
                return false;
            }
        }
    }
*/

    private boolean loadFromConfigDir(String filename, File configFolder) throws IOException {
        log.log(Level.FINE, "Attempting to load configuration file {0} from {1}...", new Object[]{filename, configFolder});

        File file = new File(configFolder, filename.startsWith("/") ? filename.substring(1) : filename);

        log.log(Level.FINER, "Configuration file {0}", file.getAbsoluteFile());

        if (file.exists()) {
            try (var inputStream = new FileInputStream(file)) {
                properties.load(inputStream);
                log.log(Level.FINE, "{0} loaded!", filename);
                return true;
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to load configuration file {0}: {1}", new Object[]{filename, e.getMessage()});
                return false;
            }
        } else {
            log.log(Level.WARNING, "The file {0} does not exist", file.getAbsoluteFile());
            return false;
        }
    }

/*
    private boolean loadFromConfigDir(String filename, File configFolder) throws IOException {

        log.log(Level.FINE, "Attempting to load configuration file {0} from {1}...", new Object[]{filename, configFolder});

        final String fname = filename.substring(1);
        var tmpFile = filename.startsWith("/") ? fname : filename;
        final File file = new File(configFolder, tmpFile);

        log.log(Level.FINER, "Configuration file {0}", file.getAbsoluteFile());

        if (file.exists()) {
            var inputStream = new FileInputStream(file);
            properties.load(inputStream);
            log.log(Level.FINE, "{0} loaded!", filename);
            return true;
        } else {
            log.log(Level.WARNING, "The file {0} does not exist", file.getAbsoluteFile());
            return false;
        }
    }
*/

}
