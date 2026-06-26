// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/data/EquipmentDao.kt
// ===================================================================
package com.id.farhaninventory.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) untuk Equipment.
 * Semua query database didefinisikan di sini sebagai fungsi abstrak,
 * Room akan generate implementasinya secara otomatis saat compile.
 *
 * Menggunakan Flow agar UI (RecyclerView) otomatis update secara realtime
 * setiap kali ada perubahan data di database (reactive programming).
 */
@Dao
interface EquipmentDao {

    // --- CREATE ---
    // OnConflictStrategy.REPLACE -> jika ada id yang sama, data akan ditimpa (berguna untuk fungsi Update juga)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: Equipment): Long

    // --- UPDATE ---
    @Update
    suspend fun updateEquipment(equipment: Equipment)

    // --- DELETE ---
    @Delete
    suspend fun deleteEquipment(equipment: Equipment)

    @Query("DELETE FROM equipment_table WHERE id = :equipmentId")
    suspend fun deleteEquipmentById(equipmentId: Int)

    // --- READ: Ambil semua data, urut dari yang terbaru diinput ---
    @Query("SELECT * FROM equipment_table ORDER BY createdAt DESC")
    fun getAllEquipment(): Flow<List<Equipment>>

    // --- READ: Ambil 1 data spesifik berdasarkan ID (untuk halaman Edit) ---
    @Query("SELECT * FROM equipment_table WHERE id = :equipmentId LIMIT 1")
    suspend fun getEquipmentById(equipmentId: Int): Equipment?

    // --- READ: Ambil daftar nama Faskes yang unik, untuk mengisi Spinner filter ---
    @Query("SELECT DISTINCT namaFaskes FROM equipment_table ORDER BY namaFaskes ASC")
    fun getAllFaskesNames(): Flow<List<String>>

    /**
     * READ + FILTER GABUNGAN (Advanced Search):
     * Filter berdasarkan Faskes (opsional, "Semua Faskes" -> kosongkan filter)
     * DAN pencarian keyword pada Nama Alat ATAU QR Code secara bersamaan.
     *
     * Parameter :faskes bisa berupa "%" (artinya semua faskes, wildcard SQL LIKE)
     * Parameter :keyword juga dibungkus wildcard %keyword% di sisi kode pemanggil.
     */
    @Query(
        """
        SELECT * FROM equipment_table 
        WHERE namaFaskes LIKE :faskes 
        AND (namaAlat LIKE :keyword OR qrCode LIKE :keyword)
        ORDER BY createdAt DESC
        """
    )
    fun getFilteredEquipment(faskes: String, keyword: String): Flow<List<Equipment>>
}
