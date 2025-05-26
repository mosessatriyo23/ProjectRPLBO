package com.rplbo.ukdw.todolistfix.dao;
import com.rplbo.ukdw.todolistfix.model.Task;

import java.sql.SQLException;
import java.util.List;

public interface TaskDao {
    boolean addTask(Task task);
    boolean deleteTask(int taskId);
    boolean updateTask(Task task);
    List<Task>getTaskByUser(int idUser) throws SQLException;
}