package com.devandre.biblia.data

data class Book(
    val abbrev: String,
    val chapters: List<List<String>>,
    val name: String
)

typealias Bible = List<Book>

// Modelos para 1911-JFA.json
data class BibleWrapper(
    val books: List<BookData>
)

data class BookData(
    val nr: Int,
    val name: String,
    val chapters: List<ChapterData>
)

data class ChapterData(
    val chapter: Int,
    val verses: List<VerseData>
)

data class VerseData(
    val chapter: Int,
    val verse: Int,
    val text: String
)

