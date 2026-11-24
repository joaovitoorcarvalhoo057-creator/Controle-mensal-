
package com.controle.mensal

import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var repository: PamonhaRepository
    private lateinit var salesManager: SalesManager
    private lateinit var expenseManager: ExpenseManager
    private lateinit var closingRepository: ClosingRepository
    private lateinit var monthlyReportManager: MonthlyReportManager

    private lateinit var content: LinearLayout

    private val green = Color.rgb(33, 134, 91)
    private val darkGreen = Color.rgb(23, 76, 58)
    private val lightGreen = Color.rgb(234, 243, 237)
    private val backgroundColor = Color.rgb(247, 249, 247)
    private val textColor = Color.rgb(38, 54, 47)
    private val gray = Color.rgb(112, 128, 119)
    private val gold = Color.rgb(231, 185, 79)
    private val red = Color.rgb(180, 65, 65)

    private val titleFont =
        Typeface.create("sans-serif-medium", Typeface.NORMAL)

    private val bodyFont =
        Typeface.create("sans-serif", Typeface.NORMAL)

    private val numberFont =
        Typeface.create("sans-serif-medium", Typeface.NORMAL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = PamonhaRepository(this)
        salesManager = SalesManager(repository)
        expenseManager = ExpenseManager(repository)
        closingRepository = ClosingRepository(this)

        monthlyReportManager = MonthlyReportManager(
            salesManager,
            expenseManager
        )

        showDashboard()
    }

    private fun showDashboard() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(25), dp(22), dp(24))
            setBackgroundColor(darkGreen)
        }

        header.addView(TextView(this).apply {
            text = "🌽 CONTADOR DE PAMONHAS"
            textSize = 22f
            typeface = titleFont
            setTextColor(Color.WHITE)
        })

        header.addView(TextView(this).apply {
            text = "Organização e controle do seu negócio"
            textSize = 13f
            typeface = bodyFont
            setTextColor(Color.rgb(220, 235, 225))
            setPadding(0, dp(6), 0, 0)
        })

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(17), dp(16), dp(25))
        }

        scroll.addView(content)

        root.addView(header)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        refreshDashboard()
    }

    private fun refreshDashboard() {
        content.removeAllViews()

        val result = FinanceCalculator.calculate(
            todaySales(),
            todayExpenses()
        )

        addSummaryCard(result)

        addSection("AÇÕES PRINCIPAIS")

        addActionGrid(
            listOf(
                Pair("➕ Adicionar sabor", { flavorDialog() }),
                Pair("💰 Registrar despesa", { expenseDialog() }),
                Pair("📜 Histórico de vendas", { historyDialog() }),
                Pair("📅 Fechar o dia", { closingDialog() }),
                Pair("📈 Relatório mensal", {
                    MonthlyReportDialog.show(
                        this,
                        monthlyReportManager
                    )
                }),
                Pair("🏆 Ranking mensal", {
                    MonthlyFlavorReportDialog.show(
                        this,
                        salesManager
                    )
                }),
                Pair("📊 Fechamentos anteriores", {
                    closingsDialog()
                }),
                Pair("↩ Desfazer última venda", {
                    if (salesManager.undoLastSale()) {
                        refreshDashboard()
                        message("Venda desfeita!")
                    } else {
                        message("Não há venda para desfazer.")
                    }
                })
            )
        )

        addSection("🌽 SABORES E ESTOQUE")

        salesManager.getFlavors().forEach { flavor ->
            addFlavorCard(flavor)
        }
    }

    private fun addSummaryCard(result: FinancialSummary) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(19), dp(19), dp(19), dp(19))
            background = roundedBackground(darkGreen, 20)
            elevation = dp(4).toFloat()
        }

        card.addView(TextView(this).apply {
            text = "RESUMO FINANCEIRO"
            textSize = 13f
            letterSpacing = 0.12f
            typeface = titleFont
            setTextColor(Color.rgb(205, 229, 214))
        })

        card.addView(TextView(this).apply {
            text = "🌽 ${result.totalUnits} pamonhas vendidas"
            textSize = 23f
            typeface = titleFont
            setTextColor(Color.WHITE)
            setPadding(0, dp(9), 0, dp(17))
        })

        val firstRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        firstRow.addView(
            metricView(
                "Faturamento",
                FinanceCalculator.formatMoney(result.revenueCents)
            ),
            weightParams()
        )

        firstRow.addView(
            metricView(
                "Despesas",
                FinanceCalculator.formatMoney(result.expensesCents)
            ),
            weightParams()
        )

        card.addView(firstRow)

        val profit = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(13), dp(14), dp(13))
            background = roundedBackground(lightGreen, 13)
        }

        profit.addView(TextView(this).apply {
            text = "LUCRO REAL DE HOJE"
            textSize = 11f
            letterSpacing = 0.1f
            typeface = titleFont
            setTextColor(gray)
        })

        profit.addView(TextView(this).apply {
            text = FinanceCalculator.formatMoney(result.profitCents)
            textSize = 25f
            typeface = numberFont
            setTextColor(darkGreen)
            setPadding(0, dp(4), 0, 0)
        })

        val profitParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(10)
        }

        card.addView(profit, profitParams)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(10)
        }

        content.addView(card, params)
    }

    private fun metricView(
        label: String,
        value: String
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(8), dp(12))
            background = roundedBackground(
                Color.rgb(43, 91, 72),
                12
            )

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 12f
                typeface = bodyFont
                setTextColor(Color.rgb(210, 229, 216))
            })

            addView(TextView(this@MainActivity).apply {
                text = value
                textSize = 17f
                typeface = numberFont
                setTextColor(Color.WHITE)
                setPadding(0, dp(5), 0, 0)
            })
        }
    }

    private fun addActionGrid(
        items: List<Pair<String, () -> Unit>>
    ) {
        items.chunked(2).forEach { group ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            group.forEach { item ->
                row.addView(
                    actionButton(item.first, item.second),
                    weightParams()
                )
            }

            if (group.size == 1) {
                row.addView(View(this), weightParams())
            }

            content.addView(row)
        }
    }

    private fun actionButton(
        title: String,
        action: () -> Unit
    ): LinearLayout {
        val parts = title.split(" ", limit = 2)
        val icon = parts.firstOrNull() ?: "•"
        val label = parts.getOrElse(1) { title }

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(11), dp(11), dp(8), dp(11))
            background = roundedBackground(
                Color.WHITE,
                15,
                Color.rgb(224, 233, 226),
                dp(1)
            )
            elevation = dp(2).toFloat()
            isClickable = true
            isFocusable = true

            val iconView = TextView(this@MainActivity).apply {
                text = icon
                textSize = 21f
                gravity = Gravity.CENTER
                background = roundedBackground(lightGreen, 12)
                setPadding(dp(8), dp(8), dp(8), dp(8))
            }

            addView(
                iconView,
                LinearLayout.LayoutParams(
                    dp(43),
                    dp(43)
                )
            )

            val labelView = TextView(this@MainActivity).apply {
                text = label
                textSize = 12.5f
                typeface = titleFont
                setTextColor(textColor)
                gravity = Gravity.CENTER_VERTICAL
                maxLines = 2
                setPadding(dp(9), 0, 0, 0)
            }

            addView(
                labelView,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            setOnClickListener {
                action()
            }

            layoutParams = LinearLayout.LayoutParams(
                0,
                dp(78),
                1f
            ).apply {
                setMargins(dp(4), dp(5), dp(4), dp(5))
            }
        }
    }

    private fun addFlavorCard(flavor: PamonhaFlavor) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(17), dp(17), dp(17))
            background = roundedBackground(Color.WHITE, 17)
            elevation = dp(3).toFloat()
        }

        card.addView(TextView(this).apply {
            text = "🌽  ${flavor.name}"
            textSize = 19f
            typeface = titleFont
            setTextColor(darkGreen)
        })

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(13))
        }

        val priceInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@MainActivity).apply {
                text = "PREÇO"
                textSize = 10f
                typeface = titleFont
                setTextColor(gray)
            })

            addView(TextView(this@MainActivity).apply {
                text = FinanceCalculator.formatMoney(
                    flavor.priceCents.toLong()
                )
                textSize = 17f
                typeface = numberFont
                setTextColor(darkGreen)
                setPadding(0, dp(3), 0, 0)
            })
        }

        val stockInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@MainActivity).apply {
                text = "ESTOQUE"
                textSize = 10f
                typeface = titleFont
                setTextColor(gray)
            })

            addView(TextView(this@MainActivity).apply {
                text = "${flavor.stock} unidade(s)"
                textSize = 17f
                typeface = numberFont
                setTextColor(
                    if (flavor.stock > 0) green else red
                )
                setPadding(0, dp(3), 0, 0)
            })
        }

        info.addView(priceInfo, weightParams())
        info.addView(stockInfo, weightParams())

        card.addView(info)

        val sell = Button(this).apply {
            text = "＋ VENDER PAMONHA"
            isAllCaps = false
            textSize = 14f
            typeface = titleFont
            setTextColor(Color.WHITE)

            setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(green)
            )

            setOnClickListener {
                if (salesManager.addSale(flavor.id)) {
                    refreshDashboard()
                    message("Venda registrada!")
                } else {
                    message("Estoque insuficiente!")
                }
            }
        }

        val edit = Button(this).apply {
            text = "Editar sabor e estoque"
            isAllCaps = false
            textSize = 13f
            typeface = titleFont
            setTextColor(darkGreen)

            setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(lightGreen)
            )

            setOnClickListener {
                editFlavorDialog(flavor)
            }
        }

        val delete = Button(this).apply {
            text = "Excluir sabor"
            isAllCaps = false
            textSize = 13f
            typeface = titleFont
            setTextColor(red)

            setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    Color.rgb(252, 241, 241)
                )
            )

            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Excluir ${flavor.name}?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Excluir") { _, _ ->
                        if (salesManager.deleteFlavor(flavor.id)) {
                            refreshDashboard()
                            message("Sabor excluído.")
                        } else {
                            message(
                                "Este sabor possui vendas registradas."
                            )
                        }
                    }.show()
            }
        }

        card.addView(sell)
        card.addView(edit)
        card.addView(delete)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dp(6), 0, dp(12))
        }

        content.addView(card, params)
    }

    private fun flavorDialog() {
        val name = EditText(this).apply {
            hint = "Nome do sabor"
        }

        val price = EditText(this).apply {
            hint = "Preço em reais (ex: 6,00)"
            inputType = 8194
        }

        val stock = EditText(this).apply {
            hint = "Quantidade inicial no estoque"
            inputType = 2
        }

        AlertDialog.Builder(this)
            .setTitle("Adicionar sabor")
            .setView(dialogLayout(name, price, stock))
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar") { _, _ ->
                val cents = parseMoney(price.text.toString())
                val quantity = stock.text.toString().toIntOrNull() ?: -1

                if (salesManager.addFlavor(
                    name.text.toString(),
                    cents,
                    quantity
                )) {
                    refreshDashboard()
                    message("Sabor cadastrado!")
                } else {
                    message("Confira os dados informados.")
                }
            }.show()
    }

    private fun editFlavorDialog(flavor: PamonhaFlavor) {
        val name = EditText(this).apply {
            setText(flavor.name)
            hint = "Nome"
        }

        val price = EditText(this).apply {
            setText(
                String.format(
                    Locale.US,
                    "%.2f",
                    flavor.priceCents / 100.0
                )
            )
            hint = "Preço em reais"
            inputType = 8194
        }

        val stock = EditText(this).apply {
            setText(flavor.stock.toString())
            hint = "Estoque"
            inputType = 2
        }

        AlertDialog.Builder(this)
            .setTitle("Editar ${flavor.name}")
            .setView(dialogLayout(name, price, stock))
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar") { _, _ ->
                val cents = parseMoney(price.text.toString())
                val quantity = stock.text.toString().toIntOrNull() ?: -1

                if (salesManager.updateFlavor(
                    flavor.id,
                    name.text.toString(),
                    cents,
                    quantity
                )) {
                    refreshDashboard()
                    message("Alterações salvas!")
                } else {
                    message("Confira os dados.")
                }
            }.show()
    }

    private fun expenseDialog() {
        val description = EditText(this).apply {
            hint = "Descrição da despesa"
        }

        val category = EditText(this).apply {
            hint = "Categoria (milho, leite, embalagem...)"
        }

        val amount = EditText(this).apply {
            hint = "Valor em reais"
            inputType = 8194
        }

        AlertDialog.Builder(this)
            .setTitle("Registrar despesa")
            .setView(dialogLayout(description, category, amount))
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar") { _, _ ->
                val cents = parseMoney(amount.text.toString())

                if (expenseManager.addExpense(
                    description.text.toString(),
                    category.text.toString(),
                    cents
                )) {
                    refreshDashboard()
                    message("Despesa registrada!")
                } else {
                    message("Confira a descrição e o valor.")
                }
            }.show()
    }

    private fun historyDialog() {
        val sales = todaySales()

        val text = sales.joinToString("\n\n") {
            "${it.flavorName} - ${it.quantity} unidade(s)\n" +
                "${FinanceCalculator.formatMoney(it.unitPriceCents.toLong())} cada"
        }.ifEmpty {
            "Nenhuma venda registrada hoje."
        }

        AlertDialog.Builder(this)
            .setTitle("Histórico de hoje")
            .setMessage(text)
            .setPositiveButton("Fechar", null)
            .show()
    }

    private fun closingDialog() {
        AlertDialog.Builder(this)
            .setTitle("Fechar o dia?")
            .setMessage(
                "O fechamento será salvo para consulta posterior. " +
                    "As vendas e despesas não serão apagadas."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ ->
                val stock = salesManager.getFlavors().sumOf {
                    it.stock
                }

                val closing = DailyClosingCalculator.create(
                    todaySales(),
                    todayExpenses(),
                    stock
                )

                closingRepository.save(closing)

                message("Fechamento diário salvo!")
            }.show()
    }

    private fun closingsDialog() {
        val closings = closingRepository.load()

        val text = closings.joinToString("\n\n") {
            val date = java.text.DateFormat
                .getDateTimeInstance()
                .format(it.closingTime)

            "$date\n" +
                "Vendas: ${it.soldQuantity}\n" +
                "Faturamento: ${FinanceCalculator.formatMoney(it.revenueCents)}\n" +
                "Despesas: ${FinanceCalculator.formatMoney(it.expensesCents)}\n" +
                "Lucro: ${FinanceCalculator.formatMoney(it.profitCents)}"
        }.ifEmpty {
            "Nenhum fechamento registrado."
        }

        AlertDialog.Builder(this)
            .setTitle("Fechamentos anteriores")
            .setMessage(text)
            .setPositiveButton("Fechar", null)
            .show()
    }

    private fun todaySales(): List<SaleRecord> {
        val start = dayStart()
        val end = start + 24L * 60L * 60L * 1000L

        return salesManager.getSales().filter {
            it.timestamp >= start && it.timestamp < end
        }
    }

    private fun todayExpenses(): List<ExpenseRecord> {
        val start = dayStart()
        val end = start + 24L * 60L * 60L * 1000L

        return expenseManager.getExpenses().filter {
            it.timestamp >= start && it.timestamp < end
        }
    }

    private fun dayStart(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun parseMoney(value: String): Int {
        return try {
            (value.trim().replace(",", ".").toBigDecimal() *
                java.math.BigDecimal(100))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .intValueExact()
        } catch (_: Exception) {
            -1
        }
    }

    private fun dialogLayout(vararg fields: EditText): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(25), dp(10), dp(25), dp(10))

            fields.forEach {
                it.typeface = bodyFont
                addView(it)
            }
        }
    }

    private fun addSection(title: String) {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(3), dp(22), dp(3), dp(9))
        }

        section.addView(TextView(this).apply {
            text = title
            textSize = 15f
            typeface = titleFont
            setTextColor(darkGreen)
        })

        content.addView(section)
    }

    private fun weightParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            setMargins(dp(3), dp(3), dp(3), dp(3))
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Int,
        strokeColor: Int = Color.TRANSPARENT,
        strokeWidth: Int = 0
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()

            if (strokeWidth > 0) {
                setStroke(strokeWidth, strokeColor)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun message(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
