package com.controle.mensal

import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var repository: PamonhaRepository
    private lateinit var salesManager: SalesManager
    private lateinit var expenseManager: ExpenseManager
    private lateinit var closingRepository: ClosingRepository
    private lateinit var monthlyReportManager: MonthlyReportManager

    private lateinit var content: LinearLayout
    private lateinit var summary: TextView

    private val green = Color.rgb(33, 140, 74)
    private val darkGreen = Color.rgb(23, 107, 56)

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
            setBackgroundColor(Color.rgb(245, 249, 245))
        }

        val header = TextView(this).apply {
            text = "🌽 CONTADOR DE PAMONHAS"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(darkGreen)
            setPadding(12, 28, 12, 28)
        }

        val scroll = ScrollView(this)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 24)
        }

        scroll.addView(content)
        root.addView(header)
        root.addView(scroll)

        setContentView(root)
        refreshDashboard()
    }

    private fun refreshDashboard() {
        content.removeAllViews()

        val sales = todaySales()
        val expenses = todayExpenses()

        val result = FinanceCalculator.calculate(
            sales,
            expenses
        )

        summary = TextView(this).apply {
            text = """
                📊 RESUMO DE HOJE

                Pamonhas vendidas: ${result.totalUnits}
                Faturamento: ${FinanceCalculator.formatMoney(result.revenueCents)}
                Despesas: ${FinanceCalculator.formatMoney(result.expensesCents)}
                Lucro: ${FinanceCalculator.formatMoney(result.profitCents)}
            """.trimIndent()
            textSize = 19f
            setTextColor(darkGreen)
            setPadding(16, 20, 16, 20)
            setBackgroundColor(Color.WHITE)
        }

        content.addView(summary)

        addButton("➕ ADICIONAR SABOR") {
            flavorDialog()
        }

        addButton("💰 REGISTRAR DESPESA") {
            expenseDialog()
        }

        addButton("📜 HISTÓRICO DE VENDAS") {
            historyDialog()
        }

        addButton("📅 FECHAR O DIA") {
            closingDialog()
        }

        addButton("📈 RELATÓRIO MENSAL") {
            MonthlyReportDialog.show(
                this,
                monthlyReportManager
            )
        }

        addButton("🏆 RANKING MENSAL DE SABORES") {
            MonthlyFlavorReportDialog.show(
                this,
                salesManager
            )
        }

        addButton("📊 FECHAMENTOS ANTERIORES") {
            closingsDialog()
        }

        addButton("↩ DESFAZER ÚLTIMA VENDA") {
            if (salesManager.undoLastSale()) {
                refreshDashboard()
                message("Venda desfeita!")
            } else {
                message("Não há venda para desfazer.")
            }
        }

        addSection("🌽 SABORES E ESTOQUE")

        salesManager.getFlavors().forEach { flavor ->

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(15, 12, 15, 12)
                setBackgroundColor(Color.WHITE)
            }

            val info = TextView(this).apply {
                text = """
                    ${flavor.name}
                    Preço: ${FinanceCalculator.formatMoney(flavor.priceCents.toLong())}
                    Estoque: ${flavor.stock}
                """.trimIndent()
                textSize = 17f
                setTextColor(Color.DKGRAY)
            }

            card.addView(info)

            val sell = Button(this).apply {
                text = "+1 VENDER"
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
                text = "EDITAR SABOR / ESTOQUE"
                setOnClickListener {
                    editFlavorDialog(flavor)
                }
            }

            val delete = Button(this).apply {
                text = "EXCLUIR SABOR"
                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage("Excluir ${flavor.name}?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Excluir") { _, _ ->
                            if (salesManager.deleteFlavor(flavor.id)) {
                                refreshDashboard()
                                message("Sabor excluído.")
                            } else {
                                message("Este sabor possui vendas registradas.")
                            }
                        }.show()
                }
            }

            card.addView(sell)
            card.addView(edit)
            card.addView(delete)

            content.addView(card)
        }
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

        val layout = dialogLayout(name, price, stock)

        AlertDialog.Builder(this)
            .setTitle("Adicionar sabor")
            .setView(layout)
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
            setText("%.2f".format(flavor.priceCents / 100.0))
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
            .setMessage("O fechamento será salvo para consulta posterior. As vendas e despesas não serão apagadas.")
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
            setPadding(30, 10, 30, 10)
            fields.forEach { addView(it) }
        }
    }

    private fun addSection(title: String) {
        content.addView(TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(darkGreen)
            setPadding(5, 25, 5, 12)
        })
    }

    private fun addButton(title: String, action: () -> Unit) {
        content.addView(Button(this).apply {
            text = title
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundColor(green)
            setOnClickListener { action() }
        })
    }

    private fun message(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
