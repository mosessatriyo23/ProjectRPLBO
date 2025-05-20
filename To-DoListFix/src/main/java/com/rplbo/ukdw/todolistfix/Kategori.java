package com.rplbo.ukdw.todolistfix;

public class Kategori {
    private int no;
    private String nama;
    private String deskripsi;
    private String tugas;

    public Kategori(int no, String nama, String deskripsi, String tugas) {
        this.no = no;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.tugas = tugas;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getTugas() {
        return tugas;
    }

    public void setTugas(String tugas) {
        this.tugas = tugas;
    }
}


















//
//package com.rplbo.ukdw.todolistfix;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class Kategori {
//    private int no;
//    private String nama;
//    private String deskripsi;
//    private List<Tugas> daftarTugas;
//
//    public Kategori(int no, String nama, String deskripsi, List<Tugas> daftarTugas) {
//        this.no = no;
//        this.nama = nama;
//        this.deskripsi = deskripsi;
//        this.daftarTugas = daftarTugas;
//    }
//
//    public int getNo() {
//        return no;
//    }
//
//    public String getNama() {
//        return nama;
//    }
//
//    public String getDeskripsi() {
//        return deskripsi;
//    }
//
//    public List<Tugas> getDaftarTugas() {
//        return daftarTugas;
//    }
//
//    public void setDaftarTugas(List<Tugas> daftarTugas) {
//        this.daftarTugas = daftarTugas;
//    }
//
//    // Digunakan untuk ditampilkan di kolom tabel
//    public String getTugasFormatted() {
//        if (daftarTugas == null || daftarTugas.isEmpty()) return "-";
//        return daftarTugas.stream()
//                .map(Tugas::getJudul)
//                .collect(Collectors.joining(", "));
//    }
//}
