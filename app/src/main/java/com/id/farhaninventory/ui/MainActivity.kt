// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/ui/MainActivity.kt
// ===================================================================
package com.id.farhaninventory.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.id.farhaninventory.R
import com.id.farhaninventory.data.Equipment
import com.id.farhaninventory.databinding.ActivityMainBinding
import com.id.farhaninventory.databinding.DialogSetupProfilBinding
import com.id.farhaninventory.util.PdfExportHelper
import com.id.farhaninventory.util.PreferencesHelper
import java.io.File

/**
 * MainActivity: Dashboard utama aplikasi.
 *
 * Fitur di halaman ini:
 * - RecyclerView daftar alat (realtime, reaktif terhadap perubahan database)
 * - Spinner filter Faskes + EditText pencarian global (Nama Alat / QR Code)
 * - Hanya bisa diakses setelah Login berhasil (lihat LoginActivity)
 * - Export laporan ke PDF berdasarkan data yang sedang tampil (hasil filter)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: EquipmentAdapter

    // Menyimpan data hasil filter TERKINI yang tampil di RecyclerView,
    // dipakai sebagai sumber data saat export PDF ditekan.
    private var currentDisplayedList: List<Equipment> = emptyList()

    // Launcher untuk meminta izin notifikasi (Android 13+ / API 33+)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(
                    this,
                    "Izin notifikasi ditolak. Smart Alert alat rusak tidak akan muncul.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // Daftar Faskes untuk dialog setup profil (disamakan dengan master data di FormActivity)
    private val daftarFaskesProfil = listOf(
        "RSUD Kota Spektrum",
        "RS Spektrum Medika",
        "Puskesmas Spektrum Pusat",
        "Puskesmas Spektrum Timur",
        "Klinik Spektrum Sehat"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, MainViewModel.Factory(application))[MainViewModel::class.java]

        requestNotificationPermissionIfNeeded()
        setupRecyclerView()
        setupSearchEditText()
        observeViewModel()
        setupFabTambah()
        setupExportPdfButton()
        cekDanTampilkanSetupProfil()
        updateGreetingTeknisi()
    }

    // ===================================================================
    // MATERI PERTEMUAN 16: SHAREDPREFERENCES — SETUP PROFIL TEKNISI
    // ===================================================================

    /**
     * Mengecek apakah teknisi sudah pernah mengisi profil (tersimpan di SharedPreferences).
     * Jika BELUM PERNAH, tampilkan dialog setup profil sebelum aplikasi bisa dipakai.
     */
    private fun cekDanTampilkanSetupProfil() {
        if (!PreferencesHelper.isProfilSudahDiatur(this)) {
            tampilkanDialogSetupProfil()
        }
    }

    private fun tampilkanDialogSetupProfil() {
        val dialogBinding = DialogSetupProfilBinding.inflate(layoutInflater)

        val adapterFaskes = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, daftarFaskesProfil)
        dialogBinding.spinnerFaskesDialog.adapter = adapterFaskes

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false) // wajib isi profil dulu, tidak boleh ditutup dengan tap luar/back
            .setPositiveButton("Simpan", null) // listener di-override manual di bawah agar bisa validasi dulu
            .create()

        dialog.show()

        // Override klik tombol positive secara manual supaya dialog TIDAK otomatis tertutup
        // jika validasi nama teknisi masih kosong.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nama = dialogBinding.etNamaTeknisiDialog.text.toString().trim()
            val faskesTerpilih = dialogBinding.spinnerFaskesDialog.selectedItem?.toString() ?: ""

            if (nama.isBlank()) {
                Toast.makeText(this, "Nama Teknisi wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Simpan profil ke SharedPreferences ---
            PreferencesHelper.saveProfilTeknisi(this, nama, faskesTerpilih)
            Toast.makeText(this, "Profil tersimpan. Selamat bekerja, $nama!", Toast.LENGTH_SHORT).show()
            updateGreetingTeknisi()
            dialog.dismiss()
        }
    }

    /** Menampilkan nama teknisi yang sedang login di Toolbar, dibaca dari SharedPreferences */
    private fun updateGreetingTeknisi() {
        val nama = PreferencesHelper.getNamaTeknisi(this)
        binding.toolbar.subtitle = "Teknisi: $nama"
    }

    // ===================================================================
    // MENU TOOLBAR: AKSES KE LOG AKTIVITAS & CATATAN TEKNISI (Internal Storage)
    // ===================================================================

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_log_aktivitas -> {
                startActivity(Intent(this, LogAktivitasActivity::class.java))
                true
            }
            R.id.menu_catatan -> {
                startActivity(Intent(this, CatatanActivity::class.java))
                true
            }
            R.id.menu_edit_profil -> {
                tampilkanDialogSetupProfil()
                true
            }
            R.id.menu_logout -> {
                konfirmasiLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** Menampilkan dialog konfirmasi sebelum kembali ke layar Login. */
    private fun konfirmasiLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout?")
            .setMessage("Anda akan keluar dan kembali ke layar Login.")
            .setPositiveButton("Logout") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /**
     * Android 13+ mewajibkan permintaan izin runtime untuk menampilkan notifikasi.
     * Tanpa ini, fitur Smart Alert (Fitur #4) tidak akan tampil sama sekali di Android 13+.
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // ===================================================================
    // FITUR 2: DASHBOARD LIST & FILTER
    // ===================================================================

    private fun setupRecyclerView() {
        adapter = EquipmentAdapter(
            onItemClick = { equipment ->
                // Klik item -> langsung buka Form Edit (gerbang keamanan sudah di Login awal)
                openFormActivity(equipment.id)
            },
            onDeleteClick = { equipment ->
                confirmAndDelete(equipment)
            }
        )
        binding.recyclerViewEquipment.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
    }

    /**
     * Setup EditText pencarian global. Menggunakan TextWatcher agar filter
     * berjalan REALTIME setiap kali user mengetik (sesuai requirement "secara real-time").
     */
    private fun setupSearchEditText() {
        binding.etSearchGlobal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchKeyword(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        // Observasi daftar nama Faskes untuk mengisi Spinner filter (otomatis update jika ada faskes baru)
        viewModel.allFaskesNames.observe(this) { faskesList ->
            val finalList = mutableListOf("Semua Faskes")
            finalList.addAll(faskesList)
            val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, finalList)
            binding.spinnerFilterFaskes.adapter = adapterSpinner

            // MATERI PERTEMUAN 16: set posisi Spinner sesuai filter terakhir yang tersimpan
            // di SharedPreferences, SEBELUM listener dipasang -> supaya tidak ter-overwrite
            // balik ke "Semua Faskes" oleh listener saat Spinner baru pertama kali di-render.
            val filterTersimpan = viewModel.getCurrentFaskesFilter()
            val posisiTersimpan = finalList.indexOf(filterTersimpan)
            if (posisiTersimpan >= 0) {
                binding.spinnerFilterFaskes.setSelection(posisiTersimpan, false)
            }

            binding.spinnerFilterFaskes.onItemSelectedListener =
                object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long
                    ) {
                        viewModel.setFaskesFilter(finalList[position])
                    }
                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                }
        }

        // Observasi hasil list yang sudah terfilter -> langsung tampil ke RecyclerView
        viewModel.filteredEquipmentList.observe(this) { list ->
            currentDisplayedList = list
            adapter.submitList(list)

            // Tampilkan empty state jika data kosong, sembunyikan jika ada data
            binding.tvEmptyState.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            binding.recyclerViewEquipment.visibility = if (list.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        }
    }

    // ===================================================================
    // FITUR 3: AKSES FORM / HAPUS DATA (gerbang keamanan sudah ditangani Login)
    // ===================================================================

    private fun setupFabTambah() {
        binding.fabTambah.setOnClickListener {
            // Tombol Tambah Data Baru -> langsung buka FormActivity
            openFormActivity(equipmentId = 0)
        }
    }

    /**
     * Membuka FormActivity.
     * @param equipmentId 0 = mode tambah baru, > 0 = mode edit data dengan id tersebut.
     */
    private fun openFormActivity(equipmentId: Int) {
        val intent = Intent(this, FormActivity::class.java)
        if (equipmentId != 0) {
            intent.putExtra(FormActivity.EXTRA_EQUIPMENT_ID, equipmentId)
        }
        startActivity(intent)
    }

    /**
     * Menampilkan dialog konfirmasi sebelum benar-benar menghapus data alat.
     */
    private fun confirmAndDelete(equipment: Equipment) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data Alat?")
            .setMessage("Data '${equipment.namaAlat}' (QR: ${equipment.qrCode}) akan dihapus permanen.")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteEquipment(equipment)
                Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ===================================================================
    // FITUR 5: EXPORT LAPORAN PDF
    // ===================================================================

    private fun setupExportPdfButton() {
        binding.btnExportPdf.setOnClickListener {
            if (currentDisplayedList.isEmpty()) {
                Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            exportCurrentListToPdf()
        }
    }

    /**
     * Mengonversi data yang SEDANG TAMPIL (sudah sesuai filter aktif) menjadi file PDF,
     * lalu menawarkan opsi untuk membuka file tersebut menggunakan aplikasi PDF viewer.
     */
    private fun exportCurrentListToPdf() {
        try {
            val faskesFilterLabel = viewModel.getCurrentFaskesFilter()
            val pdfFile: File = PdfExportHelper.generateInventoryPdf(
                context = this,
                dataList = currentDisplayedList,
                namaFaskesFilter = faskesFilterLabel
            )

            Toast.makeText(this, "PDF berhasil disimpan: ${pdfFile.name}", Toast.LENGTH_LONG).show()
            offerToOpenPdf(pdfFile)
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    /**
     * Menawarkan dialog untuk membuka file PDF yang baru dibuat menggunakan aplikasi
     * PDF viewer eksternal (via FileProvider, karena file berada di internal storage).
     *
     * PENTING: Tambahkan FileProvider di AndroidManifest.xml & res/xml/file_paths.xml
     * (lihat file terpisah yang disertakan).
     */
    private fun offerToOpenPdf(pdfFile: File) {
        AlertDialog.Builder(this)
            .setTitle("Ekspor Berhasil")
            .setMessage("Laporan PDF telah disimpan di penyimpanan internal aplikasi. Buka sekarang?")
            .setPositiveButton("Buka") { _, _ ->
                try {
                    val uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        pdfFile
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Tidak ada aplikasi PDF viewer terpasang di perangkat ini",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("Tutup", null)
            .show()
    }
}
