package nostr.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
public class BaseConfiguration {

    protected final Properties properties = new Properties();

    private static final String CONFIG_DIR = "nostr-java.config.folder";

    protected BaseConfiguration(String file) throws IOException {

        String configFolder = System.getProperty(CONFIG_DIR);
        InputStream inputStream = null;

        if (configFolder == null) {
            inputStream = file.startsWith("/") ? this.getClass().getResourceAsStream(file) : new FileInputStream(file);
        } else {
            String tmpFile = file.startsWith("/") ? file.substring(1) : file;
            inputStream = new FileInputStream(new File(new File(configFolder), tmpFile));
        }

        if (inputStream == null) {
            throw new IOException(String.format("Failed to load properties file %s", file));
        }

        this.properties.load(inputStream);
    }

    protected String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    protected byte[] getFileProperty(@NonNull String key) throws FileNotFoundException {
        if (key.startsWith("file://")) {
            String filePath = key.substring(7);
            File file = new File(filePath);
            byte[] result;

            try (FileInputStream inputStream = new FileInputStream(file)) {
                result = new byte[(int) file.length()];
                inputStream.read(result);
                return result;
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
                return new byte[]{};
            }

        }
        return new byte[]{};
    }

}
