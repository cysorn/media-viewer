package media_viewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseTestService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void testConnection() {
        String sql = "SELECT 1";
        int result = jdbcTemplate.queryForObject(sql, Integer.class);
        System.out.println("Database connection test result: " + result);
    }
}