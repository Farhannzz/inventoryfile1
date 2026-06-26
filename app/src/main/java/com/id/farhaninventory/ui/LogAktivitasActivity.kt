// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/ui/LogAktivitasActivity.kt
// ===================================================================
// MATERI PERTEMUAN 16: Demonstrasi MEMBACA file dari Internal Storage
package com.id.farhaninventory.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.id.farhaninventory.databinding.ActivityLogAktivitasBinding
import com.id.farhaninventory.util.InternalStorageHelper

/**
 * Halaman yang menampilkan riwayat aktivitas Teknisi (tambah/edit/hapus data alat).
 * Seluruh data di halaman ini DIBACA LANGSUNG dari file .txt yang tersimpan
 * di internal storage aplikasi -> bukan dari Room Database.
 */
class LogAktivitasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogAktivitasBinding
    private lateinit var logAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogAktivitasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        muatDataLog()

        binding.btnHapusLog.setOnClickListener {
            konfirmasiHapusLog()
        }
    }

    private fun setupRecyclerView() {
        logAdapter = LogAdapter(emptyList())
        binding.recyclerViewLog.apply {
            layoutManager = LinearLayoutManager(this@LogAktivitasActivity)
            adapter = logAdapter
        }
    }

    /**
     * Membaca ulang file log dari internal storage setiap kali halaman ini dibuka,
     * supaya data yang ditampilkan selalu yang terbaru (misal setelah teknisi
     * baru saja menambah data alat dari FormActivity).
     */
    private fun muatDataLog() {
        val daftarLog = InternalStorageHelper.bacaSemuaLogAktivitas(this)
        logAdapter.updateData(daftarLog)

        val isEmpty = daftarLog.isEmpty()
        binding.tvEmptyLog.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE
        binding.recyclerViewLog.visibility = if (isEmpty) android.view.View.GONE else android.view.View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Refresh data setiap kali Activity ini kembali terlihat (misal user balik dari FormActivity)
        muatDataLog()
    }

    private fun konfirmasiHapusLog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Log?")
            .setMessage("Seluruh riwayat aktivitas yang tersimpan di internal storage akan dihapus permanen. Lanjutkan?")
            .setPositiveButton("Hapus") { _, _ ->
                InternalStorageHelper.hapusSemuaLogAktivitas(this)
                muatDataLog()
                Toast.makeText(this, "Log aktivitas berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
