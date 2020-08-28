package com.raven.mynotesapp

import android.content.Intent
import android.database.ContentObservable
import android.database.ContentObserver
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
.Helper.MappingHelper
.adapter.NoteAdapter
.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
.db.NoteHelper
.entity.Note
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.E

class MainActivity : AppCompatActivity() {


    private lateinit var adapter: NoteAdapter

    companion object{
        private  const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "Notes"

        rv_notes.layoutManager = LinearLayoutManager(this)
        rv_notes.setHasFixedSize(true)
        adapter = NoteAdapter(this)
        rv_notes.adapter = adapter

        fab_add.setOnClickListener{
            val intent = Intent(this@MainActivity, NoteAddUpdateActivity::class.java)
            startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD)
        }

        val handlerThread = HandlerThread("DataObserver")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)

        val myObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                loadNotesAsync()
            }
        }

        contentResolver.registerContentObserver(CONTENT_URI, true, myObserver)


        if (savedInstanceState==null){
            loadNotesAsync()
        }else{
            val list = savedInstanceState.getParcelableArrayList<Note>(EXTRA_STATE)
            if (list != null){
                adapter.listNotes = list
            }
        }
    }

    private fun loadNotesAsync(){
        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            val deferredNotes = async(Dispatchers.IO){
            val cursor = contentResolver?.query(CONTENT_URI, null,null,null, null)
            MappingHelper.mapCursorToArrayList(cursor)
            }
            progressBar.visibility = View.INVISIBLE
            val notes = deferredNotes.await()
            if (notes.size >0) {
                adapter.listNotes = notes
            }else {
                adapter.listNotes = ArrayList()
                showSnackbarMessage("Tidak ada data disini")
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listNotes)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data!= null){
            when(requestCode){
                NoteAddUpdateActivity.REQUEST_ADD -> if (resultCode == NoteAddUpdateActivity.RESULT_ADD){
                    val note = data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE)

                    adapter.addItem(note)
                    rv_notes.smoothScrollToPosition(adapter.itemCount -1)

                    showSnackbarMessage("Satu item berhasil ditambakan")
                }
                NoteAddUpdateActivity.REQUEST_UPDATE ->
                    when(resultCode) {
                        NoteAddUpdateActivity.RESULT_UPDATE -> {
                            val note =data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE)
                            val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)

                            adapter.updateItem(position, note)
                            rv_notes.smoothScrollToPosition(position)

                            showSnackbarMessage("Satu item berhasil diubah")
                        }
                    }
                NoteAddUpdateActivity.RESULT_DELET ->{
                    val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)

                    adapter.removeItem(position)

                    showSnackbarMessage("Satu item berhasil dihapus")
                }
            }
        }


    }

    private fun showSnackbarMessage(message: String){
        Snackbar.make(rv_notes, message, Snackbar.LENGTH_SHORT).show()
    }
}