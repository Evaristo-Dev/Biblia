package com.devandre.biblia.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class BibleRepository(private val context: Context) {
    private val gson = Gson()
    private var bible: Bible? = null

    fun loadBible(): Bible {
        if (bible != null) return bible!!
        
        val inputStream = context.assets.open("acf.json")
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<Book>>() {}.type
        bible = gson.fromJson(reader, type)
        return bible!!
    }

    fun getBooks(): List<Book> = loadBible()

    fun getBook(abbrev: String): Book? = loadBible().find { it.abbrev == abbrev }

    fun getChapter(bookAbbrev: String, chapterIndex: Int): List<String>? {
        return getBook(bookAbbrev)?.chapters?.getOrNull(chapterIndex)
    }
}
