package fr.inria.robco.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Inserts or update a row into a table of the DB
 * <p>
 * Created by elmarce on 15/08/16.
 */
public class SQLiteConnector {

    private final String db;

    private Connection connection;

    private String table;

    private String sqlInsert = null;

    private String sqlUpdate = null;

    private String sqlRead = null;

    private String[] fieldNames;

    public SQLiteConnector(String db, String table, String... fieldNames) {
        this.fieldNames = fieldNames;
        this.table = table;
        this.db = db;
    }

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + db);
    }

    /**
     * Prepares an statement to send it to the DB
     */
    private PreparedStatement prepareStatement(String sql, Object... data) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        for (int i = 0; i < data.length; i++) {
            Object d = data[i];
            if (d instanceof Double) pstmt.setDouble(i + 1, (Double) d);
            if (d instanceof Long) pstmt.setLong(i + 1, (Long) d);
            if (d instanceof Integer) pstmt.setInt(i + 1, (Integer) d);
            if (d instanceof String) pstmt.setString(i + 1, (String) d);
        }
        return pstmt;
    }

    /**
     * Generates the SQL statements to write to the DB
     */
    private void prepareSQL() {
        if (sqlInsert == null) {
            StringBuilder sb = new StringBuilder("INSERT OR IGNORE INTO ").append(table).append("(");
            for (int i = 0; i < fieldNames.length - 1; i++) {
                sb.append(fieldNames[i]).append(",");
            }
            sb.append(fieldNames[fieldNames.length - 1]).append(") VALUES(");
            for (int i = 0; i < fieldNames.length - 1; i++) sb.append("?,");
            sb.append("?)");
            sqlInsert = sb.toString();
        }
        if (sqlUpdate == null) {
            StringBuilder sb = new StringBuilder("UPDATE ").append(table).append(" SET ");
            for (int i = 0; i < fieldNames.length - 2; i++) {
                sb.append(fieldNames[i]).append("= ?, ");
            }
            sb.append(fieldNames[fieldNames.length - 2]).append("=? WHERE ").append(fieldNames[fieldNames.length - 1]).append(" =?");
            sqlUpdate = sb.toString();
        }
        if (sqlRead == null) {
            StringBuilder sb = new StringBuilder("SELECT ID FROM ").append(table);
            sqlRead = sb.toString();
        }
    }

    public void write(Object... data) {
        try {
            if (connection == null) {
                prepareSQL();
                connect();
            }
            if (prepareStatement(sqlInsert, data).executeUpdate() == 0)
                prepareStatement(sqlUpdate, data).executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Integer> getIdList() {
        List<Integer> results = new ArrayList<Integer>();
        try {
            prepareSQL();
            connect();
            ResultSet rs = connection.createStatement().executeQuery("SELECT ID FROM " + table);
            while(rs.next()) {
                results.add(rs.getInt("ID"));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return results;
    }

    public void close() {
        if ( connection != null ) try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
