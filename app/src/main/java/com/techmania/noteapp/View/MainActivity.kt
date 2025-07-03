package com.techmania.noteapp.View

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.techmania.noteapp.Adapters.NoteAdapter
import com.techmania.noteapp.Adapters.OnNoteClickListener
import com.techmania.noteapp.Model.Note
import com.techmania.noteapp.NoteApplication
import com.techmania.noteapp.R
import com.techmania.noteapp.ViewModel.NoteViewModel
import com.techmania.noteapp.ViewModel.NoteViewModelFactory

class MainActivity : AppCompatActivity(), OnNoteClickListener {

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var addActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var updateActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        noteAdapter = NoteAdapter(emptyList(), this)
        recyclerView.adapter = noteAdapter

        registerActivityResultLaunchers()

        val viewModelFactory = NoteViewModelFactory((application as NoteApplication).repository)
        noteViewModel = ViewModelProvider(this, viewModelFactory)[NoteViewModel::class.java]

        noteViewModel.myAllNotes.observe(this, Observer { notes ->
            noteAdapter.updateNotes(notes)
        })

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                noteViewModel.delete(noteAdapter.getNote(viewHolder.adapterPosition))
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun registerActivityResultLaunchers() {
        addActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val title = result.data!!.getStringExtra("title") ?: return@ActivityResultCallback
                    val description = result.data!!.getStringExtra("description") ?: return@ActivityResultCallback
                    val note = Note(title, description)
                    noteViewModel.insert(note)
                }
            })

        updateActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val updatedTitle = result.data!!.getStringExtra("updatedTitle") ?: return@ActivityResultCallback
                    val updatedDescription = result.data!!.getStringExtra("updatedDescription") ?: return@ActivityResultCallback
                    val noteId = result.data!!.getIntExtra("noteId", -1)
                    val updatedNote = Note(updatedTitle, updatedDescription).apply { id = noteId }
                    noteViewModel.update(updatedNote)
                }
            })
    }

    override fun onNoteClick(note: Note) {
        val intent = Intent(this, UpdateActivity::class.java).apply {
            putExtra("currentTitle", note.title)
            putExtra("currentDescription", note.description)
            putExtra("currentId", note.id)
        }
        updateActivityResultLauncher.launch(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.new_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add_note -> {
                val intent = Intent(this, NoteAddActivity::class.java)
                addActivityResultLauncher.launch(intent)
            }
            R.id.item_delete_all_notes -> showDeleteAllDialog()
        }
        return true
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete All Notes")
            .setMessage("This will delete all notes. Swipe individual notes to delete one by one.")
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Yes") { _, _ -> noteViewModel.deleteAllNotes() }
            .create()
            .show()
    }
}
