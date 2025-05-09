package com.rplbo.ukdw.todolistprojek;

public class UserKategori {
    private String deskripsi;
    private String namaKategori;
    private int no;
    private String daftarTugas;

    public UserKategori(String deskripsi, String namaKategori, Integer no, String daftarTugas) {
        this.deskripsi = deskripsi;
        this.namaKategori = namaKategori;
        this.no = no;
        this.daftarTugas = daftarTugas;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public int getNo() {
        return no;
    }

    public String getDaftarTugas() {return daftarTugas;}
}
