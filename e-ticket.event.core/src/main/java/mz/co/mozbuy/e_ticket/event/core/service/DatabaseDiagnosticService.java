package mz.co.mozbuy.e_ticket.event.core.service;

import javax.sql.DataSource;
import java.sql.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseDiagnosticService {

    @Autowired
    private DataSource dataSource;

    public void testJdbcDirect() {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM e_ticket.events")) {

            int count = 0;
            while (rs.next()) {
                count++;
            }

            System.out.println("✅ TOTAL EVENTS = " + count);

        } catch (Exception e) {
            System.err.println("❌ ERRO JDBC DIRETO");
            e.printStackTrace();
        }
    }
}
