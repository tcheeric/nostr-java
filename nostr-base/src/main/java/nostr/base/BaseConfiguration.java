package nostr.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.Data;

/**
 *
 * @author squirrel
 */
@Data
public class BaseConfiguration {

    protected final Properties properties = new Properties();

    protected BaseConfiguration(String file) throws IOException {
        String strFile = file.startsWith("/") ? file : "/" + file;
        InputStream inputStream = this.getClass().getResourceAsStream(strFile);
        if (inputStream == null) {
            inputStream = new FileInputStream(file);
        }
        this.properties.load(inputStream);
    }

    protected String getProperty(String key) {
        return this.properties.getProperty(key);
    }

}
