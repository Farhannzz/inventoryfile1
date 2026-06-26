# PT Spektrum Inventory Mobile — Android Native (Kotlin)

Aplikasi mobile versi Teknisi dari sistem web inventory alat kesehatan PT Spektrum.
Arsitektur: **MVVM + Room Database (SQLite) + Material Design 3**.

Project ini sudah berbentuk **project Android Studio yang utuh** — bisa langsung dibuka,
tidak perlu digabung manual ke project lain.

## 🚀 Cara Membuka Project

1. Buka **Android Studio**.
2. Pilih **File → Open** (atau **Open** dari Welcome Screen).
3. Arahkan ke folder hasil extract ZIP ini — pilih folder **`PT_Spektrum_Inventory_Mobile`** (folder yang berisi `app/`, `gradle/`, `build.gradle.kts`, `settings.gradle.kts`, dll — **bukan** folder `app` itu sendiri).
4. Tunggu proses **Gradle Sync** selesai (ditandai progress bar di bawah, biasanya 1–5 menit tergantung koneksi internet karena Gradle akan download dependency).
5. Jika muncul notifikasi terkait `local.properties` / Android SDK location, Android Studio akan otomatis memperbaikinya sesuai lokasi SDK di komputer Anda — tidak perlu diedit manual.
6. Setelah Sync selesai tanpa error, klik tombol **Run ▶** (Shift+F10).

## 📁 Struktur Project

```
PT_Spektrum_Inventory_Mobile/
├── build.gradle.kts                  (root - plugin Android/Kotlin/KSP)
├── settings.gradle.kts
├── gradle.properties
├── local.properties                  (otomatis disesuaikan oleh Android Studio)
├── gradle/wrapper/gradle-wrapper.properties
├── .gitignore
└── app/
    ├── build.gradle.kts               (dependencies module :app)
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/id/farhaninventory/
        │   ├── SpektrumApplication.kt
        │   ├── data/
        │   │   ├── Equipment.kt              (Entity Room)
        │   │   ├── EquipmentDao.kt           (DAO - query database)
        │   │   ├── AppDatabase.kt            (Konfigurasi Room)
        │   │   └── EquipmentRepository.kt    (Jembatan ViewModel <-> DAO)
        │   ├── ui/
        │   │   ├── LoginActivity.kt          (Login/Daftar Akun - launcher activity)
        │   │   ├── MainActivity.kt           (Dashboard, Filter, Export PDF, Menu)
        │   │   ├── MainViewModel.kt
        │   │   ├── FormActivity.kt           (Form Input/Edit Teknisi)
        │   │   ├── FormViewModel.kt
        │   │   ├── EquipmentAdapter.kt       (RecyclerView Adapter - daftar alat)
        │   │   ├── LogAktivitasActivity.kt   (Halaman Log Aktivitas - Internal Storage)
        │   │   ├── LogAdapter.kt             (RecyclerView Adapter - daftar log)
        │   │   └── CatatanActivity.kt        (Halaman Catatan Teknisi - Internal Storage)
        │   └── util/
        │       ├── NotificationHelper.kt     (Fitur 4: Smart Alert)
        │       ├── PdfExportHelper.kt        (Fitur 5: Export PDF)
        │       ├── PreferencesHelper.kt      (Fitur 3: Akun Login + Pertemuan 16: SharedPreferences)
        │       └── InternalStorageHelper.kt  (Pertemuan 16: Internal Storage)
        └── res/
            ├── layout/      (semua file .xml halaman & item)
            ├── values/      (colors, themes, strings, styles)
            ├── menu/        (menu_main.xml)
            ├── drawable/    (semua ikon vector + launcher icon)
            ├── mipmap-anydpi-v26/ (ikon adaptive launcher)
            └── xml/         (file_paths.xml - FileProvider)
```

## ✅ Mapping 5 Fitur Utama ke Kode

| # | Fitur                          | File Utama                                      |
|---|---------------------------------|--------------------------------------------------|
| 1 | Form Input CRUD Teknisi         | `FormActivity.kt`, `FormViewModel.kt`, `Equipment.kt`, `EquipmentDao.kt` |
| 2 | Dashboard List & Filter Faskes  | `MainActivity.kt`, `MainViewModel.kt`, `EquipmentAdapter.kt` |
| 3 | Login Akun (Username & Password) | `LoginActivity.kt`, `PreferencesHelper.kt` (dipanggil dari `MainActivity.kt` untuk Logout) |
| 4 | Notifikasi Alat Rusak           | `NotificationHelper.kt` (dipanggil dari `FormViewModel.kt`) |
| 5 | Export PDF                      | `PdfExportHelper.kt` (dipanggil dari `MainActivity.kt`) |

## 🎓 Mapping Materi Pertemuan 16 (SharedPreferences & Internal Storage)

Fitur tambahan ini dibuat khusus agar aplikasi mendemonstrasikan materi **Pertemuan 16** secara nyata, terpisah dari Room Database.

| Konsep | Implementasi di Aplikasi | File Utama |
|---|---|---|
| **SharedPreferences — Profil** | Dialog setup profil (nama teknisi + faskes default) muncul otomatis saat pertama kali app dibuka. Data dibaca ulang setiap app dibuka (greeting di Toolbar). | `PreferencesHelper.kt`, `dialog_setup_profil.xml`, dipanggil dari `MainActivity.kt` |
| **SharedPreferences — Preferensi Tampilan** | Filter Faskes terakhir yang dipilih otomatis tersimpan & diingat kembali saat app dibuka ulang. | `PreferencesHelper.kt` (`saveFilterFaskesTerakhir`/`getFilterFaskesTerakhir`), dipanggil dari `MainViewModel.kt` |
| **Internal Storage — Log Aktivitas** | Setiap kali teknisi TAMBAH/EDIT/HAPUS data alat, baris log otomatis ditulis (append) ke file `log_aktivitas_teknisi.txt`. Halaman "Log Aktivitas" (menu toolbar) membaca & menampilkan isi file ini. | `InternalStorageHelper.kt`, `LogAktivitasActivity.kt`, dipanggil dari `FormViewModel.kt` & `MainViewModel.kt` |
| **Internal Storage — Catatan Manual** | Halaman "Catatan Teknisi" (menu toolbar) memungkinkan teknisi menulis catatan bebas yang disimpan ke file `catatan_teknisi.txt`, dan dibaca kembali otomatis saat halaman dibuka. | `InternalStorageHelper.kt`, `CatatanActivity.kt` |

**Cara akses di aplikasi:** buka menu (ikon di pojok kanan atas Toolbar MainActivity) → pilih **"Log Aktivitas"**, **"Catatan Teknisi"**, atau **"Edit Profil"**.

> 💡 **Tips untuk video tugas:** urutan demo yang jelas untuk dosen:
> 1. Jalankan app pertama kali → muncul layar **Daftar Akun** (buat username & password sendiri) → otomatis masuk ke Dashboard, lalu muncul dialog setup profil (SharedPreferences tertulis)
> 2. Tutup & buka app lagi → kali ini muncul layar **Login** (bukan daftar lagi) → masukkan username/password yang sama tadi → berhasil masuk ke Dashboard
> 3. Tunjukkan juga **Logout** dari menu Toolbar → kembali ke layar Login (akun tetap ada, tidak perlu daftar ulang)
> 4. Tambah/Edit/Hapus 1 data alat → buka menu "Log Aktivitas" → tunjukkan baris log baru muncul (Internal Storage tertulis & terbaca)
> 5. Buka "Catatan Teknisi" → ketik sesuatu → Simpan → keluar & masuk lagi → catatan masih ada (Internal Storage persisten)

## 🔍 Catatan Teknis Penting

1. **ViewBinding** sudah diaktifkan (`buildFeatures { viewBinding = true }`), sehingga class `ActivityMainBinding`, `ActivityFormBinding`, dll **otomatis digenerate** oleh Android Studio saat build pertama — tidak perlu dibuat manual.

2. **Room Database** menggunakan `fallbackToDestructiveMigration()` — cocok untuk development/tugas. Untuk produksi sesungguhnya, ganti dengan `Migration` object yang proper agar data tidak hilang saat update skema.

3. **Master Data Faskes** di `FormActivity.kt` (variable `daftarFaskes`) saat ini statis (hardcoded). Untuk integrasi nyata dengan sistem web PT Spektrum, ganti dengan pemanggilan API/REST.

4. **File PDF** disimpan di internal storage app (`context.filesDir/reports/`), dibuka via `FileProvider` agar bisa diakses aplikasi PDF viewer lain.

5. **Smart Alert Notification** otomatis terpicu setiap kali data dengan `kondisiAlat = "Rusak"` atau `"Butuh Kalibrasi"` disimpan.

6. **Login Akun** dibuat sendiri oleh teknisi saat pertama kali memakai aplikasi (bukan akun tetap/hardcode) — username & password (sudah di-hash SHA-256) disimpan di SharedPreferences yang sama. Setiap aplikasi dibuka dari awal, layar Login akan selalu muncul lagi; gunakan menu **"Logout"** di Toolbar Dashboard untuk kembali ke layar Login tanpa menghapus akun yang sudah dibuat.

7. **SharedPreferences** disimpan di `/data/data/com.id.farhaninventory/shared_prefs/spektrum_prefs.xml`.

8. **File Internal Storage** (`log_aktivitas_teknisi.txt`, `catatan_teknisi.txt`) disimpan di `/data/data/com.id.farhaninventory/files/` — bisa dicek via Android Studio **Device File Explorer** untuk verifikasi/demo video.

9. Untuk reset semua data SharedPreferences (termasuk **akun Login & profil teknisi**) & Internal Storage saat testing ulang: **Settings HP → Apps → Spektrum Inventory → Storage → Clear Data**, atau uninstall-reinstall aplikasi — setelah itu layar **Daftar Akun** akan muncul lagi seperti pertama kali install.

## ⚠️ Jika Sync Gradle Gagal

- Pastikan koneksi internet stabil (Gradle perlu download dependency saat pertama kali).
- Jika ada error terkait versi KSP/Kotlin tidak cocok, buka `build.gradle.kts` (root) dan sesuaikan versi `com.google.devtools.ksp` agar match dengan versi `org.jetbrains.kotlin.android` — format: `<versi_kotlin>-<versi_ksp>` (lihat https://github.com/google/ksp/releases).
- Pastikan **Android SDK Platform 34** sudah terinstall (Android Studio → Settings → SDK Manager).

Selamat coding! 🚀
