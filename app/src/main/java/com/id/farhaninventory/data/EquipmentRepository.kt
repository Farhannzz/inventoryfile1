// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/data/EquipmentRepository.kt
// ===================================================================
package com.id.farhaninventory.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository = lapisan abstraksi antara ViewModel dan sumber data (DAO).
 * Best practice arsitektur MVVM: ViewModel TIDAK BOLEH langsung memanggil DAO,
 * harus melalui Repository. Ini memudahkan jika nanti ingin menambah sumber data
 * lain (misal: sinkronisasi ke API web sistem inventory PT Spektrum).
 */
class EquipmentRepository(private val equipmentDao: EquipmentDao) {

    // Data semua alat, dibungkus Flow agar reactive (auto update ke UI)
    val allEquipment: Flow<List<Equipment>> = equipmentDao.getAllEquipment()

    val allFaskesNames: Flow<List<String>> = equipmentDao.getAllFaskesNames()

    suspend fun insert(equipment: Equipment): Long {
        return equipmentDao.insertEquipment(equipment)
    }

    suspend fun update(equipment: Equipment) {
        equipmentDao.updateEquipment(equipment)
    }

    suspend fun delete(equipment: Equipment) {
        equipmentDao.deleteEquipment(equipment)
    }

    suspend fun getEquipmentById(id: Int): Equipment? {
        return equipmentDao.getEquipmentById(id)
    }

    /**
     * Fungsi filter gabungan. Jika faskes == "Semua Faskes" maka wildcard "%"
     * dipakai supaya tidak membatasi hasil ke faskes tertentu.
     */
    fun getFilteredEquipment(faskes: String, keyword: String): Flow<List<Equipment>> {
        val faskesQuery = if (faskes == "Semua Faskes" || faskes.isBlank()) "%" else faskes
        val keywordQuery = "%$keyword%"
        return equipmentDao.getFilteredEquipment(faskesQuery, keywordQuery)
    }
}
