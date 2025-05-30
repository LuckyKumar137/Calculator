package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var inputEditText: TextView
    private lateinit var resultTextView: TextView
    private var expression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputEditText = findViewById(R.id.inputTextview) // EditText for input display
        resultTextView = findViewById(R.id.resultTextView) // TextView for result display

        val operators = setOf('+', '-', 'x', '÷', '%', '.')

        val buttons = listOf(
            R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four,
            R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine,
            R.id.plus, R.id.minus, R.id.multiply, R.id.divide,
            R.id.percentage, R.id.point
        )

        for (id in buttons) {
            findViewById<Button>(id).setOnClickListener {
                val value = (it as Button).text.toString()

                // Prevent multiple operators in a row
                if (value[0] in operators) {
                    if (expression.isEmpty()) {
                        // Don't allow operator as first char except minus for negative numbers if you want
                        if (value != "-") return@setOnClickListener
                    } else {
                        val lastChar = expression.last()
                        if (lastChar in operators) {
                            // Replace last operator with new one instead of adding
                            expression = expression.dropLast(1)
                        }
                    }
                }

                expression += value
                inputEditText.setText(expression)
            }
        }

        findViewById<Button>(R.id.Ac).setOnClickListener {
            expression = ""
            inputEditText.setText("")
            resultTextView.text = ""
        }

        findViewById<Button>(R.id.button23).setOnClickListener {
            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
                inputEditText.setText(expression)
            }
        }

        findViewById<Button>(R.id.equal).setOnClickListener {
            try {
                val result = evaluateExpression(expression)
                if (result == result.toInt().toDouble()) {
                    // Show as integer if no decimal part
                    resultTextView.text = result.toInt().toString()
                } else {
                    // Otherwise show full decimal result
                    resultTextView.text = result.toString()
                }
            } catch (e: Exception) {
                resultTextView.text = "Error"
            }
        }
    }

    private fun evaluateExpression(expr: String): Double {
        val cleanExpr = expr.replace("x", "*").replace("÷", "/")

        return object {
            var pos = -1
            var ch: Int = 0

            fun nextChar() {
                ch = if (++pos < cleanExpr.length) cleanExpr[pos].code else -1
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
                if (pos < cleanExpr.length) throw RuntimeException("Unexpected: " + cleanExpr[pos])
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    x = when {
                        eat('+'.code) -> x + parseTerm()
                        eat('-'.code) -> x - parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    x = when {
                        eat('*'.code) -> x * parseFactor()
                        eat('/'.code) -> x / parseFactor()
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
                    x = cleanExpr.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: ${ch.toChar()}")
                }

                return x
            }
        }.parse()
    }
}
