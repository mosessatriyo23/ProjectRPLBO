package com.rplbo.ukdw.todolistfix.dao;

import com.rplbo.ukdw.todolistfix.model.Kategori;
import java.sql.SQLException;
import java.util.List;


public interface KategoriDao {
    boolean addKategori(Kategori kategori) throws SQLException;
    Kategori getKategoriById(Integer id) throws SQLException;
    List<Kategori> getAllKategori() throws SQLException;
    boolean updateKategori(Kategori kategori) throws SQLException;
    boolean deleteKategori(Integer id) throws SQLException;
    boolean deleteKategori(Integer id, int userId) throws SQLException;

    List<Kategori> getKategoriByUserId(int userId) throws SQLException;
    Kategori getKategoriByIdAndUserIdWithTasks(int kategoriId, int userId) throws SQLException;
}