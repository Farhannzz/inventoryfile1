// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/util/PreferencesHelper.kt
// ===================================================================
// MATERI PERTEMUAN 16: IMPLEMENTASI SHAREDPREFERENCES
//
// SharedPreferences dipakai untuk menyimpan data KECIL berbentuk key-value
// (bukan tabel data banyak seperti Room). Cocok untuk: status login,
// nama user, preferensi tampilan, dsb. Data ini PERSISTEN -> tetap ada
// walau aplikasi ditutup dan dibuka kembali, bahkan setelah HP di-restart.
// ===================================================================
package com.id.farhaninventory.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

/**
 * Helper class untuk mengelola SharedPreferences aplikasi.
 * Dipakai untuk 2 keperluan:
 * 1. PROFIL TEKNISI -> nama teknisi yang login & faskes default-nya
 * 2. PREFERENSI TAMPILAN -> filter faskes terakhir yang dipilih, supaya
 *    saat app dibuka lagi, dashboard langsung menampilkan filter yang sama.
 */
object PreferencesHelper {

    // Nama file SharedPreferences. Disimpan di:
    // /data/data/com.id.farhaninventory/shared_prefs/spektrum_prefs.xml
    private const val PREFS_NAME = "spektrum_prefs"

    // --- KEY untuk Profil Teknisi ---
    private const val KEY_NAMA_TEKNISI = "key_nama_teknisi"
    private const val KEY_FASKES_DEFAULT = "key_faskes_default"
    private const val KEY_SUDAH_SETUP_PROFIL = "key_sudah_setup_profil"

    // --- KEY untuk Preferensi Tampilan ---
    private const val KEY_FILTER_FASKES_TERAKHIR = "key_filter_faskes_terakhir"
    private const val KEY_MODE_TAMPILAN = "key_mode_tampilan" // "list" atau "grid"

    // --- KEY untuk Akun Login (pengganti Fitur 3: Biometrik) ---
    private const val KEY_USERNAME = "key_username"
    private const val KEY_PASSWORD_HASH = "key_password_hash"
    private const val KEY_AKUN_TERDAFTAR = "key_akun_terdaftar"

    /**
     * Mengambil instance SharedPreferences.
     * MODE_PRIVATE artinya file ini HANYA bisa diakses oleh aplikasi kita sendiri,
     * aplikasi lain tidak bisa membaca/menulis data ini -> aman untuk data profil.
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ===================================================================
    // BAGIAN 1: PROFIL TEKNISI
    // ===================================================================

    /**
     * Menyimpan profil teknisi (nama & faskes default) setelah setup awal.
     * Dipanggil dari ProfileSetupActivity / dialog setup profil.
     */
    fun saveProfilTeknisi(context: Context, namaTeknisi: String, faskesDefault: String) {
        getPrefs(context).edit().apply {
            putString(KEY_NAMA_TEKNISI, namaTeknisi)
            putString(KEY_FASKES_DEFAULT, faskesDefault)
            putBoolean(KEY_SUDAH_SETUP_PROFIL, true)
            apply() // apply() = simpan secara asynchronous di background thread (tidak blocking UI)
        }
    }

    fun getNamaTeknisi(context: Context): String {
        // Default "Teknisi" jika belum pernah diisi
        return getPrefs(context).getString(KEY_NAMA_TEKNISI, "Teknisi") ?: "Teknisi"
    }

    fun getFaskesDefault(context: Context): String {
        return getPrefs(context).getString(KEY_FASKES_DEFAULT, "") ?: ""
    }

    /** Mengecek apakah teknisi sudah pernah mengisi profil (untuk menentukan tampilkan dialog setup atau tidak) */
    fun isProfilSudahDiatur(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SUDAH_SETUP_PROFIL, false)
    }

    /** Menghapus profil teknisi (misal saat logout) */
    fun clearProfilTeknisi(context: Context) {
        getPrefs(context).edit().apply {
            remove(KEY_NAMA_TEKNISI)
            remove(KEY_FASKES_DEFAULT)
            putBoolean(KEY_SUDAH_SETUP_PROFIL, false)
            apply()
        }
    }

    // ===================================================================
    // BAGIAN 2: PREFERENSI TAMPILAN
    // ===================================================================

    /**
     * Menyimpan filter Faskes terakhir yang dipilih user di Dashboard.
     * Dipanggil setiap kali user mengganti pilihan Spinner filter di MainActivity,
     * sehingga saat app ditutup-buka lagi, filter yang sama otomatis aktif.
     */
    fun saveFilterFaskesTerakhir(context: Context, namaFaskes: String) {
        getPrefs(context).edit().putString(KEY_FILTER_FASKES_TERAKHIR, namaFaskes).apply()
    }

    fun getFilterFaskesTerakhir(context: Context): String {
        return getPrefs(context).getString(KEY_FILTER_FASKES_TERAKHIR, "Semua Faskes") ?: "Semua Faskes"
    }

    /** Menyimpan mode tampilan list yang dipilih user (opsional, untuk pengembangan lanjut) */
    fun saveModeTampilan(context: Context, mode: String) {
        getPrefs(context).edit().putString(KEY_MODE_TAMPILAN, mode).apply()
    }

    fun getModeTampilan(context: Context): String {
        return getPrefs(context).getString(KEY_MODE_TAMPILAN, "list") ?: "list"
    }

    // ===================================================================
    // BAGIAN 3: AKUN LOGIN (Pengganti Fitur 3: Biometrik)
    // ===================================================================
    // Sensor sidik jari tidak selalu terdeteksi stabil di semua perangkat,
    // sehingga gerbang keamanan diganti menjadi sistem Login sederhana
    // (Username & Password) yang dibuat sendiri oleh teknisi saat PERTAMA
    // KALI memakai aplikasi (mirip proses registrasi), lalu wajib login
    // ulang setiap kali aplikasi dibuka dari awal. Lihat LoginActivity.kt.

    /** Mengecek apakah sudah pernah ada akun yang didaftarkan di perangkat ini. */
    fun isAkunTerdaftar(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AKUN_TERDAFTAR, false)
    }

    /**
     * Mendaftarkan akun baru. Hanya dipanggil SEKALI, saat teknisi pertama kali
     * memakai aplikasi ini di perangkatnya. Password TIDAK disimpan dalam bentuk
     * teks polos, melainkan di-hash terlebih dahulu (SHA-256) sebelum disimpan.
     */
    fun registerAkun(context: Context, username: String, password: String) {
        getPrefs(context).edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD_HASH, hashPassword(password))
            putBoolean(KEY_AKUN_TERDAFTAR, true)
            apply()
        }
    }

    /** Mencocokkan username + password yang diketik user dengan yang tersimpan. */
    fun cekLogin(context: Context, username: String, password: String): Boolean {
        val prefs = getPrefs(context)
        val usernameTersimpan = prefs.getString(KEY_USERNAME, "") ?: ""
        val passwordHashTersimpan = prefs.getString(KEY_PASSWORD_HASH, "") ?: ""
        return username == usernameTersimpan && hashPassword(password) == passwordHashTersimpan
    }

    /** Username yang sedang login, dipakai juga sebagai default nama teknisi. */
    fun getUsername(context: Context): String {
        return getPrefs(context).getString(KEY_USERNAME, "Teknisi") ?: "Teknisi"
    }

    /** Mengubah password menjadi hash SHA-256 agar tidak tersimpan polos di SharedPreferences. */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
