package com.controle.mensal

import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AlertDialog
import java.util.Calendar

object MonthlyFlavorReportDialog {

    fun show(
        context: Context,
        salesManager: SalesManager
    ) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, _ ->

                val results = MonthlyFlavorReport.calculate(
                    sales = salesManager.getSales(),
                    month = month,
                    year = year
                )

                val reportText = results
                    .mapIndexed { index, item ->
                        """
                        ${index + 1}º - ${item.flavorName}
                        Quantidade: ${item.totalUnits}
                        Faturamento: ${
                            FinanceCalculator.formatMoney(
                                item.revenueCents
                            )
                        }
                        """.trimIndent()
                    }
                    .joinToString("\n\n")
                    .ifEmpty {
                        "Nenhuma venda encontrada neste mês."
                    }

                AlertDialog.Builder(context)
                    .setTitle("Ranking dos sabores")
                    .setMessage(reportText)
                    .setPositiveButton("Fechar", null)
                    .show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Escolha o mês")
            show()
        }
    }
}
