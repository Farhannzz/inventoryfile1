// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/ui/MainViewModel.kt
// ===================================================================
package com.id.farhaninventory.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.id.farhaninventory.SpektrumApplication
import com.id.farhaninventory.data.Equipment
import com.id.farhaninventory.data.EquipmentRepository
import com.id.farhaninventory.util.InternalStorageHelper
import com.id.farhaninventory.util.PreferencesHelper
import kotlinx.coroutines.launch

/**
 * ViewModel untuk MainActivity (Dashboard List & Filter).
 * Mengelola state filter Faskes + keyword pencarian, lalu meneruskannya
 * ke Repository untuk query database secara reaktif.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EquipmentRepository =
        (application as SpektrumApplication).repository

    // --- State filter saat ini, disimpan sebagai LiveData agar bisa dipakai switchMap ---
    // MATERI PERTEMUAN 16: nilai awal filter Faskes diambil dari SharedPreferences,
    // sehingga filter terakhir yang dipilih user tetap "diingat" walau app ditutup-buka lagi.
    private val _selectedFaskes = MutableLiveData(PreferencesHelper.getFilterFaskesTerakhir(application))
    private val _searchKeyword = MutableLiveData("")

    // Daftar nama Faskes unik untuk mengisi Spinner (otomatis update jika ada faskes baru diinput)
    val allFaskesNames: LiveData<List<String>> = repository.allFaskesNames.asLiveData()

    /**
     * MediaLiveData hasil gabungan filter Faskes + keyword.
     * switchMap akan otomatis re-query database setiap kali _selectedFaskes ATAU
     * _searchKeyword berubah nilainya -> inilah yang membuat filter "realtime".
     */
    val filteredEquipmentList: LiveData<List<Equipment>> = _selectedFaskes.switchMap { faskes ->
        _searchKeyword.switchMap { keyword ->
            repository.getFilteredEquipment(faskes, keyword).asLiveData()
        }
    }

    /** Dipanggil dari Spinner "Pilih Faskes" di MainActivity */
    fun setFaskesFilter(faskes: String) {
        _selectedFaskes.value = faskes
        // MATERI PERTEMUAN 16: simpan pilihan filter ke SharedPreferences setiap kali berubah
        PreferencesHelper.saveFilterFaskesTerakhir(getApplication(), faskes)
    }

    /** Dipanggil dari TextWatcher EditText pencarian global di MainActivity (realtime search) */
    fun setSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
    }

    fun getCurrentFaskesFilter(): String = _selectedFaskes.value ?: "Semua Faskes"

    /** Menghapus satu data alat (dipanggil setelah verifikasi biometrik berhasil) */
    fun deleteEquipment(equipment: Equipment) {
        viewModelScope.launch {
            repository.delete(equipment)

            // MATERI PERTEMUAN 16: catat aktivitas hapus data ke file Internal Storage
            val namaTeknisi = PreferencesHelper.getNamaTeknisi(getApplication())
            val detailAlat = "${equipment.namaAlat} (QR: ${equipment.qrCode}) - ${equipment.namaFaskes}"
            InternalStorageHelper.tambahLogAktivitas(
                context = getApplication(),
                namaTeknisi = namaTeknisi,
                aksi = "HAPUS DATA",
                detailAlat = detailAlat
            )
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
