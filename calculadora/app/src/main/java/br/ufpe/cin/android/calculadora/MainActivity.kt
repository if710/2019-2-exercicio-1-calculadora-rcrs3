package br.ufpe.cin.android.calculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.RuntimeException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setNumeralButtonListeners()
        setExpressionButtonListeners()
        setEqualButtonListener()
        setClearButtonListener()
    }

    // Make numerical and dot buttons appear on the text_calc
    private fun setNumeralButtonListeners() {
        btn_0.setOnClickListener {

        }
        val buttonListeners = arrayOf(
            btn_0,
            btn_1,
            btn_2,
            btn_3,
            btn_4,
            btn_5,
            btn_6,
            btn_7,
            btn_8,
            btn_9,
            btn_Dot)

        buttonListeners.map {
            button ->  button.setOnClickListener {
                text_calc.append(button.text.toString())
            }
        }
    }

    // Every time an expression button is clicked, append the value of the text_calc field
    // with the expression value and show on text_info
    private fun setExpressionButtonListeners() {
        val buttonListeners = arrayOf(btn_Divide,
            btn_Multiply,
            btn_Add,
            btn_Subtract,
            btn_LParen,
            btn_RParen)

        buttonListeners.map {
            button -> button.setOnClickListener {
                text_info.text = text_calc.text.toString()
                text_info.append(button.text.toString())
                clearTextCalc()
            }
        }
    }

    // Clear all text fields
    private fun setClearButtonListener() {
        btn_Clear.setOnClickListener {
            text_calc.text.clear()
            text_info.text = null
        }
    }

    // Append text in text_info with decimal in text_calc and evaluate the expression
    private fun setEqualButtonListener() {
        btn_Equal.setOnClickListener {
            text_info.append(text_calc.text.toString())
            tryToEvaluateExpressionAndShowOnTextInfo()
            clearTextCalc()
        }
    }

    private fun tryToEvaluateExpressionAndShowOnTextInfo() {
        try {
            val result = eval(text_info.toString())
            text_info.append("\n" + result)
        } catch(e: RuntimeException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun clearTextCalc() {
        text_calc.text.clear()
    }

    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    private fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}

