package com.devandre.biblia.data

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class BibleRepository(private val context: Context) {
    private val gson = GsonBuilder().create()
    private var bible: Bible? = null

    // Mapeamento de números de livros (1-66) para abreviações padrão
    private val bookAbbreviations = arrayOf(
        "gn", "ex", "lv", "nm", "dt", "js", "jz", "rt", "1sm", "2sm",
        "1rs", "2rs", "1cr", "2cr", "ed", "ne", "et", "j", "sl", "pv",
        "ec", "ct", "is", "jr", "lm", "ez", "dn", "os", "jl", "am",
        "ob", "jn", "mq", "na", "hc", "sf", "ag", "zc", "ml", "mt",
        "mc", "lc", "jo", "atos", "rm", "1co", "2co", "gl", "ef", "fp",
        "cl", "1ts", "2ts", "1tm", "2tm", "tt", "fm", "hb", "tg", "1pe",
        "2pe", "1jo", "2jo", "3jo", "jd", "ap"
    )

    fun loadBible(): Bible {
        if (bible != null) return bible!!
        
        val inputStream = context.assets.open("1911-JFA.json")
        val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)

        // Parse the wrapper format from 1911-JFA.json
        val wrapperType = object : TypeToken<BibleWrapper>() {}.type
        val wrapper: BibleWrapper = gson.fromJson(reader, wrapperType)

        // Convert to the expected Bible format
        bible = wrapper.books.map { bookData ->
            Book(
                abbrev = getAbbreviation(bookData.nr),
                name = bookData.name,
                chapters = bookData.chapters.map { chapterData ->
                    chapterData.verses.map { it.text }
                }
            )
        }

        return bible!!
    }

    private fun getAbbreviation(bookNumber: Int): String {
        // bookNumber é 1-based, array é 0-based
        return if (bookNumber in 1..bookAbbreviations.size) {
            bookAbbreviations[bookNumber - 1]
        } else {
            "unknown"
        }
    }

    fun getBooks(): List<Book> = loadBible()

    fun getBook(abbrev: String): Book? = loadBible().find { it.abbrev == abbrev }

    fun getChapter(bookAbbrev: String, chapterIndex: Int): List<String>? {
        return getBook(bookAbbrev)?.chapters?.getOrNull(chapterIndex)
    }
}
