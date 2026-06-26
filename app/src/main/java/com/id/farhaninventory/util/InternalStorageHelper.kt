// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/util/InternalStorageHelper.kt
// ===================================================================
// MATERI PERTEMUAN 16: IMPLEMENTASI INTERNAL STORAGE
//
// Internal Storage dipakai untuk menyimpan FILE MENTAH (teks) langsung
// ke folder privat aplikasi: /data/data/<package>/files/
// Berbeda dengan Room (database terstruktur) dan SharedPreferences (key-value),
// di sini kita menulis & membaca file teks secara manual menggunakan
// openFileOutput() dan openFileInput() -> API bawaan Android (Context).
//
// Dipakai untuk 2 keperluan:
// 1. LOG AKTIVITAS -> otomatis tercatat setiap kali teknisi tambah/edit/hapus data alat
// 2. CATATAN MANUAL -> teknisi bisa menulis & membaca catatan/draft bebas
// ===================================================================
package com.id.farhaninventory.util

import android.content.Context
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InternalStorageHelper {

    // Nama file untuk log aktivitas (tersimpan di internal storage app)
    private const val FILE_LOG_AKTIVITAS = "log_aktivitas_teknisi.txt"

    // Nama file untuk catatan manual teknisi
    private const val FILE_CATATAN_TEKNISI = "catatan_teknisi.txt"

    // ===================================================================
    // BAGIAN 1: LOG AKTIVITAS TEKNISI (otomatis, append-only)
    // ===================================================================

    /**
     * Menambahkan satu baris log aktivitas baru ke file internal storage.
     * Dipanggil otomatis dari FormViewModel setiap kali teknisi:
     * TAMBAH data baru, UPDATE data, atau HAPUS data.
     *
     * Menggunakan Context.MODE_APPEND -> teks baru ditambahkan di akhir file,
     * BUKAN menimpa isi file yang sudah ada sebelumnya.
     */
    fun tambahLogAktivitas(context: Context, namaTeknisi: String, aksi: String, detailAlat: String) {
        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID")).format(Date())
        val barisLog = "[$timestamp] $namaTeknisi - $aksi - $detailAlat\n"

        try {
            // openFileOutput() otomatis menyimpan ke folder internal app (filesDir),
            // TIDAK butuh permission storage apapun karena ini area privat aplikasi.
            context.openFileOutput(FILE_LOG_AKTIVITAS, Context.MODE_APPEND).use { outputStream ->
                outputStream.write(barisLog.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Membaca seluruh isi log aktivitas dari internal storage.
     * Dipakai untuk menampilkan riwayat aktivitas di halaman "Log Aktivitas".
     * @return List of String, setiap elemen adalah satu baris log (terbaru di paling atas).
     */
    fun bacaSemuaLogAktivitas(context: Context): List<String> {
        val daftarLog = mutableListOf<String>()
        try {
            // openFileInput() membaca file dari internal storage. Jika file belum pernah
            // dibuat (belum ada aktivitas sama sekali), akan melempar FileNotFoundException.
            context.openFileInput(FILE_LOG_AKTIVITAS).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                    lines.forEach { daftarLog.add(it) }
                }
            }
        } catch (e: FileNotFoundException) {
            // Belum ada log sama sekali, ini bukan error -> kembalikan list kosong
            return emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Tampilkan dari yang TERBARU ke terlama, supaya aktivitas paling baru ada di atas
        return daftarLog.reversed()
    }

    /** Menghapus seluruh riwayat log aktivitas (misal untuk reset/maintenance) */
    fun hapusSemuaLogAktivitas(context: Context): Boolean {
        return context.deleteFile(FILE_LOG_AKTIVITAS)
    }

    // ===================================================================
    // BAGIAN 2: CATATAN MANUAL TEKNISI (read & write bebas)
    // ===================================================================

    /**
     * Menyimpan catatan teknisi. Menggunakan MODE_PRIVATE (bukan APPEND) karena
     * catatan ini sifatnya MENIMPA isi sebelumnya -> seperti notes/draft tunggal
     * yang terus di-edit, bukan log yang terus bertambah.
     */
    fun simpanCatatanTeknisi(context: Context, isiCatatan: String): Boolean {
        return try {
            context.openFileOutput(FILE_CATATAN_TEKNISI, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(isiCatatan.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Membaca isi catatan teknisi yang tersimpan.
     * @return isi catatan sebagai String, atau String kosong jika belum pernah menulis catatan.
     */
    fun bacaCatatanTeknisi(context: Context): String {
        return try {
            context.openFileInput(FILE_CATATAN_TEKNISI).use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: FileNotFoundException) {
            "" // Belum pernah menulis catatan, kembalikan string kosong
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /** Mengecek apakah file catatan sudah pernah dibuat sebelumnya */
    fun isCatatanAda(context: Context): Boolean {
        return context.getFileStreamPath(FILE_CATATAN_TEKNISI).exists()
    }
}
