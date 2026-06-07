package com.rplbo.ukdw.todolistfix.model;

import java.time.LocalDateTime;

public class Task {
    private int id;
    private String judul;
    private String deskripsi;
    private Integer kategoriId;
    private String namaKategori;
    private boolean prioritas;
    private int idUser;
    private LocalDateTime deadline;
    private String progress;

    public Task() {
    }

    public Task(int id, String judul, String deskripsi, Integer kategoriId, String namaKategori,
                boolean prioritas, int idUser, LocalDateTime deadline, String progress) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.kategoriId = kategoriId;
        this.namaKategori = namaKategori;
        this.prioritas = prioritas;
        this.idUser = idUser;
        this.deadline = deadline;
        this.progress = progress;
    }

    public Task(String judul, String deskripsi, Integer kategoriId, boolean prioritas, int idUser, LocalDateTime deadline, String progress) {
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.kategoriId = kategoriId;
        this.prioritas = prioritas;
        this.idUser = idUser;
        this.deadline = deadline;
        this.progress = progress;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public Integer getKategoriId() { return kategoriId; }
    public void setKategoriId(Integer kategoriId) { this.kategoriId = kategoriId; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public boolean isPrioritas() { return prioritas; }
    public void setPrioritas(boolean prioritas) { this.prioritas = prioritas; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public String getProgress() { return progress; }
    public void setProgress(String progress) { this.progress = progress; }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", judul='" + judul + '\'' +
                ", deskripsi='" + deskripsi + '\'' +
                ", kategoriId=" + kategoriId +
                ", namaKategori='" + namaKategori + '\'' +
                ", prioritas=" + prioritas +
                ", idUser=" + idUser +
                ", deadline=" + deadline +
                ", progress='" + progress + '\'' +
                '}';
    }
}