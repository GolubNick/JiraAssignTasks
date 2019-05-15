package assignTasks.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetProperties {

    private static GetProperties instance = null;

    private InputStream inputStream;
    private Properties prop;
    private String propFileName;

    public static GetProperties getInstance(){
        if (instance == null){
            instance = new GetProperties().init();
        }
        return instance;
    }

    protected GetProperties init() {
        prop = new Properties();
        propFileName = "config.properties";
        inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        try {
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getProperties(String properties) {
        return prop.getProperty(properties);
    }
}
