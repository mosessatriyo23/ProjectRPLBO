package com.rplbo.ukdw.todolistfix.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:UserDB.sqlite";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC driver successfully loaded");
        } catch (ClassNotFoundException e) {
            System.err.println("FATAL ERROR: SQLite JDBC driver not found");
            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL)");

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS kategori (" +
                            "  id             INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "  nama_kategori  TEXT    NOT NULL, " +
                            "  deskripsi      TEXT, " +
                            "  user_id        INTEGER NOT NULL, " +
                            "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                            ")"
            );

// ===== TABEL TASK =====
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS task (" +
                            "  id          INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "  judul       TEXT    NOT NULL, " +
                            "  deskripsi   TEXT, " +
                            "  kategori_id INTEGER, " +
                            "  prioritas   BOOLEAN NOT NULL DEFAULT 0, " +
                            "  idUser      INTEGER NOT NULL, " +
                            "  deadline    TEXT, " +
                            "  progress    TEXT    DEFAULT 'Belum Selesai', " +
                            "  FOREIGN KEY (kategori_id) REFERENCES kategori(id) ON DELETE SET NULL, " +
                            "  FOREIGN KEY (idUser)      REFERENCES users(id)    ON DELETE CASCADE" +
                            ")"
            );

            System.out.println("SQLite database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
