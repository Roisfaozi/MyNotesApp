package com.raven.mynotesapp

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.raven.mynotesapp.Helper.MappingHelper
import com.raven.mynotesapp.db.DatabaseContract
import com.raven.mynotesapp.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import com.raven.mynotesapp.entity.Note
import kotlinx.android.synthetic.main.activity_note_add_update.*
import java.text.SimpleDateFormat
import java.util.*

class NoteAddUpdateActivity : AppCompatActivity(), View.OnClickListener {

    private var isEdit = false
    private var note: Note? = null
    private var position:Int = 0
    private lateinit var uriWithId: Uri

    companion object{
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_POSITION = "extra_position"
        const val REQUEST_ADD = 100
        const val RESULT_ADD = 101
        const val REQUEST_UPDATE = 200
        const val RESULT_UPDATE = 201
        const val RESULT_DELET = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_add_update)

        note = intent.getParcelableExtra(EXTRA_NOTE)
        if (note != null){
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            note = Note()
        }

        val actionBarTitle: String
        val btnTitle: String

        if(isEdit){
            uriWithId = Uri.parse(CONTENT_URI.toString() + "/" + note?.id)

            val cursor = contentResolver.query(uriWithId,null, null, null, null)
            if (cursor != null){
                note = MappingHelper.mapCursorToObject(cursor)
                cursor.close()
            }

            actionBarTitle = "Ubah"
            btnTitle = "Update"

            note?.let{edt_title.setText(it.title)}
            note?.let { edt_description.setText(it.description) }
        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"

        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btn_submit.text = btnTitle

        btn_submit.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_submit){
            val title = edt_title.text.toString().trim()
            val description = edt_description.text.toString().trim()

            if (title.isEmpty()){
                edt_title.error = "Field can not be blank"
                return
            }
            note?.title = title
            note?.description = description

            val intent = Intent()
            intent.putExtra(EXTRA_NOTE, note)
            intent.putExtra(EXTRA_POSITION, position)

            val values = ContentValues()
            values.put(DatabaseContract.NoteColumns.TITLE, title)
            values.put(DatabaseContract.NoteColumns.DESCRIPTION, description)

            if(isEdit){
                contentResolver.update(uriWithId, values, null, null)
                Toast.makeText(this, "Satu item berhasil di edit", Toast.LENGTH_SHORT).show()
                finish()
            }else{
                note?.date = getCurrentDate()
                contentResolver.insert(CONTENT_URI, values)
                Toast.makeText(this, "Satu item berhasil di simpan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun getCurrentDate(): String{
        val dateFormat = SimpleDateFormat("yyy/MM/ddd HH:mm:ss", Locale.getDefault())
        val date = Date()

        return dateFormat.format(date)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delet -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    private fun showAlertDialog(type: Int){

        val isiDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle : String
        val dialogMessage : String

        if (isiDialogClose){
            dialogTitle = "Batal"
            dialogMessage= "Apakah anda ingin membatalkan poerubahan form?"
        }else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
            dialogTitle = "Hapus Note"
        }

        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("ya") { dialog, id ->
                if (isiDialogClose){
                    finish()
                }else{
                    contentResolver?.delete(uriWithId, null, null)
                    Toast.makeText(this, "Satu item berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Tidak"){ dialog, id -> dialog.cancel()}
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }

}