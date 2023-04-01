package nostr.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import java.util.logging.Level;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Data
@Log
@NoArgsConstructor
public class BaseConfiguration {

    protected final Properties properties = new Properties();

    private static final String PREFIX_FILE = "file";
    private static final String CONFIG_DIR = "config.folder";

    protected BaseConfiguration(String file) throws IOException {

        var configFolder = System.getProperty(CONFIG_DIR);
        InputStream inputStream;

        if (configFolder == null) {
            inputStream = file.startsWith("/") ? this.getClass().getResourceAsStream(file) : new FileInputStream(file);
        } else {
            var tmpFile = file.startsWith("/") ? file.substring(1) : file;
            inputStream = new FileInputStream(new File(new File(configFolder), tmpFile));
        }

        this.properties.load(inputStream);
    }

    protected String getFileLocation(String key) throws FileNotFoundException {

        String location = null;
        String prefix = getPrefix(key);

        if (PREFIX_FILE.equals(prefix)) {
            var configFolder = System.getProperty(CONFIG_DIR);

            if (configFolder != null) {
                configFolder = configFolder.endsWith("/") ? configFolder : configFolder + "/";
                location = configFolder;
            }
        }

        return location;
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
            return key.substring(0, index - 1);
        }
        return null;
    }

}
