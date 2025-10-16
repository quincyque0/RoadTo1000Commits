package com.example.segunda_etapa

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView;
import android.widget.Button;

class StringHandler {
    private var stroke: String = ""

    fun getline(_stroke: String) {
        stroke = _stroke
    }

    fun process(): Double {
        var tmp = ""
        var digit1 = 0.0
        var digit2 = 0.0
        var operator = ' '
        var hasOperator = false

        stroke.forEach { char ->
            if (!hasOperator) {
                if (char.isDigit() || char == '.') {
                    tmp += char
                } else if (char in "+-*/") {
                    operator = char
                    digit1 = tmp.toDoubleOrNull() ?: 0.0
                    tmp = ""
                    hasOperator = true
                }
            } else {
                if (char.isDigit() || char == '.') {
                    tmp += char
                }
            }
        }

        digit2 = tmp.toDoubleOrNull() ?: 0.0

        return when (operator) {
            '*' -> digit1 * digit2
            '/' -> if (digit2 != 0.0) digit1 / digit2 else Double.NaN
            '+' -> digit1 + digit2
            '-' -> digit1 - digit2
            else -> digit1
        }
    }

    fun clear() {
        stroke = ""
    }

    fun add(value: String) {
        stroke += value
    }

    fun get(): String {
        return stroke
    }
}
class MainActivity : AppCompatActivity() {

    private lateinit var math: TextView


    private val calculate = StringHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        math = findViewById(R.id.result)
        math.setText("")

        Buttons()



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
    private fun append(value: String) {
        val cur = math.text.toString().replace(',','.')
        math.setText(cur + value)
        calculate.add(value)
    }
    private fun calculating() {
        val mathing = math.text.toString().replace(',','.')
        if ( mathing.isNotEmpty()) {
            calculate.getline( mathing)
            val res = calculate.process()

            math.setText(
                if (res.isNaN()) {
                    "Ошибка"
                } else {
                    if (res % 1 == 0.0) {
                        res.toDouble().toString().replace(',','.')

                    } else {
                        String.format("%.6f", res).replace(',','.')
                    }
                }
            )

            calculate.clear()

            calculate.add(math.text.toString().replace(',','.'))
        }
    }



    private fun Buttons() {
        findViewById<Button>(R.id.button1).setOnClickListener { append("1") }
        findViewById<Button>(R.id.button2).setOnClickListener { append("2") }
        findViewById<Button>(R.id.button3).setOnClickListener { append("3") }
        findViewById<Button>(  R.id.button4).setOnClickListener { append("4") }
        findViewById<Button>(R.id.button5).setOnClickListener { append("5") }
        findViewById<Button>(R.id.button6).setOnClickListener { append("6") }
        findViewById<Button>(R.id.button7).setOnClickListener { append("7") }
        findViewById<Button>(R.id.button8).setOnClickListener { append("8") }
        findViewById<Button>(R.id.button9).setOnClickListener { append("9") }
        findViewById<Button>(R.id.button0).setOnClickListener { append("0") }
        findViewById<Button>(R.id.buttonDot).setOnClickListener { append(".") }

        findViewById<Button>(R.id.buttonPlus).setOnClickListener { append("+") }
        findViewById<Button>(R.id.buttonMinus).setOnClickListener { append("-") }
        findViewById<Button>(R.id.buttonMultiply).setOnClickListener { append("*") }
        findViewById<Button>(R.id.buttonDevide).setOnClickListener { append("/") }
        findViewById<Button>(R.id.buttonEquals).setOnClickListener { calculating() }
        findViewById<Button>(R.id.buttonClear).setOnClickListener { calculate.clear(); math.setText("") }
    }

}
