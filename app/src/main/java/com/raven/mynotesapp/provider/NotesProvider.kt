package com.raven.mynotesapp.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.raven.mynotesapp.db.DatabaseContract.AUTHORITY
import com.raven.mynotesapp.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import com.raven.mynotesapp.db.DatabaseContract.NoteColumns.Companion.TABLE_NAME
import com.raven.mynotesapp.db.NoteHelper


class NotesProvider : ContentProvider() {

    companion object{
        private const val NOTE = 1
        private const val NOTE_ID = 2
        private val sUriMather = UriMatcher(UriMatcher.NO_MATCH)
        private lateinit var noteHelper: NoteHelper

        init {
            sUriMather.addURI(AUTHORITY, TABLE_NAME, NOTE)

            sUriMather.addURI(AUTHORITY, "$TABLE_NAME/#", NOTE_ID)
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val deleted : Int = when(NOTE_ID) {
            sUriMather.match(uri) -> noteHelper.deleteById(uri.lastPathSegment.toString())
            else -> 0
        }
        context?.contentResolver?.notifyChange(CONTENT_URI, null)

        return deleted
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val added: Long = when (NOTE){
            sUriMather.match(uri) -> noteHelper.insert(contentValues)
            else -> 0
        }

        context?.contentResolver?.notifyChange(CONTENT_URI, null)

        return Uri.parse("$CONTENT_URI/$added")
    }

    override fun onCreate(): Boolean {
        noteHelper = NoteHelper.getInstance(context as Context)
        noteHelper.open()
        return true
    }
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val cursor: Cursor?
        when (sUriMather.match(uri)) {
            NOTE -> cursor = noteHelper.queryAll()
            NOTE_ID -> cursor = noteHelper.queryById(uri.lastPathSegment.toString())
            else -> cursor = null
        }
        return cursor
    }
    override fun update(
        uri: Uri, contentValues: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val updated: Int = when (NOTE_ID) {
            sUriMather.match(uri) -> noteHelper.update(uri.lastPathSegment.toString(),
                contentValues)
            else -> 0
        }

        context?.contentResolver?.notifyChange(CONTENT_URI, null)

        return updated
    }
}
