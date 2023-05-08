import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class mariadb {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/wifi";
    private static final String DB_USER = "test";
    private static final String DB_PASSWORD = "123123";
    private static final String API_URL = "https://openapi.seoul.go.kr:8088/4b6f4f4753656c7134385974707079/json/TbPublicWifiInfo/1/20/";

    public static void main(String[] args) {
        // HTTP 연결 설정
        try {
            URL apiUrl = new URL(API_URL);
            HttpURLConnection con = (HttpURLConnection) apiUrl.openConnection();
            con.setRequestMethod("GET");

            // 응답 처리
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // JSON 파싱
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(response.toString());
                JSONObject wifiInfo = (JSONObject) json.get("TbPublicWifiInfo");
                if (wifiInfo == null || wifiInfo.get("row") == null) {
                    System.out.println(".");
                    return;
                }

                JSONArray wifiList = (JSONArray) wifiInfo.get("row");
                if (wifiList == null) {
                    System.out.println("No wifi data.");
                    return;
                }
                
                

                // DB 연결
                Properties props = new Properties();
                props.setProperty("user", DB_USER);
                props.setProperty("password", DB_PASSWORD);
                Connection conn = DriverManager.getConnection(DB_URL, props);

                // 데이터 삽입
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO wifi_info VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                for (Object obj : wifiList) {
                    JSONObject wifi = (JSONObject) obj;
                    pstmt.setString(1, (String) wifi.getOrDefault("X_SWIFI_MGR_NO", null));
                    pstmt.setString(2, (String) wifi.getOrDefault("X_SWIFI_WRDOFC", null));
                    pstmt.setString(3, (String) wifi.getOrDefault("X_SWIFI_MAIN_NM", null));
                    pstmt.setString(4, (String) wifi.getOrDefault("X_SWIFI_ADRES1", null));
                    pstmt.setString(5, (String) wifi.getOrDefault("X_SWIFI_ADRES2", null));
                    pstmt.setString(6, (String) wifi.getOrDefault("X_SWIFI_INSTL_FLOOR", null));
                    pstmt.setString(7, (String) wifi.getOrDefault("X_SWIFI_INSTL_TY", null));
                    pstmt.setString(8, (String) wifi.getOrDefault("X_SWIFI_INSTL_MBY", null));
                    pstmt.setString(9, (String) wifi.getOrDefault("X_SWIFI_SVC_SE", null));
                    pstmt.setString(10, (String) wifi.getOrDefault("X_SWIFI_CMCWR", null));
                    pstmt.setString(11, (String) wifi.getOrDefault("X_SWIFI_CNSTC_YEAR", null));
                    pstmt.setString(12, (String) wifi.getOrDefault("X_SWIFI_INOUT_DOOR", null));
                    pstmt.setString(13, (String) wifi.getOrDefault("X_SWIFI_REMARS3", null));
                    pstmt.setString(14, (String) wifi.getOrDefault("LAT", null));
                    pstmt.setString(15, (String) wifi.getOrDefault("LNT", null));
                    pstmt.setString(16, (String) wifi.getOrDefault("WORK_DTTM", null));
                    pstmt.executeUpdate();
                    }
                // DB 연결 종료
                pstmt.close();
                conn.close();
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


