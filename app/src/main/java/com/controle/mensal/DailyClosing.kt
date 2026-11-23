package com.controle.mensal

import java.util.Calendar

data class DailyClosing(
    val id: Long,
    val date: Long,
    val closingTime: Long,
    val soldQuantity: Int,
    val revenueCents: Long,
    val expensesCents: Long,
    val profitCents: Long,
    val remainingStock: Int
)

object DailyClosingCalculator {

    fun create(
        sales: List<SaleRecord>,
        expenses: List<ExpenseRecord>,
        remainingStock: Int
    ): DailyClosing {

        val now = System.currentTimeMillis()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val end = calendar.timeInMillis

        val dailySales = sales.filter {
            it.timestamp >= start && it.timestamp < end
        }

        val dailyExpenses = expenses.filter {
            it.timestamp >= start && it.timestamp < end
        }

        val summary = FinanceCalculator.calculate(
            dailySales,
            dailyExpenses
        )

        return DailyClosing(
            id = now,
            date = start,
            closingTime = now,
            soldQuantity = summary.totalUnits,
            revenueCents = summary.revenueCents,
            expensesCents = summary.expensesCents,
            profitCents = summary.profitCents,
            remainingStock = remainingStock
        )
    }
}
