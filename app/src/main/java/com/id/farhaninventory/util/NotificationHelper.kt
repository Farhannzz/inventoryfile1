// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/util/NotificationHelper.kt
// ===================================================================
package com.id.farhaninventory.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.id.farhaninventory.R
import com.id.farhaninventory.data.Equipment

/**
 * Helper class untuk mengelola Push Notification lokal.
 * Dipanggil otomatis dari FormViewModel saat kondisiAlat = "Rusak" atau "Butuh Kalibrasi".
 */
object NotificationHelper {

    private const val CHANNEL_ID = "spektrum_alat_rusak_channel"
    private const val CHANNEL_NAME = "Peringatan Alat Rusak"
    private const val CHANNEL_DESC = "Notifikasi untuk alat kesehatan yang berstatus Rusak atau Butuh Kalibrasi"

    /**
     * Membuat Notification Channel. WAJIB dipanggil sebelum notifikasi pertama dikirim
     * (khususnya untuk Android 8.0 / API 26 ke atas).
     * Aman dipanggil berkali-kali, sistem akan mengabaikan jika channel sudah ada.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH // High agar muncul sebagai heads-up/alert
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Mengirim notifikasi peringatan untuk satu alat yang kondisinya bermasalah.
     * Dipanggil dari FormViewModel setelah data berhasil disimpan ke Room.
     */
    fun showAlatBermasalahNotification(context: Context, equipment: Equipment) {
        val isRusak = equipment.kondisiAlat == Equipment.KONDISI_RUSAK

        val title = if (isRusak) {
            "⚠️ Alat Rusak Terdeteksi!"
        } else {
            "🔧 Alat Memerlukan Kalibrasi"
        }

        val message = "Alat '${equipment.namaAlat}' (${equipment.merk}) di ${equipment.namaRuangGedung} " +
                "Lt. ${equipment.lantai}, ${equipment.namaFaskes} berstatus '${equipment.kondisiAlat}'. " +
                "Segera lakukan tindakan perbaikan/manajemen."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_warning)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Agar teks panjang tidak terpotong saat expand
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Notifikasi hilang otomatis saat di-tap

        // Gunakan ID unik berdasarkan id Equipment, supaya notifikasi tidak saling menimpa
        // jika ada beberapa alat rusak yang diinput berurutan.
        val notificationId = equipment.id.takeIf { it != 0 } ?: System.currentTimeMillis().toInt()

        // Cek izin notifikasi sebelum mengirim (wajib untuk Android 13 / API 33+)
        // Pengecekan permission runtime dilakukan di sisi Activity (FormActivity) sebelum panggil fungsi ini.
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Izin POST_NOTIFICATIONS belum diberikan oleh user, gagal secara aman tanpa crash.
            e.printStackTrace()
        }
    }
}
