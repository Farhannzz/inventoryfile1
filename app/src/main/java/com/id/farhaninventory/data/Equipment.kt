// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/data/Equipment.kt
// ===================================================================
package com.id.farhaninventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room yang merepresentasikan satu baris data Alat Kesehatan.
 * Struktur kolom disesuaikan PERSIS dengan sistem web inventory PT Spektrum.
 *
 * Setiap instance class ini = 1 baris di tabel SQLite "equipment_table".
 */
@Entity(tableName = "equipment_table")
data class Equipment(

    // Primary Key, auto increment otomatis oleh Room/SQLite
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Nama Fasilitas Kesehatan (misal: RS Siloam, Puskesmas Kebon Jeruk, dst)
    val namaFaskes: String,

    // Kode unik QR yang ditempel di alat fisik
    val qrCode: String,

    // Serial Number (SN) alat — nomor seri unik dari pabrik/vendor
    val serialNumber: String = "",

    // Tanggal inventory disimpan sebagai Long (epoch millis) agar mudah di-sort/filter,
    // tapi juga kita simpan representasi String yang sudah diformat untuk ditampilkan di UI.
    val tanggalInventory: Long,
    val tanggalInventoryFormatted: String,

    // Detail alat
    val namaAlat: String,
    val merk: String,
    val tipeAlat: String,

    // Lokasi alat
    val namaRuangGedung: String,
    val lantai: String,

    // Kondisi alat: hanya boleh berisi salah satu dari 3 nilai berikut.
    // Kita pakai String (bukan Enum) supaya kompatibel langsung dengan Room tanpa TypeConverter,
    // tapi validasi nilai dilakukan di sisi UI (RadioGroup) & ViewModel.
    val kondisiAlat: String,

    // Timestamp kapan record ini dibuat di aplikasi (untuk sorting "terbaru")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Konstanta kondisi alat -> dipakai di RadioGroup, Notifikasi, dan filter
        const val KONDISI_BAIK = "Baik"
        const val KONDISI_RUSAK = "Rusak"
        const val KONDISI_KALIBRASI = "Butuh Kalibrasi"
    }
}
