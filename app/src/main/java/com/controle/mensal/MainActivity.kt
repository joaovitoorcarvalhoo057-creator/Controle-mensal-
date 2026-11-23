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
            setPadding(dp(22), dp(22), dp(22), dp(22))
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
            setPadding(0, dp(5), 0, 0)
        })

        val scroll = ScrollView(this)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(17), dp(16), dp(25))
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

    private fun addSummaryCard(result: FinancialSummary) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = roundedBackground(Color.WHITE, 16)
            elevation = dp(3).toFloat()
        }

        card.addView(TextView(this).apply {
            text = "📊  RESUMO DE HOJE"
            textSize = 18f
            typeface = titleFont
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
            setPadding(dp(11), dp(13), dp(8), dp(13))
            background = roundedBackground(lightGreen, 11)

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 12f
                typeface = bodyFont
                setTextColor(gray)
            })

            addView(TextView(this@MainActivity).apply {
                text = value
                textSize = 18f
                typeface = numberFont
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
            typeface = titleFont
            setTextColor(darkGreen)
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(7), dp(5), dp(7))

            background = roundedBackground(
                Color.WHITE,
                13,
                Color.rgb(218, 231, 221),
                dp(1)
            )

            elevation = dp(2).toFloat()

            setOnClickListener {
                action()
            }

            layoutParams = LinearLayout.LayoutParams(
                0,
                dp(65),
                1f
            ).apply {
                setMargins(dp(4), dp(5), dp(4), dp(5))
            }
        }
    }

    private fun addFlavorCard(flavor: PamonhaFlavor) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = roundedBackground(Color.WHITE, 14)
            elevation = dp(3).toFloat()
        }

        card.addView(TextView(this).apply {
            text = "🌽  ${flavor.name}"
            textSize = 19f
            typeface = titleFont
            setTextColor(darkGreen)
        })

        card.addView(TextView(this).apply {
            text = "Preço: ${
                FinanceCalculator.formatMoney(
                    flavor.priceCents.toLong()
                )
            }\nEstoque: ${flavor.stock} unidade(s)"

            textSize = 15f
            typeface = bodyFont
            setTextColor(textColor)
            setPadding(0, dp(8), 0, dp(10))
        })

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

        content.addView(TextView(this).apply {
            text = title
            textSize = 16f
            typeface = titleFont
            setTextColor(darkGreen)
            setPadding(dp(3), dp(22), dp(3), dp(9))
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
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
