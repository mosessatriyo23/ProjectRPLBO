package com.rplbo.ukdw.todolistfix.model;
public class Task {
    private int id;
    private String judul;
    private String deskripsi;
    private String kategori;
    private boolean prioritas;
    private int idUser;


    // Constructors
    public Task() {}

    public Task(String judul, String deskripsi, String kategori, Boolean prioritas, int idUser) {
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.kategori = kategori;
        this.prioritas = prioritas;
        this.idUser = idUser;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public Boolean getPrioritas() { return prioritas; }
    public void setPrioritas(Boolean prioritas) { this.prioritas = prioritas; }
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
}
