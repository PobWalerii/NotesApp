package com.example.notesapp.ui.listnotes

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.DEFAULT_HEADER
import com.example.notesapp.constants.KeyConstants.DEFAULT_SPECIFICATION_LINE
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.databinding.ListNotesItemBinding
import com.example.notesapp.settings.AppSettings
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("NotifyDataSetChanged")
class NotesListAdapter: RecyclerView.Adapter<NotesListAdapter.ViewHolder>() {

    private var isSingleLine: Boolean = DEFAULT_SPECIFICATION_LINE
    private var defaultHeader: String =  DEFAULT_HEADER

    private var listener: OnItemClickListener? = null
    private var listNotes: List<Notes> = emptyList()
    private var currentId: Long = 0L

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val binding = ListNotesItemBinding.bind(itemView)
        fun bind(item: Notes, currentId: Long, isSingleLine: Boolean, defaultHeader: String) {
            binding.note = item
            binding.currentId = currentId
            binding.isSingleLine = isSingleLine
            binding.defaultHeader = defaultHeader
        }
        fun getBinding(): ListNotesItemBinding = binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_notes_item, parent, false)
        val holder = ViewHolder(view)
        val binding = holder.getBinding()

        binding.container.setOnClickListener {
            val position = holder.adapterPosition
            val current = listNotes[position]
            currentId = current.id
            binding.currentId = currentId
            notifyDataSetChanged()
            listener?.onItemClick(current.id)
        }
        return holder
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    interface OnItemClickListener {
        fun onItemClick(currentId: Long)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listNotes[position], currentId, isSingleLine, defaultHeader)
    }

    override fun getItemCount(): Int = listNotes.size

    override fun getItemId(position: Int): Long = listNotes[position].id

    fun getItemFromPosition(position: Int): Notes = listNotes[position]

    fun setCurrentId(curId: Long): Int {
        currentId = curId
        notifyDataSetChanged()
        return listNotes.indexOfFirst { it.id == currentId }
    }

    fun setList(list: List<Notes>) {
        listNotes = list
        notifyDataSetChanged()
    }

    fun setSettings(isLine: Boolean, header: String) {
        isSingleLine = isLine
        defaultHeader = header
    }

    fun refresh() {
        notifyDataSetChanged()
    }

}
