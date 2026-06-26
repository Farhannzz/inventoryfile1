// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/ui/FormViewModel.kt
// ===================================================================
package com.id.farhaninventory.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.id.farhaninventory.SpektrumApplication
import com.id.farhaninventory.data.Equipment
import com.id.farhaninventory.data.EquipmentRepository
import com.id.farhaninventory.util.InternalStorageHelper
import com.id.farhaninventory.util.NotificationHelper
import com.id.farhaninventory.util.PreferencesHelper
import kotlinx.coroutines.launch

/**
 * ViewModel khusus untuk FormActivity (Input/Edit data alat).
 * Menggunakan AndroidViewModel karena kita butuh Context (application)
 * untuk memicu NotificationHelper.
 */
class FormViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EquipmentRepository =
        (application as SpektrumApplication).repository

    // LiveData untuk memberi tahu UI (Activity) hasil proses simpan: sukses/gagal/pesan error
    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: MutableLiveData<SaveStatus> get() = _saveStatus

    // Menyimpan data Equipment yang sedang di-edit (null jika mode tambah baru)
    private val _equipmentToEdit = MutableLiveData<Equipment?>()
    val equipmentToEdit: MutableLiveData<Equipment?> get() = _equipmentToEdit

    /**
     * Sealed class untuk merepresentasikan status hasil simpan ke UI secara type-safe.
     */
    sealed class SaveStatus {
        object Success : SaveStatus()
        data class Error(val message: String) : SaveStatus()
    }

    /**
     * Memuat data existing untuk mode EDIT.
     */
    fun loadEquipmentForEdit(id: Int) {
        viewModelScope.launch {
            val data = repository.getEquipmentById(id)
            _equipmentToEdit.postValue(data)
        }
    }

    /**
     * Validasi dan simpan data alat ke Room Database.
     * Dipanggil dari FormActivity saat tombol "Simpan" ditekan.
     *
     * @param existingId jika > 0 maka ini adalah operasi UPDATE, jika 0 maka INSERT baru.
     */
    fun saveEquipment(
        existingId: Int,
        namaFaskes: String,
        qrCode: String,
        serialNumber: String,
        tanggalInventory: Long,
        tanggalInventoryFormatted: String,
        namaAlat: String,
        merk: String,
        tipeAlat: String,
        namaRuangGedung: String,
        lantai: String,
        kondisiAlat: String
    ) {
        // --- VALIDASI INPUT (Krusial: cegah data kosong/tidak valid masuk ke database) ---
        if (namaFaskes.isBlank() || namaFaskes == "Pilih Faskes") {
            _saveStatus.value = SaveStatus.Error("Faskes wajib dipilih")
            return
        }
        if (qrCode.isBlank()) {
            _saveStatus.value = SaveStatus.Error("QR Code tidak boleh kosong")
            return
        }
        if (serialNumber.isBlank()) {
            _saveStatus.value = SaveStatus.Error("Serial Number (SN) tidak boleh kosong")
            return
        }
        if (namaAlat.isBlank()) {
            _saveStatus.value = SaveStatus.Error("Nama Alat tidak boleh kosong")
            return
        }
        if (merk.isBlank()) {
            _saveStatus.value = SaveStatus.Error("Merk tidak boleh kosong")
            return
        }
        if (tipeAlat.isBlank()) {
            _saveStatus.value = SaveStatus.Error("Tipe Alat tidak boleh kosong")
            return
        }
        if (namaRuangGedung.isBlank()) {
            _saveStatus.value = SaveStatus.Error("Nama Ruang/Gedung tidak boleh kosong")
            return
        }
        if (lantai.isBlank()) {
            _saveStatus.value = SaveStatus.Error("Lantai tidak boleh kosong")
            return
        }
        if (kondisiAlat.isBlank()) {
            _saveStatus.value = SaveStatus.Error("Kondisi Alat wajib dipilih")
            return
        }

        val equipment = Equipment(
            id = existingId,
            namaFaskes = namaFaskes,
            qrCode = qrCode,
            serialNumber = serialNumber,
            tanggalInventory = tanggalInventory,
            tanggalInventoryFormatted = tanggalInventoryFormatted,
            namaAlat = namaAlat,
            merk = merk,
            tipeAlat = tipeAlat,
            namaRuangGedung = namaRuangGedung,
            lantai = lantai,
            kondisiAlat = kondisiAlat
        )

        // Tentukan label aksi untuk log SEBELUM proses simpan, karena setelah insert
        // existingId == 0 sudah tidak relevan lagi untuk membedakan tambah/edit.
        val labelAksi = if (existingId == 0) "TAMBAH DATA" else "EDIT DATA"

        viewModelScope.launch {
            try {
                if (existingId == 0) {
                    repository.insert(equipment)
                } else {
                    repository.update(equipment)
                }

                // ============================================================
                // FITUR 4: SMART ALERT NOTIFICATION
                // Jika kondisi alat Rusak atau Butuh Kalibrasi, picu notifikasi otomatis.
                // ============================================================
                if (kondisiAlat == Equipment.KONDISI_RUSAK || kondisiAlat == Equipment.KONDISI_KALIBRASI) {
                    NotificationHelper.showAlatBermasalahNotification(getApplication(), equipment)
                }

                // ============================================================
                // MATERI PERTEMUAN 16: INTERNAL STORAGE
                // Setiap kali data berhasil disimpan (tambah/edit), catat aktivitas
                // ke file log internal storage. Nama teknisi diambil dari SharedPreferences
                // (PreferencesHelper) yang sudah diisi saat setup profil di awal.
                // ============================================================
                val namaTeknisi = PreferencesHelper.getNamaTeknisi(getApplication())
                val detailAlat = "${equipment.namaAlat} (QR: ${equipment.qrCode}) - ${equipment.namaFaskes}"
                InternalStorageHelper.tambahLogAktivitas(
                    context = getApplication(),
                    namaTeknisi = namaTeknisi,
                    aksi = labelAksi,
                    detailAlat = detailAlat
                )

                _saveStatus.postValue(SaveStatus.Success)
            } catch (e: Exception) {
                _saveStatus.postValue(SaveStatus.Error("Gagal menyimpan data: ${e.message}"))
            }
        }
    }

    /**
     * Factory untuk membuat instance FormViewModel.
     * Diperlukan karena FormViewModel butuh parameter Application di constructor.
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FormViewModel::class.java)) {
                return FormViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
