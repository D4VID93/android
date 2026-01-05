package fr.uge.moneymachine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.moneymachine.ui.theme.MoneyMachineTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

val SYMBOLS = arrayOf("🎲", "🏦", "🍒", "🍓", "💰", "🏇", "🥹")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyMachineTheme {
                SlotMachineRolls(symbols = SYMBOLS, 40.sp, centerIndices = (0..6).toList())
            }
        }
    }
}

@Composable
fun SlotMachineRoll(symbols: Array<String>, fontSize: TextUnit, centerIndex: Int) {
    // Calcul mathématique pour décaler la liste afin que le symbole 'centerIndex' soit au milieu visuellement.
    // Le modulo (%) assure qu'on reste dans les limites du tableau.
    val tmp = (centerIndex+symbols.size/2)%symbols.size + 1
    
    // Création d'une nouvelle liste décalée (rotation du tableau)
    val newList = symbols.slice(tmp..<symbols.size) + symbols.slice(0..<tmp)

    Column { // Affichage vertical
        for(elem in newList.indices){ // On parcourt tous les éléments
            if(elem == newList.size/2){ // Si c'est l'élément du milieu (l'élément gagnant)
                Row(Modifier.background(Color.Yellow) // Fond jaune
                    .border(BorderStroke(width = 2.dp, Color.Red))) // Bordure rouge
                {
                    Text(newList[elem], fontSize = fontSize) // Affiche le symbole
                }
            }else{ // Pour les autres éléments (non sélectionnés)
                Row {
                    Text(newList[elem], fontSize = fontSize)
                }
            }
        }
    }
}

@Composable
fun SlotMachineRolls(symbols: Array<String>, fontSize: TextUnit, centerIndices: List<Int>){
    Row { // Affichage horizontal
        for(elem in centerIndices.indices){ // Pour chaque rouleau demandé
            if(elem != 0){ // Si ce n'est pas le premier rouleau, on ajoute une séparation
                // Une colonne bleue fine pour séparer les rouleaux
                Column(Modifier.fillMaxSize().weight(0.1f).background(Color.Blue)) {
                }
            }
            // Affiche le rouleau. weight(1f) assure que tous les rouleaux ont la même largeur.
            Column(Modifier.fillMaxSize().weight(1f)) {
                // Appel du composant précédent pour dessiner un rouleau spécifique
                SlotMachineRoll(symbols = symbols, fontSize = fontSize, centerIndices[elem])
            }
        }
    }
}

@Composable
fun SlotMachine(symbols: Array<String>, fontSize: TextUnit, rollNumber: Int, running: Boolean, onDraw: (List<String>) -> Unit){
    // État qui stocke les indices gagnants actuels (aléatoires).
    var randomValue by remember { mutableStateOf(List(rollNumber){Random.nextInt(0,rollNumber)}) }
    
    if(running){
        // Si la machine tourne, on cache les rouleaux et on affiche un écran vert
        Box(Modifier.background(Color.Green).fillMaxSize()){
            Text("Draw in Progress...", fontSize = fontSize)
        }
        // On génère de nouveaux nombres aléatoires pendant que ça tourne
        randomValue = List(rollNumber){Random.nextInt(0,rollNumber)}
    }else{
        // Si la machine est arrêtée, on affiche les rouleaux avec les valeurs générées
        SlotMachineRolls(symbols, fontSize, randomValue)
    }

    // Callback : on renvoie au parent la liste des symboles gagnants (convertit les indices en Strings)
    // Note : Appeler onDraw ici directement est un "effet de bord" lors de la composition, 
    // ce qui peut être risqué (boucles infinies), mais fonctionne pour ce TP simple.
    onDraw.invoke(randomValue.map{ on -> symbols[on] }.toList())
}

@Preview
@Composable
fun SlotMachinePreview(){
    var isRunning by remember { mutableStateOf(false) } // État : marche/arrêt
    var tmp by remember { mutableStateOf(listOf<String>())} // État : résultat du tirage

    Column {
        // Partie haute : La machine
        Column(Modifier.weight(1f).fillMaxSize()){
            SlotMachine(symbols = SYMBOLS, 40.sp, 7, isRunning){
                    a -> tmp = a // Met à jour le texte du résultat quand la machine change
            }
        }
        // Partie basse : Bouton et Résultat texte
        Column(Modifier.weight(1f).fillMaxSize()){
            Button(onClick = {isRunning = !isRunning}, modifier = // Bouton Start/Stop
                Modifier.align(Alignment.CenterHorizontally))
            {
                Text(if (isRunning) "Stop" else "Start")
            }
            // Affiche la liste des fruits gagnants (ex: [🍒, 🎲, ...])
            Text("$tmp", Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun VerticalGauge(fillRatio: Float, modifier: Modifier = Modifier){
    Column(modifier.border(10.dp, Color.Black).fillMaxSize()) {
        // Partie vide (Blanche). 1.01f est une petite astuce pour éviter les bugs d'arrondi à 0.
        Row(Modifier.background(Color.White).weight(1.01f-fillRatio).fillMaxSize()) { }
        
        // Partie pleine (Bleue). La taille dépend de fillRatio (entre 0 et 1).
        Row(Modifier.background(Color.Blue).weight(fillRatio).fillMaxSize()) { }
    }
}

@Composable
fun Handle(onReleasedHandle: (Float) -> Unit, modifier: Modifier = Modifier){
    var fill by remember { mutableFloatStateOf(0.01f)} // Niveau de remplissage actuel
    var isPressed by remember { mutableStateOf(false) } // Si l'utilisateur appuie

    // Effet lancé quand 'isPressed' change. C'est la boucle d'animation.
    LaunchedEffect(isPressed) {
        while(isPressed){ // Tant qu'on appuie
            fill = (fill + 0.01f).coerceIn(0f, 1f) // Augmente la jauge, max 1.0
            delay(16L) // Pause de 16ms (environ 60 images par seconde)
        }
    }

    Column(modifier.pointerInput(Unit){ // Gestion des gestes tactiles
        detectTapGestures (
            onPress = {
                isPressed = true // Début de l'appui -> lance le LaunchedEffect
                try{
                    awaitRelease() // Attend que l'utilisateur lève le doigt
                }finally {
                    // Quand relâché :
                    onReleasedHandle.invoke(fill) // Envoie la puissance au parent
                    fill = 0.01f // Réinitialise la jauge
                    isPressed = false // Arrête la boucle
                }
            }
        )
    }) {
        // Affiche la jauge visuelle
        VerticalGauge(fill, modifier)
    }
}

@Composable
fun SlotMachineWithHandle(symbols: Array<String>, fontSize: TextUnit, rollNumber: Int, onDraw: (List<String>) -> Unit){
    var isRunning by remember { mutableStateOf(false) }
    var fillRatio by remember { mutableFloatStateOf(0f) } // Puissance du tirage

    // Logique temporelle : détermine combien de temps la machine tourne
    LaunchedEffect(isRunning) {
        // Attend un temps proportionnel à la puissance du levier (ex: 100% = 5 secondes)
        delay(fillRatio.toLong()*5000) 
        isRunning = false // Arrête la machine automatiquement après le délai
    }

    Row(Modifier.fillMaxSize()) {
        // Partie Gauche : La machine (prend plus de place grâce au weight)
        Row(Modifier.fillMaxSize().weight(rollNumber.toFloat())) {
            SlotMachine(symbols, fontSize, rollNumber, isRunning, onDraw)
        }
        // Partie Droite : Le levier (prend 1 unité de place)
        Row(Modifier.fillMaxSize().weight(1f)) {
            Handle({t -> 
                fillRatio = t // Récupère la puissance
                isRunning = true // Démarre la machine
            })
        }
    }
}

@Preview
@Composable
fun HandlePreview(){
    SlotMachineWithHandle(SYMBOLS, 20.sp, 7) { }

}
