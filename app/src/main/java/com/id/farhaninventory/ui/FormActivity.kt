// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/ui/FormActivity.kt
// ===================================================================
package com.id.farhaninventory.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.id.farhaninventory.databinding.ActivityFormBinding
import com.id.farhaninventory.data.Equipment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * FormActivity: Halaman Input/Edit data alat kesehatan sisi Teknisi.
 *
 * Komponen UI persis seperti sistem web:
 * - Spinner Pilih Faskes
 * - EditText QR Code
 * - DatePicker Tanggal Inventory
 * - EditText Nama Alat, Merk, Tipe Alat, Nama Ruang/Gedung, Lantai
 * - RadioGroup Kondisi Alat (Baik / Rusak / Butuh Kalibrasi)
 *
 * Activity ini dibuka dari MainActivity (lihat MainActivity.openFormActivity()),
 * yang hanya bisa diakses setelah Login berhasil.
 */
class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private lateinit var viewModel: FormViewModel

    // Menyimpan tanggal terpilih sebagai epoch millis (untuk disimpan ke Room)
    private var selectedDateMillis: Long = System.currentTimeMillis()

    // ID data yang sedang di-edit. 0 berarti mode TAMBAH BARU.
    private var editingId: Int = 0

    // Daftar Faskes contoh sesuai sistem web PT Spektrum.
    // Di implementasi produksi, idealnya daftar ini diambil dari API/master data,
    // namun untuk versi lokal ini kita definisikan statis sebagai master data dasar.
    private val daftarFaskes = listOf(
        "Pilih Faskes",
        "RSUD Kota Spektrum",
        "RS Spektrum Medika",
        "Puskesmas Spektrum Pusat",
        "Puskesmas Spektrum Timur",
        "Klinik Spektrum Sehat"
    )

    companion object {
        const val EXTRA_EQUIPMENT_ID = "extra_equipment_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            FormViewModel.Factory(application)
        )[FormViewModel::class.java]

        setupToolbar()
        setupSpinnerFaskes()
        setupDatePicker()
        setupSaveButton()
        observeViewModel()

        // Cek apakah Activity dibuka dalam mode EDIT (ada extra ID dikirim dari MainActivity)
        editingId = intent.getIntExtra(EXTRA_EQUIPMENT_ID, 0)
        if (editingId != 0) {
            binding.toolbar.title = "Edit Data Alat"
            viewModel.loadEquipmentForEdit(editingId)
        } else {
            binding.toolbar.title = "Input Data Alat Baru"
            // Set tanggal default = hari ini saat mode tambah baru
            updateDateDisplay(selectedDateMillis)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    /**
     * Setup Spinner "Pilih Faskes" menggunakan ArrayAdapter sederhana,
     * sesuai komponen UI yang ada di sistem web.
     */
    private fun setupSpinnerFaskes() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, daftarFaskes)
        binding.spinnerFaskes.adapter = adapter
    }

    /**
     * Setup DatePicker untuk Tanggal Inventory.
     * Saat EditText tanggal di-klik, muncul DatePickerDialog native Android.
     */
    private fun setupDatePicker() {
        binding.etTanggalInventory.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDateMillis

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0)
                    selectedDateMillis = selectedCalendar.timeInMillis
                    updateDateDisplay(selectedDateMillis)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            // Tanggal inventory tidak boleh melebihi hari ini (logis untuk pencatatan inventory)
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    /** Menampilkan tanggal terpilih ke EditText dalam format yang mudah dibaca: dd MMMM yyyy */
    private fun updateDateDisplay(millis: Long) {
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        binding.etTanggalInventory.setText(formatter.format(millis))
    }

    /**
     * Setup tombol Simpan. Mengambil semua nilai dari komponen UI,
     * lalu mengirimkannya ke ViewModel untuk divalidasi & disimpan.
     */
    private fun setupSaveButton() {
        binding.btnSimpan.setOnClickListener {
            val namaFaskes = binding.spinnerFaskes.selectedItem?.toString() ?: ""
            val qrCode = binding.etQrCode.text.toString().trim()
            val serialNumber = binding.etSerialNumber.text.toString().trim()
            val tanggalFormatted = binding.etTanggalInventory.text.toString().trim()
            val namaAlat = binding.etNamaAlat.text.toString().trim()
            val merk = binding.etMerk.text.toString().trim()
            val tipeAlat = binding.etTipeAlat.text.toString().trim()
            val namaRuangGedung = binding.etNamaRuangGedung.text.toString().trim()
            val lantai = binding.etLantai.text.toString().trim()

            // Ambil kondisi alat dari RadioGroup yang dipilih
            val kondisiAlat = when (binding.radioGroupKondisi.checkedRadioButtonId) {
                binding.rbBaik.id -> Equipment.KONDISI_BAIK
                binding.rbRusak.id -> Equipment.KONDISI_RUSAK
                binding.rbKalibrasi.id -> Equipment.KONDISI_KALIBRASI
                else -> ""
            }

            viewModel.saveEquipment(
                existingId = editingId,
                namaFaskes = namaFaskes,
                qrCode = qrCode,
                serialNumber = serialNumber,
                tanggalInventory = selectedDateMillis,
                tanggalInventoryFormatted = tanggalFormatted,
                namaAlat = namaAlat,
                merk = merk,
                tipeAlat = tipeAlat,
                namaRuangGedung = namaRuangGedung,
                lantai = lantai,
                kondisiAlat = kondisiAlat
            )
        }
    }

    /**
     * Mengamati LiveData dari ViewModel: status simpan & data hasil load (mode edit).
     */
    private fun observeViewModel() {
        viewModel.saveStatus.observe(this) { status ->
            when (status) {
                is FormViewModel.SaveStatus.Success -> {
                    Toast.makeText(this, "Data alat berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke MainActivity, list akan otomatis update via Flow/LiveData
                }
                is FormViewModel.SaveStatus.Error -> {
                    Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }

        viewModel.equipmentToEdit.observe(this) { equipment ->
            equipment?.let { populateFormForEdit(it) }
        }
    }

    /** Mengisi seluruh komponen form dengan data existing saat mode EDIT */
    private fun populateFormForEdit(equipment: Equipment) {
        val faskesIndex = daftarFaskes.indexOf(equipment.namaFaskes)
        if (faskesIndex >= 0) binding.spinnerFaskes.setSelection(faskesIndex)

        binding.etQrCode.setText(equipment.qrCode)
        binding.etSerialNumber.setText(equipment.serialNumber)
        selectedDateMillis = equipment.tanggalInventory
        binding.etTanggalInventory.setText(equipment.tanggalInventoryFormatted)
        binding.etNamaAlat.setText(equipment.namaAlat)
        binding.etMerk.setText(equipment.merk)
        binding.etTipeAlat.setText(equipment.tipeAlat)
        binding.etNamaRuangGedung.setText(equipment.namaRuangGedung)
        binding.etLantai.setText(equipment.lantai)

        when (equipment.kondisiAlat) {
            Equipment.KONDISI_BAIK -> binding.rbBaik.isChecked = true
            Equipment.KONDISI_RUSAK -> binding.rbRusak.isChecked = true
            Equipment.KONDISI_KALIBRASI -> binding.rbKalibrasi.isChecked = true
        }
    }
}
