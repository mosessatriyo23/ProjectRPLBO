package com.rplbo.ukdw.todolistfix.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class AiService {

    private static final String API_KEY = System.getenv("ANTHROPIC_API_KEY") != null
            ? System.getenv("ANTHROPIC_API_KEY")
            : "ISI_API_KEY_KAMU_DI_SINI";

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";

    public static String suggestTask(String judul, String deskripsi) {
        String prompt = "Kamu adalah asisten produktivitas cerdas. " +
                "Berdasarkan tugas berikut, berikan saran singkat (2-3 kalimat) tentang: " +
                "1) Cara mengerjakan tugas ini secara efektif, " +
                "2) Estimasi waktu yang dibutuhkan, " +
                "3) Tips khusus jika ada. " +
                "Tugas: \"" + judul + "\". " +
                (deskripsi != null && !deskripsi.isEmpty() ? "Deskripsi: \"" + deskripsi + "\"." : "") +
                " Jawab dalam Bahasa Indonesia, singkat dan actionable.";
        return callClaude(prompt);
    }

    public static String suggestPriority(String judul, String deskripsi, String deadline) {
        String prompt = "Kamu adalah asisten produktivitas. " +
                "Berdasarkan tugas ini, tentukan apakah perlu ditandai sebagai PRIORITAS. " +
                "Jawab dengan format: [PRIORITAS/TIDAK PRIORITAS] - alasan singkat (1 kalimat). " +
                "Tugas: \"" + judul + "\". " +
                (deskripsi != null && !deskripsi.isEmpty() ? "Deskripsi: \"" + deskripsi + "\". " : "") +
                (deadline != null && !deadline.isEmpty() ? "Deadline: " + deadline + "." : "") +
                " Jawab dalam Bahasa Indonesia.";
        return callClaude(prompt);
    }

    public static String suggestKategoriDescription(String namaKategori) {
        String prompt = "Kamu adalah asisten produktivitas. " +
                "Buatkan deskripsi singkat (1-2 kalimat) yang menarik dan informatif untuk kategori tugas bernama: " +
                "\"" + namaKategori + "\". " +
                "Jawab dalam Bahasa Indonesia, langsung ke deskripsinya saja tanpa awalan.";
        return callClaude(prompt);
    }

    public static String improveKategoriDescription(String namaKategori, String deskripsiSaatIni) {
        String prompt = "Kamu adalah asisten produktivitas. " +
                "Perbaiki atau lengkapi deskripsi kategori tugas berikut agar lebih informatif dan menarik. " +
                "Nama Kategori: \"" + namaKategori + "\". " +
                "Deskripsi saat ini: \"" + (deskripsiSaatIni != null ? deskripsiSaatIni : "") + "\". " +
                "Berikan deskripsi yang lebih baik (1-2 kalimat). " +
                "Jawab dalam Bahasa Indonesia, langsung ke deskripsinya saja.";
        return callClaude(prompt);
    }

    public static String analyzePriorityTasks(String tasksSummary) {
        String prompt = "Kamu adalah asisten produktivitas cerdas. " +
                "Analisis daftar tugas prioritas berikut dan berikan: " +
                "1) Ringkasan singkat (1 kalimat), " +
                "2) Tugas mana yang sebaiknya dikerjakan pertama dan alasannya, " +
                "3) Satu tips produktivitas yang relevan. " +
                "Daftar tugas prioritas: " + tasksSummary + ". " +
                "Jawab dalam Bahasa Indonesia, maksimal 4 kalimat total.";
        return callClaude(prompt);
    }

    private static String callClaude(String userMessage) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", API_KEY);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            String escapedMessage = userMessage
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");

            String jsonBody = "{"
                    + "\"model\":\"" + MODEL + "\","
                    + "\"max_tokens\":512,"
                    + "\"messages\":[{\"role\":\"user\",\"content\":\"" + escapedMessage + "\"}]"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            InputStream is = responseCode == 200 ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }

            if (responseCode != 200) {
                return "⚠️ Error API (" + responseCode + ")";
            }

            return extractTextFromResponse(response.toString());

        } catch (java.net.SocketTimeoutException e) {
            return "⚠️ Koneksi timeout. Periksa internet dan coba lagi.";
        } catch (IOException e) {
            return "⚠️ Gagal terhubung ke AI: " + e.getMessage();
        }
    }

    private static String extractTextFromResponse(String json) {
        try {
            int contentIdx = json.indexOf("\"content\"");
            if (contentIdx == -1) return "Tidak ada respons dari AI.";
            int textIdx = json.indexOf("\"text\"", contentIdx);
            if (textIdx == -1) return "Format respons tidak dikenali.";
            int startQuote = json.indexOf("\"", textIdx + 7);
            if (startQuote == -1) return "Format respons tidak dikenali.";

            StringBuilder sb = new StringBuilder();
            int i = startQuote + 1;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    if (next == '"') { sb.append('"'); i += 2; continue; }
                    if (next == 'n') { sb.append('\n'); i += 2; continue; }
                    if (next == 't') { sb.append('\t'); i += 2; continue; }
                    if (next == '\\') { sb.append('\\'); i += 2; continue; }
                    sb.append(next); i += 2; continue;
                }
                if (c == '"') break;
                sb.append(c);
                i++;
            }
            String result = sb.toString().trim();
            return result.isEmpty() ? "AI tidak memberikan respons." : result;
        } catch (Exception e) {
            return "⚠️ Gagal memproses respons AI.";
        }
    }
}