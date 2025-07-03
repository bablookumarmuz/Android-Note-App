package com.techmania.noteapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.techmania.noteapp.Model.Note
import com.techmania.noteapp.R

interface OnNoteClickListener {
    fun onNoteClick(note: Note)
}

class NoteAdapter(
    private var notes: List<Note>,
    private val listener: OnNoteClickListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = notes[position]
        holder.textViewTitle.text = currentNote.title
        holder.textViewDescription.text = currentNote.description
        holder.cardView.setOnClickListener {
            listener.onNoteClick(currentNote)
        }
    }

    override fun getItemCount(): Int = notes.size

    fun updateNotes(updatedNotes: List<Note>) {
        this.notes = updatedNotes
        notifyDataSetChanged()
    }

    fun getNote(position: Int): Note = notes[position]
}
