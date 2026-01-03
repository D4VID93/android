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
    val tmp = (centerIndex+symbols.size/2)%symbols.size + 1
    val newList = symbols.slice(tmp..<symbols.size) + symbols.slice(0..<tmp)

    Column {
        for(elem in newList.indices){
            if(elem == newList.size/2){
                Row(Modifier.background(Color.Yellow)
                    .border(BorderStroke(width = 2.dp, Color.Red)))
                {
                    Text(newList[elem],
                        fontSize = fontSize)
                }
            }else{
                Row {
                    Text(newList[elem], fontSize = fontSize)
                }
            }
        }
    }
}

@Composable
fun SlotMachineRolls(symbols: Array<String>, fontSize: TextUnit, centerIndices: List<Int>){
    Row {
        for(elem in centerIndices.indices){
            if(elem != 0){
                Column(Modifier.fillMaxSize().weight(0.1f).background(Color.Blue)) {
                }
            }
            Column(Modifier.fillMaxSize().weight(1f)) {
                SlotMachineRoll(symbols = symbols, fontSize = fontSize, centerIndices[elem])
            }
        }
    }
}

@Composable
fun SlotMachine(symbols: Array<String>, fontSize: TextUnit, rollNumber: Int, running: Boolean, onDraw: (List<String>) -> Unit){
    var randomValue by remember { mutableStateOf(List(rollNumber){Random.nextInt(0,rollNumber)}) }
    if(running){
        Box(Modifier.background(Color.Green).fillMaxSize()){
            Text("Draw in Progress...", fontSize = fontSize)
        }
        randomValue = List(rollNumber){Random.nextInt(0,rollNumber)}
    }else{
        SlotMachineRolls(symbols, fontSize, randomValue)
    }

    onDraw.invoke(randomValue.map{ on -> symbols[on] }.toList())

}

@Preview
@Composable
fun SlotMachinePreview(){
    var isRunning by remember { mutableStateOf(false) }

    var tmp by remember { mutableStateOf(listOf<String>())}
    Column {
        Column(Modifier.weight(1f).fillMaxSize()){
            SlotMachine(symbols = SYMBOLS, 40.sp, 7, isRunning){
                    a -> tmp = a
            }
        }
        Column(Modifier.weight(1f).fillMaxSize()){
            Button(onClick = {isRunning = !isRunning}, modifier =
                Modifier.align(Alignment.CenterHorizontally))
            {
                Text(if (isRunning) "Stop" else "Start")
            }
            Text("$tmp", Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun VerticalGauge(fillRatio: Float, modifier: Modifier = Modifier){
    Column(modifier.border(10.dp, Color.Black).fillMaxSize()) {
        Row(Modifier.background(Color.White).weight(1.01f-fillRatio).fillMaxSize()) {

        }
        Row(Modifier.background(Color.Blue).weight(fillRatio).fillMaxSize()) {

        }
    }
}

@Composable
fun Handle(onReleasedHandle: (Float) -> Unit, modifier: Modifier = Modifier){
    var fill by remember { mutableFloatStateOf(0.01f)}
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        while(isPressed){
            fill = (fill + 0.01f).coerceIn(0f, 1f)
            delay(16L)
        }
    }

    Column(modifier.pointerInput(Unit){
        detectTapGestures (
            onPress = {
                isPressed = true
                try{
                    awaitRelease()
                }finally {
                    onReleasedHandle.invoke(fill)
                    fill = 0.01f
                    isPressed = false
                }
            }
        )
    }) {
        VerticalGauge(fill, modifier)
    }
}

@Composable
fun SlotMachineWithHandle(symbols: Array<String>, fontSize: TextUnit, rollNumber: Int, onDraw: (List<String>) -> Unit){
    var isRunning by remember { mutableStateOf(false) }
    var fillRatio by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isRunning) {
        delay(fillRatio.toLong()*5000)
        isRunning = false
    }

    Row(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize().weight(rollNumber.toFloat())) {
            SlotMachine(symbols, fontSize, rollNumber, isRunning, onDraw)
        }
        Row(Modifier.fillMaxSize().weight(1f)) {
            Handle({t -> fillRatio = t
                         isRunning = true})
        }
    }
}

@Preview
@Composable
fun HandlePreview(){
    SlotMachineWithHandle(SYMBOLS, 20.sp, 7) { }
}