package com.devandre.biblia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devandre.biblia.data.BibleRepository
import com.devandre.biblia.ui.screens.BookListScreen
import com.devandre.biblia.ui.screens.ChapterSelectionScreen
import com.devandre.biblia.ui.screens.VerseListScreen
import com.devandre.biblia.ui.theme.BibliaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = BibleRepository(this)
        
        setContent {
            BibliaTheme {
                BibleApp(repository)
            }
        }
    }
}

@Composable
fun BibleApp(repository: BibleRepository) {
    val navController = rememberNavController()
    val books = remember { repository.getBooks() }

    NavHost(
        navController = navController,
        startDestination = "book_list",
        // Animação quando uma nova tela entra (desliza da direita para esquerda)
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }) + fadeIn()
        },
        // Animação quando a tela atual sai (desliza para a esquerda)
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        },
        // Animação quando voltamos para a tela anterior (desliza da esquerda para direita)
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
        },
        // Animação quando a tela de cima sai ao voltar (desliza para a direita)
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        }
    ) {
        // ... os seus composables continuam iguais aqui dentro
        composable("book_list") {
            BookListScreen(
                books = books,
                onBookClick = { bookAbbrev ->
                    navController.navigate("chapter_selection/$bookAbbrev")
                }
            )
        }
        
        composable(
            route = "chapter_selection/{bookAbbrev}",
            arguments = listOf(navArgument("bookAbbrev") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookAbbrev = backStackEntry.arguments?.getString("bookAbbrev")
            val book = books.find { it.abbrev == bookAbbrev }
            
            if (book != null) {
                ChapterSelectionScreen(
                    book = book,
                    onChapterClick = { chapterIndex ->
                        navController.navigate("verse_list/$bookAbbrev/$chapterIndex")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(
            route = "verse_list/{bookAbbrev}/{chapterIndex}",
            arguments = listOf(
                navArgument("bookAbbrev") { type = NavType.StringType },
                navArgument("chapterIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val bookAbbrev = backStackEntry.arguments?.getString("bookAbbrev")
            val chapterIndex = backStackEntry.arguments?.getInt("chapterIndex") ?: 0
            val book = books.find { it.abbrev == bookAbbrev }
            
            if (book != null) {
                VerseListScreen(
                    book = book,
                    chapterIndex = chapterIndex,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
