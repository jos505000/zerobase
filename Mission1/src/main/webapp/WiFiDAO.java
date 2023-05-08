import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WiFiDAO {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/wifi";
    private static final String DB_USER = "test";
    private static final String DB_PASSWORD = "123123";

    public List<WiFi> findNearby(double lat, double lon) {
        List<WiFi> wifiList = new ArrayList<>();

        try {
            // 데이터베이스 연결 설정
            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            Connection conn = DriverManager.getConnection(DB_URL, props);

            // SQL 쿼리 실행
            String sql = "SELECT * FROM wifi ORDER BY ST_DISTANCE_SPHERE(POINT(?, ?), POINT(latitude, longitude)) LIMIT 20";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lon);
            ResultSet rs = pstmt.executeQuery();

            // 결과 처리
            while (rs.next()) {
                WiFi wifi = new WiFi();
                wifi.setId(rs.getLong("id"));
                wifi.setLatitude(rs.getDouble("latitude"));
                wifi.setLongitude(rs.getDouble("longitude"));
                wifi.setName(rs.getString("name"));
                wifi.setAddress(rs.getString("address"));
                wifiList.add(wifi);
            }

            // 데이터베이스 연결 해제
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            // 예외 처리
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
