package com.rplbo.ukdw.todolistprojek;

public class User {
    private int no;
    private String deskripsi;
    private String judulTugas;
    private String status;

    public User(int no, String deskripsi, String judulTugas, String status) {
        this.no = no;
        this.deskripsi = deskripsi;
        this.judulTugas = judulTugas;
        this.status = status;
    }

    public int getNo() {
        return no;
    }

    public String getDeskripsi() { return deskripsi; }

    public String getJudulTugas() {
        return judulTugas;
    }

    public String getStatus() {
        return status;
    }
}
