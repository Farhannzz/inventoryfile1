// ===================================================================
// FILE: app/src/main/java/com/id/farhaninventory/ui/LogAdapter.kt
// ===================================================================
package com.id.farhaninventory.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter sederhana untuk menampilkan daftar baris log aktivitas (String)
 * yang dibaca dari file internal storage.
 */
class LogAdapter(private var logList: List<String>) :
    RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    inner class LogViewHolder(view: TextView) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.id.farhaninventory.R.layout.item_log, parent, false) as TextView
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.textView.text = logList[position]
    }

    override fun getItemCount(): Int = logList.size

    /** Memperbarui data list & refresh tampilan (dipanggil setelah hapus log) */
    fun updateData(newList: List<String>) {
        logList = newList
        notifyDataSetChanged()
    }
}
