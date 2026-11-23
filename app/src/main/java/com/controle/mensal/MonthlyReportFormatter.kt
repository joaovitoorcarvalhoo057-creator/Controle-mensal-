package com.controle.mensal

import java.text.DateFormatSymbols
import java.util.Locale

object MonthlyReportFormatter {

    fun format(report: MonthlySummary): String {

        val locale = Locale("pt", "BR")

        val monthName = DateFormatSymbols(locale)
            .months[report.month]
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(locale) else it.toString()
            }

        return """
            📊 RELATÓRIO MENSAL

            Período: $monthName de ${report.year}

            Pamonhas vendidas: ${report.totalUnits}

            Faturamento: ${FinanceCalculator.formatMoney(report.revenueCents)}

            Despesas: ${FinanceCalculator.formatMoney(report.expensesCents)}

            Lucro real: ${FinanceCalculator.formatMoney(report.profitCents)}
        """.trimIndent()
    }
}
