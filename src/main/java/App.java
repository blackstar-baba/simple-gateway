import bean.Config;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import utils.ConfigUtil;
import utils.JdbcUtil;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class App {


    private static final String TS_PROPERTY="ts";

    private static final String IMAGE_COLUMN_TYPE="[B";

    public static void main(String[] args) {

        //get config
        Config config;
        if(args.length == 0) {
            System.out.println("没有指定配置文件");
            return;
        }
        config = ConfigUtil.getConfig(args[0]);
        if(config == null){
            System.out.println("没有找到有效的配置文件");
            return;
        }
        //http client
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //cps http api url
        String requestUrl = config.getCpsHttpApi().replace("{deviceToken}",config.getDeviceToken());
        System.out.println("数据地址："+requestUrl);
        //get connection
        Connection connection = JdbcUtil.getConnection(config.getDriverName(), config.getUrl(), config.getUsername(), config.getPassword());
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(config.getQuerySql());
            preparedStatement.setFetchSize(config.getQueryLimit());
            ResultSet resultSet = preparedStatement.executeQuery();

            ResultSetMetaData metaData = resultSet.getMetaData();
            int tsColumnIndex = checkTs(metaData,config.getTsColumnName());
            boolean hasTs = tsColumnIndex>0;
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                long ts = -1;
                if(hasTs){
                    ts = resultSet.getDate(tsColumnIndex).getTime();
                }
                for (int i = 1; i <= columnCount; i++) {
                    //skip ts column
                    if(columnCount == tsColumnIndex){
                        continue;
                    }
                    String columnName = metaData.getColumnLabel(i);
                    String columnClassName = metaData.getColumnClassName(i);
                    Object value;
                    if(columnClassName.equals(IMAGE_COLUMN_TYPE)){
                        value = processByteArray(resultSet.getBinaryStream(i));
                    }else{
                        value = resultSet.getObject(i);
                    }
                    if(value == null){
                        continue;
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(columnName,value);
                    if(hasTs){
                        jsonObject.put(TS_PROPERTY,ts);
                    }
                    writeToCPS(httpClient,requestUrl,jsonObject.toJSONString());

                }

            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JdbcUtil.close(connection);


    }

    private static int checkTs(ResultSetMetaData metaData,String tsColumnName) {
        try {
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                if(columnName.equals(tsColumnName)){
                    return i;
                }
            }
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;

    }

    private static void writeToCPS(HttpClient httpClient,String requestUrl, String jsonStr) {
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader("Content-Type","application/json");
        httpPost.setEntity(new StringEntity(jsonStr, "UTF-8"));

        try {
            HttpResponse response = httpClient.execute(httpPost);
            System.out.println("写入数据："+jsonStr+"，响应状态：" + response.getStatusLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Object processByteArray(InputStream inputStream) {
        if(inputStream == null){
            return null;
        }
        StringBuilder sb = new StringBuilder();

        try {
            //available may be has bug
            byte[] cache = new byte[inputStream.available()];
            inputStream.read(cache);
            //use base64
            byte[] bytes = Base64.encodeBase64(cache);
            sb.append(new String(bytes,"UTF-8"));
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();


    }


}
