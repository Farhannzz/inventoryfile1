// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/data/AppDatabase.kt
// ===================================================================
package com.id.farhaninventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Kelas utama Room Database. Menggunakan pattern Singleton agar
 * hanya ada SATU instance database di seluruh lifecycle aplikasi
 * (mencegah memory leak & konflik koneksi database).
 */
@Database(entities = [Equipment::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun equipmentDao(): EquipmentDao

    companion object {
        // @Volatile memastikan perubahan nilai INSTANCE langsung terlihat oleh semua thread
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // synchronized -> mencegah race condition jika 2 thread mencoba membuat database bersamaan
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spektrum_inventory_database"
                )
                    // fallbackToDestructiveMigration -> untuk tahap development,
                    // jika skema berubah, database lama akan dihapus & dibuat ulang.
                    // PRODUKSI: ganti dengan Migration object yang proper.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
