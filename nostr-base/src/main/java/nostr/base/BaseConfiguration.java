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
        
        InputStream inputStream = file.startsWith("/") ? this.getClass().getResourceAsStream(file) : new FileInputStream(file);
        
        if (inputStream == null) {
            throw new IOException(String.format("Failed to load properties file %s", file));
        }
        
        this.properties.load(inputStream);
    }

    protected String getProperty(String key) {
        return this.properties.getProperty(key);
    }

}
