package bean;


import lombok.Data;

@Data
public class Config {


    private String driverName;

    private String url;

    private String username;

    private String password;

    private String querySql;

    private String tsColumnName;

    private int queryLimit;

    private String deviceToken;

    private String cpsHttpApi;



}
