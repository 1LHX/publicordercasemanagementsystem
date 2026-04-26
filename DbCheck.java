import java.sql.*;
public class DbCheck {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://localhost:3306/PublicOrderCases?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&connectionCollation=utf8mb4_general_ci&useSSL=false";
    try (Connection conn = DriverManager.getConnection(url, "root", "191621")) {
      try (Statement st = conn.createStatement()) {
        ResultSet rs = st.executeQuery("select count(*) total, sum(case when acceptance_time is not null then 1 else 0 end) accepted from cases");
        while (rs.next()) {
          System.out.println("total=" + rs.getLong("total") + ", accepted=" + rs.getLong("accepted"));
        }
        rs = st.executeQuery("select date_format(acceptance_time, '%Y-%m-%d') period, count(1) cnt from cases where acceptance_time is not null group by date_format(acceptance_time, '%Y-%m-%d') order by period");
        while (rs.next()) {
          System.out.println(rs.getString("period") + "=" + rs.getLong("cnt"));
        }
        rs = st.executeQuery("select date_format(acceptance_time, '%Y-%m') period, count(1) cnt from cases where acceptance_time is not null group by date_format(acceptance_time, '%Y-%m') order by period");
        while (rs.next()) {
          System.out.println("month " + rs.getString("period") + "=" + rs.getLong("cnt"));
        }
      }
    }
  }
}
