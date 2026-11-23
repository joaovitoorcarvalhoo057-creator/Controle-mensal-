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

    private val green = Color.rgb(33, 140, 74)
    private val darkGreen = Color.rgb(23, 107, 56)
    private val lightGreen = Color.rgb(239, 247, 241)
    private val backgroundColor = Color.rgb(246, 249, 246)
    private val textColor = Color.rgb(48, 65, 53)
    private val gray = Color.rgb(110, 120, 112)

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
            setPadding(dp(22), dp(20), dp(22), dp(20))
            setBackgroundColor(darkGreen)
        }

        header.addView(TextView(this).apply {
            text = "🌽 CONTADOR DE PAMONHAS"
            textSize = 21f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
        })

        header.addView(TextView(this).apply {
            text = "Controle simples do seu negócio"
            textSize = 13f
            setTextColor(Color.rgb(220, 240, 225))
            setPadding(0, dp(5), 0, 0)
        })

        val scroll = ScrollView(this)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(16), dp(15), dp(25))
        }

        scroll.addView(content)

        root.addView(header)
        root.addView(scroll)

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

    private fun addSummaryCard(
        result: FinancialSummary
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(17), dp(17), dp(17))
            background = roundedBackground(Color.WHITE, 16)
            elevation = dp(2).toFloat()
        }

        card.addView(TextView(this).apply {
            text = "📊  RESUMO DE HOJE"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(darkGreen)
            setPadding(0, 0, 0, dp(15))
        })

        addMetricRow(
            card,
            "Pamonhas vendidas",
            result.totalUnits.toString(),
            "Faturamento",
            FinanceCalculator.formatMoney(result.revenueCents)
        )

        addMetricRow(
            card,
            "Despesas",
            FinanceCalculator.formatMoney(result.expensesCents),
            "Lucro real",
            FinanceCalculator.formatMoney(result.profitCents)
        )

        content.addView(card)
    }

    private fun addMetricRow(
        parent: LinearLayout,
        label1: String,
        value1: String,
        label2: String,
        value2: String
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row.addView(metricView(label1, value1), weightParams())
        row.addView(metricView(label2, value2), weightParams())

        parent.addView(row)
    }

    private fun metricView(
        label: String,
        value: String
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(12), dp(8), dp(12))
            background = roundedBackground(lightGreen, 10)

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 12f
                setTextColor(gray)
            })

            addView(TextView(this@MainActivity).apply {
                text = value
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(darkGreen)
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
    ): Button {
        return Button(this).apply {
            text = title
            textSize = 13f
            isAllCaps = false
            setTypeface(null, Typeface.BOLD)
            setTextColor(darkGreen)
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(8), dp(5), dp(8))
            background = roundedBackground(
                Color.WHITE,
                12,
                Color.rgb(215, 230, 218),
                dp(1)
            )
            elevation = dp(2).toFloat()
            setOnClickListener { action() }

            layoutParams = LinearLayout.LayoutParams(
                0,
                dp(62),
                1f
            ).apply {
                setMargins(dp(4), dp(5), dp(4), dp(5))
            }
        }
    }

    private fun addFlavorCard(flavor: PamonhaFlavor) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(14), dp(15), dp(14))
            background = roundedBackground(Color.WHITE, 13)
            elevation = dp(2).toFloat()
        }

        card.addView(TextView(this).apply {
            text = flavor.name
            textSize = 19f
            setTypeface(null, Typeface.BOLD)
            setTextColor(darkGreen)
        })

        card.addView(TextView(this).apply {
            text = "Preço: ${
                FinanceCalculator.formatMoney(
                    flavor.priceCents.toLong()
                )
            }\nEstoque: ${flavor.stock} unidade(s)"
            textSize = 15f
            setTextColor(textColor)
            setPadding(0, dp(7), 0, dp(10))
        })

        val sell = Button(this).apply {
            text = "＋ VENDER UMA PAMONHA"
            isAllCaps = false
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
            setTextColor(Color.rgb(180, 55, 55))
            setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    Color.rgb(252, 239, 239)
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
            setMargins(0, dp(5), 0, dp(12))
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
                addView(it)
            }
        }
    }

    private fun addSection(title: String) {
        content.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(darkGreen)
            setPadding(dp(3), dp(20), dp(3), dp(8))
        })
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
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }
}
