// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/SpektrumApplication.kt
// ===================================================================
package com.id.farhaninventory

import android.app.Application
import com.id.farhaninventory.data.AppDatabase
import com.id.farhaninventory.data.EquipmentRepository
import com.id.farhaninventory.util.NotificationHelper

/**
 * Custom Application class.
 * Dipakai untuk membuat instance Database & Repository SEKALI SAJA
 * selama aplikasi hidup (best practice MVVM, dipakai oleh ViewModelFactory).
 *
 * JANGAN LUPA: daftarkan class ini di AndroidManifest.xml
 * pada tag <application android:name=".SpektrumApplication" ...>
 */
class SpektrumApplication : Application() {

    // by lazy -> database baru dibuat saat pertama kali diakses, bukan saat app start (efisien)
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { EquipmentRepository(database.equipmentDao()) }

    override fun onCreate() {
        super.onCreate()
        // Membuat notification channel sedini mungkin di lifecycle aplikasi
        NotificationHelper.createNotificationChannel(this)
    }
}
