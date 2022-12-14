package com.hh5.records.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHandler
    (context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ARTIST_COL + " TEXT,"
                + TITLE_COL + " TEXT,"
                + GENRE_COL + " TEXT,"
                + COVER_COL + " TEXT,"
                + LISTENED_COL + " BIT,"
                + FAVORITE_COL + " BIT)")

        db.execSQL(query)
    }

    fun addNewAlbum(
        artist: String?,
        title: String?,
        genre: String?,
        cover: String?,
        listened: Boolean?,
        favorite: Boolean?
    ) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(ARTIST_COL, artist)
        values.put(TITLE_COL, title)
        values.put(GENRE_COL, genre)
        values.put(COVER_COL, cover)
        values.put(LISTENED_COL, if(listened == true) 1 else 0)
        values.put(FAVORITE_COL, if(favorite == true) 1 else 0)

        db.insert(TABLE_NAME, null, values)

        db.close()
    }

    fun updateAlbum(
        artist: String?,
        title: String?,
        keyTitle: String,
        genre: String?,
        cover: String?,
        listened: Boolean?,
        favorite: Boolean?
    ) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(ARTIST_COL, artist)
        values.put(TITLE_COL, title)
        values.put(GENRE_COL, genre)
        values.put(COVER_COL, cover)
        values.put(LISTENED_COL, if(listened == true) 1 else 0)
        values.put(FAVORITE_COL, if(favorite == true) 1 else 0)

        db.update(TABLE_NAME, values, "title=?", arrayOf(keyTitle))

        db.close()
    }

    fun clear() {
        this.writableDatabase.execSQL("DELETE FROM $TABLE_NAME")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "albumdb"

        private const val DB_VERSION = 1

        private const val TABLE_NAME = "myrecords"

        private const val ID_COL = "id"

        private const val ARTIST_COL = "artist"

        private const val TITLE_COL = "title"

        private const val GENRE_COL = "genre"

        private const val COVER_COL = "cover"

        private const val LISTENED_COL = "listened"

        private const val FAVORITE_COL = "favorite"
    }

    fun readAlbums(): ArrayList<AlbumModel>? {
        val db = this.readableDatabase

        val cursorAlbums: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        val albumModelArrayList: ArrayList<AlbumModel> = ArrayList()

        if (cursorAlbums.moveToFirst()) {
            do {
                albumModelArrayList.add(
                    AlbumModel(
                        artist = cursorAlbums.getString(1),
                        cover = cursorAlbums.getString(4),
                        title = cursorAlbums.getString(2),
                        genre = cursorAlbums.getString(3),
                        listened = cursorAlbums.getInt(5),
                        favorite = cursorAlbums.getInt(6)
                    )
                )
            } while (cursorAlbums.moveToNext())
        }

        cursorAlbums.close()
        return albumModelArrayList
    }
}