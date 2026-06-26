// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/ui/CatatanActivity.kt
// ===================================================================
// MATERI PERTEMUAN 16: Demonstrasi MENULIS & MEMBACA file Internal Storage secara manual
package com.id.farhaninventory.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.id.farhaninventory.databinding.ActivityCatatanBinding
import com.id.farhaninventory.util.InternalStorageHelper

/**
 * Halaman Catatan Teknisi.
 * Mendemonstrasikan operasi READ & WRITE Internal Storage secara manual:
 * - Saat halaman dibuka -> baca isi file catatan_teknisi.txt (jika ada) dan tampilkan di EditText
 * - Saat tombol Simpan ditekan -> tulis ulang isi EditText ke file tersebut
 */
class CatatanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatatanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatatanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // --- READ: Muat catatan yang sudah tersimpan sebelumnya (jika ada) ---
        val catatanTersimpan = InternalStorageHelper.bacaCatatanTeknisi(this)
        binding.etCatatan.setText(catatanTersimpan)

        // --- WRITE: Simpan isi EditText ke internal storage saat tombol ditekan ---
        binding.btnSimpanCatatan.setOnClickListener {
            val isiCatatan = binding.etCatatan.text.toString()
            val berhasil = InternalStorageHelper.simpanCatatanTeknisi(this, isiCatatan)

            if (berhasil) {
                Toast.makeText(this, "Catatan berhasil disimpan ke internal storage", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menyimpan catatan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
