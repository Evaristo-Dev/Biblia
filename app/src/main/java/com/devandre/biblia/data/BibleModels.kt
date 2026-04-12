package com.devandre.biblia.data

data class Book(
    val abbrev: String,
    val chapters: List<List<String>>,
    val name: String
)

typealias Bible = List<Book>
