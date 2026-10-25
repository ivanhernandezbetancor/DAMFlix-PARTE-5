package com.example.damflix.data

import android.content.ContentValues
import com.example.damflix.model.Pelicula
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PeliculaRepository(private val dbHelper: DAMFlixDbHelper) {

    suspend fun obtenerTodas(): List<Pelicula> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<Pelicula>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DAMFlixDbHelper.TABLE_PELICULAS} ORDER BY ${DAMFlixDbHelper.COLUMN_TITULO} ASC",
            null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    val id = c.getInt(c.getColumnIndexOrThrow(DAMFlixDbHelper.COLUMN_ID))
                    val titulo = c.getString(c.getColumnIndexOrThrow(DAMFlixDbHelper.COLUMN_TITULO))
                    val director = c.getString(c.getColumnIndexOrThrow(DAMFlixDbHelper.COLUMN_DIRECTOR))
                    val anio = c.getInt(c.getColumnIndexOrThrow(DAMFlixDbHelper.COLUMN_ANIO))
                    val esVista = c.getInt(c.getColumnIndexOrThrow(DAMFlixDbHelper.COLUMN_ES_VISTA)) == 1
                    val esFavorita = c.getInt(c.getColumnIndexOrThrow(DAMFlixDbHelper.COLUMN_ES_FAVORITA)) == 1
                    lista.add(Pelicula(id, titulo, director, anio, esVista, esFavorita))
                } while (c.moveToNext())
            }
        }
        lista
    }

    suspend fun insertar(pelicula: Pelicula): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DAMFlixDbHelper.COLUMN_TITULO, pelicula.titulo)
            put(DAMFlixDbHelper.COLUMN_DIRECTOR, pelicula.director)
            put(DAMFlixDbHelper.COLUMN_ANIO, pelicula.anio)
            put(DAMFlixDbHelper.COLUMN_ES_VISTA, if (pelicula.esVista) 1 else 0)
            put(DAMFlixDbHelper.COLUMN_ES_FAVORITA, if (pelicula.esFavorita) 1 else 0)
        }
        db.insert(DAMFlixDbHelper.TABLE_PELICULAS, null, values)
    }

    suspend fun actualizar(pelicula: Pelicula): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DAMFlixDbHelper.COLUMN_TITULO, pelicula.titulo)
            put(DAMFlixDbHelper.COLUMN_DIRECTOR, pelicula.director)
            put(DAMFlixDbHelper.COLUMN_ANIO, pelicula.anio)
            put(DAMFlixDbHelper.COLUMN_ES_VISTA, if (pelicula.esVista) 1 else 0)
            put(DAMFlixDbHelper.COLUMN_ES_FAVORITA, if (pelicula.esFavorita) 1 else 0)
        }
        db.update(
            DAMFlixDbHelper.TABLE_PELICULAS,
            values,
            "${DAMFlixDbHelper.COLUMN_ID} = ?",
            arrayOf(pelicula.id.toString())
        )
    }
}