package utils;



import bean.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {




    public static Config getConfig(String configPath) {

        File file = new File(configPath);
        if(!file.exists()){
            return null;
        }
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
            Config config = new Config();
            config.setDriverName(properties.getProperty("driverName"));
            config.setUrl(properties.getProperty("url"));
            config.setUsername(properties.getProperty("username"));
            config.setPassword(properties.getProperty("password"));
            config.setQuerySql(properties.getProperty("querySql"));
            config.setQueryLimit(Integer.parseInt(properties.getProperty("queryLimit")));
            config.setDeviceToken(properties.getProperty("deviceToken"));
            config.setCpsHttpApi(properties.getProperty("cpsHttpApi"));
            config.setTsColumnName(properties.getProperty("tsColumnName"));
            return config;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


}
