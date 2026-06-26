// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/ui/EquipmentAdapter.kt
// ===================================================================
package com.id.farhaninventory.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.id.farhaninventory.data.Equipment
import com.id.farhaninventory.databinding.ItemEquipmentBinding

/**
 * Adapter RecyclerView untuk menampilkan daftar Equipment.
 * Menggunakan ListAdapter + DiffUtil agar update list efisien (hanya item yang berubah
 * yang akan di-render ulang), penting untuk performa saat filter realtime aktif.
 */
class EquipmentAdapter(
    private val onItemClick: (Equipment) -> Unit,
    private val onDeleteClick: (Equipment) -> Unit
) : ListAdapter<Equipment, EquipmentAdapter.EquipmentViewHolder>(EquipmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val binding = ItemEquipmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EquipmentViewHolder(private val binding: ItemEquipmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(equipment: Equipment) {
            binding.tvNamaAlat.text = equipment.namaAlat
            binding.tvMerkTipe.text = "${equipment.merk} • ${equipment.tipeAlat}"
            binding.tvQrCode.text = "QR: ${equipment.qrCode}"
            binding.tvLokasi.text = "${equipment.namaRuangGedung}, Lt. ${equipment.lantai} — ${equipment.namaFaskes}"
            binding.tvTanggal.text = equipment.tanggalInventoryFormatted
            binding.tvKondisi.text = equipment.kondisiAlat

            // Beri warna chip kondisi sesuai status -> memudahkan teknisi scan visual dengan cepat
            val (bgColor, textColor) = when (equipment.kondisiAlat) {
                Equipment.KONDISI_BAIK -> "#E8F5E9" to "#2E7D32"       // Hijau
                Equipment.KONDISI_RUSAK -> "#FFEBEE" to "#C62828"     // Merah
                Equipment.KONDISI_KALIBRASI -> "#FFF3E0" to "#EF6C00" // Oranye
                else -> "#ECEFF1" to "#37474F"
            }
            binding.tvKondisi.setBackgroundColor(Color.parseColor(bgColor))
            binding.tvKondisi.setTextColor(Color.parseColor(textColor))

            binding.root.setOnClickListener { onItemClick(equipment) }
            binding.btnDelete.setOnClickListener { onDeleteClick(equipment) }
        }
    }

    /**
     * DiffUtil callback -> menentukan bagaimana ListAdapter membandingkan item lama vs baru
     * untuk menghitung perubahan minimal yang perlu di-render ulang.
     */
    class EquipmentDiffCallback : DiffUtil.ItemCallback<Equipment>() {
        override fun areItemsTheSame(oldItem: Equipment, newItem: Equipment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Equipment, newItem: Equipment): Boolean {
            return oldItem == newItem
        }
    }
}
