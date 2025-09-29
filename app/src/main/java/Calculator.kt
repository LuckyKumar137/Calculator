package com.example.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen() {

    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    val operators = listOf("+", "-", "*", "/")
    val operatorChars = operators.map { it[0] }

    val buttons = listOf(
        listOf("7", "8", "9", "/"),
        listOf("4", "5", "6", "*"),
        listOf("1", "2", "3", "-"),
        listOf("C", "0", "⌫", "+"),
        listOf(".", "=")
    )

    // Auto-evaluate result only if expression contains operator
    LaunchedEffect(expression) {
        if (expression.isNotEmpty() && expression.any { it in operatorChars }) {
            try {
                val value = evalExpression(expression)
                result = if (value % 1 == 0.0) value.toInt().toString() else value.toString()
            } catch (e: Exception) {
                result = ""
            }
        } else {
            result = ""
        }
    }

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = Color.Black, darkIcons = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = 15.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expression,
                    fontSize = 28.sp,
                    textAlign = TextAlign.End,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = result,
                    fontSize = 40.sp,
                    textAlign = TextAlign.End,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons grid
            for (row in buttons) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (button in row) {
                        Button(
                            onClick = {
                                when (button) {
                                    "C" -> {
                                        expression = ""
                                        result = ""
                                    }
                                    "⌫" -> {
                                        if (expression.isNotEmpty()) {
                                            expression = expression.dropLast(1)
                                        }
                                    }
                                    "=" -> {
                                        if (result.isNotEmpty()) {
                                            expression = result
                                            result = ""
                                        }
                                    }
                                    "." -> {
                                        val lastNumber = expression.takeLastWhile { it !in operatorChars }
                                        if (!lastNumber.contains(".")) {
                                            expression += "."
                                        }
                                    }
                                    else -> {
                                        if (button in operators) {
                                            if (expression.isNotEmpty()) {
                                                val lastChar = expression.last()
                                                if (lastChar in operatorChars) {
                                                    expression = expression.dropLast(1) + button
                                                } else {
                                                    expression += button
                                                }
                                            }
                                        } else {
                                            expression += button
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .weight(1f)
                                .height(70.dp),
                            colors = if (button in operators) {
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFB8C00),
                                    contentColor = Color.White
                                )
                            } else {
                                ButtonDefaults.buttonColors(
                                    containerColor = Color.DarkGray,
                                    contentColor = Color.White
                                )
                            }
                        ) {
                            Text(text = button, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

/** Math expression evaluator */
fun evalExpression(expr: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < expr.length) expr[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expr.length) throw RuntimeException("Unexpected: " + expr[pos])
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> x /= parseFactor()
                    else -> return x
                }
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = expr.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }
            return x
        }
    }.parse()
}
