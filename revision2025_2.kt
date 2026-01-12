package com.example.puzzleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

// --- Modèle de données ---
data class Puzzle(
    val id: String,
    val title: String,
    val email: String,
    val difficulty: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Logique de navigation simple : Splash -> Fetcher
                    var currentScreen by remember { mutableStateOf("splash") }

                    when (currentScreen) {
                        "splash" -> {
                            SplashScreen(onClick = {
                                currentScreen = "fetcher"
                            })
                        }
                        "fetcher" -> {
                            PuzzleFetcher(
                                initialEmail = "votre.email@edu.univ-eiffel.fr",
                                onNewPuzzle = { puzzle ->
                                    println("Puzzle récupéré : $puzzle")
                                    // TODO: Passer à l'écran de jeu
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. COMPOSANT SPLASH SCREEN
// ==========================================

@Composable
fun SplashScreen(modifier: Modifier = Modifier, onClick: () -> Unit) {
    // Animation de 0 à 1 (100%)
    val progress = remember { Animatable(0f) }
    var isFinished by remember { mutableStateOf(false) }

    // Lancé au démarrage du composant
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
        isFinished = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fond : Drapeau Delta (Jaune - Bleu - Jaune)
        Column(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFFEB3B))) // Jaune
            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF003399))) // Bleu
            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFFEB3B))) // Jaune
        }

        // Bas : Barre de progression OU Bouton Start
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .padding(horizontal = 40.dp)
                .fillMaxWidth()
        ) {
            if (isFinished) {
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start")
                }
            } else {
                FillBar(
                    fillRatio = progress.value,
                    modifier = Modifier.height(16.dp).fillMaxWidth()
                )
            }
        }
    }
}

// ==========================================
// 2. COMPOSANT BARRE DE PROGRESSION (FillBar)
// ==========================================

@Composable
fun FillBar(fillRatio: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White)
            .border(1.dp, Color.Black)
    ) {
        // Partie noire (remplissage)
        Box(
            modifier = Modifier
                .fillMaxWidth(fillRatio) // Largeur selon le ratio
                .matchParentSize()
                .background(Color.Black)
        )
        // Texte pourcentage
        val percent = (fillRatio * 100).toInt()
        Text(
            text = "$percent%",
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// ==========================================
// 3. COMPOSANT RÉCUPÉRATION PUZZLE
// ==========================================

@Composable
fun PuzzleFetcher(
    initialEmail: String = "",
    modifier: Modifier = Modifier,
    onNewPuzzle: (Puzzle) -> Unit
) {
    var email by remember { mutableStateOf(TextFieldValue(initialEmail)) }
    var difficulty by remember { mutableFloatStateOf(4f) }
    var statusMessage by remember { mutableStateOf("") }
    var isFetching by remember { mutableStateOf(false) }

    // Validation email
    val isEmailValid = email.text.endsWith("@univ-eiffel.fr") || 
                       email.text.endsWith("@edu.univ-eiffel.fr")

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Configuration du Puzzle", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        // Champ Email
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email universitaire") },
            isError = !isEmailValid && email.text.isNotEmpty(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (!isEmailValid && email.text.isNotEmpty()) {
            Text("Format requis : @univ-eiffel.fr", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))

        // Slider Difficulté
        Text("Difficulté : ${difficulty.roundToInt()}")
        Slider(
            value = difficulty,
            onValueChange = { difficulty = it },
            valueRange = 2f..7f,
            steps = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Bouton Récupérer
        Button(
            enabled = isEmailValid && !isFetching,
            onClick = {
                isFetching = true
                statusMessage = "fetching puzzle..."
                
                scope.launch(Dispatchers.IO) {
                    try {
                        val diffInt = difficulty.roundToInt()
                        val urlStr = "https://jigsaw.plade.org/puzzle/new?email=${email.text}&difficulty=$diffInt"
                        val url = URL(urlStr)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        
                        if (conn.responseCode == 200) {
                            val reader = BufferedReader(InputStreamReader(conn.inputStream))
                            val id = reader.readLine() ?: ""
                            val title = reader.readLine() ?: "Sans nom"
                            
                            withContext(Dispatchers.Main) {
                                statusMessage = "puzzle fetched! ($title)"
                                isFetching = false
                                onNewPuzzle(Puzzle(id, title, email.text, diffInt))
                            }
                        } else {
                            val msg = conn.responseMessage
                            withContext(Dispatchers.Main) {
                                statusMessage = "Erreur ${conn.responseCode}: $msg"
                                isFetching = false
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            statusMessage = "Erreur : ${e.localizedMessage}"
                            isFetching = false
                        }
                    }
                }
            }
        ) {
            Text("Récupérer l'énigme")
        }

        Spacer(Modifier.height(20.dp))
        Text(statusMessage)
    }
}

// ==========================================
// 4. TOUTES LES PREVIEWS (APERÇUS)
// ==========================================

// Preview 1 : La barre de progression seule (différents états)
@Preview(showBackground = true, widthDp = 300, heightDp = 100)
@Composable
fun FillBarPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FillBar(fillRatio = 0f, Modifier.height(20.dp).fillMaxWidth())   // 0%
        FillBar(fillRatio = 0.5f, Modifier.height(20.dp).fillMaxWidth()) // 50%
        FillBar(fillRatio = 1f, Modifier.height(20.dp).fillMaxWidth())   // 100%
    }
}

// Preview 2 : Le Splash Screen qui peut redémarrer (pour tester l'animation)
@Preview(showBackground = true)
@Composable
fun RestartableSplashScreenPreview() {
    var resetKey by remember { mutableIntStateOf(0) }
    // key() force la recréation du composant quand resetKey change
    key(resetKey) {
        SplashScreen(
            onClick = { resetKey++ } // Relance l'anim au clic
        )
    }
}

// Preview 3 : Le Fetcher (Formulaire)
@Preview(showBackground = true)
@Composable
fun PuzzleFetcherPreview() {
    PuzzleFetcher(onNewPuzzle = {})
}
