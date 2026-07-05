package com.devandre.biblia.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devandre.biblia.data.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseListScreen(
    book: Book,
    chapterIndex: Int,
    onBack: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = chapterIndex) {
        book.chapters.size
    }

    // Map page -> set of selected indices (immutable sets stored in state to trigger recomposition)
    val selectionByPage = remember { mutableStateOf<Map<Int, Set<Int>>>(emptyMap()) }
    val context = LocalContext.current

    fun toggleSelection(page: Int, index: Int) {
        val current = selectionByPage.value[page] ?: emptySet()
        val newSet = if (current.contains(index)) current - index else current + index
        selectionByPage.value = selectionByPage.value.toMutableMap().apply { put(page, newSet) }
    }

    fun clearAllSelection() {
        selectionByPage.value = emptyMap()
    }

    fun buildShareText(): String {
        val selectedPairs = selectionByPage.value.flatMap { (p, idxSet) -> idxSet.map { Pair(p, it) } }
            .sortedWith(compareBy({ it.first }, { it.second }))

        if (selectedPairs.isEmpty()) return ""

        val sb = StringBuilder()
        // Append each verse with only the verse number in front
        for ((p, i) in selectedPairs) {
            val verses = book.chapters.getOrNull(p) ?: continue
            val text = verses.getOrNull(i) ?: continue
            sb.append("${i + 1}. ")
            sb.append(text)
            sb.append("\n")
        }

        // Build reference like "Genesis 1:1-6; 2:3-4"
        val refsByPage = selectedPairs.groupBy({ it.first }, { it.second }).toSortedMap()
        val refs = mutableListOf<String>()

        for ((p, indices) in refsByPage) {
            val sorted = indices.sorted()
            val ranges = mutableListOf<String>()
            var start = sorted.first()
            var end = start
            for (idx in sorted.drop(1)) {
                if (idx == end + 1) {
                    end = idx
                } else {
                    ranges.add(if (start == end) "${start + 1}" else "${start + 1}-${end + 1}")
                    start = idx
                    end = idx
                }
            }
            ranges.add(if (start == end) "${start + 1}" else "${start + 1}-${end + 1}")
            refs.add("${p + 1}:${ranges.joinToString(",")}")
        }

        val refString = "${book.name} ${refs.joinToString("; ")}"
        sb.append("\n")
        sb.append(refString)

        return sb.toString().trim()
    }

    val totalSelected = selectionByPage.value.values.sumOf { it.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (totalSelected > 0) Text("Selecionados: $totalSelected")
                    else Text("${book.name} ${pagerState.currentPage + 1}")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (totalSelected > 0) {
                        IconButton(onClick = {
                            val text = buildShareText()
                            if (text.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartilhar versículos"))
                                // opcional: limpar seleção após compartilhar
                                clearAllSelection()
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            val verses = book.chapters.getOrNull(page) ?: emptyList()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(verses) { index, verse ->
                    val annotatedVerse = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("${index + 1} ")
                        }
                        append(verse)
                    }

                    val selected = selectionByPage.value[page]?.contains(index) == true

                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                        ) {
                            Text(
                                text = annotatedVerse,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (totalSelected > 0) toggleSelection(page, index)
                                            // else: could add single-click behavior here
                                        },
                                        onLongClick = { toggleSelection(page, index) }
                                    )
                            )
                        }

                        if (index < verses.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
