// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/util/PdfExportHelper.kt
// ===================================================================
package com.id.farhaninventory.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.RectF
import com.id.farhaninventory.data.Equipment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper untuk men-generate file PDF laporan inventory menggunakan
 * Android native PdfDocument API (tanpa library eksternal).
 *
 * PDF dirender sebagai TABEL dengan kolom: No, Nama Alat, Merk, QR Code,
 * Ruang/Gedung, Lantai, Kondisi.
 */
object PdfExportHelper {

    // Ukuran halaman A4 dalam satuan point (1 inch = 72 point) -> 595 x 842 adalah ukuran standar A4
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 32f

    /**
     * Generate file PDF dari daftar Equipment, simpan ke internal storage app,
     * dan kembalikan File hasilnya.
     *
     * @param context Context aplikasi
     * @param dataList Daftar alat yang ingin dimasukkan ke laporan (hasil filter saat ini)
     * @param namaFaskesFilter Label faskes yang sedang difilter, untuk judul laporan
     * @return File PDF yang berhasil dibuat
     * @throws IOException jika gagal menulis file
     */
    @Throws(IOException::class)
    fun generateInventoryPdf(
        context: Context,
        dataList: List<Equipment>,
        namaFaskesFilter: String
    ): File {
        val pdfDocument = PdfDocument()

        // --- Setup Paint (gaya teks) yang akan dipakai berulang ---
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
        }
        val headerPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            isFakeBoldText = true
        }
        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#1565C0") // Biru korporat, senada Material Design 3
            style = Paint.Style.FILL
        }
        val cellPaint = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#CCCCCC")
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }
        val rowAltPaint = Paint().apply {
            color = Color.parseColor("#F5F5F5")
            style = Paint.Style.FILL
        }

        // --- Definisi lebar tiap kolom tabel (total harus <= PAGE_WIDTH - 2*MARGIN) ---
        // Kolom: No(30), NamaAlat(95), Merk(65), QRCode(80), Ruang/Gedung(95), Lantai(40), Kondisi(70)
        val colWidths = floatArrayOf(30f, 95f, 65f, 80f, 95f, 40f, 70f)
        val colTitles = arrayOf("No", "Nama Alat", "Merk", "QR Code", "Ruang/Gedung", "Lantai", "Kondisi")

        val rowHeight = 26f
        val headerHeight = 22f
        var pageNumber = 1
        var rowIndexOnPage = 0
        val maxRowsPerPage = ((PAGE_HEIGHT - 160) / rowHeight).toInt() // sisakan ruang untuk header halaman

        var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas

        fun drawPageHeader(c: Canvas) {
            c.drawText("LAPORAN INVENTORY ALAT KESEHATAN", MARGIN, 40f, titlePaint)
            c.drawText("PT Spektrum - Sistem Inventory Mobile", MARGIN, 58f, subtitlePaint)
            val tanggalCetak = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
            c.drawText("Faskes: $namaFaskesFilter", MARGIN, 76f, subtitlePaint)
            c.drawText("Dicetak pada: $tanggalCetak", MARGIN, 90f, subtitlePaint)
            c.drawText("Total Data: ${dataList.size} alat", MARGIN, 104f, subtitlePaint)
        }

        fun drawTableHeaderRow(c: Canvas, startY: Float): Float {
            var x = MARGIN
            // Background biru untuk header tabel
            c.drawRect(RectF(MARGIN, startY, MARGIN + colWidths.sum(), startY + headerHeight), headerBgPaint)
            for (i in colTitles.indices) {
                c.drawText(colTitles[i], x + 4f, startY + 15f, headerPaint)
                x += colWidths[i]
            }
            return startY + headerHeight
        }

        drawPageHeader(canvas)
        var currentY = drawTableHeaderRow(canvas, 120f)

        // --- Loop semua data alat, render baris per baris ---
        dataList.forEachIndexed { index, item ->

            // Jika sudah mencapai batas baris per halaman -> buat halaman baru
            if (rowIndexOnPage >= maxRowsPerPage) {
                pdfDocument.finishPage(page)
                pageNumber++
                page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                currentY = drawTableHeaderRow(canvas, 40f) // halaman lanjutan tanpa header judul besar
                rowIndexOnPage = 0
            }

            // Warna selang-seling antar baris agar mudah dibaca (zebra striping)
            if (index % 2 == 1) {
                canvas.drawRect(
                    RectF(MARGIN, currentY, MARGIN + colWidths.sum(), currentY + rowHeight),
                    rowAltPaint
                )
            }

            var x = MARGIN
            val rowValues = arrayOf(
                (index + 1).toString(),
                item.namaAlat,
                item.merk,
                item.qrCode,
                "${item.namaRuangGedung}",
                item.lantai,
                item.kondisiAlat
            )

            for (i in rowValues.indices) {
                // Truncate teks panjang supaya tidak tumpang tindih antar kolom
                val maxChars = (colWidths[i] / 4.2).toInt()
                val text = if (rowValues[i].length > maxChars) rowValues[i].take(maxChars - 1) + "…" else rowValues[i]

                // Beri warna merah pada kolom Kondisi jika alat Rusak/Butuh Kalibrasi -> mudah terlihat saat dicetak
                val paintToUse = if (i == 6 && (item.kondisiAlat == Equipment.KONDISI_RUSAK || item.kondisiAlat == Equipment.KONDISI_KALIBRASI)) {
                    Paint(cellPaint).apply { color = Color.parseColor("#D32F2F"); isFakeBoldText = true }
                } else {
                    cellPaint
                }

                canvas.drawText(text, x + 4f, currentY + 17f, paintToUse)
                canvas.drawRect(RectF(x, currentY, x + colWidths[i], currentY + rowHeight), borderPaint)
                x += colWidths[i]
            }

            currentY += rowHeight
            rowIndexOnPage++
        }

        pdfDocument.finishPage(page)

        // --- Simpan ke internal storage app: /data/data/<package>/files/reports/ ---
        val reportsDir = File(context.filesDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "Laporan_Inventory_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(reportsDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } finally {
            pdfDocument.close()
        }

        return file
    }
}
