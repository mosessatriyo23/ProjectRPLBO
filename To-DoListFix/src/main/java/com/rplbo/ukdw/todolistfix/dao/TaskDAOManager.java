package com.rplbo.ukdw.todolistfix.dao;

import com.rplbo.ukdw.todolistfix.model.Task;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDAOManager implements TaskDao {

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setJudul(rs.getString("judul"));
        task.setDeskripsi(rs.getString("deskripsi"));
        int kategoriIdInt = rs.getInt("kategori_id");
        if (rs.wasNull()) {
            task.setKategoriId(null);
        } else {
            task.setKategoriId(kategoriIdInt);
        }
        if (hasColumn(rs, "namaKategoriDisplay")) {
            task.setNamaKategori(rs.getString("namaKategoriDisplay"));
        } else if (hasColumn(rs, "nama_kategori")) {
            task.setNamaKategori(rs.getString("nama_kategori"));
        }

        task.setPrioritas(rs.getBoolean("prioritas"));
        task.setIdUser(rs.getInt("idUser"));
        String deadlineStr = rs.getString("deadline");
        if (deadlineStr != null && !deadlineStr.isEmpty()) {
            try {
                task.setDeadline(LocalDateTime.parse(deadlineStr));
            } catch (java.time.format.DateTimeParseException e) {
                System.err.println("DAO Task: Error parsing deadline string dari DB: " + deadlineStr + " - " + e.getMessage());
                task.setDeadline(null);
            }
        } else {
            task.setDeadline(null);
        }
        task.setProgress(rs.getString("progress"));
        return task;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equalsIgnoreCase(rsmd.getColumnLabel(x))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addTask(Task task) throws SQLException {
        String sql = "INSERT INTO task (judul, deskripsi, kategori_id, prioritas, idUser, deadline, progress) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, task.getJudul());
            pstmt.setString(2, task.getDeskripsi());
            if (task.getKategoriId() != null) pstmt.setInt(3, task.getKategoriId());
            else pstmt.setNull(3, Types.INTEGER);
            pstmt.setBoolean(4, task.isPrioritas());
            pstmt.setInt(5, task.getIdUser());
            if (task.getDeadline() != null) pstmt.setString(6, task.getDeadline().toString());
            else pstmt.setNull(6, Types.VARCHAR);
            pstmt.setString(7, task.getProgress());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) task.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean deleteTask(int taskId) throws SQLException {
        System.out.println("[TaskDAOManager] Mencoba menghapus task ID: " + taskId + " (tanpa validasi user).");
        String sql = "DELETE FROM task WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[TaskDAOManager] Hasil delete task ID " + taskId + ": " + (affectedRows > 0));
            return affectedRows > 0;
        }
    }

    @Override
    public boolean deleteTask(int taskId, int userId) throws SQLException {
        System.out.println("[TaskDAOManager] Mencoba menghapus task ID: " + taskId + " untuk user ID: " + userId);
        String sql = "DELETE FROM task WHERE id = ? AND idUser = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[TaskDAOManager] Hasil delete task ID " + taskId + " untuk user ID " + userId + ": " + (affectedRows > 0));
            return affectedRows > 0;
        }
    }

    @Override
    public boolean updateTask(Task task) throws SQLException {
        String sql = "UPDATE task SET judul = ?, deskripsi = ?, kategori_id = ?, prioritas = ?, deadline = ?, progress = ? " +
                "WHERE id = ? AND idUser = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getJudul());
            pstmt.setString(2, task.getDeskripsi());
            if (task.getKategoriId() != null) pstmt.setInt(3, task.getKategoriId());
            else pstmt.setNull(3, Types.INTEGER);
            pstmt.setBoolean(4, task.isPrioritas());
            if (task.getDeadline() != null) pstmt.setString(5, task.getDeadline().toString());
            else pstmt.setNull(5, Types.VARCHAR);
            pstmt.setString(6, task.getProgress());
            pstmt.setInt(7, task.getId());
            pstmt.setInt(8, task.getIdUser());
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<Task> getTaskByUser(int idUser) throws SQLException {
        return getAllTasksByUserId(idUser);
    }

    @Override
    public List<Task> getAllTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.judul, t.deskripsi, t.kategori_id, k.nama_kategori AS namaKategoriDisplay, " +
                "t.prioritas, t.idUser, t.deadline, t.progress " +
                "FROM task t LEFT JOIN kategori k ON t.kategori_id = k.id";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        }
        return tasks;
    }

    @Override
    public List<Task> getAllTasksByUserId(int userId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.judul, t.deskripsi, t.kategori_id, k.nama_kategori AS namaKategoriDisplay, " +
                "t.prioritas, t.idUser, t.deadline, t.progress " +
                "FROM task t LEFT JOIN kategori k ON t.kategori_id = k.id " +
                "WHERE t.idUser = ? AND (t.progress != 'Selesai' OR t.progress IS NULL)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        return tasks;
    }

    @Override
    public List<Task> getTasksByKategoriIdAndUserId(int kategoriId, int userId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.judul, t.deskripsi, t.kategori_id, k.nama_kategori AS namaKategoriDisplay, " +
                "t.prioritas, t.idUser, t.deadline, t.progress " +
                "FROM task t LEFT JOIN kategori k ON t.kategori_id = k.id " +
                "WHERE t.kategori_id = ? AND t.idUser = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, kategoriId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        return tasks;
    }

    @Override
    public boolean updateTaskProgress(int taskId, String newProgress, int userId) throws SQLException {
        String sql = "UPDATE task SET progress = ? WHERE id = ? AND idUser = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newProgress);
            pstmt.setInt(2, taskId);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public int countTasksByProgress(int userId, String progressStatus) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM task WHERE idUser = ? AND progress = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, progressStatus);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }
}
