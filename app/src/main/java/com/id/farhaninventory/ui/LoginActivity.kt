// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/ui/LoginActivity.kt
// ===================================================================
package com.id.farhaninventory.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.id.farhaninventory.databinding.ActivityLoginBinding
import com.id.farhaninventory.util.PreferencesHelper

/**
 * LoginActivity: layar PALING AWAL yang tampil saat aplikasi dibuka (launcher activity),
 * SEBELUM Dashboard (MainActivity) bisa diakses.
 *
 * Pengganti FITUR 3 (Biometrik Fingerprint/Face) -> diganti menjadi sistem
 * Login sederhana berbasis Username & Password, karena sensor sidik jari
 * tidak selalu terdeteksi stabil di semua perangkat.
 *
 * Logikanya:
 * - BELUM PERNAH ada akun tersimpan di perangkat ini -> tampil mode DAFTAR AKUN
 *   (Username, Password, Konfirmasi Password). Setelah berhasil daftar, teknisi
 *   otomatis langsung masuk ke Dashboard (tidak perlu login ulang saat itu juga).
 * - SUDAH ADA akun tersimpan -> tampil mode LOGIN (Username, Password).
 *   Wajib login ulang setiap kali aplikasi dibuka dari awal.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // true = mode Daftar Akun (belum ada akun tersimpan), false = mode Login
    private var isModeDaftar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tentukan mode tampilan berdasarkan status akun di SharedPreferences
        isModeDaftar = !PreferencesHelper.isAkunTerdaftar(this)
        setupTampilanSesuaiMode()

        binding.btnLoginSubmit.setOnClickListener {
            if (isModeDaftar) prosesDaftarAkun() else prosesLogin()
        }
    }

    /** Mengatur judul, subjudul, field konfirmasi, dan teks tombol sesuai mode. */
    private fun setupTampilanSesuaiMode() {
        if (isModeDaftar) {
            binding.tvLoginTitle.text = "Buat Akun Teknisi"
            binding.tvLoginSubtitle.text =
                "Ini pertama kalinya Anda memakai aplikasi ini. Buat username & password " +
                "sendiri sebagai gerbang keamanan sebelum mengakses data inventory."
            binding.tvLabelKonfirmasi.visibility = android.view.View.VISIBLE
            binding.llKonfirmasiPassword.visibility = android.view.View.VISIBLE
            binding.btnLoginSubmit.text = "Daftar & Mulai"
        } else {
            binding.tvLoginTitle.text = "Masuk ke Aplikasi"
            binding.tvLoginSubtitle.text = "Masukkan username dan password Anda untuk melanjutkan."
            binding.tvLabelKonfirmasi.visibility = android.view.View.GONE
            binding.llKonfirmasiPassword.visibility = android.view.View.GONE
            binding.btnLoginSubmit.text = "Masuk"
        }
    }

    /** Validasi & simpan akun baru, lalu langsung lanjut ke Dashboard. */
    private fun prosesDaftarAkun() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val konfirmasi = binding.etKonfirmasiPassword.text.toString()

        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Username dan password wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 4) {
            Toast.makeText(this, "Password minimal 4 karakter", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != konfirmasi) {
            Toast.makeText(this, "Konfirmasi password tidak sama", Toast.LENGTH_SHORT).show()
            return
        }

        PreferencesHelper.registerAkun(this, username, password)
        Toast.makeText(this, "Akun berhasil dibuat. Selamat bekerja, $username!", Toast.LENGTH_SHORT).show()
        lanjutKeDashboard()
    }

    /** Validasi username & password yang diketik dengan yang tersimpan. */
    private fun prosesLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Username dan password wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (PreferencesHelper.cekLogin(this, username, password)) {
            lanjutKeDashboard()
        } else {
            Toast.makeText(this, "Username atau password salah", Toast.LENGTH_SHORT).show()
        }
    }

    /** Buka MainActivity (Dashboard) dan tutup LoginActivity agar tombol Back tidak kembali ke sini. */
    private fun lanjutKeDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
