package com.chat.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/chat_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Database driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load database driver: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static int executeUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            int result = ps.executeUpdate();
            return result;
        } catch (SQLException e) {
            System.err.println("Failed to execute SQL: " + sql + " - " + e.getMessage());
            return 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public static List<String[]> executeQuery(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String[]> results = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            rs = ps.executeQuery();
            int columnCount = rs.getMetaData().getColumnCount();

            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getString(i + 1);
                }
                results.add(row);
            }
            return results;
        } catch (SQLException e) {
            System.err.println("Failed to execute query: " + sql + " - " + e.getMessage());
            return results;
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    private static void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Failed to close resources: " + e.getMessage());
        }
    }

    public static void shutdown() {
        System.out.println("Database utility shutdown");
    }
}