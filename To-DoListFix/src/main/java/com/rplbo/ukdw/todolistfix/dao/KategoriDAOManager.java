package com.rplbo.ukdw.todolistfix.dao;

import com.rplbo.ukdw.todolistfix.model.Kategori;
import com.rplbo.ukdw.todolistfix.model.Task;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KategoriDAOManager implements KategoriDao {

    private TaskDao taskDao;

    public KategoriDAOManager() {
        this.taskDao = new TaskDAOManager();
    }

    @Override
    public boolean addKategori(Kategori kategori) throws SQLException {
        String sql = "INSERT INTO kategori (nama_kategori, deskripsi, user_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, kategori.getNamaKategori());
            pstmt.setString(2, kategori.getDeskripsi());
            pstmt.setInt(3, kategori.getUserId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        kategori.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public Kategori getKategoriById(Integer id) throws SQLException {
        String sql = "SELECT id, nama_kategori, deskripsi, user_id FROM kategori WHERE id = ?";
        Kategori kategori = null;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    kategori = new Kategori(
                            rs.getInt("id"),
                            rs.getString("nama_kategori"),
                            rs.getString("deskripsi"),
                            rs.getInt("user_id")
                    );
                }
            }
        }
        return kategori;
    }

    @Override
    public List<Kategori> getAllKategori() throws SQLException {
        String sql = "SELECT id, nama_kategori, deskripsi, user_id FROM kategori";
        List<Kategori> daftarKategori = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                daftarKategori.add(new Kategori(
                        rs.getInt("id"),
                        rs.getString("nama_kategori"),
                        rs.getString("deskripsi"),
                        rs.getInt("user_id")
                ));
            }
        }
        return daftarKategori;
    }

    @Override
    public boolean updateKategori(Kategori kategori) throws SQLException {
        // Nama kolom di DB: nama_kategori, deskripsi, id, user_id
        String sql = "UPDATE kategori SET nama_kategori = ?, deskripsi = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kategori.getNamaKategori());
            pstmt.setString(2, kategori.getDeskripsi());
            pstmt.setInt(3, kategori.getId());
            pstmt.setInt(4, kategori.getUserId());
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteKategori(Integer id) throws SQLException {
        int userId = SessionHelper.getUserId();
        if (userId == -1) {
            System.err.println("DAO: Tidak bisa menghapus kategori, user tidak login atau session tidak valid.");
            return false;
        }
        String sql = "DELETE FROM kategori WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

     @Override
     public boolean deleteKategori(Integer id, int userId) throws SQLException {
         String sql = "DELETE FROM kategori WHERE id = ? AND user_id = ?";
         try (Connection conn = DatabaseUtil.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, id);
             pstmt.setInt(2, userId);
             return pstmt.executeUpdate() > 0;
         }
     }


    @Override
    public List<Kategori> getKategoriByUserId(int userId) throws SQLException {
        List<Kategori> daftarKategoriHasil = new ArrayList<>();
        String sqlKategori = "SELECT id, nama_kategori, deskripsi, user_id FROM kategori WHERE user_id = ?";
        List<Task> semuaTugasPengguna;
        try {
            semuaTugasPengguna = taskDao.getTaskByUser(userId);
            if (semuaTugasPengguna == null) semuaTugasPengguna = Collections.emptyList();
        } catch (SQLException e) {
            System.err.println("DAO Kategori: Error mengambil tugas pengguna (userId: " + userId + "): " + e.getMessage());
            e.printStackTrace();
            semuaTugasPengguna = Collections.emptyList();
        }

        Map<Integer, List<Task>> tugasPerKategoriMap = semuaTugasPengguna.stream()
                .filter(task -> task.getKategoriId() != null)
                .collect(Collectors.groupingBy(Task::getKategoriId));

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmtKategori = conn.prepareStatement(sqlKategori)) {
            pstmtKategori.setInt(1, userId);
            try (ResultSet rsKategori = pstmtKategori.executeQuery()) {
                while (rsKategori.next()) {
                    int kategoriIdDb = rsKategori.getInt("id");
                    String namaKategoriDb = rsKategori.getString("nama_kategori");
                    String deskripsiDb = rsKategori.getString("deskripsi");
                    Kategori kategori = new Kategori(kategoriIdDb, namaKategoriDb, deskripsiDb, userId);
                    List<Task> tugasUntukKategoriIni = tugasPerKategoriMap.get(kategoriIdDb);
                    if (tugasUntukKategoriIni != null) {
                        kategori.setDaftarTugas(tugasUntukKategoriIni);
                    }
                    daftarKategoriHasil.add(kategori);
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO Kategori: SQLException saat mengambil kategori (userId: " + userId + ")");
            e.printStackTrace();
            throw e;
        }
        return daftarKategoriHasil;
    }

    @Override
    public Kategori getKategoriByIdAndUserIdWithTasks(int kategoriId, int userId) throws SQLException {
        String sql = "SELECT id, nama_kategori, deskripsi, user_id FROM kategori WHERE id = ? AND user_id = ?";
        Kategori kategori = null;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kategoriId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String nama = rs.getString("nama_kategori");
                String desc = rs.getString("deskripsi");
                kategori = new Kategori(kategoriId, nama, desc, userId);
                // taskDao akan menggunakan nama tabel dan kolom yang sudah dikoreksi
                List<Task> tugasUntukKategoriIni = this.taskDao.getTasksByKategoriIdAndUserId(kategoriId, userId);
                if (tugasUntukKategoriIni != null) {
                    kategori.setDaftarTugas(tugasUntukKategoriIni);
                }
            }
        }
        return kategori;
    }
}
