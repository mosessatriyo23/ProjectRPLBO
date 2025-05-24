package com.rplbo.ukdw.todolistfix.dao;
import com.rplbo.ukdw.todolistfix.model.Task;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskDAOManager implements TaskDao {

    @Override
    public boolean addTask(Task task) {
        String sql = "INSERT INTO tasks (title, description, category, user_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getJudul());
            pstmt.setString(2, task.getDeskripsi());
            pstmt.setString(3, task.getKategori());
            pstmt.setInt(4, task.getIdUser());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Returns true if task was deleted
        }catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

   @Override
   public List<Task> getTaskByUser(int idUser) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description, category FROM tasks WHERE user_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUser);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setJudul(rs.getString("title"));
                task.setDeskripsi(rs.getString("description"));
                task.setKategori(rs.getString("category"));
                task.setIdUser(idUser); // Set the foreign key
                tasks.add(task);
            }
        }
        return tasks;
    }
   public boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, category = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){


        }
   }
    // Implement other methods...
}
