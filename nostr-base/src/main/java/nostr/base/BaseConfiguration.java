package nostr.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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

    protected BaseConfiguration(@NonNull String file) throws IOException {
        load(file);
    }

    protected String getFileLocation(String key) throws FileNotFoundException {

        String prefix = getPrefix(key);
        final String filename = properties.getProperty(key);

        if (PREFIX_FILE.equals(prefix)) {
            var configFolder = System.getProperty(CONFIG_DIR);

            if (configFolder != null) {
                configFolder = configFolder.endsWith("/") ? configFolder : configFolder + "/";
                return (configFolder + filename).replace("//", "/");
            } else {
                return filename;
            }
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
            return key.substring(0, index);
        }
        return null;
    }

    private void load(@NonNull String filename) throws FileNotFoundException, IOException {

        var configFolder = System.getProperty(CONFIG_DIR);

        if (configFolder != null) {

            var tmpFile = filename.startsWith("/") ? filename.substring(1) : filename;

            final File file = new File(new File(configFolder), tmpFile);
            if (file.exists()) {

                var inputStream = new FileInputStream(file);
                properties.load(inputStream);
                return;
            }
        }

        if (filename.startsWith("/")) {
            var inputStream = this.getClass().getResourceAsStream(filename);
            if (inputStream != null) {
                properties.load(inputStream);
            }
            return;
        }

        if (new File(filename).exists()) {
            properties.load(new FileInputStream(filename));
            return;
        }

        throw new FileNotFoundException(filename);
    }

}
