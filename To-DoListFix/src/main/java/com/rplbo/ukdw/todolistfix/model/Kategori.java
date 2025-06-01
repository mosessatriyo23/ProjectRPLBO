package com.rplbo.ukdw.todolistfix.model;

import java.util.ArrayList;
import java.util.List;

public class Kategori {
    private int id;
    private String namaKategori;
    private String deskripsi;
    private int userId;
    private List<Task> daftarTugas;

    public Kategori(int id, String namaKategori, String deskripsi, int userId) {
        this.id = id;
        this.namaKategori = namaKategori;
        this.deskripsi = deskripsi;
        this.userId = userId;
        this.daftarTugas = new ArrayList<>();
    }

    public Kategori(String namaKategori, String deskripsi, int userId) {
        this.namaKategori = namaKategori;
        this.deskripsi = deskripsi;
        this.userId = userId;
    }


    public Kategori(int id, String namaKategori, String deskripsi) {
        this.id = id;
        this.namaKategori = namaKategori;
        this.deskripsi = deskripsi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public List<Task> getDaftarTugas() {
        return daftarTugas;
    }

    public void setDaftarTugas(List<Task> daftarTugas) {
        this.daftarTugas = daftarTugas;
    }

    @Override
    public String toString() {
        return namaKategori;
    }
}