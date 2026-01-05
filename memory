package com.example.memoryexam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ====================================================================================
// MAIN ACTIVITY
// ====================================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MemoryApp()
                }
            }
        }
    }
}

// ====================================================================================
// 1. NAVIGATION & SPLASH SCREEN
// Structure classique demandée dans les TPs (Splash -> Jeu)
// ====================================================================================

@Composable
fun MemoryApp() {
    // État simple pour gérer la navigation
    var isGameStarted by remember { mutableStateOf(false) }

    if (!isGameStarted) {
        SplashScreen(onFinished = { isGameStarted = true })
    } else {
        GameScreen()
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Simulation du chargement (3 secondes comme d'habitude)
    LaunchedEffect(true) {
        delay(3000)
        onFinished()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Memory Challenge",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Distribution des cartes...")
        Spacer(modifier = Modifier.height(32.dp))

        // La barre de progression (FillBar) demandée
        LinearProgressIndicator(
            modifier = Modifier.width(200.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// ====================================================================================
// 2. MODÈLE DE DONNÉES (DATA CLASS)
// ====================================================================================

data class MemoryCard(
    val id: Int,
    val icon: ImageVector, // L'image de la carte (Étoile, Cœur, etc.)
    var isFaceUp: Boolean = false, // Est-elle retournée ?
    var isMatched: Boolean = false // A-t-elle été trouvée ?
)

// ====================================================================================
// 3. ÉCRAN DE JEU (LOGIQUE PRINCIPALE)
// ====================================================================================

@Composable
fun GameScreen() {
    // --- ÉTATS ---
    // La liste des cartes
    var cards by remember { mutableStateOf(generateCards()) }
    
    // Quelle carte est actuellement sélectionnée (la première de la paire) ?
    // null = aucune, Int = index de la première carte retournée
    var firstCardIndex by remember { mutableStateOf<Int?>(null) }
    
    // Bloquer le jeu pendant l'animation de retournement (pour éviter de cliquer partout)
    var isProcessing by remember { mutableStateOf(false) }
    
    // Score / Coups
    var moves by remember { mutableStateOf(0) }
    
    // Victoire : si toutes les cartes sont "matched"
    val isWon = remember(cards) { cards.all { it.isMatched } }
    
    // Scope pour lancer les délais (delay) sans bloquer l'UI
    val scope = rememberCoroutineScope()

    // --- LOGIQUE MÉTIER ---
    fun onCardClick(index: Int) {
        val card = cards[index]

        // On ignore le clic si : jeu fini, carte déjà trouvée, carte déjà retournée, ou traitement en cours
        if (isWon || card.isMatched || card.isFaceUp || isProcessing) return

        // CAS 1 : C'est la première carte qu'on retourne
        if (firstCardIndex == null) {
            // On retourne visuellement la carte
            val newCards = cards.toMutableList()
            newCards[index] = newCards[index].copy(isFaceUp = true)
            cards = newCards
            
            // On mémorise son index
            firstCardIndex = index
        } 
        // CAS 2 : C'est la deuxième carte
        else {
            val firstIndex = firstCardIndex!!
            moves++ // On compte un coup

            // On retourne la 2ème carte pour la montrer
            val newCards = cards.toMutableList()
            newCards[index] = newCards[index].copy(isFaceUp = true)
            cards = newCards

            // On lance la vérification (Match ou Pas Match ?)
            isProcessing = true // On bloque les clics temporairement

            scope.launch {
                // Est-ce que les icônes sont identiques ?
                if (cards[firstIndex].icon == cards[index].icon) {
                    // OUI : C'est gagné pour cette paire
                    // On marque les deux comme "matched"
                    newCards[firstIndex] = newCards[firstIndex].copy(isMatched = true)
                    newCards[index] = newCards[index].copy(isMatched = true)
                    cards = newCards
                    // On garde la main tout de suite
                    isProcessing = false
                } else {
                    // NON : Ce n'est pas une paire
                    // On attend 1 seconde pour que le joueur voie l'erreur
                    delay(1000)
                    
                    // On retourne les deux cartes face cachée
                    newCards[firstIndex] = newCards[firstIndex].copy(isFaceUp = false)
                    newCards[index] = newCards[index].copy(isFaceUp = false)
                    cards = newCards
                    isProcessing = false
                }
                // On réinitialise la sélection
                firstCardIndex = null
            }
        }
    }

    // --- INTERFACE UTILISATEUR (UI) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En-tête
        Text(
            text = if (isWon) "BRAVO ! TERMINÉ !" else "Trouvez les paires",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isWon) Color(0xFF2E7D32) else Color.Black
        )
        
        Text("Coups : $moves", fontSize = 18.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Grille de cartes (LazyVerticalGrid comme dans le sujet Puzzle)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4), // 4 colonnes pour 16 cartes
            modifier = Modifier
                .weight(1f) // Prend toute la place disponible
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(cards) { index, card ->
                MemoryCardView(
                    card = card,
                    onClick = { onCardClick(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton Reset
        Button(
            onClick = {
                // On regénère et on remet tout à zéro
                cards = generateCards()
                moves = 0
                firstCardIndex = null
                isProcessing = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Recommencer la partie")
        }
    }
}

// ====================================================================================
// 4. COMPOSANT VISUEL : LA CARTE
// ====================================================================================

@Composable
fun MemoryCardView(
    card: MemoryCard,
    onClick: () -> Unit
) {
    // Couleur de la carte : 
    // Si retournée ou trouvée -> Blanc (pour voir l'icône)
    // Sinon -> Bleu (Dos de la carte)
    val cardColor = if (card.isFaceUp || card.isMatched) Color.White else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .aspectRatio(1f) // Carré parfait
            .clip(RoundedCornerShape(8.dp))
            .background(cardColor)
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // On affiche l'icône seulement si la carte est visible (FaceUp ou Matched)
        if (card.isFaceUp || card.isMatched) {
            Icon(
                imageVector = card.icon,
                contentDescription = "Icon",
                tint = if (card.isMatched) Color.Green else Color.Black, // Vert si trouvé
                modifier = Modifier.size(32.dp)
            )
        } else {
            // Dos de la carte (optionnel : motif ou texte "?")
            Text("?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
}

// ====================================================================================
// 5. UTILITAIRES (GÉNÉRATION DU JEU)
// ====================================================================================

/**
 * Génère une liste de 16 cartes (8 paires mélangées)
 */
fun generateCards(): List<MemoryCard> {
    // 1. On choisit 8 icônes différentes
    val icons = listOf(
        Icons.Default.Favorite,     // Coeur
        Icons.Default.Star,         // Etoile
        Icons.Default.Face,         // Visage
        Icons.Default.Home,         // Maison
        Icons.Default.Person,       // Personne
        Icons.Default.ShoppingCart, // Panier
        Icons.Default.Call,         // Téléphone
        Icons.Default.Build         // Outil
    )

    // 2. On double la liste pour avoir des paires (8 * 2 = 16)
    val pairs = icons + icons

    // 3. On mélange et on transforme en objets MemoryCard
    return pairs.shuffled().mapIndexed { index, icon ->
        MemoryCard(id = index, icon = icon)
    }
}
