
package com.controle.mensal

import android.os.Bundle
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Button
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var vendas = 0
    private var estoque = 0
    private var preco = 6.0

    private lateinit var totalVendas: TextView
    private lateinit var faturamento: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        montarTela()
    }

    private fun montarTela() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 40, 30, 40)
            setBackgroundColor(Color.rgb(245, 249, 245))
        }

        val titulo = TextView(this).apply {
            text = "🌽 CONTADOR DE PAMONHAS"
            textSize = 25f
            setTextColor(Color.rgb(25, 110, 55))
            gravity = Gravity.CENTER
        }

        totalVendas = criarTexto("Pamonhas vendidas: 0", 23f)
        faturamento = criarTexto("Faturamento: R$ 0,00", 21f)

        val vender = Button(this).apply {
            text = "+1 PAMONHA"
            textSize = 22f
            setOnClickListener {
                vendas++
                atualizar()
            }
        }

        layout.addView(titulo)
        layout.addView(totalVendas)
        layout.addView(faturamento)
        layout.addView(vender)

        setContentView(layout)
    }

    private fun criarTexto(texto: String, tamanho: Float): TextView {
        return TextView(this).apply {
            text = texto
            textSize = tamanho
            gravity = Gravity.CENTER
            setPadding(10, 25, 10, 25)
            setTextColor(Color.DKGRAY)
        }
    }

    private fun atualizar() {
        totalVendas.text = "Pamonhas vendidas: $vendas"
        faturamento.text = "Faturamento: R$ %.2f".format(vendas * preco)
    }
}
