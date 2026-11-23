
package com.controle.mensal

import java.util.Calendar

data class MonthlySummary(
    val month: Int,
    val year: Int,
    val totalUnits: Int,
    val revenueCents: Long,
    val expensesCents: Long,
    val profitCents: Long
)

object MonthlyReport {

    fun calculate(
        sales: List<SaleRecord>,
        expenses: List<ExpenseRecord>,
        month: Int,
        year: Int
    ): MonthlySummary {

        val monthlySales = sales.filter {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }
            calendar.get(Calendar.MONTH) == month &&
                calendar.get(Calendar.YEAR) == year
        }

        val monthlyExpenses = expenses.filter {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }
            calendar.get(Calendar.MONTH) == month &&
                calendar.get(Calendar.YEAR) == year
        }

        val result = FinanceCalculator.calculate(
            monthlySales,
            monthlyExpenses
        )

        return MonthlySummary(
            month = month,
            year = year,
            totalUnits = result.totalUnits,
            revenueCents = result.revenueCents,
            expensesCents = result.expensesCents,
            profitCents = result.profitCents
        )
    }
}
