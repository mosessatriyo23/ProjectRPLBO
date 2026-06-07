package com.rplbo.ukdw.todolistfix.dao;

import com.rplbo.ukdw.todolistfix.model.Task;
import java.sql.SQLException;
import java.util.List;

public interface TaskDao {
    boolean addTask(Task task) throws SQLException;
    boolean deleteTask(int taskId) throws SQLException;
    boolean deleteTask(int taskId, int userId) throws SQLException;
    boolean updateTask(Task task) throws SQLException;
    List<Task> getTaskByUser(int idUser) throws SQLException;
    List<Task> getAllTasks() throws SQLException;
    List<Task> getAllTasksByUserId(int userId) throws SQLException;
    List<Task> getTasksByKategoriIdAndUserId(int kategoriId, int userId) throws SQLException;
    boolean updateTaskProgress(int taskId, String newProgress, int userId) throws SQLException;
    int countTasksByProgress(int userId, String progressStatus) throws SQLException;
}
