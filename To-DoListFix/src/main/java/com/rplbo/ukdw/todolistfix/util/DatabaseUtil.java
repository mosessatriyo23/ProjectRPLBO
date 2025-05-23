package com.rplbo.ukdw.todolistfix.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:UserDB.sqlite";

    // Static block to ensure driver is loaded once when class is initialized
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC driver successfully loaded");
        } catch (ClassNotFoundException e) {
            System.err.println("FATAL ERROR: SQLite JDBC driver not found");
            System.exit(1); // Exit if driver not available
        }
    }

    public static Connection getConnection() throws SQLException {
        // Now we can skip the Class.forName check here since it's done in static block
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable foreign key support (good practice)
            stmt.execute("PRAGMA foreign_keys = ON");

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL)");

            System.out.println("SQLite database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            // Consider throwing a runtime exception if initialization is critical
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
